---
title: "Java SDK"
description: "Production-ready Java SDK with comprehensive e-invoicing workflows for KSA, Malaysia, and Belgium"
---

# Complyance Java SDK v3.0

Production ready Java SDK with Log on DocType support, automatic 320/32C classification, multi country compliance for KSA, Malaysia, and Belgium, and enterprise grade features. Built for real world e-invoice applications.

## Feature Summary

- **34 Types** - Registered DocType support
- **3 Countries** - KSA, Malaysia, Belgium supported
- **5 Overloads** - Good Comply methods
- **v3.0** - Production ready

## Installation

### Automated Setup (Recommended)

The automated setup will detect your operating system and provide the correct instructions.

#### macOS & Linux

1. **Install Java (JDK 11 or higher)**
   ```bash
   brew install openjdk@11
   ```

2. **Set JAVA_HOME environment variable**
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 11)
   ```

3. **Verify installation**
   ```bash
   java -version
   ```

#### Windows

1. **Download and install OpenJDK 11+**
2. **Set JAVA_HOME environment variable**
3. **Add Java to PATH**

### Manual Installation

#### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>io.complyance</groupId>
  <artifactId>unify-sdk</artifactId>
  <version>3.0.5-beta</version>
</dependency>
```

#### Gradle

```gradle
implementation 'io.complyance:unify-sdk:3.0.5-beta'
```

## Windows Installation

### Using Chocolatey (Recommended)

```powershell
# Install Chocolatey (run as Administrator)
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install OpenJDK 8 and Maven
choco install openjdk8 maven -y

# Verify installation
java -version
mvn -version
```

### PowerShell Profile Configuration

```powershell
# Edit PowerShell profile
notepad $PROFILE

# Add these lines to your PowerShell profile:
$env:JAVA_8_HOME = "C:\Program Files\OpenJDK\openjdk-8u402-b06"
$env:JAVA_HOME = $env:JAVA_8_HOME
$env:M2_HOME = "C:\Program Files\Apache\maven"
$env:PATH = "$env:JAVA_8_HOME\bin;$env:M2_HOME\bin;$env:PATH"

# Complyance SDK Environment Variables
$env:COMPLYANCE_API_BASE_URL = "http://127.0.0.1:4000"
$env:COMPLYANCE_JWT_TOKEN = "your-jwt-token-here"

# Convenient functions
function java8 { $env:JAVA_HOME = $env:JAVA_8_HOME; java -version }
function mvn8 { $env:JAVA_HOME = $env:JAVA_8_HOME; mvn @args }
```

## Linux Installation

### Ubuntu/Debian
```bash
sudo apt update && sudo apt install -y openjdk-8-jdk maven
export JAVA_8_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export JAVA_HOME="$JAVA_8_HOME"
```

### CentOS/RHEL
```bash
sudo yum install java-1.8.0-openjdk-devel maven
```

### Arch Linux
```bash
sudo pacman -S jre8-openjdk maven
```

## Quickstart (Correct Workflow)

The SDK supports two purposes:

- `Purpose.MAPPING`: send real payloads to generate mapping context (source name/version **optional**).
- `Purpose.INVOICING`: production submission using your **saved template** (source name/version **required**).

### 1) Configure the SDK (once)

```java
import io.complyance.sdk.*;
import java.util.Arrays;

Source source = new Source("my-app", "1.0", SourceType.FIRST_PARTY);
SDKConfig config = new SDKConfig(
    "YOUR_API_KEY",
    Environment.SANDBOX,
    Arrays.asList(source)
);

GETSUnifySDK.configure(config);
```

### 2) Run Mapping (push payload for mapping)

Use your real business payload (invoice header/parties/lines). For mapping runs, you can pass `null` for `sourceName` and `sourceVersion`.

```java
import io.complyance.sdk.*;
import java.util.Map;

Map<String, Object> payload = Map.of(
    "invoice_data", Map.of(
        "document_number", "INV-2026-0001",
        "invoice_date", "2026-02-18",
        "currency_code", "SAR"
    ),
    "seller_info", Map.of("seller_name", "My Company"),
    "buyer_info", Map.of("buyer_name", "My Customer"),
    "line_items", java.util.List.of(
        Map.of("item_name", "Consulting", "quantity", 1, "unit_price", 1000.00)
    )
);

UnifyResponse mappingResponse = GETSUnifySDK.pushToUnify(
    null,                       // sourceName (optional for MAPPING)
    null,                       // sourceVersion (optional for MAPPING)
    LogicalDocType.TAX_INVOICE,  // logical document type
    Country.SA,                 // SA / MY / BE ...
    Operation.SINGLE,
    Mode.DOCUMENTS,
    Purpose.MAPPING,
    payload
);

if (mappingResponse != null && mappingResponse.isSuccess()) {
    // Use the returned IDs/details to build your template in the portal.
    // Example fields (may vary by purpose/config):
    // - mappingResponse.getData().getDocument().getDocumentId()
    // - mappingResponse.getData().getTemplate()
}
```

### 3) Create / Fix Template (portal step)

After the mapping push succeeds:

1. Open the Complyance portal for your environment (Sandbox/Prod).
2. Find the mapping run and create a template for the selected `Country` + `LogicalDocType`.
3. Fix mapping fields until validation is clean.
4. Save the template and bind it to the same `sourceName` / `sourceVersion` you configured earlier (example: `my-app` / `1.0`).

> Tip: If field names/sections are unclear, start from a working country sample (this repo includes `src/test/java/io/complyance/test/UAE/UAETaxInvoiceTest.java`) and create your own per-country entrypoints (for example `KSADocumentFlow.java` for SA, `BelgiumTaxInvoiceTest.java` for BE).

### 4) Run Invoicing (production submission using your template)

Once the template is saved, switch `Purpose` to `INVOICING` and pass the same `sourceName` and `sourceVersion` used by the template.

```java
UnifyResponse invoiceResponse = GETSUnifySDK.pushToUnify(
    "my-app",                   // sourceName (required for INVOICING)
    "1.0",                      // sourceVersion (required for INVOICING)
    LogicalDocType.TAX_INVOICE,
    Country.SA,
    Operation.SINGLE,
    Mode.DOCUMENTS,
    Purpose.INVOICING,
    payload
);

if (invoiceResponse != null && invoiceResponse.isSuccess()) {
    // Typical invoicing success responses may include:
    // - invoiceResponse.getData().getSubmission()
    // - invoiceResponse.getData().getValidation()
    // - invoiceResponse.getData().getDocument()
} else if (invoiceResponse != null && invoiceResponse.getError() != null) {
    System.err.println(invoiceResponse.getError().getCode() + ": " + invoiceResponse.getError().getMessage());
}
```

## LogicalDocType

`LogicalDocType` is the recommended way to drive document behavior (B2B/B2C variants, export/self-billed/third-party, etc.).

Examples:

- B2B: `TAX_INVOICE`, `TAX_INVOICE_CREDIT_NOTE`, `TAX_INVOICE_DEBIT_NOTE`
- B2C: `SIMPLIFIED_TAX_INVOICE`, `SIMPLIFIED_TAX_INVOICE_CREDIT_NOTE`
- Special: `EXPORT_INVOICE`, `SELF_BILLED_INVOICE`, `THIRD_PARTY_INVOICE`

## Running the included example

This repo includes a runnable example with a full payload in:

- `src/test/java/io/complyance/test/UAE/UAETaxInvoiceTest.java`

Run it from your IDE, or with Maven exec tooling if you prefer.
