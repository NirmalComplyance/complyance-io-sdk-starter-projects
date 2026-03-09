package io.complyance.scripts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class UAECreditNoteTest {

    private static String sourceId = "";
    private static String sourceVersion = "";
    private static String apiKey = "";

    public static void main(String[] args) {

        System.out.println("=== UAE Credit Note Test ===");

        try {
            configureSDK();

            Map<String, Object> payload = createUAETestPayload();
            System.out.println("Payload created successfully.");

            testUAECreditNoteFlow(payload);

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateInvoiceNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return "UAE-INV-" + dateFormat.format(new Date());
    }

    private static String generateUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    private static String getDynamicDate(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, daysOffset);
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    private static String getDateFromEnv(String envVarName, int defaultOffset) {
        String envValue = System.getenv(envVarName);

        if (envValue != null && !envValue.isEmpty()) {
            try {
                return getDynamicDate(Integer.parseInt(envValue));
            } catch (NumberFormatException e) {
                return envValue;
            }
        }

        return getDynamicDate(defaultOffset);
    }

    private static void configureSDK() {
        Source source = new Source(sourceId, sourceVersion, SourceType.FIRST_PARTY);

        SDKConfig config = new SDKConfig(
                apiKey,
                Environment.SANDBOX,
                List.of(source)
        );

        GETSUnifySDK.configure(config);
        System.out.println("SDK configured.");
    }

    private static Map<String, Object> createUAETestPayload() {
        Map<String, Object> payload = new HashMap<>();

        // Generate unique identifiers
        String invoiceNumber = generateInvoiceNumber();
        String uniqueId = generateUniqueIdentifier();

        // Invoice Data - Maps to GETS header fields
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("document_number", invoiceNumber); // → header.documentNumber
        // For debit and credit notes, we need to set the note issuance reason and reference id
        // invoiceData.put("reference_id", "20260202104801557"); // → header.referenceId
        invoiceData.put("document_id", uniqueId); // → header.documentId (UUID)
        invoiceData.put("document_type", "tax_invoice"); // → header.documentType
        invoiceData.put("invoice_date", getDateFromEnv("INVOICE_DATE", 0)); // → header.issueDate
        invoiceData.put("invoice_time", "14:30:00Z"); // → header.issueTime
        invoiceData.put("currency_code", "AED"); // → header.currency (UAE Dirham)
        invoiceData.put("tax_currency_code", "AED"); // → header.taxCurrency
        invoiceData.put("due_date", getDateFromEnv("INVOICE_DUE_DATE", 30)); // → header.dueDate
        invoiceData.put("period_start_date", getDateFromEnv("INVOICE_START_DATE", -30)); // → header.invoicePeriod.startDate
        invoiceData.put("period_end_date", getDateFromEnv("INVOICE_END_DATE", 0)); // → header.invoicePeriod.endDate
        invoiceData.put("period_frequency", "MONTHLY"); // → header.invoicePeriod.frequency
        invoiceData.put("exchange_rate", 1.0); // → header.exchangeRate.rate (1.0 for AED)
        invoiceData.put("line_extension_amount", 10000.00); // → totals.totalLineTaxableAmount
        invoiceData.put("tax_exclusive_amount", 10000.00); // → totals.totalAmountExcludingTax
        invoiceData.put("total_tax_amount", 500.00); // → totals.totalTaxAmount (5% VAT)
        invoiceData.put("total_amount", 10500.00); // → totals.totalAmountIncludingTax
        invoiceData.put("total_allowances", 0.00); // → totals.totalAllowances
        invoiceData.put("total_charges", 0.00); // → totals.totalCharges
        invoiceData.put("prepaid_amount", 0.00); // → totals.prepaidAmount
        invoiceData.put("amount_due", 10500.00); // → totals.amountDue
        invoiceData.put("rounding_amount", 0.00); // → totals.roundingAmount
        // Credit / Debit note: reference to original invoice and reason (mandatory for credit_note/debit_note)
         invoiceData.put("original_reference_id", "UAE-INV-ORIG-001"); // → header.referenceId (original invoice ID/number)
         invoiceData.put("credit_note_reason", "Goods returned"); // → header.noteIssuanceReason (for credit_note)
         invoiceData.put("debit_note_reason", "Additional charges"); // → header.noteIssuanceReason (for debit_note)
        payload.put("invoice_data", invoiceData);

        // Seller Info - Maps to GETS seller party fields with UAE-specific data
        Map<String, Object> sellerInfo = new HashMap<>();
        sellerInfo.put("seller_name", "ABC Trading LLC"); // → parties.seller.name
        sellerInfo.put("seller_trade_name", "ABC Trading"); // → parties.seller.tradeName
        sellerInfo.put("seller_party_id", "SELLER-UAE-001"); // → parties.seller.partyId
        sellerInfo.put("vat_number_type", "TRN"); // → parties.seller.taxIds[0].type
        sellerInfo.put("vat_number", "100819867100003"); // → parties.seller.taxIds[0].value (15 digits TRN)
        sellerInfo.put("tax_scheme", "VAT"); // → parties.seller.taxIds[0].scheme
        sellerInfo.put("registration_number", "CN-1234567"); // → parties.seller.registrationNumbers[0].value
        sellerInfo.put("registration_type", "TL"); // → parties.seller.registrationNumbers[0].type (Trade License)
        sellerInfo.put("registration_scheme", "AE:TL"); // → parties.seller.registrationNumbers[0].scheme
        sellerInfo.put("authority_name", "Dubai Department of Economic Development"); // → extensions.ae_authorityName
        sellerInfo.put("peppol_id", "0235:1189748191"); // → parties.seller.peppolId (scheme:TRN)
        sellerInfo.put("seller_email", "contact@abctrading.ae"); // → parties.seller.contact.email
        sellerInfo.put("seller_phone", "+971-4-1234567"); // → parties.seller.contact.phone
        sellerInfo.put("seller_contact_name", "Ahmed Al Maktoum"); // → parties.seller.contact.name
        sellerInfo.put("street_name", "Sheikh Zayed Road"); // → parties.seller.address.addressLine1
        sellerInfo.put("additional_address", "Building 123"); // → parties.seller.address.addressLine2
        sellerInfo.put("building_number", "123"); // → parties.seller.address.buildingNumber
        sellerInfo.put("city_name", "Dubai"); // → parties.seller.address.city
        sellerInfo.put("state_province", "DUBAI"); // → parties.seller.address.stateOrProvince
        sellerInfo.put("postal_code", "00000"); // → parties.seller.address.postalCode
        sellerInfo.put("country_code", "AE"); // → parties.seller.address.country
        payload.put("seller_info", sellerInfo);

        // Buyer Info - Maps to GETS buyer party fields with UAE-specific data
        Map<String, Object> buyerInfo = new HashMap<>();
        buyerInfo.put("buyer_name", "XYZ Corporation LLC"); // → parties.buyer.name
        buyerInfo.put("buyer_trade_name", "XYZ Corp"); // → parties.buyer.tradeName
        buyerInfo.put("buyer_party_id", "BUYER-UAE-001"); // → parties.buyer.partyId
        buyerInfo.put("buyer_vat_type", "TRN"); // → parties.buyer.taxIds[0].type
        buyerInfo.put("buyer_vat_number", "100889867100003"); // → parties.buyer.taxIds[0].value (15 digits TRN)
        buyerInfo.put("buyer_tax_scheme", "VAT"); // → parties.buyer.taxIds[0].scheme
        buyerInfo.put("buyer_registration_number", "CN-9876543"); // → parties.buyer.registrationNumbers[0].value
        buyerInfo.put("buyer_registration_type", "TL"); // → parties.buyer.registrationNumbers[0].type
        buyerInfo.put("buyer_registration_scheme", "TL"); // → parties.buyer.registrationNumbers[0].scheme
        buyerInfo.put("buyer_authority_name", "Abu Dhabi Department of Economic Development"); // → extensions.ae_buyerAuthorityName
        buyerInfo.put("buyer_peppol_id", "0235:1297201011"); // → parties.buyer.peppolId
        buyerInfo.put("buyer_email", "purchasing@xyzcorp.ae"); // → parties.buyer.contact.email
        buyerInfo.put("buyer_phone", "+971-2-9876543"); // → parties.buyer.contact.phone
        buyerInfo.put("buyer_contact_name", "Fatima Al Mansouri"); // → parties.buyer.contact.name
        buyerInfo.put("buyer_street_name", "Al Wasl Road"); // → parties.buyer.address.addressLine1
        buyerInfo.put("buyer_additional_address", "Tower 2"); // → parties.buyer.address.addressLine2
        buyerInfo.put("buyer_building_number", "456"); // → parties.buyer.address.buildingNumber
        buyerInfo.put("buyer_city", "Dubai"); // → parties.buyer.address.city
        buyerInfo.put("buyer_state_province", "DUBAI"); // → parties.buyer.address.stateOrProvince
        buyerInfo.put("buyer_postal_code", "00000"); // → parties.buyer.address.postalCode
        buyerInfo.put("buyer_country", "AE"); // → parties.buyer.address.country
        payload.put("buyer_info", buyerInfo);

        // Line Items - Maps to GETS lineItems with UAE-specific fields
        List<Map<String, Object>> lineItems = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("line_id", "1"); // → lineItems[].id
        item1.put("item_name", "Office Equipment"); // → lineItems[].name
        item1.put("item_description", "Professional office equipment package"); // → lineItems[].description
        item1.put("quantity", 10.0); // → lineItems[].quantity
        item1.put("unit_code", "EA"); // → lineItems[].unitCode (UN/ECE Recommendation 20)
        item1.put("unit_price", 500.00); // → lineItems[].price.amount
        item1.put("net_price", 500.00); // → lineItems[].discountsOrCharges[isCharge=false].amount
        item1.put("gross_price", 500.00); // → lineItems[].price.grossAmount
        item1.put("line_taxable_value", 5000.00); // → lineItems[].lineTaxableValue
        item1.put("tax_category", "S"); // → lineItems[].taxCategory (S=Standard, Z=Zero, E=Exempt, O=Out of Scope)
        item1.put("tax_rate", 5.0); // → lineItems[].taxRate (5% standard UAE VAT)
        item1.put("tax_amount", 250.00); // → lineItems[].taxAmount
        item1.put("line_total", 5250.00); // → lineItems[].lineTotal
        item1.put("item_type", "GOODS"); // → lineItems[].customFields.ae_itemType (GOODS/SERVICES/BOTH)
        item1.put("country_of_origin", "AE"); // → lineItems[].countryOfOrigin
        item1.put("classification_code", "8471"); // → lineItems[].commodityClassification.code (HS code)
        item1.put("classification_scheme", "HS"); // → lineItems[].commodityClassification.scheme
        item1.put("seller_item_code", "SKU-001"); // → lineItems[].sellerItemCode
        item1.put("buyer_item_code", "BUYER-SKU-001"); // → lineItems[].buyerItemCode
        item1.put("batch_number", "BATCH-2024-001"); // → lineItems[].customFields.ae_batchNumber
        lineItems.add(item1);
        payload.put("line_items", lineItems);

        // UAE Extensions - Maps to extensions.ae_*
        Map<String, Object> uaeExtensions = new HashMap<>();
        uaeExtensions.put("unique_identifier", uniqueId); // → extensions.ae_uniqueIdentifier (UUID)
        uaeExtensions.put("invoiced_object_id", "OBJECT-2024-001"); // → extensions.ae_invoicedObjectId
        uaeExtensions.put("taxpoint_date", getDateFromEnv("INVOICE_DATE", 0)); // → extensions.ae_taxpointDate
        uaeExtensions.put("total_amount_including_tax_in_aed", 10500.00); // → extensions.ae_totalAmountIncludingTaxInAED
        uaeExtensions.put("authority_name", "Dubai Department of Economic Development"); // → extensions.ae_authorityName
        uaeExtensions.put("buyer_authority_name", "Abu Dhabi Department of Economic Development"); // → extensions.ae_buyerAuthorityName
        uaeExtensions.put("business_process_type", "urn:peppol:bis:billing"); // → extensions.ae_businessProcessType
        uaeExtensions.put("specification_identifier", "urn:peppol:pint:ae:invoice:v1"); // → extensions.ae_specificationIdentifier
        payload.put("uae_extensions", uaeExtensions);


        // Payment Information - Maps to payment.paymentMeans[0] (single object like Germany)
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("payment_id", "PAY-001"); // → payment.paymentMeans[0].paymentId
        paymentInfo.put("payment_means_code", "IN_CASH"); // → payment.paymentMeans[0].paymentMeansCode (30 = Credit Transfer)
        paymentInfo.put("payment_means_text", "Bank Transfer"); // → payment.paymentMeans[0].paymentMeansText
        paymentInfo.put("remittance_info", "Payment for Invoice " + invoiceNumber); // → payment.paymentMeans[0].remittanceInfo
        paymentInfo.put("account_id", "AE123456789012345678901"); // → payment.paymentMeans[0].creditTransferInfo.payeeFinancialAccountId
        paymentInfo.put("account_name", "ABC Trading LLC"); // → payment.paymentMeans[0].creditTransferInfo.payeeFinancialAccountName
        paymentInfo.put("bank_id", "AEBN0001"); // → payment.paymentMeans[0].creditTransferInfo.payeeFinancialInstitutionId
        payload.put("payment_info", paymentInfo);

        // Payment Terms - Maps to payment.paymentTerms[]
        List<Map<String, Object>> paymentTerms = new ArrayList<>();
        Map<String, Object> term = new HashMap<>();
        term.put("instructions_id", "TERMS-001"); // → payment.paymentTerms[].instructionsId
        term.put("note", "Net 30 days"); // → payment.paymentTerms[].note
        term.put("amount", 10500.00); // → payment.paymentTerms[].amount
        term.put("due_date", getDateFromEnv("INVOICE_DUE_DATE", 30)); // → payment.paymentTerms[].dueDate
        paymentTerms.add(term);
        payload.put("payment_terms", paymentTerms);


        // Supporting Documents - Maps to supportingDocuments[]
        List<Map<String, Object>> supportingDocuments = new ArrayList<>();
        Map<String, Object> purchaseOrder = new HashMap<>();
        purchaseOrder.put("type", "purchaseOrderReference");
        purchaseOrder.put("id", "PO-2024-001234");
        supportingDocuments.add(purchaseOrder);
        payload.put("supporting_documents", supportingDocuments);
        return payload;
    }

    private static void testUAECreditNoteFlow(Map<String, Object> payload) {

        try {

            UnifyResponse response = GETSUnifySDK.pushToUnify(
                    sourceId,
                    sourceVersion,
                    LogicalDocType.TAX_INVOICE_CREDIT_NOTE,
                    Country.AE,
                    Operation.SINGLE,
                    Mode.DOCUMENTS,
                    Purpose.INVOICING,
                    payload
            );

            printUnifyResponse(response);

        } catch (SDKException e) {
            System.err.println("Invoice submission failed: " + e.getMessage());
        }
    }

    private static void printUnifyResponse(UnifyResponse response) {

        if (response == null) {
            System.err.println("Response is null.");
            return;
        }

        System.out.println("Status: " + response.getStatus());

        if (response.isSuccess()) {
            if (response.getData() != null) {
                if (response.getData().getSubmission() != null) {
                    System.out.println("Submission ID: "
                            + response.getData().getSubmission().getSubmissionId());
                }
                if (response.getData().getDocument() != null) {
                    System.out.println("Document ID: "
                            + response.getData().getDocument().getDocumentId());
                }
            }
        } else {
            System.err.println("Error: " + response.getMessage());
        }
    }
}