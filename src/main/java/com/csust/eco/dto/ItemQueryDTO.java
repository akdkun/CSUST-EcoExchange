package com.csust.eco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "商品分页列表查询入参")
public class ItemQueryDTO {

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNo = 1;

    @Schema(description = "每页展示条数", example = "10")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 50, message = "单页请求量过大")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键字(支持对商品标题和描述进行模糊匹配)", example = "高数")
    private String keyword;
}