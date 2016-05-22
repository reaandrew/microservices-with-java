FROM reaandrew/java8-alpine

ADD ./build/libs/claim-payment-all.jar /opt/claim/claim-payment-all.jar
ADD ./config/development.yml /var/claim-payment/development.yml

EXPOSE 8085
CMD ["java","-jar","/opt/claim/claim-payment-all.jar","-c","/var/claim-payment/development.yml"]

