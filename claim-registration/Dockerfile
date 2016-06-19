FROM reaandrew/java8-alpine

ADD ./build/libs/claim-registration-all.jar /opt/claim/claim-registration-all.jar
ADD ./config/development.yml /var/claim-registration/development.yml

EXPOSE 8083
CMD ["java","-jar","/opt/claim/claim-registration-all.jar","-c","/var/claim-registration/development.yml"]

