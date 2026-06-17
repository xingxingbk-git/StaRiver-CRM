package cn.cordys.crm.product.service;

import cn.cordys.crm.product.domain.ProductPrice;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductPriceService {

    @Resource
    private BaseMapper<ProductPrice> productPriceMapper;

    public String getProductPriceName(String id) {
        ProductPrice productPrice = productPriceMapper.selectByPrimaryKey(id);
        return productPrice == null ? null : productPrice.getName();
    }

    public String getProductPriceNameByIds(List<String> ids) {
        List<ProductPrice> productPrices = productPriceMapper.selectByIds(ids);
        if (CollectionUtils.isEmpty(productPrices)) {
            return null;
        }
        return productPrices.stream().map(ProductPrice::getName).collect(Collectors.joining(","));
    }

    public List<ProductPrice> getProductPriceListByNames(List<String> names) {
        LambdaQueryWrapper<ProductPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductPrice::getName, names);
        return productPriceMapper.selectListByLambda(wrapper);
    }
}
