# SDK Starter Projects

This folder contains starter examples for SDK integrators.

## Java
Path: `sdk-starter-projects/java`

Install and run:
```bash
cd sdk-starter-projects/java
mvn -DskipTests compile
mvn -DskipTests exec:java -Dexec.mainClass=io.complyance.test.UAE.UAETaxInvoiceTest -Dexec.classpathScope=test
```

## PHP
Path: `sdk-starter-projects/php`

Install and run:
```bash
cd sdk-starter-projects/php
composer install
composer run run:uae
```

## .NET
Path: `sdk-starter-projects/dotnet`

Restore and test:
```bash
cd sdk-starter-projects/dotnet
dotnet restore TestProject.Tests.csproj
dotnet test TestProject.Tests.csproj --filter "FullyQualifiedName~UAETaxInvoice"
```
