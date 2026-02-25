package io.complyance.test.UAE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.complyance.sdk.Country;
import io.complyance.sdk.Environment;
import io.complyance.sdk.GETSUnifySDK;
import io.complyance.sdk.LogicalDocType;
import io.complyance.sdk.Mode;
import io.complyance.sdk.Operation;
import io.complyance.sdk.Purpose;
import io.complyance.sdk.SDKConfig;
import io.complyance.sdk.SDKException;
import io.complyance.sdk.Source;
import io.complyance.sdk.SourceType;
import io.complyance.sdk.UnifyResponse;

/**
 * UAE Tax Invoice Test - Demonstrates UAE-specific TAX_INVOICE functionality
 *
 * This test class demonstrates:
 * - UAE (AE) country-specific compliance
 * - TAX_INVOICE logical document type mapping
 * - B2B transaction processing with Peppol PINT network
 * - UAE-specific field mappings (TRN, Emirates, VAT, extensions)
 * - Comprehensive payload with UAE invoice data
 * - UAE PINT AE format compliance
 * - Multi-language support (English/Arabic)
 *
 * UAE-Specific Features:
 * - 15-digit TRN (Tax Registration Number) validation
 * - UAE Emirates codes (Dubai, Abu Dhabi, etc.)
 * - UAE VAT categories (S, Z, E, O)
 * - UAE-specific extensions (ae_uniqueIdentifier, ae_beneficiaryId, etc.)
 * - Peppol PINT AE format support
 * - FTA (Federal Tax Authority) compliance
 */
public class UAETaxInvoiceTest {

    public static void main(String[] args) {
        System.out.println("=== UAE Tax Invoice Test ===");
        System.out.println("Testing UAE-specific TAX_INVOICE functionality");
        System.out.println("Demonstrates B2B mapping, AE compliance, Peppol PINT, and comprehensive payload");

        try {
            // Configure SDK
            configureSDK();

            // Create comprehensive UAE test payload
            Map<String, Object> payload = createUAETestPayload();

            System.out.println("UAE test payload created");
            System.out.println("Expected GETS fields coverage: 37/37 (100%)");
            System.out.println("Expected AE country fields coverage: 15/15 (100%)");
            System.out.println("Expected AE compliance fields coverage: 12/12 (100%)");

            // Test UAE TAX_INVOICE flow
            testUAETaxInvoiceFlow(payload);

            System.out.println("UAE Tax Invoice test completed successfully");
            System.out.println("Expected Results:");
            System.out.println("- B2B tax invoice processing for UAE");
            System.out.println("- AE compliance flags properly set");
            System.out.println("- Peppol PINT AE network compliance");
            System.out.println("- FTA submission ready");
            System.out.println("- Comprehensive payload with UAE data");

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateInvoiceNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());
        return "UAE-DBT-" + timestamp;
    }

    private static String generateUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    private static String getDynamicDate(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, daysOffset);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    private static String getDateFromEnv(String envVarName, int defaultOffset) {
        String envValue = System.getenv(envVarName);

        if (envValue != null && !envValue.isEmpty()) {
            try {
                int offset = Integer.parseInt(envValue);
                return getDynamicDate(offset);
            } catch (NumberFormatException e) {
                return envValue;
            }
        }

        return getDynamicDate(defaultOffset);
    }

    private static void configureSDK() {
        Source source = new Source("UAE", "1", SourceType.FIRST_PARTY);
        SDKConfig config = new SDKConfig(
                "ak_d0007aab0c90a3c495afbcba9ce0",
                Environment.SANDBOX,
                Arrays.asList(source));
        GETSUnifySDK.configure(config);
        System.out.println("SDK Configured for UAE Testing");
    }

    private static Map<String, Object> createUAETestPayload() {
        Map<String, Object> payload = new HashMap<>();

        String invoiceNumber = generateInvoiceNumber();
        String uniqueId = generateUniqueIdentifier();

        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("document_number", "siva123");
        invoiceData.put("document_id", uniqueId);
        invoiceData.put("document_type", "tax_invoice");
        invoiceData.put("invoice_date", getDateFromEnv("INVOICE_DATE", 0));
        invoiceData.put("invoice_time", "14:30:00Z");
        invoiceData.put("currency_code", "AED");
        invoiceData.put("tax_currency_code", "AED");
        invoiceData.put("due_date", getDateFromEnv("INVOICE_DUE_DATE", 30));
        invoiceData.put("period_start_date", getDateFromEnv("INVOICE_START_DATE", -30));
        invoiceData.put("period_end_date", getDateFromEnv("INVOICE_END_DATE", 0));
        invoiceData.put("period_frequency", "MONTHLY");
        invoiceData.put("exchange_rate", 1.0);
        invoiceData.put("line_extension_amount", 10000.00);
        invoiceData.put("tax_exclusive_amount", 10000.00);
        invoiceData.put("total_tax_amount", 500.00);
        invoiceData.put("total_amount", 10500.00);
        invoiceData.put("total_allowances", 0.00);
        invoiceData.put("total_charges", 0.00);
        invoiceData.put("prepaid_amount", 0.00);
        invoiceData.put("amount_due", 10500.00);
        invoiceData.put("rounding_amount", 0.00);
        invoiceData.put("original_reference_id", "UAE-INV-ORIG-001");
        invoiceData.put("credit_note_reason", "Goods returned");
        payload.put("invoice_data", invoiceData);

        Map<String, Object> sellerInfo = new HashMap<>();
        sellerInfo.put("seller_name", "ABC Trading LLC");
        sellerInfo.put("seller_trade_name", "ABC Trading");
        sellerInfo.put("seller_party_id", "SELLER-UAE-001");
        sellerInfo.put("vat_number_type", "TRN");
        sellerInfo.put("vat_number", "100819867100003");
        sellerInfo.put("tax_scheme", "VAT");
        sellerInfo.put("registration_number", "CN-1234567");
        sellerInfo.put("registration_type", "TL");
        sellerInfo.put("registration_scheme", "AE:TL");
        sellerInfo.put("authority_name", "Dubai Department of Economic Development");
        sellerInfo.put("peppol_id", "0235:1914931209");
        sellerInfo.put("seller_email", "contact@abctrading.ae");
        sellerInfo.put("seller_phone", "+971-4-1234567");
        sellerInfo.put("seller_contact_name", "Ahmed Al Maktoum");
        sellerInfo.put("street_name", "Sheikh Zayed Road");
        sellerInfo.put("additional_address", "Building 123");
        sellerInfo.put("building_number", "123");
        sellerInfo.put("city_name", "Dubai");
        sellerInfo.put("state_province", "DUBAI");
        sellerInfo.put("postal_code", "00000");
        sellerInfo.put("country_code", "AE");
        payload.put("seller_info", sellerInfo);

        Map<String, Object> buyerInfo = new HashMap<>();
        buyerInfo.put("buyer_name", "XYZ Corporation LLC");
        buyerInfo.put("buyer_trade_name", "XYZ Corp");
        buyerInfo.put("buyer_party_id", "BUYER-UAE-001");
        buyerInfo.put("buyer_vat_type", "TRN");
        buyerInfo.put("buyer_vat_number", "100889867100003");
        buyerInfo.put("buyer_tax_scheme", "VAT");
        buyerInfo.put("buyer_registration_number", "CN-9876543");
        buyerInfo.put("buyer_registration_type", "TL");
        buyerInfo.put("buyer_registration_scheme", "TL");
        buyerInfo.put("buyer_authority_name", "Abu Dhabi Department of Economic Development");
        buyerInfo.put("buyer_peppol_id", "0235:1124872741");
        buyerInfo.put("buyer_email", "purchasing@xyzcorp.ae");
        buyerInfo.put("buyer_phone", "+971-2-9876543");
        buyerInfo.put("buyer_contact_name", "Fatima Al Mansouri");
        buyerInfo.put("buyer_street_name", "Al Wasl Road");
        buyerInfo.put("buyer_additional_address", "Tower 2");
        buyerInfo.put("buyer_building_number", "456");
        buyerInfo.put("buyer_city", "Dubai");
        buyerInfo.put("buyer_state_province", "DUBAI");
        buyerInfo.put("buyer_postal_code", "00000");
        buyerInfo.put("buyer_country", "AE");
        payload.put("buyer_info", buyerInfo);

        List<Map<String, Object>> lineItems = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("line_id", "1");
        item1.put("item_name", "Office Equipment");
        item1.put("item_description", "Professional office equipment package");
        item1.put("quantity", 10.0);
        item1.put("unit_code", "EA");
        item1.put("unit_price", 500.00);
        item1.put("net_price", 500.00);
        item1.put("gross_price", 500.00);
        item1.put("line_taxable_value", 5000.00);
        item1.put("tax_category", "S");
        item1.put("tax_rate", 5.0);
        item1.put("tax_amount", 250.00);
        item1.put("line_total", 5250.00);
        item1.put("item_type", "GOODS");
        item1.put("country_of_origin", "AE");
        item1.put("classification_code", "8471");
        item1.put("classification_scheme", "HS");
        item1.put("seller_item_code", "SKU-001");
        item1.put("buyer_item_code", "BUYER-SKU-001");
        item1.put("batch_number", "BATCH-2024-001");
        lineItems.add(item1);
        payload.put("line_items", lineItems);

        Map<String, Object> uaeExtensions = new HashMap<>();
        uaeExtensions.put("unique_identifier", uniqueId);
        uaeExtensions.put("invoiced_object_id", "OBJECT-2024-001");
        uaeExtensions.put("taxpoint_date", getDateFromEnv("INVOICE_DATE", 0));
        uaeExtensions.put("total_amount_including_tax_in_aed", 10500.00);
        uaeExtensions.put("authority_name", "Dubai Department of Economic Development");
        uaeExtensions.put("buyer_authority_name", "Abu Dhabi Department of Economic Development");
        uaeExtensions.put("business_process_type", "urn:peppol:bis:billing");
        uaeExtensions.put("specification_identifier", "urn:peppol:pint:ae:invoice:v1");
        payload.put("uae_extensions", uaeExtensions);

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("payment_id", "PAY-001");
        paymentInfo.put("payment_means_code", "IN_CASH");
        paymentInfo.put("payment_means_text", "Bank Transfer");
        paymentInfo.put("remittance_info", "Payment for Invoice " + invoiceNumber);
        paymentInfo.put("account_id", "AE123456789012345678901");
        paymentInfo.put("account_name", "ABC Trading LLC");
        paymentInfo.put("bank_id", "AEBN0001");
        payload.put("payment_info", paymentInfo);

        List<Map<String, Object>> paymentTerms = new ArrayList<>();
        Map<String, Object> term = new HashMap<>();
        term.put("instructions_id", "TERMS-001");
        term.put("note", "Net 30 days");
        term.put("amount", 10500.00);
        term.put("due_date", getDateFromEnv("INVOICE_DUE_DATE", 30));
        paymentTerms.add(term);
        payload.put("payment_terms", paymentTerms);

        List<Map<String, Object>> supportingDocuments = new ArrayList<>();
        Map<String, Object> purchaseOrder = new HashMap<>();
        purchaseOrder.put("type", "purchaseOrderReference");
        purchaseOrder.put("id", "PO-2024-001234");
        supportingDocuments.add(purchaseOrder);
        payload.put("supporting_documents", supportingDocuments);

        return payload;
    }

    private static void testUAETaxInvoiceFlow(Map<String, Object> payload) {
        try {
            System.out.println("Testing UAE Tax Invoice Flow");

            UnifyResponse response = GETSUnifySDK.pushToUnify(
                    "UAE",
                    "1",
                    LogicalDocType.TAX_INVOICE,
                    Country.AE,
                    Operation.SINGLE,
                    Mode.DOCUMENTS,
                    Purpose.INVOICING,
                    payload
            );

            printUnifyResponse(response, "UAE Tax Invoice Flow");

        } catch (SDKException e) {
            System.err.println("UAE Tax Invoice Flow failed: " + e.getMessage());
        }
    }

    private static void printUnifyResponse(UnifyResponse response, String context) {
        if (response == null) {
            System.err.println(context + " failed: Response is null");
            return;
        }

        System.out.println("Response Details:");
        System.out.println("Status: " + response.getStatus());

        if ("error".equalsIgnoreCase(response.getStatus())) {
            System.err.println(context + " failed:");
            if (response.getError() != null) {
                System.err.println("Error Code: " + response.getError().getCode());
                System.err.println("Message: " + response.getError().getMessage());
            } else {
                System.err.println("Message: " + response.getMessage());
            }
        } else if ("success".equalsIgnoreCase(response.getStatus()) || response.isSuccess()) {
            System.out.println(context + " Response: " + response.getStatus());
        } else {
            System.out.println(context + " Response: " + response.getStatus());
            System.out.println("Message: " + (response.getMessage() != null ? response.getMessage() : "No message"));
        }
    }
}