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
@Tag(name = "2. 商品导购与发布模块", description = "涵盖 C 端用户的商品浏览、分页查询以及卖家的商品发布逻辑")
@RestController
@RequestMapping("/api/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Operation(summary = "发布二手商品", description = "[需登录]卖家发布商品, 包含主图和详情图数据")
    @PostMapping("/publish")
    public Result<Long> publish(@Validated @RequestBody ItemPublishDTO publishDTO) {
        // 1. 在 Controller 提取当前操作用户的身份上下文
        long currentUserId = StpUtil.getLoginIdAsLong();

        // 2. 将干净的数据和身份传递给 Service 进行纯逻辑运算
        Long itemId = itemService.publish(publishDTO, currentUserId);

        return Result.success(itemId);
    }

    @Operation(summary = "获取商品详情", description = "[游客可用]根据商品主键 ID 查询完整信息(含多图数组)")
    @GetMapping("/{id}")
    public Result<ItemDetailVO> getDetail(@PathVariable Long id) {
        ItemDetailVO detail = itemService.getItemDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "商品分页列表", description = "[游客可用]首页瀑布流查询，支持模糊搜索，按发布时间倒序")
    @GetMapping("/page")
    public Result<Page<ItemListVO>> pageQuery(@Validated ItemQueryDTO dto) {
        Page<ItemListVO> result = itemService.queryItemPage(dto);
        return Result.success(result);
    }
}
