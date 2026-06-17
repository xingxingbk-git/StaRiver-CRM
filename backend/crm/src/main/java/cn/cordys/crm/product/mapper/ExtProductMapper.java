package cn.cordys.crm.product.mapper;

import cn.cordys.common.dto.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtProductMapper {

    List<OptionDTO> getOptions(@Param("orgId") String orgId);
}
