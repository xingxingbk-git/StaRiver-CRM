<template>
  <CrmCard auto-height no-content-padding hide-footer :loading="emailLoading">
    <section class="business-mail-card">
      <div class="business-mail-card__actions">
        <n-button :loading="linkLoading" type="default" @click="testLink(false)">
          {{ t('common.testLink') }}
        </n-button>
        <n-button v-permission="['SYSTEM_SETTING:UPDATE']" type="default" @click="showModal = true">
          {{ t('common.edit') }}
        </n-button>
      </div>
      <div class="business-mail-card__title">
        <span>{{ displayValue(originForm.host, 'smtp.example.com') }}</span>
        <em>{{ displayValue(originForm.port, '465') }}</em>
      </div>
      <dl class="business-mail-card__list">
        <div>
          <dt>{{ t('system.business.mailSettings.smtpAccount') }}：</dt>
          <dd>{{ displayValue(originForm.account, 'notification@example.com') }}</dd>
        </div>
        <div>
          <dt>{{ t('system.business.mailSettings.smtpPassword') }}：</dt>
          <dd>{{ desensitize(originForm.password) || '********' }}</dd>
        </div>
        <div>
          <dt>{{ t('system.business.mailSettings.from') }}：</dt>
          <dd>{{ displayValue(originForm.from, 'noreply@company.com') }}</dd>
        </div>
        <div>
          <dt>{{ t('system.business.mailSettings.recipient') }}：</dt>
          <dd class="business-mail-card__link">{{ displayValue(originForm.recipient, 'test@example.com') }}</dd>
        </div>
      </dl>
      <div class="business-mail-card__switches">
        <span>{{ t('system.business.mailSettings.ssl') }}</span>
        <n-switch :value="originForm.ssl" disabled :rubber-band="false" />
        <span>{{ t('system.business.mailSettings.tsl') }}</span>
        <n-switch :value="originForm.tsl" disabled :rubber-band="false" />
      </div>
    </section>
  </CrmCard>

  <CrmModal
    v-model:show="showModal"
    :width="680"
    :title="t('system.business.mailSettings.updateEmailSettings')"
    :positive-text="t('common.update')"
    :ok-loading="emailDrawerLoading"
    @confirm="confirm"
    @cancel="cancel"
  >
    <n-form ref="formRef" :rules="rules" label-placement="top" :model="form" require-mark-placement="right-hanging">
      <n-form-item :label="t('system.business.mailSettings.smtpHost')" path="host">
        <n-input v-model:value="form.host" :maxlength="255" :placeholder="t('common.pleaseInput')" clearable />
      </n-form-item>
      <n-form-item :label="t('system.business.mailSettings.smtpPort')" path="port">
        <n-input v-model:value="form.port" :maxlength="255" :placeholder="t('common.pleaseInput')" clearable />
      </n-form-item>
      <n-form-item :label="t('system.business.mailSettings.smtpAccount')" path="account">
        <n-input v-model:value="form.account" :maxlength="255" :placeholder="t('common.pleaseInput')" clearable />
      </n-form-item>
      <n-form-item :label="t('system.business.mailSettings.smtpPassword')" path="password">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          :input-props="{ autocomplete: 'new-password' }"
          :maxlength="255"
          :placeholder="t('common.pleaseInput')"
          clearable
        />
      </n-form-item>
      <n-form-item :label="t('system.business.mailSettings.from')" path="from">
        <n-input v-model:value="form.from" :maxlength="255" :placeholder="t('common.pleaseInput')" clearable />
      </n-form-item>
      <n-form-item :label="t('system.business.mailSettings.recipient')" path="recipient">
        <n-input v-model:value="form.recipient" :maxlength="255" :placeholder="t('common.pleaseInput')" clearable />
      </n-form-item>
      <n-form-item label=" " path="ssl">
        <div class="business-mail-modal__switches">
          <n-switch v-model:value="form.ssl" :rubber-band="false" />
          <span>{{ t('system.business.mailSettings.ssl') }}</span>
          <n-switch v-model:value="form.tsl" :rubber-band="false" />
          <span>{{ t('system.business.mailSettings.tsl') }}</span>
        </div>
      </n-form-item>
    </n-form>
    <template #footer>
      <div class="business-mail-modal__footer">
        <n-button secondary :disabled="emailDrawerLoading" @click="cancel">
          {{ t('common.cancel') }}
        </n-button>
        <n-button type="primary" ghost :loading="linkLoading" @click="testLink(true)">
          {{ t('common.testLink') }}
        </n-button>
        <n-button type="primary" :loading="emailDrawerLoading" @click="confirm">
          {{ t('common.update') }}
        </n-button>
      </div>
    </template>
  </CrmModal>
</template>

<script setup lang="ts">
  import { FormInst, FormItemRule, NButton, NForm, NFormItem, NInput, NSwitch, useMessage } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { desensitize } from '@lib/shared/method';
  import { validateEmail } from '@lib/shared/method/validate';

  import CrmCard from '@/components/pure/crm-card/index.vue';
  import CrmModal from '@/components/pure/crm-modal/index.vue';

  import { getConfigEmail, testConfigEmail, updateConfigEmail } from '@/api/modules';

  const { t } = useI18n();
  const Message = useMessage();

  const showModal = ref(false);
  const formRef = ref<FormInst | null>(null);
  const originForm = ref({
    host: '',
    port: '',
    account: '',
    password: '',
    from: '',
    recipient: '',
    ssl: false,
    tsl: false,
  });
  const form = ref({ ...originForm.value });

  const emailRule = {
    validator: (rule: FormItemRule, value: string) => {
      if (value && !validateEmail(value)) {
        return new Error(t('common.emailErrTip'));
      }
      return true;
    },
    trigger: ['blur', 'input'],
  };

  const rules = ref({
    host: [
      {
        required: true,
        message: t('common.notNull', { value: t('system.business.mailSettings.smtpHost') }),
        trigger: ['blur'],
      },
    ],
    port: [
      {
        required: true,
        message: t('common.notNull', { value: t('system.business.mailSettings.smtpPort') }),
        trigger: ['blur'],
      },
    ],
    account: [
      {
        required: true,
        message: t('common.notNull', { value: t('system.business.mailSettings.smtpAccount') }),
        trigger: ['blur'],
      },
    ],
    from: [emailRule],
    recipient: [emailRule],
  });

  const emailLoading = ref(false);
  async function initEmailInfo() {
    try {
      emailLoading.value = true;
      const res = await getConfigEmail();
      originForm.value = { ...res, ssl: res.ssl === 'true', tsl: res.tsl === 'true' };
      form.value = { ...res, ssl: res.ssl === 'true', tsl: res.tsl === 'true' };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      emailLoading.value = false;
    }
  }

  const emailDrawerLoading = ref(false);
  async function confirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          emailDrawerLoading.value = true;
          await updateConfigEmail({ ...form.value, ssl: String(form.value.ssl), tsl: String(form.value.tsl) });
          Message.success(t('common.updateSuccess'));
          showModal.value = false;
          initEmailInfo();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          emailDrawerLoading.value = false;
        }
      }
    });
  }

  function cancel() {
    form.value = { ...originForm.value };
    showModal.value = false;
  }

  const linkLoading = ref(false);
  async function testLink(isDrawer: boolean) {
    try {
      linkLoading.value = true;
      if (isDrawer) {
        await formRef.value?.validate();
      }
      const params = {
        ...(isDrawer ? form.value : originForm.value),
        ssl: String(isDrawer ? form.value.ssl : originForm.value.ssl),
        tsl: String(isDrawer ? form.value.tsl : originForm.value.tsl),
      };
      await testConfigEmail(params);
      Message.success(t('common.connectionSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      linkLoading.value = false;
    }
  }

  onBeforeMount(() => {
    initEmailInfo();
  });

  function displayValue(value: string | number | boolean | undefined, fallback: string) {
    if (value === undefined || value === null || value === '') return fallback;
    return String(value);
  }
</script>

<style lang="less" scoped>
  .business-mail-card {
    position: relative;
    padding: 36px 40px;
    min-height: 260px;
    border: 1px solid #dbe5f1;
    border-radius: 8px;
    background: #f8fafc;
  }
  .business-mail-card__actions {
    position: absolute;
    top: 36px;
    right: 40px;
    display: flex;
    gap: 8px;
  }
  .business-mail-card__title {
    display: flex;
    align-items: center;
    margin-bottom: 28px;
    font-size: 22px;
    font-weight: 800;
    color: #0f172a;
    line-height: 30px;
    gap: 10px;
  }
  .business-mail-card__title em {
    padding: 2px 8px;
    font-size: 12px;
    font-style: normal;
    font-weight: 700;
    border-radius: 6px;
    color: #64748b;
    background: #e8edf5;
    line-height: 18px;
  }
  .business-mail-card__list {
    display: flex;
    margin: 0;
    flex-direction: column;
    gap: 18px;
  }
  .business-mail-card__list div {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  .business-mail-card__list dt {
    width: 96px;
    font-size: 15px;
    font-weight: 800;
    text-align: right;
    color: #64748b;
  }
  .business-mail-card__list dd {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #64748b;
  }
  .business-mail-card__link {
    text-decoration: underline;
  }
  .business-mail-card__switches,
  .business-mail-modal__switches {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .business-mail-card__switches {
    margin-top: 22px;
    padding-left: 6px;
  }
  .business-mail-card__switches span,
  .business-mail-modal__switches span {
    font-size: 15px;
    font-weight: 800;
    color: #64748b;
  }
  .business-mail-card__switches span + :deep(.n-switch) {
    margin-left: 12px;
  }
  .business-mail-modal__footer {
    display: flex;
    justify-content: flex-end;
    width: 100%;
    gap: 12px;
  }
</style>
