package cn.cordys.crm.integration.dataease.service;

import cn.cordys.crm.system.mapper.ExtOrganizationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.LinkedHashSet;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataEaseSyncServiceTest {

    @Test
    void shouldContinueWithNextOrganizationWhenOneSyncFails() {
        DataEaseSyncService service = spy(new DataEaseSyncService());
        ExtOrganizationMapper organizationMapper = mock(ExtOrganizationMapper.class);
        ReflectionTestUtils.setField(service, "extOrganizationMapper", organizationMapper);
        when(organizationMapper.selectAllOrganizationIds()).thenReturn(new LinkedHashSet<>(List.of("org-1", "org-2")));
        doThrow(new RuntimeException("sync failed")).when(service).syncDataEase("org-1");
        doReturn(DataEaseSyncService.SyncResult.SUCCESS).when(service).syncDataEase("org-2");

        service.syncDataEase();

        verify(service).syncDataEase("org-2");
    }
}
