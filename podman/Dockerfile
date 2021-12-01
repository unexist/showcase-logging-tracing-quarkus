FROM fluent/fluentd:v1.14-debian-1

USER root

COPY ./fluentd/fluent.conf /fluentd/etc/fluent.conf

RUN ["gem", "install", "fluent-plugin-elasticsearch"]
RUN ["gem", "install", "fluent-plugin-input-gelf"]

USER fluent
