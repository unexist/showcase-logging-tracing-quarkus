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
		-p logtrace up

# Gelf
gelf-udp:
	@echo $$GELF_TEST_UDP | bash

gelf-tcp:
	@echo $$GELF_TEST_TCP | bash