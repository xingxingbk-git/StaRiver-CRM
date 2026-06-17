package cn.cordys.crm.product.service;

import cn.cordys.crm.product.domain.Product;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Resource
    private BaseMapper<Product> productBaseMapper;

    public String getProductName(String id) {
        Product product = productBaseMapper.selectByPrimaryKey(id);
        return product == null ? null : product.getName();
    }

    public String getProductNameByIds(List<String> ids) {
        List<Product> products = productBaseMapper.selectByIds(ids);
        if (CollectionUtils.isEmpty(products)) {
            return null;
        }
        return products.stream().map(Product::getName).collect(Collectors.joining(","));
    }

    public List<Product> getProductListByNames(List<String> names) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getName, names);
        return productBaseMapper.selectListByLambda(wrapper);
    }
}
