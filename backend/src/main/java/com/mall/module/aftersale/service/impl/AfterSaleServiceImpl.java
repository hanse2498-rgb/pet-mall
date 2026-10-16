package com.mall.module.aftersale.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.module.aftersale.entity.AfterSale;
import com.mall.module.aftersale.mapper.AfterSaleMapper;
import com.mall.module.aftersale.service.AfterSaleService;
import com.mall.module.aftersale.vo.AfterSaleVO;
import com.mall.module.order.entity.Order;
import com.mall.module.order.service.OrderService;
import com.mall.module.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements AfterSaleService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PASSED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int STATUS_CANCELLED = 3;

    private final OrderService orderService;
    private final NoticeService noticeService;

    @Override
    @Transactional
    public Long apply(Long userId, Long orderId, String reason, String description) {
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3002, "订单不存在");
        }
        if (order.getPayStatus() == null || order.getPayStatus() != 1) {
            throw new BusinessException("订单未支付，不可申请售后");
        }
        int st = order.getStatus();
        if (st != 1 && st != 2 && st != 3) {
            throw new BusinessException("当前订单状态不可申请售后");
        }
        Long active = this.count(Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getOrderId, orderId)
                .eq(AfterSale::getStatus, STATUS_PENDING));
        if (active > 0) {
            throw new BusinessException("该订单已有售后申请处理中");
        }

        AfterSale as = new AfterSale();
        as.setAfterSaleNo(generateNo());
        as.setOrderId(orderId);
        as.setUserId(userId);
        as.setReason(reason);
        as.setDescription(description);
        as.setAmount(order.getPayAmount());
        as.setStatus(STATUS_PENDING);
        this.save(as);

        orderService.markAfterSale(orderId);
        noticeService.send(userId, 2, "售后申请已提交",
                "订单 " + order.getOrderNo() + " 的售后申请已提交，我们会尽快审核。", orderId);
        return as.getId();
    }

    @Override
    public PageResult<AfterSaleVO> pageMine(Long userId, Integer pageNum, Integer pageSize) {
        IPage<AfterSale> page = this.page(new Page<>(pageNum, pageSize),
                Wrappers.<AfterSale>lambdaQuery()
                        .eq(AfterSale::getUserId, userId)
                        .orderByDesc(AfterSale::getCreateTime));
        return PageResult.of(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public AfterSaleVO detail(Long userId, Long id) {
        AfterSale as = this.getById(id);
        if (as == null || !as.getUserId().equals(userId)) {
            throw new BusinessException("售后单不存在");
        }
        return toVO(as);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long id) {
        AfterSale as = this.getById(id);
        if (as == null || !as.getUserId().equals(userId)) {
            throw new BusinessException("售后单不存在");
        }
        if (as.getStatus() != STATUS_PENDING) {
            throw new BusinessException("当前状态不可撤销");
        }
        as.setStatus(STATUS_CANCELLED);
        this.updateById(as);
        orderService.endAfterSale(as.getOrderId(), false);
    }

    @Override
    public PageResult<AfterSaleVO> adminPage(Integer status, Integer pageNum, Integer pageSize) {
        var wrapper = Wrappers.<AfterSale>lambdaQuery();
        if (status != null) {
            wrapper.eq(AfterSale::getStatus, status);
        }
        wrapper.orderByDesc(AfterSale::getCreateTime);
        IPage<AfterSale> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    @Transactional
    public void audit(Long id, boolean pass, String remark) {
        AfterSale as = this.getById(id);
        if (as == null) {
            throw new BusinessException("售后单不存在");
        }
        if (as.getStatus() != STATUS_PENDING) {
            throw new BusinessException("该售后单已处理");
        }
        as.setAdminRemark(remark);
        as.setAuditTime(LocalDateTime.now());
        if (pass) {
            as.setStatus(STATUS_PASSED);
            as.setRefundTime(LocalDateTime.now());
        } else {
            as.setStatus(STATUS_REJECTED);
        }
        this.updateById(as);
        orderService.endAfterSale(as.getOrderId(), pass);
        if (pass) {
            noticeService.send(as.getUserId(), 2, "售后审核通过",
                    "你的售后申请已通过，退款 ¥" + as.getAmount() + " 已原路退回。", as.getOrderId());
        } else {
            noticeService.send(as.getUserId(), 2, "售后申请未通过",
                    "你的售后申请未通过，如有疑问请联系客服。", as.getOrderId());
        }
    }

    private List<AfterSaleVO> toVOList(List<AfterSale> list) {
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private AfterSaleVO toVO(AfterSale as) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(as.getId());
        vo.setAfterSaleNo(as.getAfterSaleNo());
        vo.setOrderId(as.getOrderId());
        vo.setReason(as.getReason());
        vo.setDescription(as.getDescription());
        vo.setAmount(as.getAmount());
        vo.setStatus(as.getStatus());
        vo.setAdminRemark(as.getAdminRemark());
        vo.setAuditTime(as.getAuditTime());
        vo.setRefundTime(as.getRefundTime());
        vo.setCreateTime(as.getCreateTime());
        Order order = orderService.getById(as.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
        }
        return vo;
    }

    private String generateNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AS" + ts + rnd;
    }
}
