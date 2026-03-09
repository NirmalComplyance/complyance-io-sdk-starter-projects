<?php
/**
* Standalone script for UAE Tax Invoice Flow
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

// Constants
$API_KEY = '';
$SOURCE_NAME = '';
$SOURCE_VERSION = '';
$TEST_COUNTRY = Country::AE;

// Disable background queue worker for scripts
putenv('COMPLYANCE_SDK_DISABLE_QUEUE_WORKER=true');

echo "=== 🇦🇪 UAE Tax Invoice Flow ===\n";

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

    $invoiceNumber = 'UAE-INV-' . (new \DateTime())->format('YmdHisv');
    $uniqueId = bin2hex(random_bytes(16));

    // Create payload
    $payload = [
        'invoice_data' => [
            'document_number' =>  $invoiceNumber,
            'document_id' => $uniqueId,
            'invoice_date' => getDynamicDate(0),
            'invoice_time' => '14:30:00Z',
            'currency_code' => 'AED',
            'tax_currency_code' => 'AED',
            'due_date' => getDynamicDate(5),
            'period_start_date' => getDynamicDate(-30),
            'period_end_date' => getDynamicDate(0),
            'period_frequency' => 'MONTHLY',
            'exchange_rate' => 1.0,
            'line_extension_amount' => 10000.00,
            'tax_exclusive_amount' => 10000.00,
            'total_tax_amount' => 500.00,
            'total_amount' => 10500.00,
            'total_allowances' => 0.00,
            'total_charges' => 0.00,
            'prepaid_amount' => 0.00,
            'amount_due' => 10500.00,
            'rounding_amount' => 0.00,
            'original_reference_id' => 'UAE-INV-ORIG-001',
            'credit_note_reason' => 'Goods returned'
        ],
        'seller_info' => [
            'seller_name' => 'ABC Trading LLC',
            'seller_trade_name' => 'ABC Trading',
            'seller_party_id' => 'SELLER-UAE-001',
            'vat_number_type' => 'TRN',
            'vat_number' => '100819867100003',
            'tax_scheme' => 'VAT',
            'registration_number' => 'CN-1234567',
            'registration_type' => 'TL',
            'registration_scheme' => 'AE:TL',
            'authority_name' => 'Dubai Department of Economic Development',
            'peppol_id' => '0235:1189748191',
            'seller_email' => 'contact@abctrading.ae',
            'seller_phone' => '+971-4-1234567',
            'seller_contact_name' => 'Ahmed Al Maktoum',
            'street_name' => 'Sheikh Zayed Road',
            'additional_address' => 'Building 123',
            'building_number' => '123',
            'city_name' => 'Dubai',
            'state_province' => 'DUBAI',
            'postal_code' => '00000',
            'country_code' => 'AE'
        ],
        'buyer_info' => [
            'buyer_name' => 'XYZ Corporation LLC',
            'buyer_trade_name' => 'XYZ Corp',
            'buyer_party_id' => 'BUYER-UAE-001',
            'buyer_vat_type' => 'TRN',
            'buyer_vat_number' => '100889867100003',
            'buyer_tax_scheme' => 'VAT',
            'buyer_registration_number' => 'CN-9876543',
            'buyer_registration_type' => 'TL',
            'buyer_registration_scheme' => 'TL',
            'buyer_authority_name' => 'Abu Dhabi Department of Economic Development',
            'buyer_peppol_id' => '0235:1297201011',
            'buyer_email' => 'purchasing@xyzcorp.ae',
            'buyer_phone' => '+971-2-9876543',
            'buyer_contact_name' => 'Fatima Al Mansouri',
            'buyer_street_name' => 'Al Wasl Road',
            'buyer_additional_address' => 'Tower 2',
            'buyer_building_number' => '456',
            'buyer_city' => 'Dubai',
            'buyer_state_province' => 'DUBAI',
            'buyer_postal_code' => '00000',
            'buyer_country' => 'AE'
        ],
        'line_items' => [
            [
                'line_id' => '1',
                'item_name' => 'Office Equipment',
                'item_description' => 'Professional office equipment package',
                'quantity' => 10.0,
                'unit_code' => 'EA',
                'unit_price' => 500.00,
                'net_price' => 500.00,
                'gross_price' => 500.00,
                'line_taxable_value' => 5000.00,
                'tax_category' => 'S',
                'tax_rate' => 5.0,
                'tax_amount' => 250.00,
                'line_total' => 5250.00,
                'item_type' => 'GOODS',
                'country_of_origin' => 'AE',
                'classification_code' => '8471',
                'classification_scheme' => 'HS',
                'seller_item_code' => 'SKU-001',
                'buyer_item_code' => 'BUYER-SKU-001',
                'batch_number' => 'BATCH-2024-001'
            ]
        ],
        'uae_extensions' => [
            'unique_identifier' => $uniqueId,
            'invoiced_object_id' => 'OBJECT-2024-001',
            'taxpoint_date' => getDynamicDate(0),
            'total_amount_including_tax_in_aed' => 10500.00,
            'authority_name' => 'Dubai Department of Economic Development',
            'buyer_authority_name' => 'Abu Dhabi Department of Economic Development',
            'business_process_type' => 'urn:peppol:bis:billing',
            'specification_identifier' => 'urn:peppol:pint:ae:invoice:v1'
        ],
        'payment_info' => [
            'payment_id' => 'PAY-001',
            'payment_means_code' => 'CREDIT',
            'payment_means_text' => 'Bank Transfer',
            'remittance_info' => 'Payment for Invoice ' . $invoiceNumber,
            'account_id' => 'AE123456789012345678901',
            'account_name' => 'ABC Trading LLC',
            'bank_id' => 'AEBN0001'
        ],
        'payment_terms' => [
            [
                'instructions_id' => 'TERMS-001',
                'note' => 'Net 30 days',
                'amount' => 10500.00,
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
            'source_system' => 'uae-source-system'
        ]
    ];

    echo "🚀 Pushing to Unify...\n";

    $rawResponse = GETSUnifySDK::pushToUnify(
        $SOURCE_NAME,
        $SOURCE_VERSION,
        LogicalDocType::from(LogicalDocType::TAX_INVOICE_CREDIT_NOTE),
        Country::from($TEST_COUNTRY),
        Operation::from(Operation::SINGLE),
        Mode::from(Mode::DOCUMENTS),
        Purpose::from(Purpose::INVOICING),
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
 