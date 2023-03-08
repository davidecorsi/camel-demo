# Camel Demo

Applicazione demo per l'utilizzo di apache camel

## Flusso
![flusso](docs/flow.jpg)

## Utilizzo
```
docker-compose up -d
mvn spring-boot:run
```
## Test
```
mvn clean test
```
### Report coverage
```
mvn camel-report:route-coverage
```