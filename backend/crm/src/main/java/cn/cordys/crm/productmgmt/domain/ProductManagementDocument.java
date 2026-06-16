package cn.cordys.crm.productmgmt.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pm_product_document")
public class ProductManagementDocument extends BaseModel {
    @Schema(description = "产品ID")
    private String productId;
    @Schema(description = "文档名称")
    private String name;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "展示大小")
    private String sizeText;
    @Schema(description = "组织ID")
    private String organizationId;
}
