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
define GELF_TEST_UDP
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
export GELF_TEST_UDP

define GELF_TEST_TCP
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
} | gzip -c -f - | nc -w 1 localhost 12201
endef
export GELF_TEST_TCP

# Docker
docker-compose:
	@docker-compose -f docker/docker-compose.yaml \
		-p observability up

# Podman
PODNAME := logtrace

pd-compose:
	@podman-compose -f docker/docker-compose.yaml -p observability up

pd-machine-create:
	@podman machine init --memory=8192 --cpus=2 --disk-size=20

pd-machine-rm:
	@podman machine rm

pd-machine-create: pd-machine-rm pd-machine-create

pd-pod-create:
	@podman pod create -n $(PODNAME) --network bridge \
		-p 13133:13133 -p 4317:4317 -p 55680:55680 \
		-p 6831:6831/udp -p 16686:16686 -p 14268:14268 -p 14250:14250 \
		-p 9200:9200 -p 9300:9300 \
		-p 12201:12201 \
		-p 5601:5601 \
		-p 9092:9092

pd-pod-rm:
	@podman pod rm -f $(PODNAME)

pd-pod-recreate: pd-pod-rm pd-pod-create

pd-build-collector:
	@podman build --format docker -t custom-collector -f podman/collector/Dockerfile

pd-build-fluentd:
	@podman build --format docker -t custom-fluentd -f podman/fluentd/Dockerfile

pd-build: pd-build-collector pd-build-fluentd

pd-collector:
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

	@podman run -dit --name collector --pod=$(PODNAME) custom-collector

pd-jaeger:
	# Install Jaeger
	#jaeger:
	#  image: jaegertracing/all-in-one:latest
	#  ports:
	#    - "6831:6831/udp"
	#    - "16686:16686"
	#    - "14268"
	#    - "14250"

	@podman run -dit --name jaeger --pod=$(PODNAME) jaegertracing/all-in-one:latest

pd-elastic:
	# Install Elastic
	#elasticsearch:
	#  image: docker.elastic.co/elasticsearch/elasticsearch-oss:6.8.2
	#  ports:
	#    - "9200:9200"
	#    - "9300:9300"
	#  environment:
	#    ES_JAVA_OPTS: "-Xms512m -Xmx512m"

	@podman run -dit --name elasticsearch --pod=$(PODNAME) -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
		-e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.16.0

pd-fluentd:
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

	@podman run -dit --name fluentd --pod=$(PODNAME) custom-fluentd

pd-kibana:
	# Install Kibana
	#kibana:
	#  image: docker.elastic.co/kibana/kibana-oss:6.8.2
	#  ports:
	#    - "5601:5601"
	#  depends_on:
	#    - elasticsearch

	@podman run -dit --name kibana --pod=$(PODNAME) -e "ELASTICSEARCH_HOSTS=http://localhost:9200" \
		docker.elastic.co/kibana/kibana:7.16.0

pd-redpanda:
	# Install Redpanda
	#redpanda:
	#  container_name: redpanda
	#  image: vectorized/redpanda
	#  hostname: redpanda
	#  ports:
	#    - "9092:9092"

	@podman run -dit --name redpanda --pod=$(PODNAME) vectorized/redpanda

pd-services: pd-elastic pd-collector pd-jaeger pd-kibana pd-fluentd pd-redpanda

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

services: service-create service-check

# Tools
rest-create:
	@echo $$JSON_TODO | bash

rest-list:
	@curl -X 'GET' 'http://localhost:8090/todo' -H 'accept: */*' | jq .

rest-status:
	@curl -X 'GET' 'http://localhost:8080/todo/$(id)' -H 'accept: */*' | jq .
	@curl -X 'GET' 'http://localhost:8085/todo/$(id)' -H 'accept: */*' | jq .
	@curl -X 'GET' 'http://localhost:8090/todo/$(id)' -H 'accept: */*' | jq .

gelf-udp:
	@echo $$GELF_TEST_UDP | bash

gelf-tcp:
	@echo $$GELF_TEST_TCP | bash

# Kafka
kat-listen:
	kcat -t todo_created -b localhost:9092 -C

kat-test:
	kcat -t todo_created -b localhost:9092 -P
