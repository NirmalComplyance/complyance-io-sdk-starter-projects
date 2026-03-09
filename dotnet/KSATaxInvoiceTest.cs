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
    public class KSATaxInvoiceTest : IAsyncLifetime
    {
        private const string ApiKey = "";
        private const string SourceName = "";
        private const string SourceVersion = "";
        private static readonly Country TestCountry = Country.SA;

        public async Task InitializeAsync()
        {
            var sources = new List<Source>
            {
                new Source(SourceName, SourceVersion, SourceType.FirstParty)
            };

            var config = new SDKConfig(ApiKey, Environment.Sandbox, sources);
            GETSUnifySDK.Configure(config);

            Console.WriteLine("SDK configured for KSA.");
            await Task.CompletedTask;
        }

        public Task DisposeAsync() => Task.CompletedTask;

        [Fact]
        public async Task TestKSATaxInvoiceFlow()
        {
            Console.WriteLine("\n🚀 KSA TAX INVOICE TEST STARTED\n");

            var payload = CreateKSATestPayload();

            try
            {
                var response = await GETSUnifySDK.PushToUnifyAsync(
                    SourceName,
                    SourceVersion,
                    LogicalDocType.TaxInvoice,
                    TestCountry,
                    Operation.Single,
                    Mode.Documents,
                    Purpose.Invoicing,
                    payload);

                Assert.NotNull(response);

                Console.WriteLine($"Status : {response.Status}");

                if (response.IsSuccess)
                {
                    Console.WriteLine("✅ KSA Invoice SUCCESS");

                    if (response.Data?.Submission?.SubmissionId != null)
                        Console.WriteLine($"Submission ID : {response.Data.Submission.SubmissionId}");
                }
                else if (response.Error != null)
                {
                    Console.WriteLine("❌ FAILED");
                    Console.WriteLine($"Code : {response.Error.Code}");
                    Console.WriteLine($"Message : {response.Error.Message}");
                }
            }
            catch (SDKException ex)
            {
                Console.WriteLine("❌ SDK Exception:");
                Console.WriteLine(ex.Message);

                if (ex.ErrorDetail != null)
                {
                    Console.WriteLine($"Code : {ex.ErrorDetail.Code}");
                    Console.WriteLine($"Hint : {ex.ErrorDetail.Suggestion}");
                }

                throw;
            }
        }

        /* ============================================================
           KSA PAYLOAD (Converted from your Java version)
        ============================================================ */

        private static Dictionary<string, object> CreateKSATestPayload()
        {
            var invoiceNumber = GenerateInvoiceNumber();

            var payload = new Dictionary<string, object>();

            /* ---------------- Invoice Data ---------------- */

            var invoiceData = new Dictionary<string, object>
            {
                ["invoice_number"] = invoiceNumber,
                ["document_type"] = "tax_invoice",
                ["invoice_date"] = "2026-02-26",
                ["invoice_time"] = "14:30:00.000Z",
                ["currency_code"] = "SAR",
                ["vat_currency_code"] = "SAR",
                ["exchange_percentage"] = 1,
                ["invoice_startDate"] = "2026-02-26",
                ["invoice_endDate"] = "2026-02-26",
                ["invoice_due_date"] = "2026-02-26",
                ["line_extension_amount"] = 20000.00,
                ["tax_exclusive_amount"] = 20000.00,
                ["total_tax_amount"] = 3000.00,
                ["total_amount"] = 23000.00,
                ["total_payable_amount"] = 23000.00,
                ["paid_Amount"] = 0,
                ["due_Amount"] = 0,


                ["total_discount"] = 0,
                ["PaymentMethod"] = "CASH"
            };

            payload["invoice_data"] = invoiceData;

            /* ---------------- Seller Info ---------------- */

            payload["seller_info"] = new Dictionary<string, object>
            {
                ["company_name"] = "Advanced Tech Solutions LLC",
                ["vat_registration"] = "310123456700003",
                ["tax_scheme"] = "VAT",
                ["street_address"] = "King Fahd Road",
                ["building_number"] = "1234",
                ["district_name"] = "Al Olaya",
                ["city_name"] = "Riyadh",
                ["state_name"] = "Riyadh Province",
                ["postal_code"] = "11564",
                ["country_code"] = "SA",
                ["seller_id"] = "2034567890",
                ["phone"] = "+966501234567",
                ["email"] = "contact@advancedtech.sa",
                ["contact_name"] = "Ahmed Al-Rashid",
                ["Crn_number"] = "2034567890",
                ["Additional_Type"] = "CRN"
            };

            /* ---------------- Buyer Info ---------------- */

            payload["buyer_info"] = new Dictionary<string, object>
            {
                ["buyer_name"] = "Global Manufacturing Co.",
                ["buyer_vat"] = "310987654300003",
                ["buyer_tax_scheme"] = "VAT",
                ["buyer_address"] = "Industrial City",
                ["buyer_building"] = "4567",
                ["buyer_district"] = "Industrial Area",
                ["buyer_city"] = "Dammam",
                ["buyer_state"] = "Damman Province",
                ["buyer_postal"] = "31461",
                ["buyer_country"] = "SA",
                ["buyer_id"] = "2034567890",
                ["crn_Number"] = "2034567890",
                ["Additional_Type"] = "CRN"
            };

            /* ---------------- Line Items ---------------- */

            payload["line_items"] = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    ["item_id"] = "ITEM001",
                    ["item_name"] = "Industrial Server System",
                    ["quantity"] = 2,
                    ["unit_code"] = "PCE",
                    ["unit_price"] = 8500.00,
                    ["tax_amount"] = 2550.00,
                    ["tax_category"] = "S",
                    ["tax_rate"] = 15,
                    ["discount_amount"] = 0,
                    ["sub_Total"] = 19550,
                    ["taxable_amount"] = 17000
                },
                new Dictionary<string, object>
                {
                    ["item_id"] = "ITEM002",
                    ["item_name"] = "Network Security Module",
                    ["quantity"] = 1,
                    ["unit_code"] = "PCE",
                    ["unit_price"] = 3000.00,
                    ["tax_amount"] = 450.00,
                    ["tax_category"] = "S",
                    ["tax_rate"] = 15,
                    ["discount_amount"] = 0,
                    ["sub_Total"] = 3450,
                    ["taxable_amount"] = 3000
                }
            };

            /* ---------------- Extensions (sa_prepayment) ---------------- */

            payload["extensions"] = new Dictionary<string, object>
            {
                ["sa_prepayment"] = new List<Dictionary<string, object>>
                {
                    new Dictionary<string, object>
                    {
                        ["paymentId"] = "PP-2024-001",
                        ["issueDate"] = DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ"),
                        ["documentType"] = "tax_invoice_prepayment_invoice",
                        ["vatCategory"] = "S",
                        ["vatRate"] = 15.0,
                        ["taxableAmount"] = 1000.00,
                        ["taxAmount"] = 150.00,
                        ["adjustmentAmount"] = 1150.00
                    }
                }
            };

            /* ---------------- Destinations ---------------- */

            payload["destinations"] = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    ["type"] = "tax_authority",
                    ["details"] = new Dictionary<string, object>
                    {
                        ["authority"] = "ZATCA",
                        ["country"] = "SA",
                        ["document_type"] = "tax_invoice"
                    }
                }
            };

            /* ---------------- Additional Data ---------------- */

            // payload["additional_data"] = new Dictionary<string, object>
            // {
            //     ["order_reference"] = "PO-2024-5678",
            //     ["delivery_date"] = DateTime.UtcNow.ToString("yyyy-MM-dd"),
            //     ["source_system"] = "ksa-source-system"
            // };

            return payload;
        }

        private static string GenerateInvoiceNumber()
            => "KSA-INV-" + DateTime.UtcNow.ToString("yyyyMMddHHmmssfff");
    }
}