package cn.cordys.crm.system.dto.response;

import cn.cordys.crm.system.dto.convert.UserRoleConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "性别(0-男/1-女)")
    private Boolean gender;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门id")
    private String departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "角色列表")
    private List<UserRoleConvert> roles;

    @Schema(description = "入职时间")
    private Long onboardingDate;
}
