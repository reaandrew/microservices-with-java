base-docker:
	#FROM: https://github.com/anapsix/docker-alpine-java
	docker build -t reaandrew/java8-alpine scripts/docker/reaandrew/java8-alpine

run: base-docker
	./gradlew shadowJar
	docker-compose build
	docker-compose up

.PHONY: base-docker 
