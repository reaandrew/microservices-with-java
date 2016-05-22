FROM reaandrew/java8-alpine

ADD ./build/libs/claim-communication-all.jar /opt/claim/claim-communication-all.jar
ADD ./config/production.yml /var/claim-communication/production.yml

EXPOSE 8087
CMD ["java","-jar","/opt/claim/claim-communication-all.jar","-c","/var/claim-communication/production.yml"]

