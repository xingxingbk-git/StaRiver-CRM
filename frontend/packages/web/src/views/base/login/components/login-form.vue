<template>
  <div v-if="preheat && !props.isPreview" class="login-form login-form-loading">
    <n-spin :loading="preheat" :description="t('login.form.loading')" />
  </div>
  <div v-else class="login-form" :class="{ 'login-form-preview': props.isPreview }">
    <template v-if="props.isPreview">
      <div class="title">
        <div class="flex justify-center">
          <img :src="innerLogo" class="h-[52px] w-[240px]" />
        </div>
        <div class="title-0 mt-[16px] flex justify-center">
          <span class="title-welcome">
            {{ t(innerSlogan || '') || t('login.form.title') }}
          </span>
        </div>
      </div>
      <div class="form-preview mt-[40px] min-w-[480px]">
        <div v-if="userInfo.authenticate === 'LOCAL'" class="form-title">{{ t('login.form.accountLogin') }}</div>
        <div v-if="isShowLDAP && userInfo.authenticate === 'LDAP'" class="form-title">{{
          t('login.form.LDAPLogin')
        }}</div>
        <div v-if="!showQrCodeTab">
          <n-form
            ref="formRef"
            :model="userInfo"
            :rules="{
              username: [{ required: true, message: t('login.form.userName.errMsg'), trigger: ['input', 'blur'] }],
              password: [{ required: true, message: t('login.form.password.errMsg'), trigger: ['input', 'blur'] }],
            }"
          >
            <n-form-item class="login-form-item" path="username" :show-label="false">
              <n-input
                v-model:value="userInfo.username"
                class="login-input"
                :maxlength="64"
                size="large"
                :placeholder="
                  userInfo.authenticate !== 'LOCAL'
                    ? t('login.form.userName.placeholderOther')
                    : t('login.form.userName.placeholder')
                "
              />
            </n-form-item>
            <n-form-item class="login-form-item" path="password" :show-label="false">
              <n-input
                v-model:value="userInfo.password"
                type="password"
                class="login-password-input"
                :placeholder="t('login.form.password.placeholder')"
                clearable
                :maxlength="64"
                size="large"
                show-password-on="click"
                @keydown.enter="handleSubmit"
              />
            </n-form-item>
            <div class="mt-[12px]" :class="hasMoreLoginWay ? 'mb-[60px]' : 'mb-7'">
              <n-button type="primary" size="large" block :loading="loading" @click="handleSubmit">
                {{ t('login.form.login') }}
              </n-button>
              <div v-if="showDemo" class="mb-[-16px] mt-[16px] flex items-center gap-[16px]">
                <div class="flex items-center">
                  <div>{{ t('login.form.username') }}：</div>
                  <div>demo</div>
                </div>
                <div class="flex items-center">
                  <div>{{ t('login.form.password') }}：</div>
                  <div>demo</div>
                </div>
              </div>
            </div>
          </n-form>
        </div>
        <div v-if="showQrCodeTab">
          <tab-qr-code />
        </div>
        <template v-if="hasMoreLoginWay">
          <n-divider orientation="center" dashed class="!m-0 !mb-2">
            <span class="text-[12px] font-normal text-[var(--text-n4)]">{{ t('login.form.modeLoginMethods') }}</span>
          </n-divider>
          <div class="mt-4 flex items-center justify-center">
            <div v-if="isShowQRCode && !showQrCodeTab" class="loginType" @click="switchLoginType('QR_CODE')">
              <CrmIcon type="iconicon_scan" class="text-[#4666e5]" :size="20" />
            </div>
            <div
              v-if="userInfo.authenticate !== 'LDAP' && isShowLDAP"
              class="loginType"
              @click="switchLoginType('LDAP')"
            >
              <span class="type-text text-[10px]">LDAP</span>
            </div>
            <div v-if="userInfo.authenticate !== 'LOCAL'" class="loginType" @click="switchLoginType('LOCAL')">
              <CrmIcon type="iconicon_that_person" class="text-[#4666e5]" :size="20" />
            </div>
            <div v-if="isShowOIDC && userInfo.authenticate !== 'OIDC'" class="loginType" @click="redirectAuth('OIDC')">
              <span class="type-text text-[10px]">OIDC</span>
            </div>
            <div
              v-if="isShowOAUTH && userInfo.authenticate !== 'OAUTH2'"
              class="loginType"
              @click="redirectAuth('OAUTH2')"
            >
              <span class="type-text text-[10px]">OAuth</span>
            </div>
            <div v-if="isShowCAS && userInfo.authenticate !== 'CAS'" class="loginType" @click="redirectAuth('CAS')">
              <span class="type-text text-[10px]">CAS</span>
            </div>
          </div>
        </template>
        <div v-if="props.isPreview" class="mask"></div>
      </div>
    </template>
    <template v-else>
      <div v-if="userInfo.authenticate === 'LOCAL'" class="form-title">{{ t('login.form.accountLogin') }}</div>
      <div v-if="isShowLDAP && userInfo.authenticate === 'LDAP'" class="form-title">{{
        t('login.form.LDAPLogin')
      }}</div>
      <div v-if="!showQrCodeTab">
        <n-form
          ref="formRef"
          :model="userInfo"
          :rules="{
            username: [{ required: true, message: t('login.form.userName.errMsg'), trigger: ['input', 'blur'] }],
            password: [{ required: true, message: t('login.form.password.errMsg'), trigger: ['input', 'blur'] }],
          }"
        >
          <n-form-item class="login-form-item" path="username" :show-label="false">
            <n-input
              v-model:value="userInfo.username"
              class="login-input"
              :maxlength="64"
              size="large"
              :placeholder="
                userInfo.authenticate !== 'LOCAL'
                  ? t('login.form.userName.placeholderOther')
                  : t('login.form.userName.placeholder')
              "
            />
          </n-form-item>
          <n-form-item class="login-form-item" path="password" :show-label="false">
            <n-input
              v-model:value="userInfo.password"
              type="password"
              class="login-password-input"
              :placeholder="t('login.form.password.placeholder')"
              clearable
              :maxlength="64"
              size="large"
              show-password-on="click"
              @keydown.enter="handleSubmit"
            />
          </n-form-item>
          <div class="login-submit">
            <n-button type="primary" size="large" block :loading="loading" @click="handleSubmit">
              {{ t('login.form.login') }}
            </n-button>
            <div v-if="showDemo" class="mb-[-16px] mt-[16px] flex items-center gap-[16px]">
              <div class="flex items-center">
                <div>{{ t('login.form.username') }}：</div>
                <div>demo</div>
              </div>
              <div class="flex items-center">
                <div>{{ t('login.form.password') }}：</div>
                <div>demo</div>
              </div>
            </div>
          </div>
        </n-form>
      </div>
      <div v-if="showQrCodeTab">
        <tab-qr-code />
      </div>
      <template v-if="hasMoreLoginWay">
        <n-divider orientation="center" dashed class="!m-0 !mb-2">
          <span class="text-[12px] font-normal text-[var(--text-n4)]">{{ t('login.form.modeLoginMethods') }}</span>
        </n-divider>
        <div class="mt-4 flex items-center justify-center">
          <div v-if="isShowQRCode && !showQrCodeTab" class="loginType" @click="switchLoginType('QR_CODE')">
            <CrmIcon type="iconicon_scan" class="text-[#4666e5]" :size="20" />
          </div>
          <div v-if="userInfo.authenticate !== 'LDAP' && isShowLDAP" class="loginType" @click="switchLoginType('LDAP')">
            <span class="type-text text-[10px]">LDAP</span>
          </div>
          <div v-if="userInfo.authenticate !== 'LOCAL'" class="loginType" @click="switchLoginType('LOCAL')">
            <CrmIcon type="iconicon_that_person" class="text-[#4666e5]" :size="20" />
          </div>
          <div v-if="isShowOIDC && userInfo.authenticate !== 'OIDC'" class="loginType" @click="redirectAuth('OIDC')">
            <span class="type-text text-[10px]">OIDC</span>
          </div>
          <div
            v-if="isShowOAUTH && userInfo.authenticate !== 'OAUTH2'"
            class="loginType"
            @click="redirectAuth('OAUTH2')"
          >
            <span class="type-text text-[10px]">OAuth</span>
          </div>
          <div v-if="isShowCAS && userInfo.authenticate !== 'CAS'" class="loginType" @click="redirectAuth('CAS')">
            <span class="type-text text-[10px]">CAS</span>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { FormInst, NButton, NDivider, NForm, NFormItem, NInput, NSpin, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { clearToken, getLoginType, isLoginExpires, setLoginExpires, setLoginType } from '@lib/shared/method/auth';
  import { encrypted } from '@lib/shared/method/index';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import TabQrCode from './tabQrCode.vue';

  import { getThirdConfigByType } from '@/api/modules';
  import { defaultLoginLogo } from '@/config/business';
  import useLoading from '@/hooks/useLoading';
  import useUser from '@/hooks/useUser';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import useUserStore from '@/store/modules/user';

  const { goUserHasPermissionPage } = useUser();
  const { t } = useI18n();
  const appStore = useAppStore();
  const userStore = useUserStore();
  const licenseStore = useLicenseStore();
  const Message = useMessage();

  const formRef = ref<FormInst | null>(null);

  const preheat = ref(true);

  const props = defineProps<{
    isPreview?: boolean;
    slogan?: string;
    logo?: string;
  }>();

  const innerLogo = computed(() => {
    return props.isPreview && props.logo ? props.logo : appStore.pageConfig.loginLogo[0]?.url ?? defaultLoginLogo;
  });

  const innerSlogan = computed(() => {
    return props.isPreview && props.slogan ? props.slogan : appStore.pageConfig.slogan;
  });

  const showDemo = window.location.hostname === 'demo.metersphere.com';

  const { loading, setLoading } = useLoading();

  const userInfo = ref<{
    authenticate: string;
    username: string;
    password: string;
  }>({
    authenticate: getLoginType() || 'LOCAL',
    username: '',
    password: '',
  });

  const showQrCodeTab = ref(false);
  const activeName = ref('');

  function switchLoginType(type: string) {
    userInfo.value.authenticate = type;
    if (type === 'QR_CODE') {
      showQrCodeTab.value = showQrCodeTab.value === false;
    } else {
      showQrCodeTab.value = false;
    }
  }

  const handleSubmit = () => {
    if (loading.value) return;
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        setLoading(true);
        try {
          try {
            await userStore.logout(true);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log('logout error', error);
          }
          await userStore.login({
            username: encrypted(userInfo.value.username) || '',
            password: encrypted(userInfo.value.password) || '',
            authenticate: userInfo.value.authenticate,
            platform: 'WEB',
          });
          await licenseStore.getValidateLicense();
          if (licenseStore.hasLicense()) {
            appStore.initPageConfig();
          }
          setLoginExpires();
          setLoginType(userInfo.value.authenticate);
          Message.success(t('login.form.login.success'));
          goUserHasPermissionPage();
        } catch (err) {
          // eslint-disable-next-line no-console
          console.log(err);
        } finally {
          setLoading(false);
          userStore.getAuthentication();
        }
      }
    });
  };

  const isShowLDAP = computed(() => {
    return userStore.loginType.includes('LDAP');
  });
  const isShowOIDC = computed(() => {
    return userStore.loginType.includes('OIDC');
  });
  const isShowOAUTH = computed(() => {
    return userStore.loginType.includes('OAUTH2');
  });
  const isShowCAS = computed(() => {
    return userStore.loginType.includes('CAS');
  });

  const isShowQRCode = ref(false);
  const hasMoreLoginWay = computed(
    () => isShowLDAP.value || isShowOIDC.value || isShowOAUTH.value || isShowCAS.value || isShowQRCode.value
  );

  const activePlatformType = computed<string>(() => appStore.activePlatformResource.syncResource);

  async function initPlatformInfo() {
    if (!activePlatformType.value) return;
    try {
      const res = await getThirdConfigByType(activePlatformType.value);
      if (getLoginType() && getLoginType() !== 'LOCAL' && getLoginType() !== 'LDAP') {
        showQrCodeTab.value = true;
        activeName.value = getLoginType() || activePlatformType.value;
      }
      isShowQRCode.value = res.config?.startEnable ?? false;
    } catch (error) {
      isShowQRCode.value = false;
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function redirectAuth(authType: string) {
    if (authType === 'LDAP' || authType === 'LOCAL') {
      return;
    }
    const res: any = {};
    if (!res) {
      return;
    }
    if (!res.enable) {
      Message.error(t('login.auth_not_enable'));
    }
  }

  onMounted(async () => {
    if (!props.isPreview) {
      initPlatformInfo();
      appStore.initPublicKey();
      try {
        if (isLoginExpires()) {
          preheat.value = false;
          clearToken();
        } else {
          preheat.value = await userStore.isLogin();
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
        preheat.value = false;
      }
    }
  });
</script>

<style lang="less" scoped>
  .login-form {
    @apply flex flex-col;
  }
  .login-form-loading {
    @apply items-center justify-center;
    height: 100%;
    min-height: 200px;
  }
  .login-form-preview {
    @apply h-screen items-center justify-center;
    transform: translateY(-10%);
  }
  .form-title {
    margin-bottom: 24px;
    font-size: 18px;
    font-weight: 500;
    color: #4666e5;
  }
  .login-form-item {
    @apply block;
  }
  .login-form-item:not(:last-child) {
    margin-bottom: 16px;
  }
  :deep(.n-form-item-feedback-wrapper) {
    min-height: auto;
  }
  .login-submit {
    margin-top: 12px;
    margin-bottom: 28px;
  }
  :deep(.login-submit .n-button) {
    height: 40px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 3px;
  }
  :deep(.login-submit .n-button--primary-type) {
    background-color: #4666e5 !important;
    border-color: #4666e5 !important;
  }
  :deep(.login-submit .n-button--primary-type:hover) {
    background-color: #3a56c4 !important;
    border-color: #3a56c4 !important;
  }
  :deep(.login-submit .n-button--primary-type:active) {
    background-color: #2f46a3 !important;
    border-color: #2f46a3 !important;
  }
  .loginType {
    margin: 0 8px;
    width: 32px;
    height: 32px;
    border: 1px solid var(--text-n8);
    border-radius: 50%;
    @apply flex cursor-pointer items-center justify-center;
  }
  .type-text {
    color: #4666e5;
    @apply font-medium;
  }

  /* Preview mode styles */
  .form-preview {
    @apply relative;
    padding: 40px;
    border-radius: var(--border-radius-large);
    background-color: var(--text-n10);
    box-shadow: 0 8px 10px 0 #3232330d, 0 16px 24px 0 #3232330d, 0 6px 30px 0 #3232330d;
  }
  .title-welcome {
    @apply flex items-center justify-center;
    font-size: 20px;
    color: var(--primary-8);
  }
  .mask {
    @apply absolute left-0 top-0 h-full w-full;
  }
</style>
