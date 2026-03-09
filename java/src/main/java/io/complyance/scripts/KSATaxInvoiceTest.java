package io.complyance.scripts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class KSATaxInvoiceTest {
    private static String sourceId = "";
    private static String sourceVersion = "";
    private static String apiKey = "";

    public static void main(String[] args) {
        try {

            configureSDK();
            System.out.println("✅ SDK Configured");

            Map<String, Object> payload = createSimpleTestPayload();
            System.out.println("✅ Payload Created");

            testLogicalDocumentTypeFlow(payload);

        } catch (Exception e) {
            System.err.println("❌ Sample failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ============================================================
       SDK CONFIGURATION
    ============================================================ */

    private static void configureSDK() {

        List<Source> sources = Arrays.asList(
                new Source(sourceId, sourceVersion, SourceType.FIRST_PARTY)
        );

        SDKConfig config = new SDKConfig(
                apiKey,
                Environment.DEV,
                sources
        );

        GETSUnifySDK.configure(config);
    }

    /* ============================================================
       PAYLOAD BUILDER
    ============================================================ */

    private static Map<String, Object> createSimpleTestPayload() {

        Map<String, Object> payload = new HashMap<>();

        // String invoiceNumber = System.getenv("INVOICE_NUMBER") != null ? System.getenv("INVOICE_NUMBER") : "from sdk 004";
        String invoiceDate = System.getenv("INVOICE_DATE") != null ? System.getenv("INVOICE_DATE") : "2025-11-04";
        String invoiceTime = System.getenv("INVOICE_TIME") != null ? System.getenv("INVOICE_TIME") : "14:30:00.000Z";
        String invoiceStartDate = System.getenv("INVOICE_START_DATE") != null ? System.getenv("INVOICE_START_DATE") : "2025-07-19";
        String invoiceEndDate = System.getenv("INVOICE_END_DATE") != null ? System.getenv("INVOICE_END_DATE") : "2025-07-21";
        String invoiceDueDate = System.getenv("INVOICE_DUE_DATE") != null ? System.getenv("INVOICE_DUE_DATE") : "2026-07-30";

        /* ---------------- Invoice Data ---------------- */

        Map<String, Object> invoiceData = new HashMap<>();
        String invoiceNumber = generateInvoiceNumber();

        invoiceData.put("invoice_number", invoiceNumber);
        invoiceData.put("invoice_date", invoiceDate);
        invoiceData.put("invoice_time", invoiceTime);
        invoiceData.put("currency_code", "SAR");
        invoiceData.put("total_amount", 23000.00);
        invoiceData.put("total_payable_amount", 23000.00);
        invoiceData.put("tax_exclusive_amount", 20000.00);
        invoiceData.put("line_extension_amount", 20000.00);
        invoiceData.put("total_tax_amount", 3000.00);
        invoiceData.put("paid_Amount", 0);
        invoiceData.put("invoice_endDate", invoiceEndDate);
        invoiceData.put("invoice_startDate", invoiceStartDate);
        invoiceData.put("vat_currency_code", "SAR");
        invoiceData.put("exchange_percentage", 1);
        invoiceData.put("invoice_due_date", invoiceDueDate);
        invoiceData.put("total_discount", "0");
        invoiceData.put("PaymentMethod", "CASH");
        payload.put("invoice_data", invoiceData);

        /* ---------------- Seller Info ---------------- */

        Map<String, Object> sellerInfo = new HashMap<>();
        sellerInfo.put("company_name", "Advanced Tech Solutions LLC");
        sellerInfo.put("vat_registration", "310123456700003");
        sellerInfo.put("tax_scheme", "VAT");
        sellerInfo.put("street_address", "King Fahd Road");
        sellerInfo.put("additional_address_info", "Building 123");
        sellerInfo.put("building_number", "1234");
        sellerInfo.put("district_name", "Al Olaya");
        sellerInfo.put("city_name", "Riyadh");
        sellerInfo.put("state_name", "Riyadh Province");
        sellerInfo.put("postal_code", "11564");
        sellerInfo.put("country_code", "SA");
        sellerInfo.put("seller_id", "2034567890");
        sellerInfo.put("phone", "+966501234567");
        sellerInfo.put("email", "contact@advancedtech.sa");
        sellerInfo.put("contact_name", "Ahmed Al-Rashid");
        sellerInfo.put("Crn_number", "2034567890");
        sellerInfo.put("Additional_Type", "CRN");
        payload.put("seller_info", sellerInfo);

        /* ---------------- Buyer Info ---------------- */

        Map<String, Object> buyerInfo = new HashMap<>();
        buyerInfo.put("buyer_name", "Global Manufacturing Co.");
        buyerInfo.put("buyer_vat", "310987654300003");
        buyerInfo.put("buyer_tax_scheme", "VAT");
        buyerInfo.put("buyer_address", "Industrial City");
        buyerInfo.put("buyer_additional_address_info", "Block A");
        buyerInfo.put("buyer_building", "4567");
        buyerInfo.put("buyer_district", "Industrial Area");
        buyerInfo.put("buyer_city", "Dammam");
        buyerInfo.put("buyer_state", "Damman Province");
        buyerInfo.put("buyer_postal", "31461");
        buyerInfo.put("buyer_country", "SA");
        buyerInfo.put("buyer_id", "2034567890");
        buyerInfo.put("crn_Number", "2034567890");
        buyerInfo.put("Additional_Type", "CRN");
        payload.put("buyer_info", buyerInfo);

        /* ---------------- Line Items ---------------- */

        List<Map<String, Object>> lineItems = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("item_id", "ITEM001");
        item1.put("item_name", "Industrial Server System");
        item1.put("quantity", 2);
        item1.put("unit_code", "PCE");
        item1.put("unit_price", 8500.00);
        item1.put("tax_amount", 2550.00);
        item1.put("tax_category", "S");
        item1.put("tax_rate", 15);
        item1.put("discount_amount", 0);
        item1.put("sub_Total", 19550);
        item1.put("taxable_amount", 17000);
        lineItems.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("item_id", "ITEM002");
        item2.put("item_name", "Network Security Module");
        item2.put("quantity", 1);
        item2.put("unit_code", "PCE");
        item2.put("unit_price", 3000.00);
        item2.put("tax_amount", 450.00);
        item2.put("tax_category", "S");
        item2.put("tax_rate", 15);
        item2.put("discount_amount", 0);
        item2.put("sub_Total", 3450);
        item2.put("taxable_amount", 3000);
        lineItems.add(item2);

        payload.put("line_items", lineItems);

        /* ---------------- Extensions (Prepayment) ---------------- */

        Map<String, Object> extensions = new HashMap<>();
        List<Map<String, Object>> prepaymentDetails = new ArrayList<>();

        Map<String, Object> prepayment = new HashMap<>();
        prepayment.put("paymentId", "PP-2024-001");
        prepayment.put("issueDate", "2024-01-15T14:30:00Z");
        prepayment.put("documentType", "tax_invoice_prepayment_invoice");
        prepayment.put("vatCategory", "S");
        prepayment.put("vatRate", 15.0);
        prepayment.put("taxableAmount", 1000.00);
        prepayment.put("taxAmount", 150.00);
        prepayment.put("adjustmentAmount", 1150.00);

        prepaymentDetails.add(prepayment);
        extensions.put("sa_prepayment", prepaymentDetails);
        payload.put("extensions", extensions);

        /* ---------------- Destinations ---------------- */

        List<Map<String, Object>> destinations = new ArrayList<>();

        Map<String, Object> destination = new HashMap<>();
        destination.put("type", "tax_authority");

        Map<String, Object> destinationDetails = new HashMap<>();
        destinationDetails.put("authority", "ZATCA");
        destinationDetails.put("country", "SA");
        destinationDetails.put("document_type", "tax_invoice");

        destination.put("details", destinationDetails);
        destinations.add(destination);

        payload.put("destinations", destinations);

        /* ---------------- Additional Data ---------------- */

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("order_reference", "PO-2024-5678");
        additionalData.put("delivery_date", "2024-01-20");
        additionalData.put("source_system", "test-soruce-11111111");

        payload.put("additional_data", additionalData);

        return payload;
    }

    /* ============================================================
       LOGICAL FLOW EXECUTION
    ============================================================ */

    private static void testLogicalDocumentTypeFlow(Map<String, Object> payload) {

        try {

            UnifyResponse response = GETSUnifySDK.pushToUnify(
                    sourceId,
                    sourceVersion,
                    LogicalDocType.TAX_INVOICE,
                    Country.SA,
                    Operation.SINGLE,
                    Mode.DOCUMENTS,
                    Purpose.INVOICING,
                    payload
            );

            printResponse(response);

        } catch (SDKException e) {

            System.err.println("❌ Logical Document Type Flow failed: " + e.getMessage());

            if (e.getErrorDetail() != null) {
                System.err.println("   Error Code: " + e.getErrorDetail().getCode());
                System.err.println("   Suggestion: " + e.getErrorDetail().getSuggestion());
            }
        }
    }


    private static String generateInvoiceNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return "KSA-INV-" + dateFormat.format(new Date());
    }

    /* ============================================================
       RESPONSE HANDLER
    ============================================================ */

    private static void printResponse(UnifyResponse response) {

        if (response == null) {
            System.err.println("❌ Response is null");
            return;
        }

        if ("error".equalsIgnoreCase(response.getStatus())) {

            System.err.println("❌ Error: " + response.getMessage());

            if (response.getError() != null) {
                System.err.println("   Code: " + response.getError().getCode());
                System.err.println("   Suggestion: " + response.getError().getSuggestion());
            }

        } else {

            System.out.println("✅ Status: " + response.getStatus());

            if (response.getData() != null &&
                response.getData().getSubmission() != null) {

                System.out.println("🆔 Submission ID: " +
                        response.getData().getSubmission().getSubmissionId());
            }
        }
    }
}