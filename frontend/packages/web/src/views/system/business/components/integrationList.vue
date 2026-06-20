<template>
  <div class="business-integration" :class="{ 'is-loading': loading }">
    <section class="business-integration__section">
      <div class="business-integration__section-title"> 企业软件协同 </div>
      <CrmTab
        v-model:active-tab="activePlatformTab"
        class="business-integration__segment"
        no-content
        :tab-list="tabList"
        type="segment"
        :before-leave="handleBeforeLeave"
      />

      <div class="business-integration__grid business-integration__grid--single">
        <article v-for="item of platFormIntegrationList" :key="item.type" class="business-integration-card">
          <div class="business-integration-card__main">
            <div class="business-integration-card__logo">
              <CrmIcon :type="item.logo" :size="26" />
            </div>
            <div class="business-integration-card__content">
              <div class="business-integration-card__title-row">
                <h3>{{ item.title }}</h3>
                <CrmTag theme="light" size="small" custom-class="px-[6px]">
                  {{ getStatusText(item) }}
                </CrmTag>
              </div>
              <p>{{ item.description }}</p>
            </div>
            <n-tooltip :disabled="item.verify">
              <template #trigger>
                <n-switch
                  class="business-integration-card__switch"
                  :rubber-band="false"
                  :value="item.config.startEnable"
                  :disabled="!item.hasConfig || !item.verify || !hasAnyPermission(['SYSTEM_SETTING:UPDATE'])"
                  @update:value="handleChangeEnable(item, 'startEnable')"
                />
              </template>
              {{ t('system.business.notConfiguredTip') }}
            </n-tooltip>
          </div>
          <div class="business-integration-card__actions">
            <n-button
              v-permission="['SYSTEM_SETTING:UPDATE']"
              size="small"
              type="default"
              class="outline--secondary"
              @click="handleEdit(item)"
            >
              {{ t('common.config') }}
            </n-button>
            <n-button
              :disabled="!item.hasConfig"
              size="small"
              type="default"
              class="outline--secondary"
              @click="testLink(item)"
            >
              {{ t('common.testLink') }}
            </n-button>
          </div>
        </article>
      </div>
    </section>

    <section class="business-integration__section">
      <div class="business-integration__section-title">
        {{ t('system.business.authenticationSettings.openSourceDataTools') }}
      </div>
      <div class="business-integration__grid">
        <article v-for="item of integrationList" :key="item.type" class="business-integration-card">
          <div class="business-integration-card__main">
            <div class="business-integration-card__logo">
              <CrmSvgIcon :name="item.logo" width="26px" height="26px" />
            </div>
            <div class="business-integration-card__content">
              <div class="business-integration-card__title-row">
                <h3>{{ item.title }}</h3>
                <CrmTag theme="light" size="small" custom-class="px-[6px]">
                  {{ getStatusText(item) }}
                </CrmTag>
              </div>
              <p>{{ item.description }}</p>
            </div>
            <n-tooltip :disabled="item.verify">
              <template #trigger>
                <n-switch
                  v-if="item.type === CompanyTypeEnum.DATA_EASE"
                  class="business-integration-card__switch"
                  :rubber-band="false"
                  :value="item.config.deBoardEnable"
                  :disabled="!item.hasConfig || !item.verify || !hasAnyPermission(['SYSTEM_SETTING:UPDATE'])"
                  @update:value="handleChangeEnable(item, 'deBoardEnable')"
                />
                <n-switch
                  v-else-if="item.type === CompanyTypeEnum.SQLBot"
                  class="business-integration-card__switch"
                  :rubber-band="false"
                  :value="item.config.sqlBotChatEnable"
                  :disabled="!item.hasConfig || !item.verify || !hasAnyPermission(['SYSTEM_SETTING:UPDATE'])"
                  @update:value="handleChangeEnable(item, 'sqlBotChatEnable')"
                />
              </template>
              {{ t('system.business.notConfiguredTip') }}
            </n-tooltip>
          </div>
          <div class="business-integration-card__actions">
            <n-button
              v-if="item.type === CompanyTypeEnum.DATA_EASE"
              v-permission="['SYSTEM_SETTING:UPDATE']"
              size="small"
              type="default"
              class="outline--secondary"
              @click="handleSyncDE()"
            >
              {{ t('common.sync') }}
            </n-button>
            <n-button
              v-permission="['SYSTEM_SETTING:UPDATE']"
              size="small"
              type="default"
              class="outline--secondary"
              @click="handleEdit(item)"
            >
              {{ t('common.config') }}
            </n-button>
            <n-button
              :disabled="!item.hasConfig"
              size="small"
              type="default"
              class="outline--secondary"
              @click="testLink(item)"
            >
              {{ t('common.testLink') }}
            </n-button>
          </div>
        </article>
      </div>
    </section>
  </div>
  <EditIntegrationModal
    v-model:show="showEditIntegrationModal"
    :title="currentTitle"
    :integration="currentIntegration"
    @init-sync="editDone"
  />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { NButton, NSwitch, NTooltip, useMessage } from 'naive-ui';

  import { CompanyTypeEnum } from '@lib/shared/enums/commonEnum';
  import { loadScript, removeScript } from '@lib/shared/method/scriptLoader';
  import type { IntegrationItem, ThirdPartyResourceConfig } from '@lib/shared/models/system/business';

  import CrmSvgIcon from '@/components/pure/crm-svg/index.vue';
  import CrmTab from '@/components/pure/crm-tab/index.vue';
  import CrmTag from '@/components/pure/crm-tag/index.vue';
  import EditIntegrationModal from './editIntegrationModal.vue';

  import {
    getConfigSynchronization,
    switchThirdParty,
    syncDE,
    testConfigSynchronization,
    updateConfigSynchronization,
  } from '@/api/modules';
  import { defaultThirdPartyConfigMap, platformType } from '@/config/business';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  const { t } = useI18n();
  const Message = useMessage();
  const appStore = useAppStore();
  const { openModal } = useModal();
  const activePlatformTab = ref<CompanyTypeEnum>(CompanyTypeEnum.WECOM);

  const tabList = [
    { name: CompanyTypeEnum.WECOM, tab: t('system.business.WE_COM') },
    { name: CompanyTypeEnum.DINGTALK, tab: t('system.business.DING_TALK') },
    { name: CompanyTypeEnum.LARK, tab: t('system.business.LARK') },
  ];

  // 所有可用的集成平台配置
  const allIntegrations = [
    {
      type: CompanyTypeEnum.WECOM,
      title: t('system.business.WE_COM'),
      description: t('system.business.WE_COM.description'),
      logo: 'iconlogo_wechat-work',
    },
    {
      type: CompanyTypeEnum.DINGTALK,
      title: t('system.business.DING_TALK'),
      description: t('system.business.DING_TALK.description'),
      logo: 'iconlogo_dingtalk',
    },
    {
      type: CompanyTypeEnum.LARK,
      title: t('system.business.LARK'),
      description: t('system.business.LARK.description'),
      logo: 'iconlogo_lark',
    },
    {
      type: CompanyTypeEnum.DATA_EASE,
      title: 'DataEase',
      description: t('system.business.DE.description'),
      logo: 'dataease',
    },
    {
      type: CompanyTypeEnum.SQLBot,
      title: 'SQLBot',
      description: t('system.business.SQLBot.description'),
      logo: 'SQLBot',
    },
    {
      type: CompanyTypeEnum.MAXKB,
      title: 'MaxKB',
      description: t('system.business.agent.agentMaxKBDescription'),
      logo: 'maxKB',
    },
    {
      type: CompanyTypeEnum.TENDER,
      title: t('system.business.tenderTitle'),
      description: t('system.business.tenderDescription'),
      logo: 'dadan',
    },
    {
      type: CompanyTypeEnum.QCC,
      title: t('system.business.qichacha'),
      description: t('system.business.thirdQueryQccDescription'),
      logo: 'qichacha',
    },
  ];

  const originIntegrationList = ref<IntegrationItem[]>([]);
  const integrationList = ref<IntegrationItem[]>([]);

  const platFormIntegrationList = computed<IntegrationItem[]>(() =>
    originIntegrationList.value.filter((e) => e.type === activePlatformTab.value)
  );

  function getStatusText(item: IntegrationItem) {
    if (!item.hasConfig) return t('system.business.notConfigured');
    if (item.verify === false) return t('common.fail');
    if (item.verify === null) return t('common.unVerify');
    return t('common.success');
  }

  const loading = ref(false);
  async function initSyncList() {
    try {
      loading.value = true;
      const res = await getConfigSynchronization();
      const configMap = new Map(res.map((item) => [item.type, item]));
      originIntegrationList.value = allIntegrations
        .filter((item) =>
          [
            ...platformType,
            CompanyTypeEnum.DATA_EASE,
            CompanyTypeEnum.SQLBot,
            CompanyTypeEnum.MAXKB,
            CompanyTypeEnum.TENDER,
            CompanyTypeEnum.QCC,
          ].includes(item.type)
        )
        .map((item) => {
          const result = configMap.get(item.type);
          const config = result?.config;
          return {
            ...item,
            ...result,
            verify: result?.verify ?? false,
            hasConfig: !!config,
            config: {
              ...defaultThirdPartyConfigMap[item.type as CompanyTypeEnum],
              ...config,
            },
          };
        });

      integrationList.value = originIntegrationList.value.filter(
        (e) =>
          ![...platformType, CompanyTypeEnum.MAXKB, CompanyTypeEnum.TENDER, CompanyTypeEnum.QCC].includes(
            e.type as CompanyTypeEnum
          )
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const showEditIntegrationModal = ref(false);

  const currentTitle = ref('');
  const currentIntegration = ref<ThirdPartyResourceConfig>({
    type: CompanyTypeEnum.WECOM,
    verify: false,
    config: defaultThirdPartyConfigMap[CompanyTypeEnum.WECOM],
  });

  function handleEdit(item: IntegrationItem) {
    currentTitle.value = item.title;
    currentIntegration.value = { ...item };
    showEditIntegrationModal.value = true;
  }

  async function handleSyncDE() {
    try {
      loading.value = true;
      await syncDE();
      Message.success(t('org.syncSuccess'));
      await initSyncList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function makeParams(item: IntegrationItem, key?: string) {
    const config = {
      ...item.config,
      ...(key ? { [key]: !item.config[key] } : {}),
    };
    const thirdConfigKeys = Object.keys(defaultThirdPartyConfigMap[item.type as CompanyTypeEnum]);
    const params: Record<string, any> = {};
    thirdConfigKeys.forEach((configKey: string) => {
      params[configKey] = config[configKey];
    });
    return {
      verify: item.verify,
      type: item.type,
      config: params,
    };
  }

  async function handleChangeEnable(
    item: IntegrationItem,
    key:
      | 'deBoardEnable'
      | 'sqlBotBoardEnable'
      | 'sqlBotChatEnable'
      | 'startEnable'
      | 'mkEnable'
      | 'tenderEnable'
      | 'qccEnable'
  ) {
    try {
      loading.value = true;
      updateConfigSynchronization(makeParams(item, key))
        .then(async () => {
          Message.success(item.config[key] ? t('common.disableSuccess') : t('common.enableSuccess'));
          await initSyncList();
          appStore.initThirdPartyResource();
          if (item.config[key]) {
            removeScript(CompanyTypeEnum.SQLBot);
          } else {
            await loadScript(item.config.appSecret as string, { identifier: CompanyTypeEnum.SQLBot });
          }
        })
        .catch(() => {
          item.verify = false;
          item.config = {
            ...defaultThirdPartyConfigMap[item.type as CompanyTypeEnum],
          };
        })
        .finally(() => {
          loading.value = false;
        });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  }

  async function testLink(item: IntegrationItem) {
    try {
      testConfigSynchronization(makeParams(item))
        .then((res) => {
          const isSuccess = res.data.data;
          if (isSuccess) {
            Message.success(t('org.testConnectionSuccess'));
          } else {
            Message.error(t('org.testConnectionError'));
          }
          initSyncList();
        })
        .catch(() => {
          item.verify = false;
          item.config = {
            ...defaultThirdPartyConfigMap[item.type as CompanyTypeEnum],
          };
        });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function editDone() {
    await initSyncList();
    const sqlItem = integrationList.value.find((item) => item.type === CompanyTypeEnum.SQLBot);
    removeScript(CompanyTypeEnum.SQLBot);
    if (sqlItem && sqlItem.config.sqlBotChatEnable) {
      await loadScript(sqlItem.config.appSecret as string, { identifier: CompanyTypeEnum.SQLBot });
    }
  }

  async function initThirdPartyResource() {
    try {
      await appStore.initThirdPartyResource();
      activePlatformTab.value = appStore.activePlatformResource.syncResource ?? CompanyTypeEnum.WECOM;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function handleBeforeLeave(newVal: string | number, _o: string | number | null) {
    if (!newVal) return true;
    const currentPlatformName = allIntegrations.find((e) => e.type === newVal)?.title ?? '';

    return new Promise<boolean>((resolve) => {
      openModal({
        type: 'error',
        title: t('system.business.authenticationSettings.confirmTogglePlatform'),
        content: t('system.business.authenticationSettings.togglePlatformTip', { type: currentPlatformName }),
        positiveText: t('common.confirm'),
        negativeText: t('common.cancel'),

        onPositiveClick: async () => {
          try {
            await switchThirdParty(newVal as CompanyTypeEnum);
            initSyncList();
            appStore.initThirdPartyResource();
            Message.success(t('common.operationSuccess'));
            resolve(true);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
            resolve(false);
          }
        },

        onNegativeClick: () => {
          resolve(false);
        },
      });
    });
  }

  onBeforeMount(() => {
    initSyncList();
    initThirdPartyResource();
  });
</script>

<style lang="less" scoped>
  .business-integration {
    display: flex;
    flex-direction: column;
    gap: 16px;
    opacity: 1;
    transition: opacity 0.2s ease;
  }
  .business-integration.is-loading {
    opacity: 0.72;
  }
  .business-integration__section {
    padding: 24px;
    border-radius: 8px;
    background: #ffffff;
  }
  .business-integration__section-title {
    margin-bottom: 16px;
    font-size: 18px;
    font-weight: 800;
    color: #0f172a;
    line-height: 24px;
  }
  .business-integration__segment {
    margin-bottom: 16px;
    width: 330px;
  }
  .business-integration__grid {
    display: grid;
    max-width: 980px;
    grid-template-columns: repeat(2, minmax(320px, 1fr));
    gap: 16px;
  }
  .business-integration__grid--single {
    grid-template-columns: minmax(320px, 460px);
  }
  .business-integration-card {
    display: flex;
    justify-content: space-between;
    padding: 24px;
    min-height: 150px;
    border: 1px solid #dbe5f1;
    border-radius: 8px;
    background: #ffffff;
    flex-direction: column;
  }
  .business-integration-card__main {
    display: flex;
    align-items: flex-start;
    gap: 12px;
  }
  .business-integration-card__logo {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 42px;
    height: 42px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #f8fafc;
    flex-shrink: 0;
  }
  .business-integration-card__content {
    min-width: 0;
    flex: 1;
  }
  .business-integration-card__title-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .business-integration-card__title-row h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 800;
    color: #0f172a;
    line-height: 22px;
  }
  .business-integration-card__content p {
    margin: 8px 0 0;
    font-size: 13px;
    color: #64748b;
    line-height: 20px;
  }
  .business-integration-card__switch {
    margin-top: 2px;
    flex-shrink: 0;
  }
  .business-integration-card__actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
  @media (max-width: 1280px) {
    .business-integration__grid {
      grid-template-columns: minmax(320px, 1fr);
    }
  }
</style>
