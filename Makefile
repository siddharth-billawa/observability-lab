.PHONY: build up down status logs test smoke-test load-normal load-burst load-degraded clean reset
MVN = mvn -Dmaven.repo.local=$${MAVEN_REPO:-/tmp/telemetry-m2}

build:
	$(MVN) -q package -DskipTests
up: build
	docker compose up --build -d

down:
	docker compose down
status:
	docker compose ps
logs:
	docker compose logs -f --tail=100

test:
	$(MVN) test
smoke-test: build
	docker compose up --build -d
	./scripts/smoke-test.sh
load-normal:
	docker compose --profile load run --rm -e BASE_URL=http://order-service:8080 -e VUS=$${VUS:-3} -e DURATION=$${DURATION:-2m} k6 run /scripts/normal-load.js
load-burst:
	docker compose --profile load run --rm -e BASE_URL=http://order-service:8080 -e RAMP_SECONDS=$${RAMP_SECONDS:-30} -e BURST_SECONDS=$${BURST_SECONDS:-30} -e BURST_VUS=$${BURST_VUS:-15} k6 run /scripts/burst-load.js
load-degraded:
	./scripts/load-degraded.sh
clean:
	docker compose down
reset:
	docker compose down -v
