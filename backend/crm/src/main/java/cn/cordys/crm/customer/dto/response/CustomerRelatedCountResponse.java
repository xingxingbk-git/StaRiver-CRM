package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * StaRiver 客户列表关联业务统计。
 */
@Data
public class CustomerRelatedCountResponse {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "关联商机数量")
    private Integer opportunityCount;

    @Schema(description = "关联合同数量")
    private Integer contractCount;
}
