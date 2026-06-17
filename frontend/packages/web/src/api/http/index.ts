import useDiscreteApi from '@/hooks/useDiscreteApi';
import useAppStore from '@/store/modules/app';

import checkStatus from './checkStatus';
import createAxios from './createAxios';

const { message } = useDiscreteApi();

const CDR = createAxios({
  useAppStore,
  showErrorMsg: message.error,
  checkStatus,
});

export default CDR;
