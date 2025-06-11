FROM registry.cn-beijing.aliyuncs.com/daodao-bot/maven:latest AS builder
WORKDIR /build
COPY . .
RUN mvn clean install -Dmaven.test.skip=true -U -e -X -B -N
RUN mvn clean install -Dmaven.test.skip=true -U -e -X -B -f data-platform-open-common/pom.xml
RUN mvn clean package -Dmaven.test.skip=true -U -e -X -B -f data-platform-open-flow/pom.xml

FROM registry.cn-beijing.aliyuncs.com/daodao-bot/openjdk:21
COPY --from=builder /build/data-platform-open-flow/target/*.jar app.jar
VOLUME /tmp
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
