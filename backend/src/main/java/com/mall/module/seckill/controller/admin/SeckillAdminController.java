package com.mall.module.seckill.controller.admin;

import com.mall.common.Result;
import com.mall.module.seckill.entity.SeckillActivity;
import com.mall.module.seckill.entity.SeckillItem;
import com.mall.module.seckill.mapper.SeckillActivityMapper;
import com.mall.module.seckill.mapper.SeckillItemMapper;
import com.mall.module.seckill.service.SeckillService;
import com.mall.module.seckill.vo.SeckillActivityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台秒杀管理
 */
@Tag(name = "后台秒杀")
@RestController
@RequestMapping("/api/admin/seckill")
@RequiredArgsConstructor
public class SeckillAdminController {

    private final SeckillActivityMapper activityMapper;
    private final SeckillItemMapper itemMapper;
    private final SeckillService seckillService;

    @Operation(summary = "场次列表")
    @GetMapping("/activity")
    public Result<List<SeckillActivity>> activities() {
        return Result.ok(activityMapper.selectList(null));
    }

    @Operation(summary = "新建场次")
    @PostMapping("/activity")
    public Result<SeckillActivity> createActivity(@RequestBody ActivityForm form) {
        SeckillActivity a = new SeckillActivity();
        a.setName(form.getName());
        a.setStartTime(form.getStartTime());
        a.setEndTime(form.getEndTime());
        a.setStatus(0);
        activityMapper.insert(a);
        return Result.ok(a);
    }

    @Operation(summary = "更新场次")
    @PutMapping("/activity/{id}")
    public Result<SeckillActivity> updateActivity(@PathVariable("id") Long id, @RequestBody ActivityForm form) {
        SeckillActivity a = activityMapper.selectById(id);
        if (a == null) {
            return Result.fail(5001, "场次不存在");
        }
        a.setName(form.getName());
        a.setStartTime(form.getStartTime());
        a.setEndTime(form.getEndTime());
        if (form.getStatus() != null) {
            a.setStatus(form.getStatus());
        }
        activityMapper.updateById(a);
        return Result.ok(a);
    }

    @Operation(summary = "删除场次")
    @DeleteMapping("/activity/{id}")
    public Result<Void> deleteActivity(@PathVariable("id") Long id) {
        activityMapper.deleteById(id);
        itemMapper.delete(com.baomidou.mybatisplus.core.toolkit.Wrappers.<SeckillItem>lambdaQuery()
                .eq(SeckillItem::getActivityId, id));
        return Result.ok();
    }

    @Operation(summary = "场次商品（预览）")
    @GetMapping("/activity/{id}")
    public Result<SeckillActivityVO> activityDetail(@PathVariable("id") Long id) {
        return Result.ok(seckillService.activity(id));
    }

    @Operation(summary = "场次商品项")
    @GetMapping("/activity/{id}/items")
    public Result<List<SeckillItem>> items(@PathVariable("id") Long id) {
        return Result.ok(itemMapper.selectList(com.baomidou.mybatisplus.core.toolkit.Wrappers.<SeckillItem>lambdaQuery()
                .eq(SeckillItem::getActivityId, id).orderByAsc(SeckillItem::getSort)));
    }

    @Operation(summary = "添加秒杀商品")
    @PostMapping("/activity/{id}/item")
    public Result<SeckillItem> addItem(@PathVariable("id") Long id, @RequestBody ItemForm form) {
        SeckillItem item = new SeckillItem();
        item.setActivityId(id);
        item.setSkuId(form.getSkuId());
        item.setSeckillPrice(form.getSeckillPrice());
        item.setSeckillStock(form.getSeckillStock());
        item.setSeckillSold(0);
        item.setPerUserLimit(form.getPerUserLimit() == null ? 1 : form.getPerUserLimit());
        item.setStatus(1);
        item.setSort(form.getSort() == null ? 0 : form.getSort());
        itemMapper.insert(item);
        return Result.ok(item);
    }

    @Operation(summary = "更新秒杀商品")
    @PutMapping("/item/{id}")
    public Result<SeckillItem> updateItem(@PathVariable("id") Long id, @RequestBody ItemForm form) {
        SeckillItem item = itemMapper.selectById(id);
        if (item == null) {
            return Result.fail(5003, "秒杀商品不存在");
        }
        if (form.getSeckillPrice() != null) item.setSeckillPrice(form.getSeckillPrice());
        if (form.getSeckillStock() != null) item.setSeckillStock(form.getSeckillStock());
        if (form.getPerUserLimit() != null) item.setPerUserLimit(form.getPerUserLimit());
        if (form.getStatus() != null) item.setStatus(form.getStatus());
        if (form.getSort() != null) item.setSort(form.getSort());
        itemMapper.updateById(item);
        return Result.ok(item);
    }

    @Operation(summary = "删除秒杀商品")
    @DeleteMapping("/item/{id}")
    public Result<Void> deleteItem(@PathVariable("id") Long id) {
        itemMapper.deleteById(id);
        return Result.ok();
    }

    @Data
    public static class ActivityForm {
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer status;
    }

    @Data
    public static class ItemForm {
        private Long skuId;
        private java.math.BigDecimal seckillPrice;
        private Integer seckillStock;
        private Integer perUserLimit;
        private Integer status;
        private Integer sort;
    }
}