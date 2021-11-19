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
podman-start:
	@podman machine start

podman-compose:
	@podman-compose -f docker/docker-compose.yaml \
		-p observability up

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