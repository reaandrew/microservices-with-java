FROM reaandrew/java8-alpine

ADD ./build/libs/claim-fraud-all.jar /opt/claim/claim-fraud-all.jar
ADD ./config/production.yml /var/claim-fraud/production.yml

EXPOSE 8087
CMD ["java","-jar","/opt/claim/claim-fraud-all.jar","-c","/var/claim-fraud/production.yml"]

