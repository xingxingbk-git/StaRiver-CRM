package cn.cordys.crm.productmgmt.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pm_product_requirement")
public class ProductManagementRequirement extends BaseModel {
    @Schema(description = "需求编号")
    private String requirementNo;
    @Schema(description = "需求标题")
    private String title;
    @Schema(description = "需求类型")
    private String type;
    @Schema(description = "需求来源")
    private String source;
    @Schema(description = "产品ID")
    private String productId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "预发布版本")
    private String targetVersion;
    @Schema(description = "模块ID")
    private String moduleId;
    @Schema(description = "模块名称")
    private String moduleName;
    @Schema(description = "优先级")
    private String priority;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "阶段")
    private String stage;
    @Schema(description = "期望上线")
    private String expectedRelease;
    @Schema(description = "负责人ID")
    private String ownerId;
    @Schema(description = "负责人")
    private String ownerName;
    @Schema(description = "需求描述")
    private String description;
    @Schema(description = "验收标准")
    private String acceptanceCriteria;
    @Schema(description = "审批状态")
    private String approvalStatus;
    @Schema(description = "流程记录JSON")
    private String stageRecordJson;
    @Schema(description = "交付流程配置快照JSON")
    private String workflowConfigJson;
    @Schema(description = "当前阶段负责人ID列表JSON")
    private String currentAssigneeIds;
    @Schema(description = "当前阶段负责人名称")
    private String currentAssigneeNames;
    @Schema(description = "预发布版本ID")
    private String targetVersionId;
    @Schema(description = "组织ID")
    private String organizationId;
}
