package cn.cordys.crm.product.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtProductPriceMapper {

    List<String> getRepeatNameById(@Param("name") String name, @Param("orgId") String orgId);
}
