using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Tests.Events
{
    [TestFixture]
    [Category("UnitTest")]
    public class SchemaCheckTests
    {
        [Test]
        [TestCase("querydoc_fail_schemacheck.xml", false)]
        public void EPCISQueryDocument_XML_1_2(string file, bool pass)
        {
            // read object events from test data specified in the file argument
            string xmlObjectEvents = OpenTraceabilityTests.ReadTestData(file);

            XDocument xDoc = XDocument.Parse(xmlObjectEvents);
            try
            {
                EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, Models.Events.EPCISVersion.V1);
                Assert.That(pass, Is.True);
            }
            catch (Exception)
            {
                Assert.That(pass, Is.False);
            }
        }

        [Test]
        public async Task GDST_JSON_Schema_Validation()
        {
            // read GDST test data
            string jsonData = OpenTraceabilityTests.ReadTestData("gdst_data_withmasterdata.jsonld");

            // validate against GDST schema
            List<string> errors = await JsonSchemaChecker.IsValidAsync(jsonData, "GDST");

            // assert that there are no validation errors
            if (errors.Count > 0)
            {
                string errorMessage = string.Join("\n", errors);
                Assert.Fail($"GDST JSON schema validation failed with {errors.Count} errors:\n{errorMessage}");
            }

            Assert.That(errors.Count, Is.EqualTo(0), "GDST JSON should be valid against GDST schema");
        }

        [Test]
        public async Task GDST_Data_Against_Base_EPCIS_Schema_Validation()
        {
            // read GDST test data
            string jsonData = OpenTraceabilityTests.ReadTestData("gdst_data_withmasterdata.jsonld");

            // validate against base EPCIS schema
            List<string> errors = await JsonSchemaChecker.IsValidAsync(jsonData, "EPCIS_BASE");

            // output results for comparison
            if (errors.Count > 0)
            {
                string errorMessage = string.Join("\n", errors);
                Console.WriteLine($"Base EPCIS validation failed with {errors.Count} errors:\n{errorMessage}");
                Assert.Fail($"GDST data should be valid against base EPCIS schema. Errors:\n{errorMessage}");
            }

            Assert.That(errors.Count, Is.EqualTo(0), "GDST JSON should be valid against base EPCIS schema");
        }

        /// <summary>
        /// Each case carries the property the fixture breaks. Asserting that the property is named in
        /// the output, rather than only counting errors, is what stops the validator from regressing
        /// to messages that report a failure without saying where it is.
        /// </summary>
        [Test]
        [TestCase("gdst_hatching_event_invalid.jsonld", "broodstockSource", "Invalid broodstockSource enum value should fail")]
        [TestCase("gdst_fishing_event_invalid.jsonld", "productionMethodForFishAndSeafoodCode", "Invalid production method, and missing vessel information, should fail")]
        [TestCase("gdst_farm_harvest_event_invalid.jsonld", "productionMethodForFishAndSeafoodCode", "Invalid production method should fail")]
        [TestCase("gdst_feedmill_object_event_invalid.jsonld", "proteinSource", "Missing gdst:proteinSource should fail")]
        [TestCase("gdst_processing_event_invalid.jsonld", "lotNumber", "Missing required lotNumber should fail")]
        public async Task GDST_Invalid_Events_Should_Fail_Validation(string testFile, string expectedProperty, string expectedFailureReason)
        {
            await AssertValidationNamesProperty(testFile, expectedProperty, expectedFailureReason);
        }

        [Test]
        [TestCase("gdst_hatching_missing_broodstock.jsonld", "broodstockSource", "Hatching event missing gdst:broodstockSource should fail")]
        [TestCase("gdst_event_missing_product_owner.jsonld", "productOwner", "GDST event missing gdst:productOwner should fail")]
        [TestCase("gdst_event_missing_information_provider.jsonld", "informationProvider", "GDST event missing cbvmda:informationProvider should fail")]
        [TestCase("gdst_event_invalid_human_welfare_policy.jsonld", "humanWelfarePolicy", "GDST event with invalid humanWelfarePolicy should fail")]
        [TestCase("gdst_certification_invalid_type.jsonld", "certificationType", "GDST event with invalid certification type pattern should fail")]
        [TestCase("gdst_vessel_invalid_imo.jsonld", "imoNumber", "Fishing event with invalid IMO number pattern should fail")]
        [TestCase("gdst_vessel_invalid_country_code.jsonld", "vesselFlagState", "Fishing event with invalid country code should fail")]
        [TestCase("gdst_farm_harvest_missing_aquaculture_method.jsonld", "aquacultureMethod", "Farm harvest event missing gdst:aquacultureMethod should fail")]
        [TestCase("gdst_feedmill_missing_protein_source.jsonld", "proteinSource", "Feedmill event missing gdst:proteinSource should fail")]
        [TestCase("gdst_processing_missing_lot_number.jsonld", "lotNumber", "Processing event missing cbvmda:lotNumber should fail")]
        public async Task GDST_Required_Properties_Validation(string testFile, string expectedProperty, string expectedFailureReason)
        {
            await AssertValidationNamesProperty(testFile, expectedProperty, expectedFailureReason);
        }

        private static async Task AssertValidationNamesProperty(
            string testFile,
            string expectedProperty,
            string expectedFailureReason)
        {
            string jsonData = OpenTraceabilityTests.ReadTestData(testFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(jsonData, "GDST");
            string rendered = string.Join("\n", errors);

            Console.WriteLine($"{testFile} - {expectedFailureReason}");
            Console.WriteLine(rendered);

            Assert.Multiple(() =>
            {
                Assert.That(
                    errors,
                    Is.Not.Empty,
                    $"Invalid GDST event should fail validation: {expectedFailureReason}");

                // A property name in an instance location, or inside a "required" message, is what
                // tells the reader which element to go and fix.
                Assert.That(
                    rendered,
                    Does.Contain(expectedProperty),
                    $"Validation output should name '{expectedProperty}' so the reader knows where the failure is.\n{rendered}");

                // Every entry either names an element or is the truncation notice.
                Assert.That(
                    errors.All(e => e.StartsWith("/") || e.StartsWith("...")),
                    Is.True,
                    $"Every entry should start with the location of the offending element.\n{rendered}");
            });
        }
    }
}