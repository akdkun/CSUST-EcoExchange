package com.csust.eco.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csust.eco.common.Result;
import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.dto.ItemQueryDTO;
import com.csust.eco.service.ItemService;
import com.csust.eco.vo.ItemDetailVO;
import com.csust.eco.vo.ItemListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 二手商品表 前端控制器
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Tag(name = "3. 商品导购与发布模块")
@RestController
@RequestMapping("/api/items") // 统一使用复数名词
@RequiredArgsConstructor
public class ItemController {
    final private ItemService itemService;

    @Operation(summary = "发布二手商品")
    @PostMapping // 隐式映射到 /api/items
    public Result<Long> publish(@Validated @RequestBody ItemPublishDTO publishDTO) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        return Result.success(itemService.publish(publishDTO, currentUserId));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}") // 映射到 /api/items/{id}
    public Result<ItemDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(itemService.getItemDetail(id));
    }

    @Operation(summary = "商品分页列表")
    @GetMapping // 隐式映射到 /api/items, 与 publish 形成动静分离
    public Result<Page<ItemListVO>> pageQuery(@Validated ItemQueryDTO dto) {
        return Result.success(itemService.queryItemPage(dto));
    }
}
