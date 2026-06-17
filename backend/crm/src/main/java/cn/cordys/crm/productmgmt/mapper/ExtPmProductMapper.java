package cn.cordys.crm.productmgmt.mapper;

import cn.cordys.common.dto.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPmProductMapper {

    List<OptionDTO> getOptions(@Param("orgId") String orgId);

    List<OptionDTO> getProductOptions(@Param("keyword") String keyword, @Param("orgId") String orgId);

    List<OptionDTO> listIdNameByIds(@Param("ids") List<String> ids);
}
