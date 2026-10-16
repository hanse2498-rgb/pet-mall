package com.mall.module.dashboard.service.impl;

import com.mall.module.dashboard.mapper.DashboardMapper;
import com.mall.module.dashboard.service.DashboardService;
import com.mall.module.dashboard.vo.DashboardKpiVO;
import com.mall.module.dashboard.vo.DashboardVO;
import com.mall.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** 库存预警阈值 */
    private static final int STOCK_THRESHOLD = 10;

    private final DashboardMapper dashboardMapper;
    private final OrderService orderService;

    @Override
    public DashboardVO overview(int range) {
        if (range < 1) {
            range = 30;
        }
        LocalDateTime start = LocalDate.now().minusDays(range - 1L).atStartOfDay();

        DashboardVO vo = new DashboardVO();
        vo.setRange(range);

        DashboardKpiVO kpi = dashboardMapper.kpi(start);
        if (kpi == null) {
            kpi = new DashboardKpiVO();
        }
        long paid = kpi.getPaidOrderCount() == null ? 0 : kpi.getPaidOrderCount();
        BigDecimal sales = kpi.getSalesAmount() == null ? BigDecimal.ZERO : kpi.getSalesAmount();
        kpi.setAvgPrice(paid > 0
                ? sales.divide(BigDecimal.valueOf(paid), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setKpi(kpi);

        vo.setTrend(dashboardMapper.salesTrend(start));
        vo.setStatusDist(dashboardMapper.statusDist());
        vo.setCategoryShare(dashboardMapper.categoryShare(start));
        vo.setTopProducts(dashboardMapper.topProducts(start, 10));
        vo.setStockAlerts(dashboardMapper.stockAlerts(STOCK_THRESHOLD));
        vo.setLatestOrders(orderService.adminPage(null, 1, 8).getList());
        return vo;
    }
}
