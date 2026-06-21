package cn.cordys.crm.productmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequirementAdvanceStageRequest {
    @Schema(description = "阶段提交内容")
    private String content;
    @Schema(description = "附件ID集合")
    private List<String> attachmentIds;

    @Schema(description = "关联产品模块ID（产品验收阶段必填）")
    private String moduleId;

    @Schema(description = "关联预发布版本ID（产品验收阶段必填）")
    private String versionId;
}
