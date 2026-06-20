<template>
  <StariverModulePage title="角色权限">
    <div class="role-permission-page" :class="{ 'role-permission-page--license-offset': licenseStore.expiredDuring }">
      <section class="role-card">
        <div class="role-card__header">
          <h2>角色</h2>
          <n-tooltip trigger="hover" :delay="300">
            <template #trigger>
              <n-button
                v-permission="['SYSTEM_ROLE:ADD']"
                quaternary
                class="role-card__add"
                :focusable="false"
                @click="addRole"
              >
                <template #icon>
                  <n-icon><Add /></n-icon>
                </template>
              </n-button>
            </template>
            {{ t('role.addRole') }}
          </n-tooltip>
        </div>
        <n-scrollbar class="role-card__body">
          <CrmTree
            ref="roleTreeRef"
            v-model:selected-keys="selectedKeys"
            v-model:data="roles"
            class="role-tree"
            :render-prefix="renderPrefix"
            :render-extra="renderExtra"
            :node-more-actions="nodeMoreActions"
            node-more-action-size="small"
            title-tooltip-position="top-start"
            title-class="role-tree__title"
            :filter-more-action-func="filterMoreActionFunc"
            :field-names="{ keyField: 'id', labelField: 'name', childrenField: 'children' }"
            :rename-api="updateRoleName"
            :rename-static="renameStatic"
            :selectable="roleTreeSelectable"
            @click="handleRoleClick"
            @more-action-select="handleMoreActionSelect"
          />
        </n-scrollbar>
      </section>

      <section class="role-detail-card">
        <div class="role-detail-card__header">
          <div class="role-segmented">
            <button
              v-for="item in tabList"
              :key="item.name"
              class="role-segmented__item"
              :class="{ 'role-segmented__item--active': activeTab === item.name }"
              type="button"
              @click="activeTab = String(item.name)"
            >
              {{ item.tab }}
            </button>
          </div>
        </div>

        <div class="role-detail-card__body">
          <permissionTab
            v-if="activeRole && activeTab === 'permission'"
            :active-role-id="selectedKeys[0]"
            :is-new="!!activeRole.isNew"
            :is-copy="!!activeRole.isCopy"
            :copy-from="activeRole.copyFrom"
            :role-name="activeRole.name"
            @create-success="handleCreated"
            @cancel-create="handleCancelCreate"
            @unsave-change="handleUnsaveChange"
          />
          <memberTab v-if="activeRole && activeTab === 'member'" :active-role-id="selectedKeys[0]" />
        </div>
      </section>
    </div>
  </StariverModulePage>
</template>

<script lang="ts" setup>
  import { NButton, NIcon, NScrollbar, NTooltip, TabPaneProps, useMessage } from 'naive-ui';
  import { Add } from '@vicons/ionicons5';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { characterLimit, getGenerateId } from '@lib/shared/method';
  import type { RoleItem } from '@lib/shared/models/system/role';

  import { ActionsItem } from '@/components/pure/crm-more-action/type';
  import CrmTree from '@/components/pure/crm-tree/index.vue';
  import { CrmTreeNodeData } from '@/components/pure/crm-tree/type';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import memberTab from './components/memberTab.vue';
  import permissionTab from './components/permissionTab.vue';

  import { deleteRole, getRoles, updateRole } from '@/api/modules';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import useModal from '@/hooks/useModal';
  import useLicenseStore from '@/store/modules/setting/license';
  import { hasAnyPermission } from '@/utils/permission';

  const { t } = useI18n();
  const { openModal } = useModal();
  const message = useMessage();
  const { setIsSave } = useLeaveUnSaveTip();
  const licenseStore = useLicenseStore();

  const loading = ref(false);
  const roles = ref<RoleItem[]>([]);
  const selectedKeys = ref<string[]>([]);
  const roleTreeRef = ref<InstanceType<typeof CrmTree> | null>(null);
  const rolePalette = [
    '#ef4444',
    '#7c3aed',
    '#f0a44f',
    '#0f82bd',
    '#7c3aed',
    '#c65a08',
    '#2563eb',
    '#7c3aed',
    '#0ea5e9',
  ];

  function renderPrefix(node: { option: CrmTreeNodeData; checked: boolean; selected: boolean }) {
    const index = roles.value.findIndex((role) => role.id === node.option.id);
    return h('span', {
      class: 'role-tree__color',
      style: {
        backgroundColor: rolePalette[index % rolePalette.length],
      },
    });
  }

  function getRoleMemberCount(role: CrmTreeNodeData) {
    return role.userCount ?? role.memberCount ?? role.userNum ?? role.count ?? 0;
  }

  function renderExtra(node: { option: CrmTreeNodeData; checked: boolean; selected: boolean }) {
    return h('span', { class: 'role-tree__count' }, String(getRoleMemberCount(node.option)));
  }

  function updateRoleName(node: CrmTreeNodeData) {
    return Promise.resolve(
      updateRole({
        id: node.id,
        name: node.name,
      })
    );
  }

  const activeRole = computed(() => roles.value.find((e) => e.id === selectedKeys.value[0]));
  const nodeMoreActions: ActionsItem[] = [
    {
      key: 'rename',
      label: t('common.rename'),
      permission: ['SYSTEM_ROLE:UPDATE'],
    },
    {
      key: 'copy',
      label: t('common.copy'),
      permission: ['SYSTEM_ROLE:ADD'],
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: t('common.delete'),
      danger: true,
      permission: ['SYSTEM_ROLE:DELETE'],
    },
  ];

  function filterMoreActionFunc(items: ActionsItem[], node: CrmTreeNodeData) {
    if (node.internal || !hasAnyPermission(['SYSTEM_ROLE:UPDATE', 'SYSTEM_ROLE:ADD', 'SYSTEM_ROLE:DELETE'])) {
      return [];
    }
    if (activeRole.value?.isNew) {
      return node.id === activeRole.value?.id
        ? [
            {
              key: 'rename',
              label: t('common.rename'),
            },
          ]
        : [];
    }
    return items.filter((item) => {
      const { permission } = item;
      if (permission && !hasAnyPermission(permission)) {
        return false;
      }
      return true;
    });
  }

  const roleTreeSelectable = computed(() => !roles.value.some((role) => role.isNew || role.unsave));

  function handleRoleClick() {
    if (!roleTreeSelectable.value) {
      message.warning(t('role.saveFirst'));
    }
  }

  function handleMoreActionSelect(item: ActionsItem, node: CrmTreeNodeData) {
    switch (item.key) {
      case 'rename':
        break;
      case 'copy':
        if (!roleTreeSelectable.value) {
          message.warning(t('role.saveFirst'));
          return;
        }
        const id = getGenerateId();
        roles.value.push({
          ...roles.value[roles.value.length - 1],
          name: `${node.name}Copy`,
          internal: false,
          isNew: true,
          isCopy: true,
          copyFrom: node.id,
          id,
        });
        selectedKeys.value = [id];
        setIsSave(false);
        break;
      case 'delete':
        openModal({
          type: 'error',
          title: t('common.deleteConfirmTitle', { name: characterLimit(node.name) }),
          content: t('role.deleteConfirmContent'),
          positiveText: t('common.confirmDelete'),
          negativeText: t('common.cancel'),
          onPositiveClick: async () => {
            try {
              loading.value = true;
              await deleteRole(node.id);
              roles.value = roles.value.filter((role) => role.id !== node.id);
              if (selectedKeys.value.includes(node.id)) {
                selectedKeys.value = [roles.value[0].id];
              }
              message.success(t('common.deleteSuccess'));
            } catch (error) {
              // eslint-disable-next-line no-console
              console.log(error);
            } finally {
              loading.value = false;
            }
          },
        });
        break;
      default:
        break;
    }
  }

  const renameStatic = computed(() => activeRole.value?.isNew);

  const activeTab = ref('permission');

  function addRole() {
    if (!roleTreeSelectable.value) {
      message.warning(t('role.saveFirst'));
      return;
    }
    activeTab.value = 'permission';
    const id = getGenerateId();
    roles.value.push({
      id,
      name: t('role.newRole'),
      internal: false,
      dataScope: 'ALL',
      description: '',
      isNew: true,
    });
    selectedKeys.value = [id];
    setIsSave(false);
    nextTick(() => {
      roleTreeRef.value?.toggleEdit(id);
    });
  }

  const tabList = computed<TabPaneProps[]>(() => {
    if (activeRole.value?.isNew) {
      return [
        {
          name: 'permission',
          tab: t('role.permission'),
        },
      ];
    }
    return [
      {
        name: 'permission',
        tab: t('role.permission'),
      },
      {
        name: 'member',
        tab: t('role.member'),
      },
    ];
  });

  async function init() {
    loading.value = true;
    try {
      roles.value = await getRoles();
      selectedKeys.value = roles.value[0] ? [roles.value[0].id] : [];
      activeTab.value = 'permission';
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  async function handleCreated(id: string) {
    if (activeRole.value) {
      roles.value = await getRoles();
      selectedKeys.value = [id];
      activeRole.value.isNew = false;
      activeRole.value.unsave = false;
      setIsSave(true);
    }
  }

  function handleCancelCreate() {
    roles.value = roles.value.filter((role) => role.id !== selectedKeys.value[0]);
    selectedKeys.value = roles.value[0] ? [roles.value[0].id] : [];
    activeTab.value = 'permission';
  }

  function handleUnsaveChange(val: boolean) {
    if (activeRole.value) {
      setIsSave(!val);
      activeRole.value.unsave = val;
    }
  }

  onBeforeMount(() => {
    init();
  });
</script>

<style lang="less" scoped>
  .role-permission-page {
    display: grid;
    height: 100%;
    min-height: 0;
    grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
    gap: 16px;
  }
  .role-permission-page--license-offset {
    height: calc(100% - 64px);
  }
  .role-card,
  .role-detail-card {
    display: flex;
    overflow: hidden;
    min-height: 0;
    border: 1px solid #dbe5f1;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: 0 1px 2px rgb(15 23 42 / 3%);
    flex-direction: column;
  }
  .role-card__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 22px 10px;
  }
  .role-card__header h2 {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: #0f172a;
    line-height: 24px;
  }
  .role-card__add {
    width: 30px;
    height: 30px;
    border: 1px solid #d8e2ee;
    border-radius: 6px;
    color: #1f2a44;
    background: #ffffff;
  }
  .role-card__body {
    padding: 4px 22px 22px;
    min-height: 0;
    flex: 1;
  }
  .role-detail-card__header {
    display: flex;
    align-items: center;
    padding: 0 20px;
    min-height: 64px;
    flex-shrink: 0;
  }
  .role-detail-card__body {
    overflow: hidden;
    min-height: 0;
    flex: 1;
  }
  .role-segmented {
    display: inline-flex;
    align-items: center;
    padding: 3px;
    border-radius: 7px;
    background: #f5f7fb;
    gap: 2px;
  }
  .role-segmented__item {
    padding: 0 14px;
    min-width: 64px;
    height: 30px;
    font-size: 14px;
    font-weight: 600;
    border: 0;
    border-radius: 6px;
    color: #64748b;
    background: transparent;
    line-height: 30px;
    cursor: pointer;
  }
  .role-segmented__item--active {
    color: #4f46e5;
    background: #ffffff;
    box-shadow: 0 1px 4px rgb(15 23 42 / 8%);
  }
  :deep(.n-tree-node-wrapper) {
    padding: 0;
  }
  :deep(.n-tree-node) {
    margin-bottom: 4px;
    padding: 0 8px 0 10px;
    height: 44px;
    border-radius: 6px;
  }
  :deep(.n-tree-node-content) {
    min-width: 0;
    max-width: 100% !important;
    cursor: pointer;
  }
  :deep(.n-tree-node-switcher) {
    display: none;
  }
  :deep(.n-tree-node--selected) {
    background: #eef2ff !important;
  }
  :deep(.n-tree-node--selected .role-tree__title) {
    color: #4f46e5;
  }
  :deep(.crm-tree-node-extra) {
    display: flex;
    align-items: center;
    min-width: 54px;
    flex-direction: row-reverse;
    gap: 8px;
  }
  :deep(.role-tree__title) {
    font-size: 16px;
    font-weight: 600;
    color: #0f172a;
    line-height: 22px;
  }
  :deep(.role-tree__color) {
    display: inline-block;
    width: 4px;
    height: 22px;
    border-radius: 999px;
  }
  :deep(.role-tree__count) {
    min-width: 18px;
    font-size: 14px;
    text-align: right;
    color: #64748b;
    line-height: 20px;
  }
  :deep(.n-data-table-th) {
    background-color: #f8fafc;
  }

  @media (max-width: 1200px) {
    .role-permission-page {
      grid-template-columns: 280px minmax(0, 1fr);
    }
  }

  @media (max-width: 900px) {
    .role-permission-page {
      overflow: auto;
      grid-template-columns: minmax(280px, 1fr);
    }
    .role-card {
      min-height: 360px;
    }
    .role-detail-card {
      min-height: 640px;
    }
  }
</style>
