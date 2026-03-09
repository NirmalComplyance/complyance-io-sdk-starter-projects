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
    public class KSATaxInvoiceJsonUploadTest : IAsyncLifetime
    {
        private const string ApiKey = "";
        private const string SourceName = "";
        private const string SourceVersion = "";
        private static readonly Country TestCountry = Country.SA;

        private static readonly string PayloadTemplate = string.Join("\n", new[]
        {
            "{",
            "  \"invoice_data\": {",
            "    \"invoice_number\": \"{{AUTO_KSA_INVOICE_NUMBER}}\",",
            "    \"invoice_date\": \"2026-02-26\",",
            "    \"invoice_time\": \"14:30:00.000Z\",",
            "    \"document_type\": \"tax_invoice\",",
            "    \"currency_code\": \"SAR\",",
            "    \"vat_currency_code\": \"SAR\",",
            "    \"exchange_percentage\": 1,",
            "    \"invoice_startDate\": \"2026-02-26\",",
            "    \"invoice_endDate\": \"2026-02-28\",",
            "    \"invoice_due_date\": \"2026-03-28\",",
            "    \"line_extension_amount\": 20000.0,",
            "    \"tax_exclusive_amount\": 20000.0,",
            "    \"total_tax_amount\": 3000.0,",
            "    \"total_amount\": 23000.0,",
            "    \"total_payable_amount\": 23000.0,",
            "    \"paid_Amount\": 0,",
            "    \"total_discount\": 0,",
            "    \"PaymentMethod\": \"CASH\"",
            "  },",
            "  \"seller_info\": {",
            "    \"company_name\": \"Advanced Tech Solutions LLC\",",
            "    \"vat_registration\": \"310123456700003\",",
            "    \"tax_scheme\": \"VAT\",",
            "    \"street_address\": \"King Fahd Road\",",
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
            "        \"issueDate\": \"2026-02-26T18:30:00Z\",",
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
            "  ]",
            "}"
        });

        public async Task InitializeAsync()
        {
            var sources = new List<Source>
            {
                new Source(SourceName, SourceVersion, SourceType.FirstParty)
            };

            var config = new SDKConfig(ApiKey, Environment.Sandbox, sources);
            GETSUnifySDK.Configure(config);
            await Task.CompletedTask;
        }

        public Task DisposeAsync() => Task.CompletedTask;

        [Fact]
        public async Task TestKSATaxInvoiceJsonUploadFlow()
        {
            var payloadJson = PayloadTemplate.Replace("{{AUTO_KSA_INVOICE_NUMBER}}", GenerateInvoiceNumber());

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
                    payloadJson);

                Assert.NotNull(response);
                Console.WriteLine($"Status : {response.Status}");
            }
            catch (SDKException ex)
            {
                Console.WriteLine(ex.Message);
                throw;
            }
        }

        private static string GenerateInvoiceNumber()
            => "KSA-INV-" + DateTime.UtcNow.ToString("yyyyMMddHHmmssfff");
    }
}
