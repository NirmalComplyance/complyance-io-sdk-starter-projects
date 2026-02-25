using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Complyance.SDK;
using Complyance.SDK.Exceptions;
using Complyance.SDK.Models;
using Xunit;
using Environment = Complyance.SDK.Models.Environment;

namespace Complyance.SDK.TestProject
{
    /// <summary>
    /// UAE Tax Invoice Test - Demonstrates UAE-specific TAX_INVOICE functionality.
    /// Mirrors the Java test-project class UAETaxInvoiceTest.java.
    /// </summary>
    /// <remarks>
    /// This test class demonstrates:
    /// - UAE (AE) country-specific compliance
    /// - TAX_INVOICE logical document type mapping
    /// - B2B transaction processing with Peppol PINT network
    /// - UAE-specific field mappings (TRN, Emirates, VAT, extensions)
    /// - Comprehensive payload with UAE invoice data
    /// - UAE PINT AE format compliance
    /// - FTA (Federal Tax Authority) compliance
    /// </remarks>
    public class UAETaxInvoiceTest : IClassFixture<UAETaxInvoiceTest.UAETestFixture>
    {
        private const string ApiKey = "ak_759bbdaf715791563af0548c96d3";
        private const string SourceName = "uae_test_source";
        private const string SourceVersion = "1.0";
        private static readonly Country TestCountry = Country.AE;

        private readonly UAETestFixture _fixture;

        public UAETaxInvoiceTest(UAETestFixture fixture)
        {
            _fixture = fixture;
        }

        [Fact]
        public async Task TestUAETaxInvoiceFlow()
        {
            var payload = CreateUAETestPayload();

            Console.WriteLine("✅ UAE test payload created");
            Console.WriteLine("   📊 Expected GETS fields coverage: 37/37 (100%)");
            Console.WriteLine("   🇦🇪 Expected AE country fields coverage: 15/15 (100%)");
            Console.WriteLine("   💰 Expected AE compliance fields coverage: 12/12 (100%)");

            Console.WriteLine("\n=== 🚀 Testing UAE Tax Invoice Flow ===");
            Console.WriteLine("   🎯 Testing: TAX_INVOICE with UAE");
            Console.WriteLine("   📋 Logical Type: TAX_INVOICE");
            Console.WriteLine("   🇦🇪 Country: AE");
            Console.WriteLine("   🔄 Expected: B2B tax invoice processing with UAE compliance");
            Console.WriteLine("   🌐 Expected: Peppol PINT AE format");
            Console.WriteLine("   🏛️ Expected: FTA submission ready");

            var response = await GETSUnifySDK.PushToUnifyAsync(
                SourceName,
                SourceVersion,
                LogicalDocType.TaxInvoice,
                TestCountry,
                Operation.Single,
                Mode.Documents,
                Purpose.Mapping,
                payload);

            Assert.NotNull(response);
            Assert.NotNull(response.Status);

            PrintUnifyResponse(response, "UAE Tax Invoice Flow");

            if ("success".Equals(response.Status, StringComparison.OrdinalIgnoreCase) || response.IsSuccess)
            {
                Console.WriteLine("   ✅ UAE invoice submitted successfully");
                Console.WriteLine("   📋 Expected: Document created in integration-engine");
                Console.WriteLine("   🔄 Expected: Payload available for mapping");
                Console.WriteLine("   🌐 Expected: Peppol PINT AE format generated");
                Console.WriteLine("   🏛️ Expected: FTA submission ready");

                if (response.Data?.Submission != null && !string.IsNullOrEmpty(response.Data.Submission.SubmissionId))
                {
                    Console.WriteLine($"   Submission ID: {response.Data.Submission.SubmissionId}");
                }
                if (response.Data?.Document != null && !string.IsNullOrEmpty(response.Data.Document.DocumentId))
                {
                    Console.WriteLine($"   Document ID: {response.Data.Document.DocumentId}");
                }
            }
            else if ("error".Equals(response.Status, StringComparison.OrdinalIgnoreCase) && response.Error != null)
            {
                Console.WriteLine($"⚠️  Test returned error: {response.Error.Code} - {response.Error.Message}");
                _fixture.Reporter.RecordFailure("TC-UAE-TAX-INVOICE-001",
                    $"Status=error, code={response.Error.Code}, message={response.Error.Message}");
            }
        }

        [Fact]
        // public async Task TestUAECreditNoteFlow()
        // {
        //     var payload = CreateUAETestPayload();
        //     var invoiceData = (Dictionary<string, object>)payload["invoice_data"];
        //     invoiceData["document_type"] = "credit_note";
        //     invoiceData["original_reference_id"] = "UAE-INV-ORIG-001";
        //     invoiceData["credit_note_reason"] = "Goods returned";

        //     var response = await GETSUnifySDK.PushToUnifyAsync(
        //         SourceName,
        //         SourceVersion,
        //         LogicalDocType.TaxInvoiceCreditNote,
        //         TestCountry,
        //         Operation.Single,
        //         Mode.Documents,
        //         Purpose.Invoicing,
        //         payload);

        //     Assert.NotNull(response);
        //     PrintUnifyResponse(response, "UAE Credit Note Flow");
        // }

        private static string GenerateInvoiceNumber()
        {
            return "UAE-INV-" + DateTime.UtcNow.ToString("yyyyMMddHHmmssfff");
        }

        private static string GenerateUniqueIdentifier()
        {
            return Guid.NewGuid().ToString();
        }

        private static string GetDynamicDate(int daysOffset)
        {
            return DateTime.UtcNow.AddDays(daysOffset).ToString("yyyy-MM-dd");
        }

        private static string GetDateFromEnv(string envVarName, int defaultOffset)
        {
            var envValue = System.Environment.GetEnvironmentVariable(envVarName);
            if (!string.IsNullOrWhiteSpace(envValue) && int.TryParse(envValue, out var offset))
            {
                return GetDynamicDate(offset);
            }
            return GetDynamicDate(defaultOffset);
        }

        /// <summary>
        /// Creates UAE test payload with UAE-specific fields based on UAE GETS mapping.
        /// Field names align with Java UAETaxInvoiceTest.createUAETestPayload().
        /// </summary>
        private static Dictionary<string, object> CreateUAETestPayload()
        {
            var invoiceNumber = GenerateInvoiceNumber();
            var uniqueId = GenerateUniqueIdentifier();

            var payload = new Dictionary<string, object>();

            // Invoice Data
            var invoiceData = new Dictionary<string, object>
            {
                ["document_number"] = invoiceNumber,
                ["document_id"] = uniqueId,
                ["document_type"] = "tax_invoice",
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

            // Seller Info (UAE)
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
                ["peppol_id"] = "0235:1008198671",
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

            // Buyer Info (UAE)
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
                ["buyer_peppol_id"] = "0235:8523824416",
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

            // Line Items
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

            // UAE Extensions
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

            // Payment Information
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

            // Payment Terms
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

            // Supporting Documents
            var supportingDocuments = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    ["type"] = "purchaseOrderReference",
                    ["id"] = "PO-2024-001234"
                }
            };
            payload["supporting_documents"] = supportingDocuments;

            // Additional Data
            payload["additional_data"] = new Dictionary<string, object>
            {
                ["delivery_date"] = GetDateFromEnv("INVOICE_DATE", 0),
                ["order_reference"] = "PO-2024-001234",
                ["source_system"] = "uae-source-system"
            };

            return payload;
        }

        private static void PrintUnifyResponse(UnifyResponse response, string context)
        {
            if (response == null)
            {
                Console.WriteLine($"❌ {context} failed: Response is null");
                return;
            }

            Console.WriteLine("\n📊 Response Details:");
            Console.WriteLine($"   Status: {response.Status}");

            if ("error".Equals(response.Status, StringComparison.OrdinalIgnoreCase) && response.Error != null)
            {
                Console.WriteLine($"❌ {context} failed:");
                Console.WriteLine($"   Error Code: {response.Error.Code}");
                Console.WriteLine($"   Message:    {response.Error.Message}");
                if (!string.IsNullOrEmpty(response.Error.Suggestion))
                {
                    Console.WriteLine($"   Suggestion: {response.Error.Suggestion}");
                }
            }
            else if ("success".Equals(response.Status, StringComparison.OrdinalIgnoreCase) || response.IsSuccess)
            {
                Console.WriteLine($"✅ {context} Response: {response.Status}");
            }
            else
            {
                Console.WriteLine($"📋 {context} Response: {response.Status}");
                Console.WriteLine($"   Message: {response.Message ?? "No message"}");
            }
        }

        public class UAETestFixture : IAsyncLifetime
        {
            public TestResultReporter Reporter { get; } = new TestResultReporter("UAETaxInvoiceTest");

            public Task InitializeAsync()
            {
                var sources = new List<Source>
                {
                    new Source(SourceName, SourceVersion, SourceType.FirstParty)
                };
                var config = new SDKConfig(ApiKey, Environment.Sandbox, sources);
                GETSUnifySDK.Configure(config);
                Reporter.MarkRunStarted();
                Console.WriteLine("✅ SDK configured for UAE testing");
                return Task.CompletedTask;
            }

            public Task DisposeAsync() => Task.CompletedTask;
        }

        public class TestResultReporter
        {
            private const string FileTimestampFormat = "yyyyMMdd-HHmmss";
            private readonly string _suiteName;
            private readonly string _reportPath;
            private int _passCount;
            private int _failCount;
            private bool _summaryWritten;
            private bool _runStartLogged;
            private readonly object _writeLock = new object();

            public TestResultReporter(string suiteName)
            {
                _suiteName = suiteName;
                var timestamp = DateTime.UtcNow.ToString(FileTimestampFormat);
                var testOutputDir = Path.Combine(Directory.GetCurrentDirectory(), "test-output");
                Directory.CreateDirectory(testOutputDir);
                _reportPath = Path.Combine(testOutputDir, $"{suiteName}-{timestamp}.log");
            }

            public void MarkRunStarted()
            {
                if (_runStartLogged) return;
                _runStartLogged = true;
                WriteLine($"RUN_START|{_suiteName}|{DateTime.UtcNow:O}");
            }

            public void RecordSuccess(string testId, string details)
            {
                Interlocked.Increment(ref _passCount);
                WriteLine($"PASS|{Sanitize(testId)}|{DateTime.UtcNow:O}|{Sanitize(details)}");
            }

            public void RecordFailure(string testId, string details)
            {
                Interlocked.Increment(ref _failCount);
                WriteLine($"FAIL|{Sanitize(testId)}|{DateTime.UtcNow:O}|{Sanitize(details)}");
            }

            public void WriteSummary()
            {
                if (_summaryWritten) return;
                _summaryWritten = true;
                WriteLine($"SUMMARY|{_suiteName}|{DateTime.UtcNow:O}|pass={_passCount}|fail={_failCount}");
            }

            private void WriteLine(string line)
            {
                lock (_writeLock)
                {
                    try
                    {
                        File.AppendAllText(_reportPath, line + System.Environment.NewLine, Encoding.UTF8);
                    }
                    catch (Exception ex)
                    {
                        Console.Error.WriteLine($"⚠️  Unable to write test report line: {ex.Message}");
                    }
                }
            }

            private static string Sanitize(string value)
            {
                if (string.IsNullOrEmpty(value)) return "UNKNOWN";
                return value.Replace("\r", " ").Replace("\n", " ").Trim();
            }
        }
    }
}
