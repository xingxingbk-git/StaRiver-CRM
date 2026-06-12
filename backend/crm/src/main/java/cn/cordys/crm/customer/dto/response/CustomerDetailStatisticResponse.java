package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerDetailStatisticResponse {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "总商机数")
    private Integer totalOpportunities;

    @Schema(description = "总合同数")
    private Integer totalContracts;

    @Schema(description = "本月新增商机数")
    private Integer monthlyOpportunities;

    @Schema(description = "本月新增合同数")
    private Integer monthlyContracts;

    @Schema(description = "首签合同时间")
    private Long firstContractDate;
}
