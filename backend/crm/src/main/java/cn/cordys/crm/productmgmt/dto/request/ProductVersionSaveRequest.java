package cn.cordys.crm.productmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProductVersionSaveRequest {
    @Schema(description = "版本ID")
    private String id;
    @Schema(description = "产品ID")
    private String productId;
    @Schema(description = "版本号")
    private String version;
    @Schema(description = "版本状态")
    private String status;
    @Schema(description = "计划发布日期")
    private String releaseDate;
    @Schema(description = "版本说明")
    private String description;
    @Schema(description = "产品负责人ID")
    private String productOwnerId;
    @Schema(description = "产品负责人")
    private String productOwner;
    @Schema(description = "研发负责人ID")
    private String devOwnerId;
    @Schema(description = "研发负责人")
    private String devOwner;
    @Schema(description = "附件ID集合")
    private List<String> attachmentIds;
}
