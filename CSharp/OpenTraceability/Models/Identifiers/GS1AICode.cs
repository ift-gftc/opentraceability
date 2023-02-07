using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Text.Json;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace OpenTraceability.Models.Identifiers
{
    public class GS1AICode
    {
        public string title { get; set; }
        public string label { get; set; }
        public string shortcode { get; set; }
        public string ai { get; set; }
        public string format { get; set; }
        public string type { get; set; }
        public bool fixedLength { get; set; }
        public string checkDigit { get; set; }
        public string regex { get; set; }
        public string[] qualifiers { get; set; }

        public GS1AICode()
        {

        }

        public bool VerifySyntax(string value)
        {
            if (ai == "01" || ai == "02")
            {
                if (GS1.Models.Identifiers.GTIN.IsGTIN(value))
                {
                    return (true);
                }
                else
                {
                    throw new Exception("SYNTAX ERROR: invalid syntax for value of (" + ai + ")" + value);
                }
            }
            else if (ai == "414")
            {
                if (GS1.Models.Identifiers.GLN.IsGLN(value))
                {
                    return (true);
                }
                else
                {
                    throw new Exception("SYNTAX ERROR: invalid syntax for value of (" + ai + ")" + value);
                }
            }
            else
            {

                string regExExpression = "^" + this.regex + "$";
                Regex regex = new Regex(regExExpression);
                if (!(regex.IsMatch(value)))
                {
                    throw new Exception("SYNTAX ERROR: invalid syntax for value of (" + ai + ")" + value);
                }
                return (true);
            }
        }
        public string padGTIN(string value)
        {
            // always pad the value of any GTIN [ AI (01) or (02) ] to 14 digits in element string representation
            string newvalue = value;
            if ((this.ai == "01") || (ai == "(01)") || (ai == "02") || (ai == "(02)"))
            {
                if (value.Length == 8) { newvalue = "000000" + value; }
                if (value.Length == 12) { newvalue = "00" + value; }
                if (value.Length == 13) { newvalue = "0" + value; }
            }
            return newvalue;
        }
        Int32 calculateCheckDigit(string gs1IDValue)
        {
            Int32 counter = 0;
            //string reversed = "";
            Int32 total = 0;
            Int32 l;
            string strCheckDigitPosition = this.checkDigit;
            if (strCheckDigitPosition == "L")
            {
                l = gs1IDValue.Length;
            }
            else
            {

                l = System.Convert.ToInt32(strCheckDigitPosition);
            }
            Int32 multiplier = 0;
            for (Int32 i = l - 2; i >= 0; i--)
            {
                string d = gs1IDValue.Substring(i, 1);
                Int32 iDig = System.Convert.ToInt32(d);
                if ((counter % 2) == 0)
                {
                    multiplier = 3;
                }
                else
                {
                    multiplier = 1;
                }
                total += (iDig * multiplier);
                counter++;
            }
            Int32 expectedCheckDigit = (10 - (total % 10)) % 10;
            return expectedCheckDigit;
        }

        public bool ValidateCheckDigit(string gs1IDValue)
        {
            Int32 expectedCheckDigit;
            bool rv = true;
            if (ai == "01" || ai == "02")
            {
                if (GS1.Models.Identifiers.GTIN.IsGTIN(gs1IDValue))
                {
                    return (true);
                }
                else
                {
                    throw new Exception("SYNTAX ERROR: invalid syntax for value of (" + ai + ")" + gs1IDValue);
                }
            }
            else if (ai == "414")
            {
                if (GS1.Models.Identifiers.GLN.IsGLN(gs1IDValue))
                {
                    return (true);
                }
                else
                {
                    throw new Exception("SYNTAX ERROR: invalid syntax for value of (" + ai + ")" + gs1IDValue);
                }
            }
            else
            {
                string strCheckDigitPosition = checkDigit;
                Int32 CheckDigitPosition = 0;
                if (strCheckDigitPosition != "undefined" && strCheckDigitPosition != null)
                {
                    expectedCheckDigit = this.calculateCheckDigit(gs1IDValue);
                    if (strCheckDigitPosition == "L")
                    {
                        CheckDigitPosition = gs1IDValue.Length;
                    }
                    else
                    {
                        CheckDigitPosition = System.Convert.ToInt32(strCheckDigitPosition);
                    }
                    string stractualDigit = gs1IDValue[CheckDigitPosition - 1].ToString();
                    Int32 actualCheckDigit = System.Convert.ToInt32(stractualDigit);

                    if (actualCheckDigit != expectedCheckDigit)
                    {
                        rv = false;
                        throw new Exception("INVALID CHECK DIGIT:  An invalid check digit was found for the primary identification key (" + ai + ")" + gs1IDValue + " ; the correct check digit should be " + expectedCheckDigit + " at position " + strCheckDigitPosition);
                    }

                }
            }
            return rv;
        }
    }
    public class GS1AIDefinitions
    {
        public List<GS1AICode> AICodes { get; set; }
        Dictionary<string, string> shortCodeToNumericMap = new Dictionary<string, string>();
        Dictionary<string, GS1AICode> aiCodeMap = new Dictionary<string, GS1AICode>();
        Dictionary<string, Regex> aiRegExMap = new Dictionary<string, Regex>();

        public GS1AIDefinitions()
        {
            Load();
        }

        private void Load()
        {
            Assembly assembly = Assembly.GetExecutingAssembly();
            string resourceName = "TEModels.Identifiers.GS1AICodes.json";
            string json = "";
            using (Stream stream = assembly.GetManifestResourceStream(resourceName))
            {
                using (StreamReader sr = new StreamReader(stream))
                {
                    json = sr.ReadToEnd();
                }
            }
            AICodes = JsonSerializer.Deserialize<List<GS1AICode>>(json);


            foreach (GS1AICode aiCode in AICodes)
            {
                if (aiCode.shortcode != null)
                {
                    shortCodeToNumericMap.Add(aiCode.shortcode, aiCode.ai);
                }
                aiCodeMap.Add(aiCode.ai, aiCode);
                string regExExpression = "^" + aiCode.regex + "$";
                aiRegExMap.Add(aiCode.ai, new Regex(regExExpression));
            }
        }
        public string shortCodeToNumeric(string shortCode)
        {
            if (shortCodeToNumericMap.ContainsKey(shortCode))
            {
                return (shortCodeToNumericMap[shortCode]);
            }
            else
            {
                return ("");
            }
        }

        public bool HasShortCode(string shortCode)
        {
            if (shortCodeToNumericMap.ContainsKey(shortCode))
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public bool HasAICode(string aiCode)
        {
            if (aiCodeMap.ContainsKey(aiCode))
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }

        public bool IsAIRegExMatch(string aiCode, string strvalue)
        {
            if (aiRegExMap.ContainsKey(aiCode))
            {
                return (aiRegExMap[aiCode].IsMatch(strvalue));
            }
            else
            {
                return (false);
            }


        }

        public string CheckDigitPosition(string aiCode)
        {
            string str = "undefined";
            if (aiCodeMap.ContainsKey(aiCode))
            {
                str = aiCodeMap[aiCode].checkDigit;
            }
            return (str);

        }

        public GS1AICode FromCode(string code)
        {
            foreach (GS1AICode codeDef in AICodes)
            {
                if (codeDef.shortcode == code)
                {
                    return (codeDef);
                }
            }
            return (null);

        }

        public GS1AICode FromAI(string aiCode)
        {
            if (aiCodeMap.ContainsKey(aiCode))
            {
                return aiCodeMap[aiCode];
            }
            else
            {
                return (null);
            }

        }

    }
}
