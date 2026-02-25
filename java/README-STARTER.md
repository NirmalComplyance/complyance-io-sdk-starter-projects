# Java Starter Project

## Prerequisites

- JDK 11+
- Maven

## Resolve dependencies and compile

```bash
cd sdk-starter-projects/java
mvn -DskipTests test-compile
```

## Run UAE example

```bash
mvn -DskipTests exec:java -Dexec.mainClass=io.complyance.test.UAE.UAETaxInvoiceTest -Dexec.classpathScope=test
```
