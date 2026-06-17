package cn.cordys.crm.product.domain;

import cn.cordys.common.domain.BaseModel;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table(name = "pm_product")
public class Product extends BaseModel {

    private String organizationId;
    private String name;
    private BigDecimal price;
    private String status;
    private Long pos;
}
