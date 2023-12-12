FROM maven:latest as builder

COPY . /workspace

WORKDIR /workspace

RUN mvn clean package



FROM alpine:latest

ENV UID=1000
ENV GID=1000

RUN apk update && apk add openjdk17 shadow sudo

COPY --from=builder /workspace/target/Graphite-*.jar /graphite/Graphite.jar
COPY --from=mrletsplay/docker_launcher /usr/local/bin/docker_launcher /usr/local/bin/docker_launcher
COPY ./docker/launcher_config.json /graphite/launcher_config.json

RUN useradd graphite

RUN mkdir /graphite/data && chown -R graphite:graphite /graphite/data

VOLUME ["/graphite/data"]

WORKDIR /graphite/data

EXPOSE 8880

ENTRYPOINT [ "docker_launcher", "--config", "/graphite/launcher_config.json", "sudo", "-u", "graphite", "--" ]
CMD [ "java", "-jar", "/graphite/Graphite.jar" ]
