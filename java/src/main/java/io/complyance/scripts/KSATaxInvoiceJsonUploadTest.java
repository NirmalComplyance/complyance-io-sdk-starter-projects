package io.complyance.scripts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.complyance.sdk.Country;
import io.complyance.sdk.Environment;
import io.complyance.sdk.GETSUnifySDK;
import io.complyance.sdk.LogicalDocType;
import io.complyance.sdk.Mode;
import io.complyance.sdk.Operation;
import io.complyance.sdk.Purpose;
import io.complyance.sdk.SDKConfig;
import io.complyance.sdk.Source;
import io.complyance.sdk.SourceType;
import io.complyance.sdk.UnifyResponse;

public class KSATaxInvoiceJsonUploadTest {

    private static String sourceId = "";
    private static String sourceVersion = "";
    private static String apiKey = "";

    private static final String KSA_TAX_INVOICE_JSON = String.join("\n",
            "{",
            "  \"invoice_data\": {",
            "    \"invoice_number\": \"{{AUTO_KSA_INVOICE_NUMBER}}\",",
            "    \"invoice_date\": \"2025-11-04\",",
            "    \"invoice_time\": \"14:30:00.000Z\",",
            "    \"currency_code\": \"SAR\",",
            "    \"total_amount\": 23000.0,",
            "    \"total_payable_amount\": 23000.0,",
            "    \"tax_exclusive_amount\": 20000.0,",
            "    \"line_extension_amount\": 20000.0,",
            "    \"total_tax_amount\": 3000.0,",
            "    \"paid_Amount\": 0,",
            "    \"invoice_endDate\": \"2025-07-21\",",
            "    \"invoice_startDate\": \"2025-07-19\",",
            "    \"vat_currency_code\": \"SAR\",",
            "    \"exchange_percentage\": 1,",
            "    \"invoice_due_date\": \"2026-07-30\",",
            "    \"total_discount\": \"0\",",
            "    \"PaymentMethod\": \"CASH\"",
            "  },",
            "  \"seller_info\": {",
            "    \"company_name\": \"Advanced Tech Solutions LLC\",",
            "    \"vat_registration\": \"310123456700003\",",
            "    \"tax_scheme\": \"VAT\",",
            "    \"street_address\": \"King Fahd Road\",",
            "    \"additional_address_info\": \"Building 123\",",
            "    \"building_number\": \"1234\",",
            "    \"district_name\": \"Al Olaya\",",
            "    \"city_name\": \"Riyadh\",",
            "    \"state_name\": \"Riyadh Province\",",
            "    \"postal_code\": \"11564\",",
            "    \"country_code\": \"SA\",",
            "    \"seller_id\": \"2034567890\",",
            "    \"phone\": \"+966501234567\",",
            "    \"email\": \"contact@advancedtech.sa\",",
            "    \"contact_name\": \"Ahmed Al-Rashid\",",
            "    \"Crn_number\": \"2034567890\",",
            "    \"Additional_Type\": \"CRN\"",
            "  },",
            "  \"buyer_info\": {",
            "    \"buyer_name\": \"Global Manufacturing Co.\",",
            "    \"buyer_vat\": \"310987654300003\",",
            "    \"buyer_tax_scheme\": \"VAT\",",
            "    \"buyer_address\": \"Industrial City\",",
            "    \"buyer_additional_address_info\": \"Block A\",",
            "    \"buyer_building\": \"4567\",",
            "    \"buyer_district\": \"Industrial Area\",",
            "    \"buyer_city\": \"Dammam\",",
            "    \"buyer_state\": \"Damman Province\",",
            "    \"buyer_postal\": \"31461\",",
            "    \"buyer_country\": \"SA\",",
            "    \"buyer_id\": \"2034567890\",",
            "    \"crn_Number\": \"2034567890\",",
            "    \"Additional_Type\": \"CRN\"",
            "  },",
            "  \"line_items\": [",
            "    {",
            "      \"item_id\": \"ITEM001\",",
            "      \"item_name\": \"Industrial Server System\",",
            "      \"quantity\": 2,",
            "      \"unit_code\": \"PCE\",",
            "      \"unit_price\": 8500.0,",
            "      \"tax_amount\": 2550.0,",
            "      \"tax_category\": \"S\",",
            "      \"tax_rate\": 15,",
            "      \"discount_amount\": 0,",
            "      \"sub_Total\": 19550,",
            "      \"taxable_amount\": 17000",
            "    },",
            "    {",
            "      \"item_id\": \"ITEM002\",",
            "      \"item_name\": \"Network Security Module\",",
            "      \"quantity\": 1,",
            "      \"unit_code\": \"PCE\",",
            "      \"unit_price\": 3000.0,",
            "      \"tax_amount\": 450.0,",
            "      \"tax_category\": \"S\",",
            "      \"tax_rate\": 15,",
            "      \"discount_amount\": 0,",
            "      \"sub_Total\": 3450,",
            "      \"taxable_amount\": 3000",
            "    }",
            "  ],",
            "  \"extensions\": {",
            "    \"sa_prepayment\": [",
            "      {",
            "        \"paymentId\": \"PP-2024-001\",",
            "        \"issueDate\": \"2024-01-15T14:30:00Z\",",
            "        \"documentType\": \"tax_invoice_prepayment_invoice\",",
            "        \"vatCategory\": \"S\",",
            "        \"vatRate\": 15.0,",
            "        \"taxableAmount\": 1000.0,",
            "        \"taxAmount\": 150.0,",
            "        \"adjustmentAmount\": 1150.0",
            "      }",
            "    ]",
            "  },",
            "  \"destinations\": [",
            "    {",
            "      \"type\": \"tax_authority\",",
            "      \"details\": {",
            "        \"authority\": \"ZATCA\",",
            "        \"country\": \"SA\",",
            "        \"document_type\": \"tax_invoice\"",
            "      }",
            "    }",
            "  ],",
            "  \"additional_data\": {",
            "    \"order_reference\": \"PO-2024-5678\",",
            "    \"delivery_date\": \"2024-01-20\",",
            "    \"source_system\": \"test-soruce-11111111\"",
            "  }",
            "}");

    public static void main(String[] args) {
        System.out.println("=== KSA Tax Invoice JSON Upload Test ===");

        try {
            configureSDK();
            String invoiceNumber = generateInvoiceNumber();
            String payloadJson = KSA_TAX_INVOICE_JSON.replace("{{AUTO_KSA_INVOICE_NUMBER}}", invoiceNumber);

            UnifyResponse response = GETSUnifySDK.pushToUnify(
                    sourceId,
                    sourceVersion,
                    LogicalDocType.TAX_INVOICE,
                    Country.SA,
                    Operation.SINGLE,
                    Mode.DOCUMENTS,
                    Purpose.INVOICING,
                    payloadJson
            );

            printResponse(response);

        } catch (Exception e) {
            System.err.println("JSON upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateInvoiceNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return "KSA-INV-" + dateFormat.format(new Date());
    }

    private static void configureSDK() {
        Source source = new Source(sourceId, sourceVersion, SourceType.FIRST_PARTY);

        SDKConfig config = new SDKConfig(
                apiKey,
                Environment.DEV,
                List.of(source)
        );

        GETSUnifySDK.configure(config);
        System.out.println("SDK configured.");
    }

    private static void printResponse(UnifyResponse response) {
        if (response == null) {
            System.err.println("Response is null.");
            return;
        }

        System.out.println("Status: " + response.getStatus());

        if (response.isSuccess()) {
            if (response.getData() != null && response.getData().getSubmission() != null) {
                System.out.println("Submission ID: " + response.getData().getSubmission().getSubmissionId());
            }
        } else {
            System.err.println("Error: " + response.getMessage());
        }
    }
}
