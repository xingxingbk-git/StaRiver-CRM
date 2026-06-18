package cn.cordys.crm.system.dto.request;

import lombok.Data;

@Data
public class UserBatchEditRequest extends UserBatchRequest {

    private String departmentId;

    private String onboardingDate;
}
