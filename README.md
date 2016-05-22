# Microservices in Java

This is a small project for me to explore how I would go about creating Microservices in Java following good practices and principles that apply to any language.

Some of the characteristics of the solution I want to end up with include:

- I want to use libraries for common things like TRANSPORT, DATA ACCESS, LOGGING, MONITORING etc...
- I want to focus on using good programming practices and principles
- I want to use the hexaganol architectural style
- I want to be able to test the application like I would with Golang, Node.js, Python etc...
- I want to able to easily deploy the application inside a Container using Docker
- I want to follow the principles contained inside the Reactive Manifesto

> IMPORTANT 
> - I want this to be simple to create, understand, explain and extend.
> - I want each project to be independent of each other

## Getting Started

To jump straight in you can simply run:

```shell
make run
```

The core requirements which need to be in the path are:

- docker
- java 8 JDK

##  Domain

A system which supports the submission, registration, verification, calculation, payment and communication of a claim to a claimant.  There is no definition as to what the claim is for simply only criteria which must be met in order to receive the claim as this is for example only.  

Depending on where you live will affect how much you will be awarded and you must be over the age of 65 to claim.

## Project Dependencies

**Spark** - Micro web framework for Java 8.

**Unirest** - HTTP client library from the folks who made KONG.

**Codehale Metrics** - Metrics library (the main one which other languages has implemented)

**Gson** - JSON library

**RabbitMQ AMQP Client** - AMQP client from RabbitMQ

**Apache Commons CLI** - Command line arguments parser

**Yaml Beans** - YAML library

## Services

### Claim Portal

The UI

### Claim Portal Query Service

The `Backend for the Front End` Query Service.  This is the service which the Claim Portal service will use a the Read Service.  This services handles the read concerns of the store.

### Claim Portal Query Updater Service

This will subscribe to the relevant events which are required to update the Claim Portal read store.  This service handles the write concerns of the store.

### Claim Registration Service

This service handles the subsmission, duplication checks and registration of new claims and change of circumstances **(NOT CURRENTLY SUPPORTED)**.

### Claim Fraud Service

This services checks the bank account and passport number supplied match the specified address of the claimant.

### Claim Award Service

This service calculates the amount the claimant is entitiled to based on the informaiton in the claim

### Claim Payment Service

This service integrates with payment providers in order to pay the claimant once a claim has been awarded to them.

### Claim Communication Service

This service deals with the different forms of communication which are required including integration with email and postal gateways.

## Default Configuration

|Project|Configuration|
|---|---|
|Claim Portal|port: 8080|
|Claim Portal Query Servicev|port: 8081|
|Claim Portal Query Updater Service|port: 8082|
|Claim Registration Service|port: 8083|
|Claim Fraud Service|port: 8084|
|Claim Award Service|port: 8085|
|Claim Payment Service|port: 8086|
|Claim Communication Service|port: 8087|

## Characteristics

### Starting the Service

- commons cli
- using normal command line arguments
- packging into a single jar using shadow with gradle

### Configuration

- using yaml configuration
- using environment variables allowing them to be overridden by the command line args

### Common Service Endpoints

- live
- ready

### Hexaganol Architecture

- ports and adapters
- hosts, infrastructure, domain, config
