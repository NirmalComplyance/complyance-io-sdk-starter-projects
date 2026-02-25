# .NET Starter Project

## Prerequisites

- .NET SDK 6.0+

## Restore dependencies

```bash
cd sdk-starter-projects/dotnet
dotnet restore TestProject.Tests.csproj
```

## Run UAE test

```bash
dotnet test TestProject.Tests.csproj --filter "FullyQualifiedName~UAETaxInvoice"
```
