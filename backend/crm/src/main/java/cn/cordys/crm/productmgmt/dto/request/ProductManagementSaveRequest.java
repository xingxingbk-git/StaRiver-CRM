package cn.cordys.crm.productmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProductManagementSaveRequest {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "产品代号")
    private String code;
    @Schema(description = "产品名称")
    private String name;
    @Schema(description = "版本号")
    private String version;
    @Schema(description = "产品状态")
    private String status;
    @Schema(description = "发布日期")
    private String releaseDate;
    @Schema(description = "简介")
    private String slogan;
    @Schema(description = "产品负责人")
    private String productOwner;
    @Schema(description = "产品负责人ID")
    private String productOwnerId;
    @Schema(description = "研发负责人")
    private String devOwner;
    @Schema(description = "研发负责人ID")
    private String devOwnerId;
    @Schema(description = "模块架构")
    private List<ModulePayload> modules;

    @Data
    public static class ModulePayload {
        private String name;
        private String ownerId;
        private String ownerName;
        private Integer pendingCount;
        private List<ModulePayload> children;
    }
}
