define JSON_TODO_DIRECT
curl -X 'POST' \
  'http://localhost:8080/todo/direct' \
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
export JSON_TODO_DIRECT

define JSON_TODO_INDIRECT
curl -X 'POST' \
  'http://localhost:8080/todo/indirect' \
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
export JSON_TODO_INDIRECT

# Docker
.PHONY: docker
docker:
	@docker-compose -f docker/docker-compose.yaml \
		-p observability up

# Web
kibana:
	open http://localhost:5601

elastic:
	open http://localhost:9200

jaeger:
	open http://localhost:16686/

# Tools
todo-direct:
	@echo $$JSON_TODO_DIRECT | bash

todo-indirect:
	@echo $$JSON_TODO_INDIRECT | bash

list:
	@curl -X 'GET' 'http://localhost:8080/todo' -H 'accept: */*' | jq .

open:
	@open "http://localhost:8080"

kat-listen:
	kcat -t todo_created -b localhost:9092 -C

kat-test:
	kcat -t todo_created -b localhost:9092 -P