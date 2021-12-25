define JSON_TODO
curl -X 'POST' \
  'http://localhost:8080/todo' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "description": "string",
  "done": true,
  "dueDate": {
    "due": "2021-05-07",
    "start": "2021-05-07"
  },
  "title": "string"
}'
endef
export JSON_TODO

# timestamp: $(date +'%s.%N')
define GELF_TEST
echo \
{
  "version": "1.0",
  "host": "localhost",
  "short_message": "Short message",
  "full_message": "Full message",
  "timestamp": "1640456345.940518000",
  "level": 1,
  "facility": "Tester",
  "_user_id": 42,
  "_Environment": "test",
  "_AppName": "Tester"
} | gzip -c -f - | nc -w 1 -u localhost 12201
endef
export GELF_TEST

# Docker
docker-compose:
	@docker-compose -f docker/docker-compose.yaml \
		-p observability up

# Podman
PODNAME_REST := logtrace
PODNAME_OTEL := otel

podman-compose:
	@podman-compose -f docker/docker-compose.yaml -p observability up

podman-machine-create:
	@podman machine init --memory=8192 --cpus=2

podman-machine-rm:
	@podman machine rm

podman-machine-create: podman-machine-rm podman-machine-create

podman-pod-create:
	@podman pod create -n $(PODNAME_OTEL) --network bridge \
		-p 13133:13133 -p 4317:4317 -p 55680:55680

	@podman pod create -n $(PODNAME_REST) --network bridge \
		-p 6831:6831/udp -p 16686:16686 -p 14268:14268 -p 14250:14250 \
		-p 9200:9200 -p 9300:9300 \
		-p 12201:12201/udp \
		-p 5601:5601 \
		-p 9092:9092

podman-pod-rm:
	@podman pod rm -f $(PODNAME_OTEL)
	@podman pod rm -f $(PODNAME_REST)

podman-pod-recreate: podman-pod-rm podman-pod-create

podman-build-collector:
	@podman build --format docker -t custom-collector -f podman/collector/Dockerfile

podman-build-fluentd:
	@podman build --format docker -t custom-fluentd -f podman/fluentd/Dockerfile

podman-build: podman-build-collector podman-build-fluentd

podman-jaeger:
	# Install Jaeger
	#jaeger:
	#  image: jaegertracing/all-in-one:latest
	#  ports:
	#    - "6831:6831/udp"
	#    - "16686:16686"
	#    - "14268"
	#    - "14250"

	@podman run -dit --name jaeger --pod=$(PODNAME_REST) jaegertracing/all-in-one:latest

podman-collector:
	# Install Collector
	#collector:
	#  image: otel/opentelemetry-collector:latest
	#  command: ["--config=/etc/otel-collector-config.yaml"]
	#  volumes:
	#    - ./otel-collector-config.yaml:/etc/otel-collector-config.yaml
	#  ports:
	#    - "13133:13133" # Health_check extension
	#    - "4317:4317"   # OTLP gRPC receiver
	#    - "55680:55680" # OTLP gRPC receiver alternative port
	#  depends_on:
	#    - jaeger

	@podman run -dit --name collector --pod=$(PODNAME_OTEL) custom-collector

podman-elastic:
	# Install Elastic
	#elasticsearch:
	#  image: docker.elastic.co/elasticsearch/elasticsearch-oss:6.8.2
	#  ports:
	#    - "9200:9200"
	#    - "9300:9300"
	#  environment:
	#    ES_JAVA_OPTS: "-Xms512m -Xmx512m"

	@podman run -dit --name elasticsearch --pod=$(PODNAME_REST) -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
		-e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.14.2

podman-fluentd:
	# Install Fluentd
	#fluentd:
	#build: .
	#ports:
	#  - "12201:12201/udp"
	#volumes:
	#  - source: ./fluentd
	#    target: /fluentd/etc
	#    type: bind
	#depends_on:
	#  - elasticsearch

	@podman run -dit --name fluentd --pod=$(PODNAME_REST) custom-fluentd

podman-kibana:
	# Install Kibana
	#kibana:
	#  image: docker.elastic.co/kibana/kibana-oss:6.8.2
	#  ports:
	#    - "5601:5601"
	#  depends_on:
	#    - elasticsearch

	@podman run -dit --name kibana --pod=$(PODNAME_REST) -e "ELASTICSEARCH_HOSTS=http://localhost:9200" \
		docker.elastic.co/kibana/kibana:7.14.2

podman-redpanda:
	# Install Redpanda
	#redpanda:
	#  container_name: redpanda
	#  image: vectorized/redpanda
	#  hostname: redpanda
	#  ports:
	#    - "9092:9092"

	@podman run -dit --name redpanda --pod=$(PODNAME_REST) vectorized/redpanda

podman-services: podman-elastic podman-jaeger podman-collector podman-kibana podman-fluentd podman-redpanda

# Web
open-kibana:
	open http://localhost:5601

open-elastic:
	open http://localhost:9200

open-jaeger:
	open http://localhost:16686/

# Quarkus
service-create:
	mvn -f todo-service-create/pom.xml quarkus:dev

service-check:
	mvn -f todo-service-check/pom.xml quarkus:dev

# Tools
todo:
	@echo $$JSON_TODO | bash

list:
	@curl -X 'GET' 'http://localhost:8080/todo' -H 'accept: */*' | jq .

gelf:
	@echo $$GELF_TEST | bash

# Kafka
kat-listen:
	kcat -t todo_created -b localhost:9092 -C

kat-test:
	kcat -t todo_created -b localhost:9092 -P