const express = require('express');
const path = require('path');
const { createProxyMiddleware } = require('http-proxy-middleware');
const dotenv = require('dotenv'); // 导入dotenv

// 加载环境变量
dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

// 从环境变量获取代理配置
const webApiProxyTarget = process.env.WEB_API_PROXY_TARGET || 'http://localhost:8080/dp-web/';
const webApiProxyPrefix = process.env.WEB_API_PROXY_PREFIX || '/dp-web';
const webApiProxyPathRewrite = process.env.WEB_API_PROXY_PATH_REWRITE || '^/dp-web';

const queryApiProxyTarget = process.env.QUERY_API_PROXY_TARGET || 'http://localhost:8080/dp-query/';
const queryApiProxyPrefix = process.env.QUERY_API_PROXY_PREFIX || '/dp-query';
const queryApiProxyPathRewrite = process.env.QUERY_API_PROXY_PATH_REWRITE || '^/dp-query';

// 配置API请求代理
app.use(
  webApiProxyPrefix, // 使用环境变量中的前缀
  createProxyMiddleware({
    target: webApiProxyTarget, // 使用环境变量中的目标地址
    changeOrigin: true,
    pathRewrite: {
      [webApiProxyPathRewrite]: '', // 使用环境变量中的路径重写规则
    },
  })
);
app.use(
  queryApiProxyPrefix, // 使用环境变量中的前缀
  createProxyMiddleware({
    target: queryApiProxyTarget, // 使用环境变量中的目标地址
    changeOrigin: true,
    pathRewrite: {
      [queryApiProxyPathRewrite]: '', // 使用环境变量中的路径重写规则
    },
  })
);

// 配置静态文件服务
app.use(express.static(path.join(__dirname, 'dist')));

// 解析JSON请求体
app.use(express.json());

// 解析URL编码的请求体
app.use(express.urlencoded({ extended: true }));

// 处理所有路由请求，返回 index.html（适用于单页应用）
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

// 错误处理中间件
app.use((err, req, res, next) => {
  console.error('服务器错误:', err);
  res.status(500).send('服务器内部错误');
});

// 启动服务器
app.listen(port, () => {
  console.log(`服务器运行在 http://localhost:${port}`);
  console.log(`WEB API请求将代理到 ${webApiProxyTarget}`);
  console.log(`QUERY API请求将代理到 ${queryApiProxyTarget}`);
});
