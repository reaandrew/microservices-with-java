base-docker:
	#FROM: https://github.com/anapsix/docker-alpine-java
	docker build -t reaandrew/java8-alpine scripts/docker/reaandrew/java8-alpine

run: base-docker
#	./gradlew test
	./gradlew shadowJar
	docker-compose build
	docker-compose up -d

#line-cout:
	#echo "LOC" && echo "-----" && for i in `ls ./ | grep claim-`; do echo "${i} $(cd ${i} && find ./ -type f | grep java$ | xargs cat | wc -l)"; done;

.PHONY: base-docker 
