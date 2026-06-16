package cn.cordys.crm.productmgmt.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pm_product_module")
public class ProductManagementModule extends BaseModel {
    @Schema(description = "产品ID")
    private String productId;
    @Schema(description = "父级模块ID")
    private String parentId;
    @Schema(description = "模块名称")
    private String name;
    @Schema(description = "负责人ID")
    private String ownerId;
    @Schema(description = "负责人")
    private String ownerName;
    @Schema(description = "待发布需求数")
    private Integer pendingCount;
    @Schema(description = "排序")
    private Integer pos;
    @Schema(description = "组织ID")
    private String organizationId;
}
