package cn.cordys.crm.product.domain;

import cn.cordys.common.domain.BaseModel;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pm_product_price")
public class ProductPrice extends BaseModel {

    private String name;
    private String status;
    private Long pos;
    private String organizationId;
}
