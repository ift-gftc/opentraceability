using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Schema;
using System.Xml;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    public class XmlSchemaChecker
    {
        private Dictionary<string, CachedXmlSchema> _cache = new Dictionary<string, CachedXmlSchema>();

        public bool Validate(XDocument xml, string schemaURL, out string? error)
        {
            XmlReader reader = null;

            // sets the string value of the passed in parameter called error to null
            StringBuilder validationError = new StringBuilder();
            error = null;

            // initializes a bool to false
            bool bFileOk = false;

            // initializes a TextReader object
            TextReader stringReader = null;

            try
            {
                // Creates a new XmlReaderSettings object
                XmlReaderSettings settings = new XmlReaderSettings();

                // Sets the settings of the XmlReaderSettings objects to ValidationType None, ValidationFlags ReportValidationWarnings, ValidationEventHandler subscribes to the ValidationCallback 
                settings.ValidationType = ValidationType.None;
                settings.ValidationEventHandler += new ValidationEventHandler((o, args) =>
                {
                    // checks if the ValidationEventArgs has a Severity property of Warning and Logs No Validation occurred
                    if (args.Severity == XmlSeverityType.Warning)
                    {
                        validationError.AppendLine("Warning: Matching schema not found.  No validation occurred." + args.Message);
                    }
                    // else Logs  Validation error
                    else
                    {
                        validationError.AppendLine("Validation error: " + args.Message);
                    }
                });

                // and ValidationType then to Schema
                settings.ValidationType = ValidationType.Schema;

                // assigns the value of XmlSchemaSet to our XmlReaderSettings Schemas property
                settings.Schemas = GetSchema(schemaURL) ?? throw new Exception("Failed to load schema with url " + schemaURL);

                // sets bool to true
                bool bOk = true;

                // Creates a new StringReader from this.DSXML's OuterElement string property
                stringReader = new StringReader(xml.ToString());

                // Creates a XmlReader instance using the StringReader instance and our XmlReaderSettings
                reader = XmlReader.Create(stringReader, settings);

                // Reads through the XmlReader instance until no nodes are left to read
                do
                {
                    bOk = reader.Read();
                } while (bOk);

                // Closes the XmlReader instance
                reader.Close();

                // Disposes the XmlReader instance
                reader.Dispose();

                // sets XmlReader to null
                reader = null;

                // Validation complete and bool set to true
                bFileOk = true;
            }
            catch (OperationCanceledException)
            {
                throw;
            }
            catch (FileNotFoundException NotFoundException)
            {
                OTLogger.Error(NotFoundException);
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
            }
            finally
            {
                // checks if XmlReader instance is not null and then closes, disposes and sets it to null
                if (reader != null)
                {
                    reader.Close();
                    reader.Dispose();
                    reader = null;
                }
                // checks if StringReader instance is not null and then closes, disposes and sets it to null
                if (stringReader != null)
                {
                    stringReader.Close();
                    stringReader.Dispose();
                    stringReader = null;
                }

                error = validationError.ToString();
                bFileOk = string.IsNullOrWhiteSpace(error);
            }
            // returns whether the Validation was a sucess or not
            return (bFileOk);
        }

        private XmlSchemaSet? GetSchema(string url)
        {
            if (!_cache.ContainsKey(url) || (DateTime.UtcNow - _cache[url].LastUpdated).TotalHours > 1)
            {
                // Creates a new XmlSchemaSet object
                XmlSchemaSet sc = new XmlSchemaSet();
                sc.XmlResolver = new XmlUrlResolver();

                // creates a XmlSchema from reading the passed in Stream and using the Validation Event Handler which callsback to the ValidationCallBack method subscription
                XmlSchema schema;
                using (var reader2 = XmlReader.Create(url))
                {
                    schema = XmlSchema.Read(reader2, (sender, e) => Console.WriteLine(e.Message));
                }

                // check if the schema is null
                if (schema == null) throw new NullReferenceException("Failed to load the schema from the URL " + url);

                // adds this XmlSchema to our XmlSchemaSet
                sc.Add(schema);

                CachedXmlSchema cachedSchema = new CachedXmlSchema();
                cachedSchema.URL = url;
                cachedSchema.SchemaSet = sc;
                _cache.Add(url, cachedSchema);
            }
            return _cache[url]?.SchemaSet;
        }
    }

    class CachedXmlSchema
    {
        public DateTime LastUpdated { get; set; } = DateTime.UtcNow;
        public string? URL { get; set; }
        public XmlSchemaSet? SchemaSet { get; set; }
    }
}
