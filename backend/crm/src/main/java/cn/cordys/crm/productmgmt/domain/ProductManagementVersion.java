package cn.cordys.crm.productmgmt.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pm_product_version")
public class ProductManagementVersion extends BaseModel {
    @Schema(description = "产品ID")
    private String productId;
    @Schema(description = "版本号")
    private String version;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "发布日期")
    private String releaseDate;
    @Schema(description = "版本说明")
    private String description;
    @Schema(description = "需求数")
    private Integer pendingCount;
    @Schema(description = "组织ID")
    private String organizationId;
}
