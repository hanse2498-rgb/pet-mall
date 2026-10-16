package com.mall.module.member.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.member.entity.UserAddress;
import com.mall.module.member.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址
 */
@Tag(name = "收货地址")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;

    @Operation(summary = "地址列表")
    @GetMapping
    public Result<List<UserAddress>> list() {
        return Result.ok(addressService.listByUser(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<UserAddress> add(@RequestBody UserAddress address) {
        return Result.ok(addressService.add(StpUtil.getLoginIdAsLong(), address));
    }

    @Operation(summary = "更新地址")
    @PutMapping
    public Result<Void> update(@RequestBody UserAddress address) {
        addressService.update(StpUtil.getLoginIdAsLong(), address);
        return Result.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        addressService.deleteAddress(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "设为默认")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable("id") Long id) {
        addressService.setDefault(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }
}