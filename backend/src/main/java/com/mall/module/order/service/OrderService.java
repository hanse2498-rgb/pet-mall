package com.mall.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.module.order.dto.OrderCreateRequest;
import com.mall.module.order.entity.Order;
import com.mall.module.order.vo.OrderVO;

public interface OrderService extends IService<Order> {

    /** 创建订单（锁库存、地址快照、运费规则） */
    Long createOrder(Long userId, OrderCreateRequest request);

    /** 取消订单（未支付/未发货），回补库存 */
    void cancel(Long userId, Long orderId);

    /** 确认收货 */
    void confirmReceive(Long userId, Long orderId);

    /** 订单列表 */
    PageResult<OrderVO> page(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /** 订单详情 */
    OrderVO detail(Long userId, Long orderId);

    /** 管理员查看订单详情（不做归属校验） */
    OrderVO adminDetail(Long orderId);

    /** 管理员分页查询全部订单 */
    PageResult<OrderVO> adminPage(Integer status, Integer pageNum, Integer pageSize);

    /** 管理员发货 */
    void adminShip(Long orderId, String logisticsCompany, String trackingNo);

    /** 标记订单进入售后（status=5） */
    void markAfterSale(Long orderId);

    /** 售后结束：refunded=true 时回补库存并置为已退款，订单状态回退到售后前 */
    void endAfterSale(Long orderId, boolean refunded);
}