using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Complyance.SDK;
using Complyance.SDK.Exceptions;
using Complyance.SDK.Models;
using Xunit;
using Environment = Complyance.SDK.Models.Environment;

namespace Complyance.SDK.TestProject
{
    public class UAECreditNoteTest : IAsyncLifetime
    {
        private const string ApiKey = "";
        private const string SourceName = "";
        private const string SourceVersion = "";
        private static readonly Country TestCountry = Country.AE;

        public async Task InitializeAsync()
        {
            var sources = new List<Source>
            {
                new Source(SourceName, SourceVersion, SourceType.FirstParty)
            };

            var config = new SDKConfig(ApiKey, Environment.Sandbox, sources);
            GETSUnifySDK.Configure(config);

            Console.WriteLine("SDK configured.");
            await Task.CompletedTask;
        }

        public Task DisposeAsync() => Task.CompletedTask;

        [Fact]
public async Task TestUAETaxInvoiceFlow()
{
    Console.WriteLine("\n==============================================");
    Console.WriteLine("🚀 UAE TAX INVOICE TEST STARTED");
    Console.WriteLine("==============================================");
    Console.WriteLine($"Source       : {SourceName} v{SourceVersion}");
    Console.WriteLine($"Logical Type : {LogicalDocType.TaxInvoice}");
    Console.WriteLine($"Country      : {TestCountry}");
    Console.WriteLine($"Operation    : {Operation.Single}");
    Console.WriteLine($"Mode         : {Mode.Documents}");
    Console.WriteLine($"Purpose      : {Purpose.Invoicing}");
    Console.WriteLine("----------------------------------------------");

    var payload = CreateUAETestPayload();
    Console.WriteLine("📦 Payload created successfully.");

    try
    {
        var response = await GETSUnifySDK.PushToUnifyAsync(
            SourceName,
            SourceVersion,
            LogicalDocType.TaxInvoiceCreditNote,
            TestCountry,
            Operation.Single,
            Mode.Documents,
            Purpose.Invoicing,
            payload);

        Assert.NotNull(response);

        Console.WriteLine("\n📊 RESPONSE RECEIVED");
        Console.WriteLine("----------------------------------------------");
        Console.WriteLine($"Status : {response.Status}");

        if (response.IsSuccess)
        {
            Console.WriteLine("✅ Invoice submission SUCCESS");

            if (response.Data?.Submission?.SubmissionId != null)
                Console.WriteLine($"Submission ID : {response.Data.Submission.SubmissionId}");

            if (response.Data?.Document?.DocumentId != null)
                Console.WriteLine($"Document ID   : {response.Data.Document.DocumentId}");
        }
        else if (response.Error != null)
        {
            Console.WriteLine("❌ Invoice submission FAILED");
            Console.WriteLine($"Error Code    : {response.Error.Code}");
            Console.WriteLine($"Error Message : {response.Error.Message}");

            if (!string.IsNullOrWhiteSpace(response.Error.Suggestion))
                Console.WriteLine($"Suggestion    : {response.Error.Suggestion}");
        }
        else
        {
            Console.WriteLine("⚠️ Unknown response state.");
            Console.WriteLine($"Message : {response.Message}");
        }

    }
    catch (SDKException ex)
    {
        Console.WriteLine("\n❌ SDK EXCEPTION OCCURRED");
        Console.WriteLine("----------------------------------------------");
        Console.WriteLine($"Message : {ex.Message}");

        if (ex.ErrorDetail != null)
        {
            Console.WriteLine($"Code    : {ex.ErrorDetail.Code}");
            Console.WriteLine($"Detail  : {ex.ErrorDetail.Message}");
            Console.WriteLine($"Hint    : {ex.ErrorDetail.Suggestion}");
        }

        Console.WriteLine("==============================================\n");
        throw;
    }
}

        private static string GenerateInvoiceNumber()
            => "UAE-INV-" + DateTime.UtcNow.ToString("yyyyMMddHHmmssfff");

        private static string GenerateUniqueIdentifier()
            => Guid.NewGuid().ToString();

        private static string GetDynamicDate(int daysOffset)
            => DateTime.UtcNow.AddDays(daysOffset).ToString("yyyy-MM-dd");

        private static string GetDateFromEnv(string envVarName, int defaultOffset)
        {
            var envValue = System.Environment.GetEnvironmentVariable(envVarName);
            if (!string.IsNullOrWhiteSpace(envValue) && int.TryParse(envValue, out var offset))
            {
                return GetDynamicDate(offset);
            }
            return GetDynamicDate(defaultOffset);
        }

        private static Dictionary<string, object> CreateUAETestPayload()
        {
            var invoiceNumber = GenerateInvoiceNumber();
            var uniqueId = GenerateUniqueIdentifier();

            var payload = new Dictionary<string, object>();

            var invoiceData = new Dictionary<string, object>
            {
                ["document_number"] = invoiceNumber,
                ["document_id"] = uniqueId,
                ["invoice_date"] = GetDateFromEnv("INVOICE_DATE", 0),
                ["invoice_time"] = "14:30:00Z",
                ["currency_code"] = "AED",
                ["tax_currency_code"] = "AED",
                ["due_date"] = GetDateFromEnv("INVOICE_DUE_DATE", 30),
                ["period_start_date"] = GetDateFromEnv("INVOICE_START_DATE", -30),
                ["period_end_date"] = GetDateFromEnv("INVOICE_END_DATE", 0),
                ["period_frequency"] = "MONTHLY",
                ["exchange_rate"] = 1.0,
                ["line_extension_amount"] = 10000.00,
                ["tax_exclusive_amount"] = 10000.00,
                ["total_tax_amount"] = 500.00,
                ["total_amount"] = 10500.00,
                ["total_allowances"] = 0.00,
                ["total_charges"] = 0.00,
                ["prepaid_amount"] = 0.00,
                ["amount_due"] = 10500.00,
                ["rounding_amount"] = 0.00,
                ["original_reference_id"] = "UAE-INV-ORIG-001",
                ["credit_note_reason"] = "Goods returned"
            };
            payload["invoice_data"] = invoiceData;

            var sellerInfo = new Dictionary<string, object>
            {
                ["seller_name"] = "ABC Trading LLC",
                ["seller_trade_name"] = "ABC Trading",
                ["seller_party_id"] = "SELLER-UAE-001",
                ["vat_number_type"] = "TRN",
                ["vat_number"] = "100819867100003",
                ["tax_scheme"] = "VAT",
                ["registration_number"] = "CN-1234567",
                ["registration_type"] = "TL",
                ["registration_scheme"] = "AE:TL",
                ["authority_name"] = "Dubai Department of Economic Development",
                ["peppol_id"] = "0235:1189748191",
                ["seller_email"] = "contact@abctrading.ae",
                ["seller_phone"] = "+971-4-1234567",
                ["seller_contact_name"] = "Ahmed Al Maktoum",
                ["street_name"] = "Sheikh Zayed Road",
                ["additional_address"] = "Building 123",
                ["building_number"] = "123",
                ["city_name"] = "Dubai",
                ["state_province"] = "DUBAI",
                ["postal_code"] = "00000",
                ["country_code"] = "AE"
            };
            payload["seller_info"] = sellerInfo;

            var buyerInfo = new Dictionary<string, object>
            {
                ["buyer_name"] = "XYZ Corporation LLC",
                ["buyer_trade_name"] = "XYZ Corp",
                ["buyer_party_id"] = "BUYER-UAE-001",
                ["buyer_vat_type"] = "TRN",
                ["buyer_vat_number"] = "100889867100003",
                ["buyer_tax_scheme"] = "VAT",
                ["buyer_registration_number"] = "CN-9876543",
                ["buyer_registration_type"] = "TL",
                ["buyer_registration_scheme"] = "TL",
                ["buyer_authority_name"] = "Abu Dhabi Department of Economic Development",
                ["buyer_peppol_id"] = "0235:1297201011",
                ["buyer_email"] = "purchasing@xyzcorp.ae",
                ["buyer_phone"] = "+971-2-9876543",
                ["buyer_contact_name"] = "Fatima Al Mansouri",
                ["buyer_street_name"] = "Al Wasl Road",
                ["buyer_additional_address"] = "Tower 2",
                ["buyer_building_number"] = "456",
                ["buyer_city"] = "Dubai",
                ["buyer_state_province"] = "DUBAI",
                ["buyer_postal_code"] = "00000",
                ["buyer_country"] = "AE"
            };
            payload["buyer_info"] = buyerInfo;

            var lineItems = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    ["line_id"] = "1",
                    ["item_name"] = "Office Equipment",
                    ["item_description"] = "Professional office equipment package",
                    ["quantity"] = 10.0,
                    ["unit_code"] = "EA",
                    ["unit_price"] = 500.00,
                    ["net_price"] = 500.00,
                    ["gross_price"] = 500.00,
                    ["line_taxable_value"] = 5000.00,
                    ["tax_category"] = "S",
                    ["tax_rate"] = 5.0,
                    ["tax_amount"] = 250.00,
                    ["line_total"] = 5250.00,
                    ["item_type"] = "GOODS",
                    ["country_of_origin"] = "AE",
                    ["classification_code"] = "8471",
                    ["classification_scheme"] = "HS",
                    ["seller_item_code"] = "SKU-001",
                    ["buyer_item_code"] = "BUYER-SKU-001",
                    ["batch_number"] = "BATCH-2024-001"
                }
            };
            payload["line_items"] = lineItems;

            var uaeExtensions = new Dictionary<string, object>
            {
                ["unique_identifier"] = uniqueId,
                ["invoiced_object_id"] = "OBJECT-2024-001",
                ["taxpoint_date"] = GetDateFromEnv("INVOICE_DATE", 0),
                ["total_amount_including_tax_in_aed"] = 10500.00,
                ["authority_name"] = "Dubai Department of Economic Development",
                ["buyer_authority_name"] = "Abu Dhabi Department of Economic Development",
                ["business_process_type"] = "urn:peppol:bis:billing",
                ["specification_identifier"] = "urn:peppol:pint:ae:invoice:v1"
            };
            payload["uae_extensions"] = uaeExtensions;

            var paymentInfo = new Dictionary<string, object>
            {
                ["payment_id"] = "PAY-001",
                ["payment_means_code"] = "CREDIT",
                ["payment_means_text"] = "Bank Transfer",
                ["remittance_info"] = "Payment for Invoice " + invoiceNumber,
                ["account_id"] = "AE123456789012345678901",
                ["account_name"] = "ABC Trading LLC",
                ["bank_id"] = "AEBN0001"
            };
            payload["payment_info"] = paymentInfo;

            var paymentTerms = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    ["instructions_id"] = "TERMS-001",
                    ["note"] = "Net 30 days",
                    ["amount"] = 10500.00,
                    ["due_date"] = GetDateFromEnv("INVOICE_DUE_DATE", 30)
                }
            };
            payload["payment_terms"] = paymentTerms;

            var supportingDocuments = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    ["type"] = "purchaseOrderReference",
                    ["id"] = "PO-2024-001234"
                }
            };
            payload["supporting_documents"] = supportingDocuments;

            payload["additional_data"] = new Dictionary<string, object>
            {
                ["delivery_date"] = GetDateFromEnv("INVOICE_DATE", 0),
                ["order_reference"] = "PO-2024-001234",
                ["source_system"] = "uae-source-system"
            };

            return payload;
        }
    }
}
 