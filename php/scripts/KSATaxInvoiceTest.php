<?php
/**
* Standalone script for KSA Tax Invoice Flow
*/

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

// Constants - Using User's latest configuration
$API_KEY = 'ak_818533253a30b5c34b818f423533';
$SOURCE_NAME = 'YS';
$SOURCE_VERSION = '1.2';
$TEST_COUNTRY = Country::SA;

// Disable background queue worker for scripts
putenv('COMPLYANCE_SDK_DISABLE_QUEUE_WORKER=true');

echo "=== 🇸🇦 KSA Tax Invoice Flow ===\n";

// Helper functions
function getDynamicDate(int $daysOffset): string
{
    $date = new \DateTime();
    $date->modify("{$daysOffset} days");
    return $date->format('Y-m-d');
}

try {
    // Configure SDK
    $sources = [new Source($SOURCE_NAME, $SOURCE_VERSION, SourceType::fromString(SourceType::FIRST_PARTY))];
    $config = new SDKConfig($API_KEY, Environment::from(Environment::SANDBOX), $sources);
    GETSUnifySDK::configure($config);
    echo "✅ SDK Configured\n";

    $invoiceNumber = 'SA-INV-' . (new \DateTime())->format('YmdHisv');
    $uniqueId = bin2hex(random_bytes(16));

    // Create payload
    $payload = [
        'invoice_data' => [
            'document_number' => $invoiceNumber,
            'document_id' => $uniqueId,
            'document_type' => 'tax_invoice',
            'invoice_date' => getDynamicDate(-1),
            'invoice_time' => '14:30:00Z',
            'currency_code' => 'SAR',
            'tax_currency_code' => 'SAR',
            'due_date' => getDynamicDate(30),
            'period_start_date' => getDynamicDate(-30),
            'period_end_date' => getDynamicDate(1),
            'period_frequency' => 'MONTHLY',
            'exchange_rate' => 1.0,
            'line_extension_amount' => 10000.00,
            'tax_exclusive_amount' => 10000.00,
            'total_tax_amount' => 1500.00,
            'total_amount' => 11500.00,
            'total_allowances' => 0.00,
            'total_charges' => 0.00,
            'prepaid_amount' => 0.00,
            'amount_due' => 11500.00,
            'rounding_amount' => 0.00,
            'original_reference_id' => 'SA-INV-ORIG-001',
            'credit_note_reason' => 'Goods returned'
        ],

        'seller_info' => [
            'seller_name' => 'Al Riyadh Trading Co.',
            'seller_trade_name' => 'Al Riyadh Trading',
            'seller_party_id' => 'SELLER-SA-001',
            'vat_number_type' => 'VAT',
            'vat_number' => '300593161500003',
            'tax_scheme' => 'CRN',
            'registration_number' => '2034567890',
            'registration_type' => 'CRN',
            'registration_scheme' => 'SA:CRN',
            'authority_name' => 'Ministry of Commerce - Riyadh',
            'peppol_id' => '0235:3008213264',
            'seller_email' => 'contact@alriyadhtrading.sa',
            'seller_phone' => '+966-11-1234567',
            'seller_contact_name' => 'Mohammed Al Saud',
            'street_name' => 'King Fahd Road',
            'additional_address' => 'Building 456',
            'building_number' => '4562',
            'city_name' => 'Riyadh',
            'state_province' => 'RIYADH',
            'postal_code' => '11564',
            'country_code' => 'SA',
            'seller_District' => 'Saudi Arabia'
        ],

        'buyer_info' => [
            'buyer_name' => 'Jeddah Supplies LLC',
            'buyer_trade_name' => 'Jeddah Supplies',
            'buyer_party_id' => 'BUYER-SA-001',
            'buyer_vat_type' => 'VAT',
            'buyer_vat_number' => '300889867100003',
            'buyer_tax_scheme' => 'CRN',
            'buyer_registration_number' => '2034567890',
            'buyer_registration_type' => 'CRN',
            'buyer_registration_scheme' => 'CRN',
            'buyer_authority_name' => 'Ministry of Commerce - Jeddah',
            'buyer_peppol_id' => '0235:3008215673',
            'buyer_email' => 'purchasing@jeddahsupplies.sa',
            'buyer_phone' => '+966-12-9876543',
            'buyer_contact_name' => 'Fatima Al Zahrani',
            'buyer_street_name' => 'Prince Sultan Road',
            'buyer_additional_address' => 'Tower 3',
            'buyer_building_number' => '1234',
            'buyer_city' => 'Jeddah',
            'buyer_state_province' => 'MAKKAH',
            'buyer_postal_code' => '21589',
            'buyer_country' => 'SA',
            'buyer_District' => 'Saudi Arabia'
        ],

        'line_items' => [
            [
                'line_id' => '1',
                'item_name' => 'Office Equipment',
                'item_description' => 'Professional office equipment package',
                'quantity' => 10.0,
                'unit_code' => 'PCE',
                'unit_price' => 500.00,
                'gross_price' => 500.00,
                'line_taxable_value' => 5000.00,
                'Discount' => 0.00,
                'tax_category' => 'S',
                'tax_rate' => 15.0,
                'tax_amount' => 750.00,
                'line_total' => 5750.00,
                'item_type' => 'GOODS',
                'country_of_origin' => 'SA',
                'classification_code' => '8471',
                'classification_scheme' => 'HS',
                'seller_item_code' => 'SKU-001',
                'buyer_item_code' => 'BUYER-SKU-001',
                'batch_number' => 'BATCH-2024-001'
            ]
        ],

        'ksa_extensions' => [
            'unique_identifier' => $uniqueId,
            'invoiced_object_id' => 'OBJECT-2024-001',
            'taxpoint_date' => getDynamicDate(0),
            'total_amount_including_tax_in_sar' => 11500.00,
            'authority_name' => 'Ministry of Commerce - Riyadh',
            'buyer_authority_name' => 'Ministry of Commerce - Jeddah',
            'business_process_type' => 'urn:peppol:bis:billing',
            'specification_identifier' => 'urn:peppol:pint:sa:invoice:v1'
        ],

        'payment_info' => [
            'payment_id' => 'PAY-001',
            'payment_means_code' => 'CREDIT',
            'payment_means_text' => 'Bank Transfer',
            'remittance_info' => 'Payment for Invoice ' . $invoiceNumber,
            'account_id' => 'SA1234567890123456789012',
            'account_name' => 'Al Riyadh Trading Co.',
            'bank_id' => 'SABN0001'
        ],

        'payment_terms' => [
            [
                'instructions_id' => 'TERMS-001',
                'note' => 'Net 30 days',
                'amount' => 11500.00,
                'due_date' => getDynamicDate(30)
            ]
        ],

        'supporting_documents' => [
            [
                'type' => 'purchaseOrderReference',
                'id' => 'PO-2024-001234'
            ]
        ],

        'additional_data' => [
            'delivery_date' => getDynamicDate(0),
            'order_reference' => 'PO-2024-001234',
            'source_system' => 'ksa-source-system'
        ]
    ];

    echo "🚀 Pushing to Unify...\n";

    $rawResponse = GETSUnifySDK::pushToUnify(
        $SOURCE_NAME,
        $SOURCE_VERSION,
        LogicalDocType::from(LogicalDocType::TAX_INVOICE),
        Country::from($TEST_COUNTRY),
        Operation::from(Operation::SINGLE),
        Mode::from(Mode::DOCUMENTS),
        Purpose::from(Purpose::INVOICING), // Using user's preference
        $payload
    );

    // Decode response
    $response = is_string($rawResponse) ? json_decode($rawResponse, true) : (is_array($rawResponse) ? $rawResponse : (method_exists($rawResponse, 'toArray') ? $rawResponse->toArray() : (array) $rawResponse));

    echo "\n📊 Response Summary:\n";
    echo "   Status: " . ($response['status'] ?? 'UNKNOWN') . "\n";

    if ($response['status'] === 'success') {
        echo "✅ Success! Request ID: " . ($response['data']['processing']['requestId'] ?? $response['metadata']['requestId'] ?? 'N/A') . "\n";
    } else {
        echo "❌ Error: " . ($response['error']['message'] ?? $response['message'] ?? 'Unknown error') . "\n";
    }

} catch (\Exception $e) {
    echo "❌ Execution failed: " . $e->getMessage() . "\n";
}

 