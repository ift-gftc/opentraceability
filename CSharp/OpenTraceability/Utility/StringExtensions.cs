using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
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

        private static Regex _isURICompatibleCharsRegex = new Regex(@"(.*[^._\-/%:*0-9A-Za-z])", RegexOptions.Compiled);

        /// <summary>
        /// Tries and converts a string value into a DateTimeOffset using the ISO standard format. If it fails, it returns null.
        /// </summary>
        public static DateTimeOffset? TryConvertToDateTimeOffset(this string str)
        {
            if (DateTimeOffset.TryParseExact(str, $"yyyy-MM-ddTHH:mm:ssZ", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTimeOffset dt))
            {
                return dt;
            }
            else if (DateTimeOffset.TryParseExact(str, $"yyyy-MM-ddTHH:mm:ssK", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
            {
                return dt;
            }
            for (int i = 0; i <= 7; i++)
            {
                string f = "".PadLeft(i, 'f');
                if (DateTimeOffset.TryParseExact(str, $"yyyy-MM-ddTHH:mm:ss.{f}Z", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
                {
                    return dt;
                }
                else if (DateTimeOffset.TryParseExact(str, $"yyyy-MM-ddTHH:mm:ss.{f}K", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
                {
                    return dt;
                }
            }
            return null;
        }

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

        public static List<string> SplitXPath(this string str)
        {
            Regex r = new Regex("(?=[^{}]*(?:{[^{}]*}[^{}]*)*$)\\/");
            while (r.IsMatch(str))
            {
                str = r.Replace(str, "%SLASH%");
            }
            return str.Split(new[] { "%SLASH%" }, StringSplitOptions.None).ToList();
        }
    }
}