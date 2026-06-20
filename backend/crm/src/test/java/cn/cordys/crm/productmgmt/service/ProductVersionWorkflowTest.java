package cn.cordys.crm.productmgmt.service;

import cn.cordys.crm.productmgmt.domain.ProductManagementVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductVersionWorkflowTest {

    @Test
    void planningVersionCanOnlyMoveToDeveloping() {
        assertTrue(ProductManagementService.canMoveVersionStatus("规划中", "开发中"));
        assertFalse(ProductManagementService.canMoveVersionStatus("规划中", "已发布"));
    }

    @Test
    void developingVersionCanMoveToReleased() {
        assertTrue(ProductManagementService.canMoveVersionStatus("开发中", "已发布"));
    }

    @Test
    void currentVersionCanBeDeletedWhenAnotherVersionRemains() {
        assertTrue(ProductManagementService.canDeleteVersion(true, 2));
        assertFalse(ProductManagementService.canDeleteVersion(true, 1));
    }

    @Test
    void deletingCurrentVersionFallsBackToLatestReleasedVersion() {
        ProductManagementVersion released = version("v3.8.2", "已发布", "2026-05-20");
        ProductManagementVersion planning = version("v4.1", "规划中", "2026-12-15");

        ProductManagementVersion replacement = ProductManagementService.selectCurrentVersionReplacement(
                List.of(released, planning)
        );

        assertTrue(replacement == released);
    }

    private ProductManagementVersion version(String number, String status, String releaseDate) {
        ProductManagementVersion version = new ProductManagementVersion();
        version.setVersion(number);
        version.setStatus(status);
        version.setReleaseDate(releaseDate);
        return version;
    }
}
