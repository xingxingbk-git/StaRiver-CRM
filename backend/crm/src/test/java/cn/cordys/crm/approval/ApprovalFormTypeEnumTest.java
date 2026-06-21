package cn.cordys.crm.approval;

import cn.cordys.common.constants.FormKey;
import cn.cordys.common.service.FieldSourceServiceProvider;
import cn.cordys.crm.approval.constants.ApprovalFormTypeEnum;
import cn.cordys.crm.approval.service.ApprovalResourceService;
import cn.cordys.crm.productmgmt.service.ProductManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ApprovalFormTypeEnumTest {

    @Test
    void shouldSupportProductRequirementFormType() {
        ApprovalFormTypeEnum formType = ApprovalFormTypeEnum.getByValue("productRequirement");
        assertNotNull(formType);
        assertEquals(ApprovalFormTypeEnum.PRODUCT_REQUIREMENT, formType);
    }

    @Test
    void productRequirementHasApprovalNoticeTranslations() {
        assertEquals("产品需求", ResourceBundle.getBundle("i18n/cordys-crm", Locale.SIMPLIFIED_CHINESE).getString("productRequirement"));
        assertEquals("Product Requirement", ResourceBundle.getBundle("i18n/cordys-crm", Locale.US).getString("productRequirement"));
    }

    @Test
    void productRequirementIsRegisteredAsApprovalResourceService() {
        FieldSourceServiceProvider provider = new FieldSourceServiceProvider();
        ProductManagementService productManagementService = new ProductManagementService();
        ReflectionTestUtils.setField(provider, "productManagementService", productManagementService);

        provider.init();

        assertSame(productManagementService, FieldSourceServiceProvider.getServiceOfKey(FormKey.PRODUCT_REQUIREMENT.getKey()));
    }

    @Test
    void productRequirementUsesTitleAsApprovalResourceName() {
        assertEquals("title", ApprovalResourceService.FORM_APPROVAL_NAME_COLUMN.get(FormKey.PRODUCT_REQUIREMENT.getKey()));
        assertEquals("name", ApprovalResourceService.FORM_APPROVAL_NAME_COLUMN.get(FormKey.CONTRACT.getKey()));
    }
}
