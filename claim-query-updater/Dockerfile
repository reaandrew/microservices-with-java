FROM reaandrew/java8-alpine

ADD ./build/libs/claim-query-updater-all.jar /opt/claim/claim-query-updater-all.jar
ADD ./config/development.yml /var/claim-query-updater/development.yml

EXPOSE 8082
CMD ["java","-jar","/opt/claim/claim-query-updater-all.jar","-c","/var/claim-query-updater/development.yml"]

