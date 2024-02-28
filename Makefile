define JSON_TODO_OK
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
export JSON_TODO_OK

define JSON_TODO_ERR
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
  "title": "badwordbadwordbadword"
}'
endef
export JSON_TODO_ERR

# Web
open-kibana:
	open http://localhost:5601

open-elastic:
	open http://localhost:9200

open-jaeger:
	open http://localhost:16686

# Quarkus
service-create:
	mvn -f todo-service-create/pom.xml quarkus:dev

service-verify:
	mvn -f todo-service-verify/pom.xml quarkus:dev

service-store:
	mvn -f todo-service-store/pom.xml quarkus:dev

services: service-create service-verify service-store

# Tools
rest-create-ok:
	@echo $$JSON_TODO_OK | bash

rest-create-err:
	@echo $$JSON_TODO_ERR | bash

rest-list:
	@curl -X 'GET' 'http://localhost:8090/todo' -H 'accept: */*' | jq .

rest-status:
	@curl -X 'GET' 'http://localhost:8080/todo/$(id)' -H 'accept: */*' | jq .
	@curl -X 'GET' 'http://localhost:8085/todo/$(id)' -H 'accept: */*' | jq .
	@curl -X 'GET' 'http://localhost:8090/todo/$(id)' -H 'accept: */*' | jq .

# Kafka
kat-listen:
	kcat -t todo_created -b localhost:9092 -C

kat-test:
	kcat -t todo_created -b localhost:9092 -P

kt-listen:
	kt consume -topic todo_created