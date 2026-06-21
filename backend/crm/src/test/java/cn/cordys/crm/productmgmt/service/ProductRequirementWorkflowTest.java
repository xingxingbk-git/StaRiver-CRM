package cn.cordys.crm.productmgmt.service;

import cn.cordys.crm.approval.constants.ApprovalStatus;
import cn.cordys.crm.productmgmt.dto.ProductRequirementWorkflowConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductRequirementWorkflowTest {

    @Test
    void rejectedOrRevokedRequirementCanBeEditedInPool() {
        assertTrue(ProductManagementService.canEditRequirement("需求池", ApprovalStatus.UNAPPROVED.name()));
        assertTrue(ProductManagementService.canEditRequirement("需求池", ApprovalStatus.REVOKED.name()));
        assertFalse(ProductManagementService.canEditRequirement("需求池", ApprovalStatus.APPROVING.name()));
        assertFalse(ProductManagementService.canEditRequirement("产品设计", ApprovalStatus.UNAPPROVED.name()));
    }

    @Test
    void onlyReviewingRequirementCreatedBySubmitterCanBeRevoked() {
        assertTrue(ProductManagementService.canRevokeRequirementReview("需求评审", ApprovalStatus.APPROVING.name(), "u1", "u1"));
        assertFalse(ProductManagementService.canRevokeRequirementReview("需求评审", ApprovalStatus.APPROVING.name(), "u1", "u2"));
        assertFalse(ProductManagementService.canRevokeRequirementReview("需求池", ApprovalStatus.APPROVING.name(), "u1", "u1"));
        assertFalse(ProductManagementService.canRevokeRequirementReview("需求评审", ApprovalStatus.UNAPPROVED.name(), "u1", "u1"));
    }

    @Test
    void approvalResultMapsRequirementBackToExpectedStage() {
        assertEquals("产品设计", ProductManagementService.resolveRequirementStatusByApproval("需求评审", ApprovalStatus.APPROVED.name()));
        assertEquals("需求池", ProductManagementService.resolveRequirementStatusByApproval("需求评审", ApprovalStatus.UNAPPROVED.name()));
        assertEquals("需求池", ProductManagementService.resolveRequirementStatusByApproval("需求评审", ApprovalStatus.REVOKED.name()));
        assertEquals("需求评审", ProductManagementService.resolveRequirementStatusByApproval("需求池", ApprovalStatus.APPROVING.name()));
    }

    @Test
    void onlyDesignStageCanAdvance() {
        assertTrue(ProductManagementService.canAdvanceRequirementStage("产品设计"));
        assertFalse(ProductManagementService.canAdvanceRequirementStage("需求评审"));
    }

    @Test
    void configuredWorkflowDrivesNextAndPreviousStages() {
        ProductRequirementWorkflowConfig config = ProductRequirementWorkflowConfig.defaultConfig();

        assertEquals("技术评审", config.nextStage("产品设计").name());
        assertEquals("产品设计", config.previousStage("技术评审").name());
        assertTrue(config.previousStage("产品设计") == null);
    }

    @Test
    void onlyConfiguredStageOwnerCanOperate() {
        ProductRequirementWorkflowConfig.Stage design = new ProductRequirementWorkflowConfig.Stage(
                "PRODUCT_DESIGN", "产品设计", List.of("u1", "u2"), List.of("张三", "李四"), true, false
        );

        assertTrue(design.canOperate("u1", "fallback"));
        assertFalse(design.canOperate("u3", "fallback"));
        assertTrue(new ProductRequirementWorkflowConfig.Stage(
                "PRODUCT_DESIGN", "产品设计", List.of(), List.of(), true, false
        ).canOperate("fallback", "fallback"));
    }

    @Test
    void acceptanceStageRequiresProductModuleAndVersion() {
        ProductRequirementWorkflowConfig config = ProductRequirementWorkflowConfig.defaultConfig();

        assertFalse(config.requiresProductLink("技术评审"));
        assertTrue(config.requiresProductLink("产品验收"));
    }
}
