FROM registry.cn-beijing.aliyuncs.com/daodao-bot/openjdk:21
VOLUME /tmp
EXPOSE 8080
COPY ./target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

