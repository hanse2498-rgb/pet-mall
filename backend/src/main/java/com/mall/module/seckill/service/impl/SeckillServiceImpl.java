package com.mall.module.seckill.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.BusinessException;
import com.mall.module.member.entity.UserAddress;
import com.mall.module.member.service.UserAddressService;
import com.mall.module.order.entity.Order;
import com.mall.module.order.entity.OrderItem;
import com.mall.module.order.entity.Payment;
import com.mall.module.order.mapper.OrderItemMapper;
import com.mall.module.order.mapper.OrderMapper;
import com.mall.module.order.mapper.PaymentMapper;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuMapper;
import com.mall.module.seckill.dto.SeckillOrderRequest;
import com.mall.module.seckill.entity.SeckillActivity;
import com.mall.module.seckill.entity.SeckillItem;
import com.mall.module.seckill.entity.SeckillRecord;
import com.mall.module.seckill.mapper.SeckillActivityMapper;
import com.mall.module.seckill.mapper.SeckillItemMapper;
import com.mall.module.seckill.mapper.SeckillRecordMapper;
import com.mall.module.seckill.service.SeckillService;
import com.mall.module.seckill.vo.SeckillActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private static final BigDecimal FREE_THRESHOLD = new BigDecimal("200");
    private static final BigDecimal FLAT_FREIGHT = new BigDecimal("10");

    private final SeckillActivityMapper activityMapper;
    private final SeckillItemMapper itemMapper;
    private final SeckillRecordMapper recordMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final UserAddressService addressService;
    private final ObjectMapper objectMapper;

    @Override
    public SeckillActivityVO current() {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> activities = activityMapper.selectList(
                Wrappers.<SeckillActivity>lambdaQuery()
                        .ne(SeckillActivity::getStatus, 3)
                        .orderByAsc(SeckillActivity::getStartTime));
        if (activities.isEmpty()) {
            SeckillActivityVO empty = new SeckillActivityVO();
            empty.setServerTime(now);
            empty.setItems(List.of());
            return empty;
        }
        // 优先进行中；其次最近未开始；都没有则最近结束
        SeckillActivity chosen = activities.stream()
                .filter(a -> !a.getStartTime().isAfter(now) && !a.getEndTime().isBefore(now))
                .findFirst()
                .orElseGet(() -> activities.stream()
                        .filter(a -> a.getStartTime().isAfter(now))
                        .findFirst()
                        .orElse(activities.get(activities.size() - 1)));
        return build(chosen);
    }

    @Override
    public SeckillActivityVO activity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(5001, "场次不存在");
        }
        return build(activity);
    }

    @Override
    @Transactional
    public Long createOrder(Long userId, SeckillOrderRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new BusinessException(5001, "场次不存在");
        }
        if (Integer.valueOf(3).equals(activity.getStatus())) {
            throw new BusinessException(5002, "该场次已关闭");
        }
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException(5002, "秒杀还未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(5002, "秒杀已结束");
        }

        SeckillItem item = itemMapper.selectById(request.getItemId());
        if (item == null || item.getActivityId().longValue() != request.getActivityId()
                || (item.getStatus() != null && item.getStatus() != 1)) {
            throw new BusinessException(5003, "秒杀商品不存在或已停用");
        }
        Sku sku = skuMapper.selectById(item.getSkuId());
        if (sku == null || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException(2001, "商品不可购买");
        }
        if (item.getSeckillStock() == null || item.getSeckillStock() <= 0) {
            throw new BusinessException(5004, "手慢了，已被抢光");
        }
        // 每人限购
        int limit = item.getPerUserLimit() == null ? 1 : item.getPerUserLimit();
        SeckillRecord exist = recordMapper.selectOne(Wrappers.<SeckillRecord>lambdaQuery()
                .eq(SeckillRecord::getUserId, userId)
                .eq(SeckillRecord::getItemId, item.getId()).last("limit 1"));
        if (exist != null && exist.getQuantity() >= limit) {
            throw new BusinessException(5005, "每人限购 " + limit + " 件");
        }
        int remaining = limit - (exist == null ? 0 : exist.getQuantity());
        if (request.getQuantity() > remaining) {
            throw new BusinessException(5005, "超出限购数量，最多还能买 " + remaining + " 件");
        }
        // 原子扣秒杀库存（防超卖）
        int affected = recordMapper.deductSecStock(item.getId(), request.getQuantity());
        if (affected == 0) {
            throw new BusinessException(5004, "手慢了，已被抢光");
        }
        recordMapper.incrSold(item.getId(), request.getQuantity());
        // 落限购记录（唯一约束兜底）
        try {
            SeckillRecord record = new SeckillRecord();
            record.setUserId(userId);
            record.setItemId(item.getId());
            record.setQuantity(request.getQuantity());
            recordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            recordMapper.deductSecStock(item.getId(), 0); // no-op
            throw new BusinessException(5005, "你已参加过该商品的秒杀");
        }

        UserAddress address = addressService.getById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1004, "收货地址不存在");
        }
        return buildOrder(userId, sku, item.getSeckillPrice(), request.getQuantity(), address, "秒杀订单");
    }

    // ---------- build ----------

    private SeckillActivityVO build(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setActivityId(activity.getId());
        vo.setName(activity.getName());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setState(stateOf(activity));
        vo.setServerTime(LocalDateTime.now());

        List<SeckillItem> items = itemMapper.selectList(Wrappers.<SeckillItem>lambdaQuery()
                .eq(SeckillItem::getActivityId, activity.getId())
                .eq(SeckillItem::getStatus, 1)
                .orderByAsc(SeckillItem::getSort));
        if (items.isEmpty()) {
            vo.setItems(List.of());
            return vo;
        }
        List<Long> skuIds = items.stream().map(SeckillItem::getSkuId).toList();
        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, s -> s));
        List<Long> productIds = skuMap.values().stream().map(Sku::getProductId).distinct().toList();
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));
        vo.setItems(items.stream().map(it -> toItemVO(it, skuMap.get(it.getSkuId()), productMap)).collect(Collectors.toList()));
        return vo;
    }

    private SeckillActivityVO.ItemVO toItemVO(SeckillItem it, Sku sku, Map<Long, Product> productMap) {
        SeckillActivityVO.ItemVO item = new SeckillActivityVO.ItemVO();
        item.setItemId(it.getId());
        item.setSkuId(it.getSkuId());
        Product product = sku == null ? null : productMap.get(sku.getProductId());
        item.setProductId(product == null ? null : product.getId());
        item.setProductName(product == null ? "商品已下架" : product.getName());
        item.setCoverImg(product == null ? null : product.getCoverImg());
        item.setSpecName(sku == null ? null : sku.getSpecName());
        item.setNormalPrice(sku == null ? BigDecimal.ZERO : sku.getPrice());
        item.setSeckillPrice(it.getSeckillPrice());
        item.setSeckillStock(it.getSeckillStock());
        item.setSeckillSold(it.getSeckillSold());
        int total = it.getSeckillStock() + it.getSeckillSold();
        item.setPercent(total <= 0 ? 0 : BigDecimal.valueOf(it.getSeckillSold())
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP).intValue());
        item.setPerUserLimit(it.getPerUserLimit());
        return item;
    }

    private Integer stateOf(SeckillActivity a) {
        LocalDateTime now = LocalDateTime.now();
        if (Integer.valueOf(3).equals(a.getStatus())) {
            return 2;
        }
        if (now.isBefore(a.getStartTime())) {
            return 0;
        }
        if (now.isAfter(a.getEndTime())) {
            return 2;
        }
        return 1;
    }

    private Long buildOrder(Long userId, Sku sku, BigDecimal seckillPrice, Integer qty,
                            UserAddress address, String remark) {
        BigDecimal subtotal = seckillPrice.multiply(BigDecimal.valueOf(qty));
        BigDecimal freight = subtotal.compareTo(FREE_THRESHOLD) >= 0 ? BigDecimal.ZERO : FLAT_FREIGHT;
        Product product = productMapper.selectById(sku.getProductId());

        Order order = new Order();
        order.setOrderNo("S" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 999999));
        order.setUserId(userId);
        order.setReceiver(writeReceiver(address));
        order.setStatus(0);
        order.setGoodsAmount(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreight(freight);
        order.setPayAmount(subtotal.add(freight));
        order.setPayStatus(0);
        order.setRemark(remark);
        orderMapper.insert(order);

        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setSkuId(sku.getId());
        oi.setProductName(product == null ? "" : product.getName());
        oi.setSkuImg(product == null ? null : product.getCoverImg());
        oi.setSpec(sku.getSpecName());
        oi.setPrice(seckillPrice);
        oi.setQuantity(qty);
        oi.setSubtotal(subtotal);
        orderItemMapper.insert(oi);

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPayNo("P" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999));
        payment.setChannel(0);
        payment.setAmount(order.getPayAmount());
        payment.setStatus(0);
        paymentMapper.insert(payment);
        return order.getId();
    }

    private String writeReceiver(UserAddress address) {
        try {
            Map<String, Object> receiver = new LinkedHashMap<>();
            receiver.put("receiverName", address.getReceiverName());
            receiver.put("phone", address.getPhone());
            receiver.put("province", address.getProvince());
            receiver.put("city", address.getCity());
            receiver.put("district", address.getDistrict());
            receiver.put("detail", address.getDetail());
            return objectMapper.writeValueAsString(receiver);
        } catch (Exception e) {
            throw new BusinessException(500, "地址序列化失败");
        }
    }
}