package com.csust.eco.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csust.eco.common.Result;
import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.dto.ItemQueryDTO;
import com.csust.eco.service.ItemService;
import com.csust.eco.vo.ItemListVO;
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
@RestController
@RequestMapping("/api/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping("/publish")
    public Result<Long> publish(@Validated @RequestBody ItemPublishDTO publishDTO) {
        // 1. 在 Controller 提取当前操作用户的身份上下文
        long currentUserId = StpUtil.getLoginIdAsLong();

        // 2. 将干净的数据和身份传递给 Service 进行纯逻辑运算
        Long itemId = itemService.publish(publishDTO, currentUserId);

        return Result.success(itemId);
    }

    @GetMapping("/page")
    public Result<Page<ItemListVO>> pageQuery(@Validated ItemQueryDTO dto) {
        Page<ItemListVO> result = itemService.queryItemPage(dto);
        return Result.success(result);
    }
}
