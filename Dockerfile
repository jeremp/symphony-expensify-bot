FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/expensify-bot*.jar expensify-bot.jar
CMD java ${JAVA_OPTS} -jar expensify-bot.jar