FROM reaandrew/java8-alpine

ADD ./build/libs/claim-award-all.jar /opt/claim/claim-award-all.jar
ADD ./config/development.yml /var/claim-award/development.yml

EXPOSE 8085
CMD ["java","-jar","/opt/claim/claim-award-all.jar","-c","/var/claim-award/development.yml"]

