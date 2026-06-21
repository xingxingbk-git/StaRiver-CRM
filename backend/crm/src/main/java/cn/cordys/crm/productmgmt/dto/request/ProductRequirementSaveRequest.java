package cn.cordys.crm.productmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductRequirementSaveRequest {
    @Schema(description = "需求ID（更新时必填）")
    private String id;
    @Schema(description = "需求标题")
    private String title;
    @Schema(description = "需求类型")
    private String type;
    @Schema(description = "需求来源")
    private String source;
    @Schema(description = "目标产品展示名")
    private String product;
    @Schema(description = "目标产品ID")
    private String productId;
    @Schema(description = "期望上线/预发布版本")
    private String release;
    @Schema(description = "优先级")
    private String priority;
    @Schema(description = "需求描述")
    private String description;
    @Schema(description = "验收标准")
    private String acceptance;
}
