package cn.cordys.crm.approval.service;

import cn.cordys.common.exception.GenericException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalFlowServiceTest {

    @Test
    void shouldRejectUnknownFormType() {
        ApprovalFlowService service = new ApprovalFlowService();

        assertThrows(GenericException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "getPermissionsByFormType", "unknown-form"));
    }
}
