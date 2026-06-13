import baseConfig from './vite.config.base';
import { config } from 'dotenv';
import { mergeConfig } from 'vite';
import eslint from 'vite-plugin-eslint';
/** 🚨 REMOVE_BEFORE_PRODUCTION: 无后端 mock 插件，上线前必须删除下面这行和 plugins 中的 mockPlugin() */
import { mockPlugin } from './plugin/mock';

// 注入本地/开发配置环境变量(先导入的配置优先级高)
config({ path: ['.env.development.local', '.env.development'] });

export default mergeConfig(
  {
    mode: 'development',
    server: {
      open: true,
      fs: {
        strict: true,
      },
      proxy: {
        '/sse': {
          target: process.env.VITE_DEV_DOMAIN,
          changeOrigin: true,
        },
        '/front': {
          target: process.env.VITE_DEV_DOMAIN,
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front/, ''),
        },
        '/pic': {
          target: process.env.VITE_DEV_DOMAIN,
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/pic/, ''),
        },
        '/attachment': {
          target: process.env.VITE_DEV_DOMAIN,
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/attachment/, ''),
        },
        '/ui': {
          target: process.env.VITE_DEV_DOMAIN,
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/ui/, ''),
        },
      },
    },
    plugins: [
      mockPlugin(), // 🚨 REMOVE_BEFORE_PRODUCTION: 上线前删除这行
      eslint({
        overrideConfigFile: 'eslint.config.cjs',
        cache: false,
        include: ['src/**/*.ts', 'src/**/*.tsx', 'src/**/*.vue'],
        exclude: ['node_modules'],
      }),
    ],
  },
  baseConfig
);
