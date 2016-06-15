compile:
	./gradlew clean
	./gradlew build

dist: compile
	./gradlew shadowJar

quickDist:
	./gradlew compileJava
	./gradlew shadowJar

    	 
base-docker:
	#FROM: https://github.com/anapsix/docker-alpine-java
	docker build -t reaandrew/java8-alpine scripts/docker/reaandrew/java8-alpine

run: dist base-docker
	docker-compose build
	docker-compose up -d

#line-cout:
	#echo "LOC" && echo "-----" && for i in `ls ./ | grep claim-`; do echo "${i} $(cd ${i} && find ./ -type f | grep java$ | xargs cat | wc -l)"; done;

.PHONY: compile dist run base-docker 
