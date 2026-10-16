package com.mall.module.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.common.ResultCode;
import com.mall.module.member.entity.UserAddress;
import com.mall.module.member.service.UserAddressService;
import com.mall.module.order.dto.OrderCreateRequest;
import com.mall.module.order.entity.Order;
import com.mall.module.order.entity.OrderItem;
import com.mall.module.order.entity.Payment;
import com.mall.module.order.entity.Shipping;
import com.mall.module.order.mapper.OrderItemMapper;
import com.mall.module.order.mapper.OrderMapper;
import com.mall.module.order.mapper.PaymentMapper;
import com.mall.module.order.mapper.ShippingMapper;
import com.mall.module.order.service.OrderService;
import com.mall.module.notice.service.NoticeService;
import com.mall.module.order.vo.OrderItemVO;
import com.mall.module.order.vo.OrderVO;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.entity.SkuBatch;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuBatchMapper;
import com.mall.module.product.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    public static final int STATUS_PENDING_PAY = 0;
    public static final int STATUS_PENDING_SHIP = 1;
    public static final int STATUS_PENDING_RECEIVE = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_CANCELLED = 4;
    public static final int STATUS_AFTER_SALE = 5;

    /** 包邮门槛 */
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200");
    /** 固定运费 */
    private static final BigDecimal FLAT_FREIGHT = new BigDecimal("10");
    /** 食品发货剩余保质期下限 */
    private static final BigDecimal MIN_REMAIN_RATIO = new BigDecimal("0.6667");

    /** 幂等令牌（内存版，开发用） */
    private final Map<String, Long> idempotentTokens = new ConcurrentHashMap<>();

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final ShippingMapper shippingMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final SkuBatchMapper skuBatchMapper;
    private final UserAddressService addressService;
    private final ObjectMapper objectMapper;
    private final NoticeService noticeService;

    @Override
    @Transactional
    public Long createOrder(Long userId, OrderCreateRequest request) {
        // 幂等：同一 clientToken 重复提交直接返回已创建的订单
        if (request.getClientToken() != null && !request.getClientToken().isBlank()) {
            Long existing = idempotentTokens.get(userId + ":" + request.getClientToken());
            if (existing != null) {
                return existing;
            }
        }

        UserAddress address = addressService.getById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1004, "收货地址不存在");
        }

        String receiverJson;
        try {
            Map<String, Object> receiver = new LinkedHashMap<>();
            receiver.put("receiverName", address.getReceiverName());
            receiver.put("phone", address.getPhone());
            receiver.put("province", address.getProvince());
            receiver.put("city", address.getCity());
            receiver.put("district", address.getDistrict());
            receiver.put("detail", address.getDetail());
            receiverJson = objectMapper.writeValueAsString(receiver);
        } catch (Exception e) {
            throw new BusinessException(500, "地址信息序列化失败");
        }

        // 逐项校验 + 扣库存 + 食品批次校验
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (OrderCreateRequest.Item item : request.getItems()) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku == null || sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
            }
            int affected = skuMapper.deductStock(item.getSkuId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            Product product = productMapper.selectById(sku.getProductId());
            BigDecimal price = effectivePrice(sku);
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem oi = new OrderItem();
            oi.setSkuId(sku.getId());
            oi.setProductName(product == null ? "" : product.getName());
            oi.setSkuImg(product == null ? null : product.getCoverImg());
            oi.setSpec(sku.getSpecName());
            oi.setPrice(price);
            oi.setQuantity(item.getQuantity());
            oi.setSubtotal(subtotal);
            if (product != null && product.getSaleType() != null && product.getSaleType() == 1) {
                SkuBatch batch = pickBatch(sku, product);
                if (batch != null) {
                    oi.setBatchId(batch.getId());
                    oi.setProduceDate(batch.getProduceDate());
                    oi.setExpiryDate(batch.getProduceDate() == null ? null
                            : batch.getProduceDate().plusDays(batch.getExpiryDays() == null ? 0 : batch.getExpiryDays()));
                }
            }
            orderItems.add(oi);
            goodsAmount = goodsAmount.add(subtotal);
        }

        BigDecimal freight = goodsAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : FLAT_FREIGHT;
        BigDecimal payAmount = goodsAmount.add(freight);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiver(receiverJson);
        order.setStatus(STATUS_PENDING_PAY);
        order.setGoodsAmount(goodsAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreight(freight);
        order.setPayAmount(payAmount);
        order.setPayStatus(0);
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        for (OrderItem oi : orderItems) {
            oi.setId(null);
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        if (request.getClientToken() != null && !request.getClientToken().isBlank()) {
            idempotentTokens.put(userId + ":" + request.getClientToken(), order.getId());
        }
        noticeService.send(userId, 1, "下单成功",
                "订单 " + order.getOrderNo() + " 已创建，请尽快完成支付。", order.getId());
        return order.getId();
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = mustOwn(userId, orderId);
        if (order.getStatus() != STATUS_PENDING_PAY && order.getStatus() != STATUS_PENDING_SHIP) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        // 回补库存
        restoreStock(orderId);
        order.setStatus(STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            order.setPayStatus(2); // 已退款（模拟）
        }
        orderMapper.updateById(order);
        noticeService.send(userId, 1, "订单已取消",
                "订单 " + order.getOrderNo() + " 已取消。", order.getId());
    }

    @Override
    @Transactional
    public void confirmReceive(Long userId, Long orderId) {
        Order order = mustOwn(userId, orderId);
        if (order.getStatus() != STATUS_PENDING_RECEIVE) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(STATUS_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);
        noticeService.send(userId, 1, "订单已完成",
                "订单 " + order.getOrderNo() + " 已确认收货，感谢你的购买！", order.getId());
        Shipping shipping = shippingMapper.selectOne(Wrappers.<Shipping>lambdaQuery()
                .eq(Shipping::getOrderId, orderId).orderByDesc(Shipping::getCreateTime).last("limit 1"));
        if (shipping != null) {
            shipping.setStatus(2);
            shipping.setReceiveTime(LocalDateTime.now());
            shippingMapper.updateById(shipping);
        }
    }

    @Override
    public PageResult<OrderVO> page(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        var wrapper = Wrappers.<Order>lambdaQuery().eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<OrderVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    public PageResult<OrderVO> adminPage(Integer status, Integer pageNum, Integer pageSize) {
        var wrapper = Wrappers.<Order>lambdaQuery();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<OrderVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    public OrderVO detail(Long userId, Long orderId) {
        Order order = mustOwn(userId, orderId);
        return buildDetailVO(order);
    }

    @Override
    public OrderVO adminDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(3002, "订单不存在");
        }
        return buildDetailVO(order);
    }

    @Override
    @Transactional
    public void adminShip(Long orderId, String logisticsCompany, String trackingNo) {
        if (logisticsCompany == null || logisticsCompany.isBlank()
                || trackingNo == null || trackingNo.isBlank()) {
            throw new BusinessException(400, "请填写物流公司与运单号");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(3002, "订单不存在");
        }
        if (order.getStatus() != STATUS_PENDING_SHIP) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        Shipping shipping = shippingMapper.selectOne(Wrappers.<Shipping>lambdaQuery()
                .eq(Shipping::getOrderId, orderId).orderByDesc(Shipping::getCreateTime).last("limit 1"));
        if (shipping == null) {
            shipping = new Shipping();
            shipping.setOrderId(orderId);
        }
        shipping.setLogisticsCompany(logisticsCompany);
        shipping.setTrackingNo(trackingNo);
        shipping.setStatus(0); // 已发货
        shipping.setShipTime(LocalDateTime.now());
        if (shipping.getId() == null) {
            shippingMapper.insert(shipping);
        } else {
            shippingMapper.updateById(shipping);
        }
        order.setStatus(STATUS_PENDING_RECEIVE);
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
        noticeService.send(order.getUserId(), 1, "订单已发货",
                "订单 " + order.getOrderNo() + " 已由「" + logisticsCompany + "」发出，运单号 " + trackingNo + "。",
                order.getId());
    }

    private OrderVO buildDetailVO(Order order) {
        OrderVO vo = toVO(order);
        Payment payment = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderId, order.getId()).orderByDesc(Payment::getCreateTime).last("limit 1"));
        if (payment != null) {
            vo.setPayNo(payment.getPayNo());
            vo.setPaymentTime(payment.getPayTime());
        }
        Shipping shipping = shippingMapper.selectOne(Wrappers.<Shipping>lambdaQuery()
                .eq(Shipping::getOrderId, order.getId()).orderByDesc(Shipping::getCreateTime).last("limit 1"));
        if (shipping != null) {
            vo.setLogisticsCompany(shipping.getLogisticsCompany());
            vo.setTrackingNo(shipping.getTrackingNo());
        }
        return vo;
    }

    // ---------- helpers ----------

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setGoodsAmount(order.getGoodsAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFreight(order.getFreight());
        vo.setPayAmount(order.getPayAmount());
        vo.setRemark(order.getRemark());
        vo.setPayStatus(order.getPayStatus());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setReceiver(order.getReceiver());
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(this::toItemVO).collect(Collectors.toList()));
        return vo;
    }

    private OrderItemVO toItemVO(OrderItem oi) {
        OrderItemVO vo = new OrderItemVO();
        vo.setSkuId(oi.getSkuId());
        vo.setProductName(oi.getProductName());
        vo.setSkuImg(oi.getSkuImg());
        vo.setSpec(oi.getSpec());
        vo.setPrice(oi.getPrice());
        vo.setQuantity(oi.getQuantity());
        vo.setSubtotal(oi.getSubtotal());
        vo.setProduceDate(oi.getProduceDate());
        vo.setExpiryDate(oi.getExpiryDate());
        return vo;
    }

    private BigDecimal effectivePrice(Sku sku) {
        if (sku.getIsDiscount() != null && sku.getIsDiscount() == 1 && sku.getDiscountPrice() != null) {
            return sku.getDiscountPrice();
        }
        return sku.getPrice();
    }

    /** 食品批次选择：临期（折扣）取临期批次；普通取剩余保质期≥2/3 的批次（FIFO） */
    private SkuBatch pickBatch(Sku sku, Product product) {
        List<SkuBatch> batches = skuBatchMapper.selectList(Wrappers.<SkuBatch>lambdaQuery()
                .eq(SkuBatch::getSkuId, sku.getId())
                .orderByAsc(SkuBatch::getProduceDate));
        if (batches.isEmpty()) {
            return null;
        }
        boolean discount = sku.getIsDiscount() != null && sku.getIsDiscount() == 1;
        if (discount && product.getIsNearExpiry() != null && product.getIsNearExpiry() == 1) {
            return batches.stream()
                    .filter(b -> Integer.valueOf(1).equals(b.getIsNearExpiry()))
                    .findFirst().orElse(batches.get(0));
        }
        return batches.stream()
                .filter(b -> b.getRemainRatio() != null && b.getRemainRatio().compareTo(MIN_REMAIN_RATIO) >= 0)
                .findFirst()
                .orElseThrow(() -> new BusinessException(2003, "该食品当前批次剩余保质期不足，暂不可下单"));
    }

    @Override
    @Transactional
    public void markAfterSale(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        order.setStatus(STATUS_AFTER_SALE);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void endAfterSale(Long orderId, boolean refunded) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        if (refunded) {
            restoreStock(orderId);
            order.setPayStatus(2);
        }
        order.setStatus(derivePreAfterSaleStatus(order));
        orderMapper.updateById(order);
    }

    /** 售后结束时的订单状态：按时间戳推导售后前的状态 */
    private int derivePreAfterSaleStatus(Order order) {
        if (order.getFinishTime() != null) {
            return STATUS_FINISHED;
        }
        if (order.getShipTime() != null) {
            return STATUS_PENDING_RECEIVE;
        }
        return STATUS_PENDING_SHIP;
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            skuMapper.increaseStock(item.getSkuId(), item.getQuantity());
        }
    }

    private Order mustOwn(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3002, "订单不存在");
        }
        return order;
    }

    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "M" + ts + rand;
    }
}