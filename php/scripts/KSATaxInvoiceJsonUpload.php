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

$API_KEY = 'ak_818533253a30b5c34b818f423533';
$SOURCE_NAME = 'YS';
$SOURCE_VERSION = '1.2';
$TEST_COUNTRY = Country::SA;

putenv('COMPLYANCE_SDK_DISABLE_QUEUE_WORKER=true');

function generateInvoiceNumber(): string
{
    return 'SA-INV-' . (new \DateTime())->format('YmdHisv');
}

$payloadTemplate = <<<'JSON'
{
  "invoice_data": {
    "document_number": "{{AUTO_KSA_INVOICE_NUMBER}}",
    "document_id": "8f5f4b7b8c9042a8a3502d4dd8fd31e7",
    "document_type": "tax_invoice",
    "invoice_date": "2026-02-25",
    "invoice_time": "14:30:00Z",
    "currency_code": "SAR",
    "tax_currency_code": "SAR",
    "due_date": "2026-03-28",
    "period_start_date": "2026-01-27",
    "period_end_date": "2026-02-27",
    "period_frequency": "MONTHLY",
    "exchange_rate": 1.0,
    "line_extension_amount": 10000.0,
    "tax_exclusive_amount": 10000.0,
    "total_tax_amount": 1500.0,
    "total_amount": 11500.0,
    "total_allowances": 0.0,
    "total_charges": 0.0,
    "prepaid_amount": 0.0,
    "amount_due": 11500.0,
    "rounding_amount": 0.0,
    "original_reference_id": "SA-INV-ORIG-001",
    "credit_note_reason": "Goods returned"
  },
  "seller_info": {
    "seller_name": "Al Riyadh Trading Co.",
    "seller_trade_name": "Al Riyadh Trading",
    "seller_party_id": "SELLER-SA-001",
    "vat_number_type": "VAT",
    "vat_number": "300593161500003",
    "tax_scheme": "CRN",
    "registration_number": "2034567890",
    "registration_type": "CRN",
    "registration_scheme": "SA:CRN",
    "authority_name": "Ministry of Commerce - Riyadh",
    "peppol_id": "0235:3008213264",
    "seller_email": "contact@alriyadhtrading.sa",
    "seller_phone": "+966-11-1234567",
    "seller_contact_name": "Mohammed Al Saud",
    "street_name": "King Fahd Road",
    "additional_address": "Building 456",
    "building_number": "4562",
    "city_name": "Riyadh",
    "state_province": "RIYADH",
    "postal_code": "11564",
    "country_code": "SA",
    "seller_District": "Saudi Arabia"
  },
  "buyer_info": {
    "buyer_name": "Jeddah Supplies LLC",
    "buyer_trade_name": "Jeddah Supplies",
    "buyer_party_id": "BUYER-SA-001",
    "buyer_vat_type": "VAT",
    "buyer_vat_number": "300889867100003",
    "buyer_tax_scheme": "CRN",
    "buyer_registration_number": "2034567890",
    "buyer_registration_type": "CRN",
    "buyer_registration_scheme": "CRN",
    "buyer_authority_name": "Ministry of Commerce - Jeddah",
    "buyer_peppol_id": "0235:3008215673",
    "buyer_email": "purchasing@jeddahsupplies.sa",
    "buyer_phone": "+966-12-9876543",
    "buyer_contact_name": "Fatima Al Zahrani",
    "buyer_street_name": "Prince Sultan Road",
    "buyer_additional_address": "Tower 3",
    "buyer_building_number": "1234",
    "buyer_city": "Jeddah",
    "buyer_state_province": "MAKKAH",
    "buyer_postal_code": "21589",
    "buyer_country": "SA",
    "buyer_District": "Saudi Arabia"
  },
  "line_items": [
    {
      "line_id": "1",
      "item_name": "Office Equipment",
      "item_description": "Professional office equipment package",
      "quantity": 10.0,
      "unit_code": "PCE",
      "unit_price": 500.0,
      "gross_price": 500.0,
      "line_taxable_value": 5000.0,
      "Discount": 0.0,
      "tax_category": "S",
      "tax_rate": 15.0,
      "tax_amount": 750.0,
      "line_total": 5750.0,
      "item_type": "GOODS",
      "country_of_origin": "SA",
      "classification_code": "8471",
      "classification_scheme": "HS",
      "seller_item_code": "SKU-001",
      "buyer_item_code": "BUYER-SKU-001",
      "batch_number": "BATCH-2024-001"
    }
  ],
  "ksa_extensions": {
    "unique_identifier": "8f5f4b7b8c9042a8a3502d4dd8fd31e7",
    "invoiced_object_id": "OBJECT-2024-001",
    "taxpoint_date": "2026-02-26",
    "total_amount_including_tax_in_sar": 11500.0,
    "authority_name": "Ministry of Commerce - Riyadh",
    "buyer_authority_name": "Ministry of Commerce - Jeddah",
    "business_process_type": "urn:peppol:bis:billing",
    "specification_identifier": "urn:peppol:pint:sa:invoice:v1"
  },
  "payment_info": {
    "payment_id": "PAY-001",
    "payment_means_code": "CREDIT",
    "payment_means_text": "Bank Transfer",
    "remittance_info": "Payment for Invoice SA-INV-20260226192000000",
    "account_id": "SA1234567890123456789012",
    "account_name": "Al Riyadh Trading Co.",
    "bank_id": "SABN0001"
  },
  "payment_terms": [
    {
      "instructions_id": "TERMS-001",
      "note": "Net 30 days",
      "amount": 11500.0,
      "due_date": "2026-03-28"
    }
  ],
  "supporting_documents": [
    {
      "type": "purchaseOrderReference",
      "id": "PO-2024-001234"
    }
  ],
  "additional_data": {
    "delivery_date": "2026-02-26",
    "order_reference": "PO-2024-001234",
    "source_system": "ksa-source-system"
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
