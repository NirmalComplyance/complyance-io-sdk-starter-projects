<?php

require_once __DIR__ . '/../vendor/autoload.php';

use ComplyanceSDK\GETSUnifySDK;
use ComplyanceSDK\Models\SDKConfig;
use ComplyanceSDK\Enums\Country;
use ComplyanceSDK\Enums\Environment;
use ComplyanceSDK\Enums\LogicalDocType;
use ComplyanceSDK\Enums\Operation;
use ComplyanceSDK\Enums\Mode;
use ComplyanceSDK\Enums\Purpose;
use ComplyanceSDK\Enums\SourceType;
use ComplyanceSDK\Models\Source;

$API_KEY = '';
$SOURCE_NAME = '';
$SOURCE_VERSION = '';
$TEST_COUNTRY = Country::SA;

putenv('COMPLYANCE_SDK_DISABLE_QUEUE_WORKER=true');

function generateInvoiceNumber(): string
{
    return 'KSA-INV-' . (new \DateTime())->format('YmdHisv');
}

$payloadTemplate = <<<'JSON'
{
  "invoice_data": {
    "invoice_number": "{{AUTO_KSA_INVOICE_NUMBER}}",
    "invoice_date": "2025-11-04",
    "invoice_time": "14:30:00.000Z",
    "currency_code": "SAR",
    "total_amount": 23000.0,
    "total_payable_amount": 23000.0,
    "tax_exclusive_amount": 20000.0,
    "line_extension_amount": 20000.0,
    "total_tax_amount": 3000.0,
    "paid_Amount": 0,
    "invoice_endDate": "2025-07-21",
    "invoice_startDate": "2025-07-19",
    "vat_currency_code": "SAR",
    "exchange_percentage": 1,
    "invoice_due_date": "2026-07-30",
    "total_discount": "0",
    "PaymentMethod": "CASH"
  },
  "seller_info": {
    "company_name": "Advanced Tech Solutions LLC",
    "vat_registration": "310123456700003",
    "tax_scheme": "VAT",
    "street_address": "King Fahd Road",
    "additional_address_info": "Building 123",
    "building_number": "1234",
    "district_name": "Al Olaya",
    "city_name": "Riyadh",
    "state_name": "Riyadh Province",
    "postal_code": "11564",
    "country_code": "SA",
    "seller_id": "2034567890",
    "phone": "+966501234567",
    "email": "contact@advancedtech.sa",
    "contact_name": "Ahmed Al-Rashid",
    "Crn_number": "2034567890",
    "Additional_Type": "CRN"
  },
  "buyer_info": {
    "buyer_name": "Global Manufacturing Co.",
    "buyer_vat": "310987654300003",
    "buyer_tax_scheme": "VAT",
    "buyer_address": "Industrial City",
    "buyer_additional_address_info": "Block A",
    "buyer_building": "4567",
    "buyer_district": "Industrial Area",
    "buyer_city": "Dammam",
    "buyer_state": "Damman Province",
    "buyer_postal": "31461",
    "buyer_country": "SA",
    "buyer_id": "2034567890",
    "crn_Number": "2034567890",
    "Additional_Type": "CRN"
  },
  "line_items": [
    {
      "item_id": "ITEM001",
      "item_name": "Industrial Server System",
      "quantity": 2,
      "unit_code": "PCE",
      "unit_price": 8500.0,
      "tax_amount": 2550.0,
      "tax_category": "S",
      "tax_rate": 15,
      "discount_amount": 0,
      "sub_Total": 19550,
      "taxable_amount": 17000
    },
    {
      "item_id": "ITEM002",
      "item_name": "Network Security Module",
      "quantity": 1,
      "unit_code": "PCE",
      "unit_price": 3000.0,
      "tax_amount": 450.0,
      "tax_category": "S",
      "tax_rate": 15,
      "discount_amount": 0,
      "sub_Total": 3450,
      "taxable_amount": 3000
    }
  ],
  "extensions": {
    "sa_prepayment": [
      {
        "paymentId": "PP-2024-001",
        "issueDate": "2024-01-15T14:30:00Z",
        "documentType": "tax_invoice_prepayment_invoice",
        "vatCategory": "S",
        "vatRate": 15.0,
        "taxableAmount": 1000.0,
        "taxAmount": 150.0,
        "adjustmentAmount": 1150.0
      }
    ]
  },
  "destinations": [
    {
      "type": "tax_authority",
      "details": {
        "authority": "ZATCA",
        "country": "SA",
        "document_type": "tax_invoice"
      }
    }
  ],
  "additional_data": {
    "order_reference": "PO-2024-5678",
    "delivery_date": "2024-01-20",
    "source_system": "test-soruce-11111111"
  }
}
JSON;

try {
    $sources = [new Source($SOURCE_NAME, $SOURCE_VERSION, SourceType::fromString(SourceType::FIRST_PARTY))];
    $config = new SDKConfig($API_KEY, Environment::from(Environment::SANDBOX), $sources);
    GETSUnifySDK::configure($config);

    $payloadJson = str_replace('{{AUTO_KSA_INVOICE_NUMBER}}', generateInvoiceNumber(), $payloadTemplate);
    $payload = json_decode($payloadJson, true);
    if (!is_array($payload)) {
        throw new \RuntimeException('Invalid JSON payload: ' . json_last_error_msg());
    }

    GETSUnifySDK::pushToUnify(
        $SOURCE_NAME,
        $SOURCE_VERSION,
        LogicalDocType::from(LogicalDocType::TAX_INVOICE),
        Country::from($TEST_COUNTRY),
        Operation::from(Operation::SINGLE),
        Mode::from(Mode::DOCUMENTS),
        Purpose::from(Purpose::INVOICING),
        $payload
    );

    echo "Done\n";
} catch (\Exception $e) {
    echo "Failed: " . $e->getMessage() . "\n";
}
