FROM fluent/fluentd:v1.14-debian-1

USER root

RUN ["gem", "install", "fluent-plugin-elasticsearch"]
RUN ["gem", "install", "fluent-plugin-input-gelf"]

USER fluent