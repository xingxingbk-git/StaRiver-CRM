package cn.cordys.crm.productmgmt.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pm_product")
public class ProductManagementProduct extends BaseModel {
    @Schema(description = "产品代号")
    private String code;
    @Schema(description = "产品名称")
    private String name;
    @Schema(description = "当前版本")
    private String version;
    @Schema(description = "下个版本")
    private String nextVersion;
    @Schema(description = "产品状态")
    private String status;
    @Schema(description = "计划发布日期")
    private String releaseDate;
    @Schema(description = "产品简介")
    private String slogan;
    @Schema(description = "产品负责人ID")
    private String productOwnerId;
    @Schema(description = "产品负责人")
    private String productOwnerName;
    @Schema(description = "研发负责人ID")
    private String devOwnerId;
    @Schema(description = "研发负责人")
    private String devOwnerName;
    @Schema(description = "组织ID")
    private String organizationId;
}
