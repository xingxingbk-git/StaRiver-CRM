<template>
  <n-layout-sider
    v-model:collapsed="collapsed"
    collapse-mode="width"
    :collapsed-width="72"
    :width="230"
    :native-scrollbar="false"
    class="crm-layout-sider stariver-sider"
    @update-collapsed="appStore.setMenuCollapsed"
  >
    <div class="stariver-sider__inner">
      <div class="stariver-sider__brand" @click="router.push({ name: AppRouteEnum.WORKBENCH_INDEX })">
        <span class="stariver-sider__brand-logo" v-html="brandLogo"></span>
        <div v-if="!collapsed" class="stariver-sider__brand-copy">
          <span class="stariver-sider__brand-text" v-html="brandText"></span>
          <div class="stariver-sider__brand-version">v1.0</div>
        </div>
      </div>
      <n-scrollbar content-style="min-height: 500px;height: 100%;width: 100%">
        <n-menu
          v-model:value="menuValue"
          v-model:expanded-keys="expandedKeys"
          :root-indent="18"
          :indent="appStore.getMenuIconStatus ? 28 : 8"
          :collapsed-width="72"
          :icon-size="18"
          :collapsed-icon-size="22"
          :options="menuOptions"
          :render-label="renderLabel"
          accordion
          @update-value="menuChange"
        />
      </n-scrollbar>
      <div class="stariver-sider__bottom">
        <n-dropdown
          class="personal-dropdown"
          trigger="hover"
          placement="right-end"
          :options="personalMenuOptions"
          @select="personalMenuChange"
          @update-show="personalMenuUpdateShow"
        >
          <div class="stariver-sider__user" :class="personalMenuShow ? 'stariver-sider__user--active' : ''">
            <CrmPopConfirm
              v-model:show="showPopModal"
              :title="t('system.personal.addNewExport')"
              icon-type="primary"
              :content="t('system.personal.addNewExportPopContent')"
              :positive-text="t('common.gotIt')"
              negative-text=""
              placement="right-end"
              @confirm="confirmHandler"
            >
              <span class="personal-export-pop"></span>
            </CrmPopConfirm>
            <CrmAvatar :size="collapsed ? 25 : 40" class="flex-shrink-0 transition-all" />
            <div v-if="!collapsed" class="min-w-0 flex-1">
              <div class="one-line-text stariver-sider__user-name">{{ userStore.userInfo.name }}</div>
              <div class="one-line-text stariver-sider__user-role">
                {{ userStore.userInfo.departmentName || t('common.currentUser') }}
              </div>
            </div>
            <CrmIcon v-if="!collapsed" type="iconicon_chevron_right" :size="14" class="text-[#94a3b8]" />
          </div>
        </n-dropdown>
        <button class="stariver-sider__collapse" @click="() => appStore.setMenuCollapsed(!collapsed)">
          <CrmIcon :type="collapsed ? 'iconicon_menu_fold1' : 'iconicon_menu_unfold1'" :size="16" />
        </button>
      </div>
    </div>
  </n-layout-sider>
  <personalExportDrawer v-model:visible="showPersonalExport" />
</template>

<script setup lang="ts">
  import { RouteLocationNormalizedGeneric, useRouter } from 'vue-router';
  import { NDropdown, NLayoutSider, NMenu, NScrollbar, NTooltip } from 'naive-ui';

  import brandLogo from '@/assets/icons/project/brand-logo.svg?raw';
  import brandText from '@/assets/icons/project/brand-text.svg?raw';

  import { PersonalEnum } from '@lib/shared/enums/systemEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { listenerRouteChange } from '@lib/shared/method/route-listener';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import CrmPopConfirm from '@/components/pure/crm-pop-confirm/index.vue';
  import CrmAvatar from '@/components/business/crm-avatar/index.vue';
  import personalExportDrawer from '@/views/system/business/components/personalExportDrawer.vue';

  import useUser from '@/hooks/useUser';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import useUserStore from '@/store/modules/user';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    AppRouteEnum,
    ClueRouteEnum,
    ContractRouteEnum,
    CustomerRouteEnum,
    DashboardRouteEnum,
    OpportunityRouteEnum,
    ProductRouteEnum,
    SalesRouteEnum,
    SystemRouteEnum,
    WorkbenchRouteEnum,
  } from '@/enums/routeEnum';

  import { MenuGroupOption, MenuOption } from 'naive-ui/es/menu/src/interface';

  const emit = defineEmits<{
    (e: 'openPersonalInfo', tab: PersonalEnum): void;
  }>();

  const { logout } = useUser();

  const { t } = useI18n();
  const appStore = useAppStore();
  const userStore = useUserStore();
  const licenseStore = useLicenseStore();
  const router = useRouter();
  const collapsed = ref(appStore.getMenuCollapsed);
  const menuValue = ref<string>(AppRouteEnum.SYSTEM_ORG);
  const expandedKeys = ref<string[]>([]);
  const personalMenuValue = ref<string>('');
  const personalTab = ref(PersonalEnum.INFO);
  const visitedKey = 'doNotShowPersonalExportAgain';
  const { addVisited, getIsVisited } = useVisit(visitedKey);
  const navIconSources = import.meta.glob('../../assets/icons/project/nav/*.svg', {
    query: '?raw',
    import: 'default',
    eager: true,
  }) as Record<string, string>;

  watch(
    () => appStore.getMenuCollapsed,
    (value) => {
      collapsed.value = value;
    }
  );

  function renderIcon(type: string) {
    const svgSource = type.startsWith('/icons/') ? navIconSources[`../../assets${type}`] : '';

    return () =>
      svgSource
        ? h('span', {
            class: 'stariver-sider__menu-icon',
            innerHTML: svgSource,
          })
        : h(CrmIcon, {
            size: 18,
            type,
            class: 'text-[var(--text-n1)]',
          });
  }

  const hasExportPermission = computed(() =>
    hasAnyPermission(['CUSTOMER_MANAGEMENT:EXPORT', 'OPPORTUNITY_MANAGEMENT:EXPORT', 'CLUE_MANAGEMENT:EXPORT'])
  );

  const personalMenuOptions = computed(() => [
    {
      key: 'header',
      type: 'render',
      render: () =>
        h(
          NTooltip,
          {
            delay: 300,
          },
          {
            trigger: () =>
              h(
                'div',
                { class: 'personal-name one-line-text max-w-[110px]' },
                { default: () => userStore.userInfo.name }
              ),
            default: () => h('div', {}, { default: () => userStore.userInfo.name }),
          }
        ),
    },
    {
      key: 'header-divider',
      type: 'divider',
    },
    {
      label: t('module.personal.info'),
      key: AppRouteEnum.PERSONAL_INFO,
      icon: renderIcon('iconicon_set_up'),
    },
    {
      label: t('module.personal.plan'),
      key: AppRouteEnum.PERSONAL_PLAN,
      icon: renderIcon('iconicon_calendar1'),
    },
    ...(hasExportPermission.value
      ? [
          {
            label: t('module.personal.myExport'),
            key: AppRouteEnum.PERSONAL_EXPORT,
            icon: renderIcon('iconicon_export'),
          },
        ]
      : []),
    {
      label: t('module.logout'),
      key: AppRouteEnum.LOGOUT,
      icon: renderIcon('iconicon_logout'),
    },
  ]);

  function renderLabel(option: MenuOption | MenuGroupOption) {
    return h(
      NTooltip,
      {
        delay: 300,
      },
      {
        trigger: () =>
          h('div', { class: 'stariver-sider__menu-label' }, [
            h('span', { class: 'stariver-sider__menu-text' }, option.label as string),
            (option as any).badge ? h('span', { class: 'stariver-sider__menu-badge' }, (option as any).badge) : null,
          ]),
        default: () => h('div', {}, { default: () => option.label }),
      }
    );
  }

  const showPopModal = ref(false);
  function confirmHandler() {
    addVisited();
    showPopModal.value = false;
  }

  let timer: any = null;
  function initExportPop() {
    if (!getIsVisited() && hasExportPermission.value) {
      showPopModal.value = true;

      if (timer) {
        clearTimeout(timer);
        timer = null;
      }
      timer = setTimeout(() => {
        confirmHandler();
        timer = null;
      }, 5000);
    }
  }

  const exportRouteNames = [
    CustomerRouteEnum.CUSTOMER,
    CustomerRouteEnum.CUSTOMER_INDEX,
    ClueRouteEnum.CLUE_MANAGEMENT,
    ClueRouteEnum.CLUE_MANAGEMENT_CLUE,
    OpportunityRouteEnum.OPPORTUNITY,
    OpportunityRouteEnum.OPPORTUNITY_OPT,
    OpportunityRouteEnum.OPPORTUNITY_QUOTATION,
  ];

  const isRequiredExportRoute = (key: string) => exportRouteNames.includes(key as any);

  async function menuChange(key: string) {
    menuValue.value = key;
    await router.push({ name: key });
    if (isRequiredExportRoute(key)) {
      initExportPop();
    }
    expandedKeys.value = [];
  }

  const personalMenuShow = ref(false);
  function personalMenuUpdateShow(value: boolean) {
    personalMenuShow.value = value;
  }

  const showPersonalExport = ref(false);

  async function personalMenuChange(key: string) {
    personalMenuValue.value = key;
    if (key === AppRouteEnum.PERSONAL_INFO || key === AppRouteEnum.PERSONAL_PLAN) {
      if (key === AppRouteEnum.PERSONAL_INFO) {
        personalTab.value = PersonalEnum.INFO;
      } else {
        personalTab.value = PersonalEnum.MY_PLAN;
      }
      emit('openPersonalInfo', personalTab.value);
    } else if (key === AppRouteEnum.PERSONAL_EXPORT) {
      showPersonalExport.value = true;
    } else {
      await userStore.logout();
      logout();
      if (!licenseStore.hasLicense()) {
        // license到期后，退出登录重置界面配置
        appStore.resetPageConfig();
        window.location.reload();
      }
    }
  }

  type StariverMenuItem = {
    label: string;
    key: string;
    icon: string;
    permissions?: string[];
    badge?: string | number;
  };

  type StariverMenuGroup = {
    label: string;
    children: StariverMenuItem[];
  };

  const canAccess = (permissions?: string[]) => !permissions?.length || hasAnyPermission(permissions);

  const routeMenuMap: Record<string, string> = {
    [WorkbenchRouteEnum.WORKBENCH]: WorkbenchRouteEnum.WORKBENCH_INDEX,
    [WorkbenchRouteEnum.WORKBENCH_INDEX]: WorkbenchRouteEnum.WORKBENCH_INDEX,
    [DashboardRouteEnum.DASHBOARD]: DashboardRouteEnum.DASHBOARD_INDEX,
    [DashboardRouteEnum.DASHBOARD_INDEX]: DashboardRouteEnum.DASHBOARD_INDEX,
    [ProductRouteEnum.PRODUCT]: ProductRouteEnum.PRODUCT_PRO,
    [ProductRouteEnum.PRODUCT_PRO]: ProductRouteEnum.PRODUCT_PRO,
    [ProductRouteEnum.PRODUCT_REQUIREMENT]: ProductRouteEnum.PRODUCT_REQUIREMENT,
    [ProductRouteEnum.PRODUCT_PRICE]: ProductRouteEnum.PRODUCT_PRO,
    [ProductRouteEnum.PRODUCT_CREATE]: ProductRouteEnum.PRODUCT_PRO,
    [ProductRouteEnum.PRODUCT_DETAIL]: ProductRouteEnum.PRODUCT_PRO,
    [CustomerRouteEnum.CUSTOMER]: CustomerRouteEnum.CUSTOMER_INDEX,
    [CustomerRouteEnum.CUSTOMER_INDEX]: CustomerRouteEnum.CUSTOMER_INDEX,
    [CustomerRouteEnum.CUSTOMER_DETAIL]: CustomerRouteEnum.CUSTOMER_INDEX,
    [CustomerRouteEnum.CUSTOMER_CONTACT]: CustomerRouteEnum.CUSTOMER_INDEX,
    [CustomerRouteEnum.CUSTOMER_OPEN_SEA]: CustomerRouteEnum.CUSTOMER_INDEX,
    [ClueRouteEnum.CLUE_MANAGEMENT]: ClueRouteEnum.CLUE_MANAGEMENT_CLUE,
    [ClueRouteEnum.CLUE_MANAGEMENT_CLUE]: ClueRouteEnum.CLUE_MANAGEMENT_CLUE,
    [ClueRouteEnum.CLUE_MANAGEMENT_POOL]: ClueRouteEnum.CLUE_MANAGEMENT_CLUE,
    [OpportunityRouteEnum.OPPORTUNITY]: OpportunityRouteEnum.OPPORTUNITY_OPT,
    [OpportunityRouteEnum.OPPORTUNITY_OPT]: OpportunityRouteEnum.OPPORTUNITY_OPT,
    [OpportunityRouteEnum.OPPORTUNITY_QUOTATION]: OpportunityRouteEnum.OPPORTUNITY_QUOTATION,
    [ContractRouteEnum.CONTRACT]: ContractRouteEnum.CONTRACT_INDEX,
    [ContractRouteEnum.CONTRACT_INDEX]: ContractRouteEnum.CONTRACT_INDEX,
    [ContractRouteEnum.CONTRACT_PAYMENT]: ContractRouteEnum.CONTRACT_INDEX,
    [ContractRouteEnum.CONTRACT_PAYMENT_RECORD]: ContractRouteEnum.CONTRACT_INDEX,
    [ContractRouteEnum.CONTRACT_BUSINESS_NAME]: ContractRouteEnum.CONTRACT_INDEX,
    [ContractRouteEnum.CONTRACT_INVOICE]: ContractRouteEnum.CONTRACT_INDEX,
    [SalesRouteEnum.SALES_TEAM_ROOT]: SalesRouteEnum.SALES_TEAM,
    [SalesRouteEnum.SALES_TEAM]: SalesRouteEnum.SALES_TEAM,
    [SalesRouteEnum.SALES_ANALYTICS_ROOT]: SalesRouteEnum.SALES_ANALYTICS,
    [SalesRouteEnum.SALES_ANALYTICS]: SalesRouteEnum.SALES_ANALYTICS,
    [SystemRouteEnum.SYSTEM]: SystemRouteEnum.SYSTEM_ORG,
    [SystemRouteEnum.SYSTEM_ORG]: SystemRouteEnum.SYSTEM_ORG,
    [SystemRouteEnum.SYSTEM_ROLE]: SystemRouteEnum.SYSTEM_ROLE,
    [SystemRouteEnum.SYSTEM_MESSAGE]: SystemRouteEnum.SYSTEM_MESSAGE,
    [SystemRouteEnum.SYSTEM_PROCESS]: SystemRouteEnum.SYSTEM_PROCESS_INDEX,
    [SystemRouteEnum.SYSTEM_PROCESS_INDEX]: SystemRouteEnum.SYSTEM_PROCESS_INDEX,
    [SystemRouteEnum.SYSTEM_BUSINESS]: SystemRouteEnum.SYSTEM_BUSINESS,
    [SystemRouteEnum.SYSTEM_LOG]: SystemRouteEnum.SYSTEM_LOG,
  };

  const menuGroups = computed<StariverMenuGroup[]>(() => [
    {
      label: '主导航',
      children: [
        { label: '工作台', key: WorkbenchRouteEnum.WORKBENCH_INDEX, icon: '/icons/project/nav/home.svg' },
        {
          label: '数据洞察',
          key: DashboardRouteEnum.DASHBOARD_INDEX,
          icon: '/icons/project/nav/data.svg',
          permissions: ['DASHBOARD:READ'],
        },
      ],
    },
    {
      label: '产品需求',
      children: [
        {
          label: '产品集',
          key: ProductRouteEnum.PRODUCT_PRO,
          icon: '/icons/project/nav/product.svg',
          permissions: ['PRODUCT_MANAGEMENT:READ'],
        },
        {
          label: '需求管理',
          key: ProductRouteEnum.PRODUCT_REQUIREMENT,
          icon: '/icons/project/nav/requirement.svg',
          permissions: ['PRODUCT_MANAGEMENT:READ'],
          badge: 6,
        },
      ],
    },
    {
      label: '销售 CRM',
      children: [
        {
          label: '客户管理',
          key: CustomerRouteEnum.CUSTOMER_INDEX,
          icon: '/icons/project/nav/personal.svg',
          permissions: ['CUSTOMER_MANAGEMENT:READ'],
        },
        {
          label: '销售线索',
          key: ClueRouteEnum.CLUE_MANAGEMENT_CLUE,
          icon: '/icons/project/nav/notification.svg',
          permissions: ['CLUE_MANAGEMENT:READ'],
          badge: 18,
        },
        {
          label: '商机管理',
          key: OpportunityRouteEnum.OPPORTUNITY_OPT,
          icon: '/icons/project/nav/opportunity.svg',
          permissions: ['OPPORTUNITY_MANAGEMENT:READ'],
          badge: 24,
        },
        {
          label: '报价管理',
          key: OpportunityRouteEnum.OPPORTUNITY_QUOTATION,
          icon: '/icons/project/nav/quote.svg',
          permissions: ['QUOTE_MANAGEMENT:READ'],
        },
        {
          label: '合同管理',
          key: ContractRouteEnum.CONTRACT_INDEX,
          icon: '/icons/project/nav/contract.svg',
          permissions: ['CONTRACT:READ'],
        },
        {
          label: '团队与业绩',
          key: SalesRouteEnum.SALES_TEAM,
          icon: '/icons/project/nav/team.svg',
          permissions: ['CUSTOMER_MANAGEMENT:READ', 'CLUE_MANAGEMENT:READ', 'OPPORTUNITY_MANAGEMENT:READ'],
        },
        {
          label: '数据分析',
          key: SalesRouteEnum.SALES_ANALYTICS,
          icon: '/icons/project/nav/data.svg',
          permissions: ['DASHBOARD:READ'],
        },
      ],
    },
    {
      label: '系统',
      children: [
        {
          label: '组织架构',
          key: SystemRouteEnum.SYSTEM_ORG,
          icon: '/icons/project/nav/user.svg',
          permissions: ['SYS_ORGANIZATION:READ'],
        },
        {
          label: '角色权限',
          key: SystemRouteEnum.SYSTEM_ROLE,
          icon: '/icons/project/nav/role.svg',
          permissions: ['SYSTEM_ROLE:READ'],
        },
        {
          label: '消息设置',
          key: SystemRouteEnum.SYSTEM_MESSAGE,
          icon: '/icons/project/nav/notification.svg',
          permissions: ['SYSTEM_NOTICE:READ'],
        },
        {
          label: '流程设置',
          key: SystemRouteEnum.SYSTEM_PROCESS_INDEX,
          icon: '/icons/project/nav/process.svg',
          permissions: ['PROCESS_SETTING:READ'],
        },
        {
          label: '企业设置',
          key: SystemRouteEnum.SYSTEM_BUSINESS,
          icon: '/icons/project/nav/enterprise.svg',
          permissions: ['SYSTEM_SETTING:READ'],
        },
        {
          label: '系统日志',
          key: SystemRouteEnum.SYSTEM_LOG,
          icon: '/icons/project/nav/log.svg',
          permissions: ['OPERATION_LOG:READ'],
        },
      ],
    },
  ]);

  const menuOptions = computed<MenuOption[]>(
    () =>
      menuGroups.value
        .map((group) => {
          const children = group.children
            .filter((item) => canAccess(item.permissions))
            .map((item) => ({
              label: item.label,
              key: item.key,
              badge: item.badge,
              icon: renderIcon(item.icon),
            }));
          if (!children.length) return null;
          return {
            type: 'group',
            label: group.label,
            key: `group-${group.label}`,
            children,
          };
        })
        .filter(Boolean) as MenuOption[]
  );

  function setMenuValue(_route: RouteLocationNormalizedGeneric) {
    const routeNames = [
      _route.name?.toString(),
      ..._route.matched.map((item) => item.name?.toString()).reverse(),
    ].filter(Boolean) as string[];
    const matchedMenuValue = routeNames.map((name) => routeMenuMap[name]).find(Boolean);
    menuValue.value = matchedMenuValue || routeNames[0] || '';
    expandedKeys.value = [];
  }

  onBeforeMount(() => {
    setMenuValue(router.currentRoute.value);

    const routeName = router.currentRoute.value.name?.toString() ?? '';
    if (isRequiredExportRoute(routeName)) {
      initExportPop();
    }
  });

  /**
   * 监听路由变化，切换菜单选中
   */
  listenerRouteChange((newRoute) => {
    setMenuValue(newRoute);
  }, true);

  watch(
    () => appStore.getRestoreMenuTimeStamp,
    (value) => {
      if (value) {
        setMenuValue(router.currentRoute.value);
      }
    }
  );

  watch(
    () => appStore.orgId,
    (orgId) => {
      if (orgId) {
        appStore.initModuleConfig();
        appStore.initNavTopConfig();
      }
    },
    { immediate: true }
  );
</script>

<style lang="less">
  .crm-layout-sider {
    font-weight: 500;
    background: #ffffff !important;
    border-right: 1px solid #e2e8f0;
    .n-scrollbar-content {
      @apply h-full;
    }
    .n-menu {
      --n-item-height: 36px !important;
      --n-border-radius: 6px !important;
      --n-item-text-color: #475569 !important;
      --n-item-text-color-hover: #0f172a !important;
      --n-item-text-color-active: #3730a3 !important;
      --n-item-icon-color: #64748b !important;
      --n-item-icon-color-active: #4f46e5 !important;
      --n-item-color-active: #eef2ff !important;
      --n-item-color-active-hover: #eef2ff !important;
    }
    .n-menu .n-menu-item-content {
      margin: 2px 12px;
      padding-right: 12px !important;
    }
    .n-menu .n-menu-item-content-header {
      font-size: 13px;
      font-weight: 500;
    }
    .n-menu .n-menu-item-group-title {
      height: 24px;
      padding: 10px 18px 4px !important;
      color: #94a3b8 !important;
      font-size: 11px;
      font-weight: 700;
      line-height: 16px;
    }
    .n-menu-item-content::before {
      left: 0 !important;
      right: 0 !important;
    }
  }
  .crm-layout-sider .n-menu-item-content--selected,
  .crm-layout-sider .n-menu-item-content--child-active {
    .n-icon {
      color: var(--n-item-text-color-active) !important;
    }
  }
  .crm-layout-sider .n-menu-item-content .stariver-sider__menu-icon {
    color: var(--n-item-icon-color) !important;
  }
  .crm-layout-sider .n-menu-item-content:hover .stariver-sider__menu-icon {
    color: var(--n-item-text-color-hover) !important;
  }
  .crm-layout-sider .n-menu-item-content--selected .stariver-sider__menu-icon,
  .crm-layout-sider .n-menu-item-content--child-active .stariver-sider__menu-icon {
    color: var(--n-item-text-color-active) !important;
  }
  .stariver-sider__inner {
    display: flex;
    height: 100%;
    flex-direction: column;
    justify-content: space-between;
  }
  .stariver-sider__brand {
    display: flex;
    align-items: center;
    gap: 9px;
    flex: 0 0 60px;
    height: 60px;
    min-height: 60px;
    max-height: 60px;
    border-bottom: 1px solid #e2e8f0;
    padding: 12px 18px;
    background: #ffffff;
    cursor: pointer;
  }
  .stariver-sider__brand-logo {
    display: flex;
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
  }
  .stariver-sider__brand-logo svg {
    display: block;
    width: 34px;
    height: 34px;
  }
  .stariver-sider__brand-copy {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 6px;
  }
  .stariver-sider__brand-text {
    display: block;
    width: 96px;
    height: 14px;
    overflow: hidden;
  }
  .stariver-sider__brand-text svg {
    display: block;
    width: 96px;
    height: auto;
  }
  .stariver-sider__brand-version {
    display: inline-flex;
    height: 18px;
    flex-shrink: 0;
    align-items: center;
    border-radius: 999px;
    padding: 0 6px;
    background: #f1f5f9;
    color: #64748b;
    font-size: 10px;
    font-weight: 600;
    line-height: 18px;
  }
  .stariver-sider__menu-label {
    display: flex;
    min-width: 0;
    flex: 1;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }
  .stariver-sider__menu-icon {
    display: block;
    width: 16px;
    height: 16px;
    color: currentColor;
    --stroke-0: currentColor;
    --fill-0: currentColor;
  }
  .stariver-sider__menu-icon svg {
    display: block !important;
    width: 16px;
    height: 16px;
  }
  .stariver-sider__menu-icon svg [stroke]:not([stroke='none']) {
    stroke: currentColor !important;
  }
  .stariver-sider__menu-icon svg [fill]:not([fill='none']) {
    fill: currentColor !important;
  }
  .stariver-sider__menu-text {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .stariver-sider__menu-badge {
    display: inline-flex;
    min-width: 18px;
    height: 18px;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    padding: 0 6px;
    background: #eef2ff;
    color: #4f46e5;
    font-size: 11px;
    font-weight: 700;
    line-height: 18px;
  }
  .stariver-sider__bottom {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 12px;
    border-top: 1px solid #e2e8f0;
  }
  .stariver-sider__user {
    position: relative;
    display: flex;
    width: 100%;
    cursor: pointer;
    align-items: center;
    gap: 9px;
    border-radius: 8px;
    padding: 8px;
    background: #f8fafc;
    transition: background-color 0.2s ease;
  }
  .stariver-sider__user:hover,
  .stariver-sider__user--active {
    background: #eef2ff;
  }
  .stariver-sider__user-name {
    max-width: 130px;
    color: #0f172a;
    font-size: 13px;
    font-weight: 600;
    line-height: 18px;
  }
  .stariver-sider__user-role {
    max-width: 130px;
    color: #64748b;
    font-size: 11px;
    line-height: 16px;
  }
  .stariver-sider__collapse {
    display: flex;
    height: 30px;
    width: 100%;
    align-items: center;
    justify-content: center;
    border: 0;
    border-radius: 6px;
    background: transparent;
    color: #64748b;
    cursor: pointer;
  }
  .stariver-sider__collapse:hover {
    background: #f1f5f9;
    color: #0f172a;
  }
  .personal-menu {
    min-width: 120px;
    .n-menu .n-menu-item {
      align-items: flex-start;
      margin-top: 0;
      padding: 4px 12px;
      height: 30px;
      border-radius: 4px;
    }
    .n-menu .n-menu-item:hover {
      transition: 0.7s;

      --n-item-color-hover: var(--primary-7);
    }
    .n-menu-item-content {
      padding-left: 0 !important;
      .n-menu-item-content-header {
        color: var(--text-n2);
      }
      .n-menu-item-content__icon {
        width: 16px;
        height: 16px;
        color: var(--text-n2);
      }
    }
    .n-menu-item-content::before {
      top: -4px;
      right: -8px;
      bottom: -4px;
      left: -8px;
    }
  }
  .personal-popover {
    min-width: 120px;
    background-color: var(--text-n10);
    .n-popover__content {
      padding: 0 !important;
    }
  }
  .personal-name {
    padding: 4px 8px;
    font-size: 14px;
    font-weight: 500;
    color: var(--text-n1);
    line-height: 22px;
  }
  .personal-dropdown {
    .n-dropdown-option-body {
      color: var(--text-n2) !important;
    }
  }
</style>

<style lang="less" scoped>
  .personal-export-pop {
    position: absolute;
    right: 0;
    bottom: 0;
  }
</style>
