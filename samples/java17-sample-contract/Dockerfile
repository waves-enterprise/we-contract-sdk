FROM openjdk:17-alpine
MAINTAINER Waves Enterprise <>

ENV JAVA_MEM="-Xmx256M"
# Note additional java options for io.grpc to work ok with jdk11+ (maybe should be fixed by upgrading io.grpc)
ENV JAVA_OPTS="--add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true "

ADD build/libs/*-all.jar app.jar

RUN chmod +x app.jar
RUN eval $SET_ENV_CMD
CMD ["/bin/sh", "-c", "eval ${SET_ENV_CMD} ; java $JAVA_MEM $JAVA_OPTS -jar app.jar"]
