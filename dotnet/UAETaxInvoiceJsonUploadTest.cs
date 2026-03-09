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
    public class UAETaxInvoiceJsonUploadTest : IAsyncLifetime
    {
        private const string ApiKey = "";
        private const string SourceName = "";
        private const string SourceVersion = "";
        private static readonly Country TestCountry = Country.AE;

        private static readonly string PayloadTemplate = string.Join("\n", new[]
        {
            "{",
            "  \"invoice_data\": {",
            "    \"document_number\": \"{{AUTO_UAE_INVOICE_NUMBER}}\",",
            "    \"document_id\": \"76f89c07-62ac-4e5f-bc55-a14fa1992e65\",",
            "    \"document_type\": \"tax_invoice\",",
            "    \"invoice_date\": \"2026-02-26\",",
            "    \"invoice_time\": \"14:30:00Z\",",
            "    \"currency_code\": \"AED\",",
            "    \"tax_currency_code\": \"AED\",",
            "    \"due_date\": \"2026-03-28\",",
            "    \"period_start_date\": \"2026-01-27\",",
            "    \"period_end_date\": \"2026-02-26\",",
            "    \"period_frequency\": \"MONTHLY\",",
            "    \"exchange_rate\": 1.0,",
            "    \"line_extension_amount\": 10000.0,",
            "    \"tax_exclusive_amount\": 10000.0,",
            "    \"total_tax_amount\": 500.0,",
            "    \"total_amount\": 10500.0,",
            "    \"total_allowances\": 0.0,",
            "    \"total_charges\": 0.0,",
            "    \"prepaid_amount\": 0.0,",
            "    \"amount_due\": 10500.0,",
            "    \"rounding_amount\": 0.0,",
            "    \"original_reference_id\": \"UAE-INV-ORIG-001\",",
            "    \"credit_note_reason\": \"Goods returned\"",
            "  },",
            "  \"seller_info\": {",
            "    \"seller_name\": \"ABC Trading LLC\",",
            "    \"seller_trade_name\": \"ABC Trading\",",
            "    \"seller_party_id\": \"SELLER-UAE-001\",",
            "    \"vat_number_type\": \"TRN\",",
            "    \"vat_number\": \"100819867100003\",",
            "    \"tax_scheme\": \"VAT\",",
            "    \"registration_number\": \"CN-1234567\",",
            "    \"registration_type\": \"TL\",",
            "    \"registration_scheme\": \"AE:TL\",",
            "    \"authority_name\": \"Dubai Department of Economic Development\",",
            "    \"peppol_id\": \"0235:1189748191\",",
            "    \"seller_email\": \"contact@abctrading.ae\",",
            "    \"seller_phone\": \"+971-4-1234567\",",
            "    \"seller_contact_name\": \"Ahmed Al Maktoum\",",
            "    \"street_name\": \"Sheikh Zayed Road\",",
            "    \"additional_address\": \"Building 123\",",
            "    \"building_number\": \"123\",",
            "    \"city_name\": \"Dubai\",",
            "    \"state_province\": \"DUBAI\",",
            "    \"postal_code\": \"00000\",",
            "    \"country_code\": \"AE\"",
            "  },",
            "  \"buyer_info\": {",
            "    \"buyer_name\": \"XYZ Corporation LLC\",",
            "    \"buyer_trade_name\": \"XYZ Corp\",",
            "    \"buyer_party_id\": \"BUYER-UAE-001\",",
            "    \"buyer_vat_type\": \"TRN\",",
            "    \"buyer_vat_number\": \"100889867100003\",",
            "    \"buyer_tax_scheme\": \"VAT\",",
            "    \"buyer_registration_number\": \"CN-9876543\",",
            "    \"buyer_registration_type\": \"TL\",",
            "    \"buyer_registration_scheme\": \"TL\",",
            "    \"buyer_authority_name\": \"Abu Dhabi Department of Economic Development\",",
            "    \"buyer_peppol_id\": \"0235:1297201011\",",
            "    \"buyer_email\": \"purchasing@xyzcorp.ae\",",
            "    \"buyer_phone\": \"+971-2-9876543\",",
            "    \"buyer_contact_name\": \"Fatima Al Mansouri\",",
            "    \"buyer_street_name\": \"Al Wasl Road\",",
            "    \"buyer_additional_address\": \"Tower 2\",",
            "    \"buyer_building_number\": \"456\",",
            "    \"buyer_city\": \"Dubai\",",
            "    \"buyer_state_province\": \"DUBAI\",",
            "    \"buyer_postal_code\": \"00000\",",
            "    \"buyer_country\": \"AE\"",
            "  },",
            "  \"line_items\": [",
            "    {",
            "      \"line_id\": \"1\",",
            "      \"item_name\": \"Office Equipment\",",
            "      \"item_description\": \"Professional office equipment package\",",
            "      \"quantity\": 10.0,",
            "      \"unit_code\": \"EA\",",
            "      \"unit_price\": 500.0,",
            "      \"net_price\": 500.0,",
            "      \"gross_price\": 500.0,",
            "      \"line_taxable_value\": 5000.0,",
            "      \"tax_category\": \"S\",",
            "      \"tax_rate\": 5.0,",
            "      \"tax_amount\": 250.0,",
            "      \"line_total\": 5250.0,",
            "      \"item_type\": \"GOODS\",",
            "      \"country_of_origin\": \"AE\",",
            "      \"classification_code\": \"8471\",",
            "      \"classification_scheme\": \"HS\",",
            "      \"seller_item_code\": \"SKU-001\",",
            "      \"buyer_item_code\": \"BUYER-SKU-001\",",
            "      \"batch_number\": \"BATCH-2024-001\"",
            "    }",
            "  ],",
            "  \"uae_extensions\": {",
            "    \"unique_identifier\": \"76f89c07-62ac-4e5f-bc55-a14fa1992e65\",",
            "    \"invoiced_object_id\": \"OBJECT-2024-001\",",
            "    \"taxpoint_date\": \"2026-02-26\",",
            "    \"total_amount_including_tax_in_aed\": 10500.0,",
            "    \"authority_name\": \"Dubai Department of Economic Development\",",
            "    \"buyer_authority_name\": \"Abu Dhabi Department of Economic Development\",",
            "    \"business_process_type\": \"urn:peppol:bis:billing\",",
            "    \"specification_identifier\": \"urn:peppol:pint:ae:invoice:v1\"",
            "  },",
            "  \"payment_info\": {",
            "    \"payment_id\": \"PAY-001\",",
            "    \"payment_means_code\": \"CREDIT\",",
            "    \"payment_means_text\": \"Bank Transfer\",",
            "    \"remittance_info\": \"Payment for Invoice UAE-INV-20260226190000000\",",
            "    \"account_id\": \"AE123456789012345678901\",",
            "    \"account_name\": \"ABC Trading LLC\",",
            "    \"bank_id\": \"AEBN0001\"",
            "  },",
            "  \"payment_terms\": [",
            "    {",
            "      \"instructions_id\": \"TERMS-001\",",
            "      \"note\": \"Net 30 days\",",
            "      \"amount\": 10500.0,",
            "      \"due_date\": \"2026-03-28\"",
            "    }",
            "  ],",
            "  \"supporting_documents\": [",
            "    {",
            "      \"type\": \"purchaseOrderReference\",",
            "      \"id\": \"PO-2024-001234\"",
            "    }",
            "  ],",
            "  \"additional_data\": {",
            "    \"delivery_date\": \"2026-02-26\",",
            "    \"order_reference\": \"PO-2024-001234\",",
            "    \"source_system\": \"uae-source-system\"",
            "  }",
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
        public async Task TestUAETaxInvoiceJsonUploadFlow()
        {
            var payloadJson = PayloadTemplate.Replace("{{AUTO_UAE_INVOICE_NUMBER}}", GenerateInvoiceNumber());

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
            => "UAE-INV-" + DateTime.UtcNow.ToString("yyyyMMddHHmmssfff");
    }
}
