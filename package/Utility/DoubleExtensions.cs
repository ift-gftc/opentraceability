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
        public static double Round(this double val)
        {
            double roundedValue = val;
            string strVal = val.ToString("e12", CultureInfo.InvariantCulture);
            roundedValue = System.Convert.ToDouble(strVal);
            return (roundedValue);
        }

        public static double? Round(this double? val)
        {
            if (val == null)
            {
                return (val);
            }
            double? roundedValue = val;
            string strVal = val.Value.ToString("e12", CultureInfo.InvariantCulture);
            roundedValue = System.Convert.ToDouble(strVal);
            return (roundedValue);
        }
    }
}
