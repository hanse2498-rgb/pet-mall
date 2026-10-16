package com.mall.module.common.controller;

import com.mall.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 健康检查
 */
@Tag(name = "健康检查")
@RestController
@RequestMapping("/api/ping")
public class PingController {

    @Operation(summary = "探活接口")
    @GetMapping
    public Result<String> ping() {
        return Result.ok("pong @ " + LocalDateTime.now());
    }
}