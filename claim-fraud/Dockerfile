FROM reaandrew/java8-alpine

ADD ./build/libs/claim-fraud-all.jar /opt/claim/claim-fraud-all.jar
ADD ./config/development.yml /var/claim-fraud/development.yml

EXPOSE 8087
CMD ["java","-jar","/opt/claim/claim-fraud-all.jar","-c","/var/claim-fraud/development.yml"]

