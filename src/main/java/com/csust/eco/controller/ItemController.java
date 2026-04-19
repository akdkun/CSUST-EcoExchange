package com.csust.eco.controller;

import com.csust.eco.common.Result;
import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 二手商品表 前端控制器
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@RestController
@RequestMapping("/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping("/publish")
    public Result<String> publish(@Validated @RequestBody ItemPublishDTO publishDTO) {
        itemService.publish(publishDTO);
        return Result.success("商品发布成功");
    }
}
