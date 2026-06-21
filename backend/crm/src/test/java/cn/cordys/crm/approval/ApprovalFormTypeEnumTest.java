package cn.cordys.crm.approval;

import cn.cordys.crm.approval.constants.ApprovalFormTypeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApprovalFormTypeEnumTest {

    @Test
    void shouldSupportProductRequirementFormType() {
        ApprovalFormTypeEnum formType = ApprovalFormTypeEnum.getByValue("productRequirement");
        assertNotNull(formType);
        assertEquals(ApprovalFormTypeEnum.PRODUCT_REQUIREMENT, formType);
    }
}
