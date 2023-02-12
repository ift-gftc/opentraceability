using System.Text.RegularExpressions;

namespace OpenTraceability.Utility
{
    public static class StringExtensions
    {
        private static Regex _digitsOnlyRegex = new Regex("^[0-9]+$", RegexOptions.Compiled);

        public static bool IsOnlyDigits(this string str)
        {
            try
            {
                return _digitsOnlyRegex.IsMatch(str);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        private static Regex _isURICompatibleCharsRegex = new Regex(@"(.*[^._\-:0-9A-Za-z])", RegexOptions.Compiled);

        public static bool IsURICompatibleChars(this string str)
        {
            try
            {
                return !_isURICompatibleCharsRegex.IsMatch(str);
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
    }
}