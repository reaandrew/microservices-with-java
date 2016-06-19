FROM reaandrew/java8-alpine

ADD ./build/libs/claim-portal-all.jar /opt/claim/claim-portal-all.jar
ADD ./config/development.yml /var/claim-portal/development.yml

EXPOSE 8080
CMD ["java","-jar","/opt/claim/claim-portal-all.jar","-c","/var/claim-portal/development.yml"]

