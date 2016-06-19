FROM reaandrew/java8-alpine

ADD ./build/libs/claim-query-all.jar /opt/claim/claim-query-all.jar
ADD ./config/development.yml /var/claim-query/development.yml

EXPOSE 8080
CMD ["java","-jar","/opt/claim/claim-query-all.jar","-c","/var/claim-query/development.yml"]

