package com.csust.eco.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ItemQueryDTO {
    // 强制赋予合理的默认值, 防止空指针
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 50, message = "单页请求量过大")
    private Integer pageSize = 10;

    // 搜索关键字 (用于标题和描述的模糊匹配)
    private String keyword;
}