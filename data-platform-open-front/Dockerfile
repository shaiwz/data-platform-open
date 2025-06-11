FROM registry.cn-beijing.aliyuncs.com/daodao-bot/nginx:1.27.4
VOLUME /tmp
COPY dist /usr/share/nginx/html
EXPOSE 80
ENTRYPOINT ["nginx", "-g", "daemon off;"]

