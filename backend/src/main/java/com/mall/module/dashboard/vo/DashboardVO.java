package com.mall.module.dashboard.vo;

import com.mall.module.order.vo.OrderVO;
import lombok.Data;

import java.util.List;

/**
 * 后台数据看板（一屏聚合）
 */
@Data
public class DashboardVO {
    private int range;
    private DashboardKpiVO kpi;
    private List<TrendPointVO> trend;
    private List<StatusCountVO> statusDist;
    private List<CategoryShareVO> categoryShare;
    private List<TopProductVO> topProducts;
    private List<OrderVO> latestOrders;
    private List<StockAlertVO> stockAlerts;
}
