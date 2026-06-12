import { SalesRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const salesRoutes: AppRouteRecordRaw[] = [
  {
    path: '/sales/team',
    name: SalesRouteEnum.SALES_TEAM_ROOT,
    component: DEFAULT_LAYOUT,
    redirect: '/sales/team/index',
    meta: {
      locale: 'module.salesTeam',
      permissions: ['CUSTOMER_MANAGEMENT:READ', 'CLUE_MANAGEMENT:READ', 'OPPORTUNITY_MANAGEMENT:READ'],
      icon: 'iconicon_team',
      hideChildrenInMenu: true,
      collapsedLocale: 'module.salesTeam',
    },
    children: [
      {
        path: 'index',
        name: SalesRouteEnum.SALES_TEAM,
        component: () => import('@/views/sales/teamPerformance.vue'),
        meta: {
          locale: 'module.salesTeam',
          permissions: ['CUSTOMER_MANAGEMENT:READ', 'CLUE_MANAGEMENT:READ', 'OPPORTUNITY_MANAGEMENT:READ'],
          isTopMenu: true,
        },
      },
    ],
  },
  {
    path: '/sales/analytics',
    name: SalesRouteEnum.SALES_ANALYTICS_ROOT,
    component: DEFAULT_LAYOUT,
    redirect: '/sales/analytics/index',
    meta: {
      locale: 'module.salesAnalytics',
      permissions: ['DASHBOARD:READ'],
      icon: 'iconicon_dashboard1',
      hideChildrenInMenu: true,
      collapsedLocale: 'module.salesAnalytics',
    },
    children: [
      {
        path: 'index',
        name: SalesRouteEnum.SALES_ANALYTICS,
        component: () => import('@/views/sales/dataAnalysis.vue'),
        meta: {
          locale: 'module.salesAnalytics',
          permissions: ['DASHBOARD:READ'],
          isTopMenu: true,
        },
      },
    ],
  },
];

export default salesRoutes;
