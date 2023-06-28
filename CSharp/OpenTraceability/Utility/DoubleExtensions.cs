using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public static class DoubleExtensions
    {
        public static double Round(this double number)
        {
            double roundedValue = number;
            string strVal = number.ToString("e12", CultureInfo.InvariantCulture);
            roundedValue = System.Convert.ToDouble(strVal);
            return (roundedValue);
        }

        public static double? Round(this double? number)
        {
            if (number == null)
            {
                return (number);
            }
            double? roundedValue = number;
            string strVal = number.Value.ToString("e12", CultureInfo.InvariantCulture);
            roundedValue = System.Convert.ToDouble(strVal);
            return (roundedValue);
        }
    }
}
