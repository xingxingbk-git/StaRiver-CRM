package cn.cordys.crm.productmgmt.controller;

import cn.cordys.common.dto.BasePageRequest;
import cn.cordys.common.pager.Pager;
import cn.cordys.crm.productmgmt.dto.request.ProductManagementSaveRequest;
import cn.cordys.crm.productmgmt.dto.request.ProductRequirementSaveRequest;
import cn.cordys.crm.productmgmt.service.ProductManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/product-management")
public class ProductManagementController {

    @Resource
    private ProductManagementService productManagementService;

    @PostMapping("/products/page")
    @Operation(summary = "产品集列表")
    public Pager<List<Map<String, Object>>> listProducts(@Validated @RequestBody BasePageRequest request) {
        return productManagementService.listProducts(request);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "产品详情")
    public Map<String, Object> getProduct(@PathVariable String id) {
        return productManagementService.getProduct(id);
    }

    @PostMapping("/products")
    @Operation(summary = "新建产品")
    public Map<String, Object> addProduct(@RequestBody ProductManagementSaveRequest request) {
        return productManagementService.saveProduct(request);
    }

    @PostMapping("/products/update")
    @Operation(summary = "更新产品")
    public Map<String, Object> updateProduct(@RequestBody ProductManagementSaveRequest request) {
        return productManagementService.saveProduct(request);
    }

    @GetMapping("/roadmap")
    @Operation(summary = "产品版本路线图")
    public List<Map<String, Object>> roadmap() {
        return productManagementService.roadmap();
    }

    @PostMapping("/requirements/page")
    @Operation(summary = "产品需求列表")
    public Pager<List<Map<String, Object>>> listRequirements(@Validated @RequestBody BasePageRequest request) {
        return productManagementService.listRequirements(request);
    }

    @GetMapping("/requirements/{id}")
    @Operation(summary = "产品需求详情")
    public Map<String, Object> getRequirement(@PathVariable String id) {
        return productManagementService.getRequirement(id);
    }

    @PostMapping("/requirements")
    @Operation(summary = "新建产品需求")
    public Map<String, Object> addRequirement(@RequestBody ProductRequirementSaveRequest request) {
        return productManagementService.saveRequirement(request);
    }
}
