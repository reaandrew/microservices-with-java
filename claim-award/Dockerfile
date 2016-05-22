FROM reaandrew/java8-alpine

ADD ./build/libs/claim-award-all.jar /opt/claim/claim-award-all.jar
ADD ./config/production.yml /var/claim-award/production.yml

EXPOSE 8085
CMD ["java","-jar","/opt/claim/claim-award-all.jar","-c","/var/claim-award/production.yml"]

