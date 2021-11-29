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

# Docker
docker-compose:
	@docker-compose -f docker/docker-compose.yaml \
		-p observability up

# Podman
PODNAME := observ

podman-machine-create:
	@podman machine init --memory=8192 --cpus=2

podman-machine-rm:
	@podman machine rm

podman-machine-create: podman-machine-rm podman-machine-create

podman-pod-create:
	@podman pod create -n $(PODNAME) --network bridge -p 6831:6831/udp -p 16686:16686 \
		-p 9200:9200 -p 9300:9300 -p 12201:12201/udp -p 5601:5601 -p 9092:9092

podman-pod-rm:
	@podman pod rm -f $(PODNAME)

podman-pod-recreate: podman-pod-rm podman-pod-create

podman-compose:
	@podman-compose -f docker/docker-compose.yaml -p observability up

podman-jaeger:
	# Install jaeger
	#jaeger:
	#  image: jaegertracing/all-in-one:latest
	#  ports:
	#    - "6831:6831/udp"
	#    - "16686:16686"

	@podman run -dit --name jaeger --pod=$(PODNAME) jaegertracing/all-in-one:latest

podman-elastic:
	# Install elastic
	#elasticsearch:
	#  image: docker.elastic.co/elasticsearch/elasticsearch-oss:6.8.2
	#  ports:
	#    - "9200:9200"
	#    - "9300:9300"
	#  environment:
	#    ES_JAVA_OPTS: "-Xms512m -Xmx512m"

	@podman run -dit --name elasticsearch --pod=$(PODNAME) -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
		-e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2

podman-fluent-build:
	@podman build --format docker -t fluent .

podman-fluent:
	# Install fluentd
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

	@podman run -dit --name fluent --pod=$(PODNAME) -v docker/fluent:/fluentd/etc:Z fluent

podman-kibana:
	# Kibana
	#kibana:
	#  image: docker.elastic.co/kibana/kibana-oss:6.8.2
	#  ports:
	#    - "5601:5601"
	#  depends_on:
	#    - elasticsearch

	@podman run -dit --name kibana --pod=$(PODNAME) -e "ELASTICSEARCH_HOSTS=http://localhost:9200" \
		docker.elastic.co/kibana/kibana-oss:7.10.2

podman-redpanda:
	# Install redpanda
	#redpanda:
	#  container_name: redpanda
	#  image: vectorized/redpanda
	#  hostname: redpanda
	#  ports:
	#    - "9092:9092"

	@podman run -dit --name redpanda --pod=$(PODNAME) vectorized/redpanda

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

# Kafka
kat-listen:
	kcat -t todo_created -b localhost:9092 -C

kat-test:
	kcat -t todo_created -b localhost:9092 -P