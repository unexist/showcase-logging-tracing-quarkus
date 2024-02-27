FROM fluent/fluentd:v1.14-debian-1

USER root

COPY ./fluent.conf /fluentd/etc/fluent.conf

RUN ["gem", "install", "faraday-net_http", "-v", "3.0.2"]
RUN ["gem", "install", "faraday", "-v", "2.8.1"]
RUN ["gem", "install", "fluent-plugin-elasticsearch"]
RUN ["gem", "install", "fluent-plugin-input-gelf"]

USER fluent
