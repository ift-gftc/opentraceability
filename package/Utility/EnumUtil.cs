using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public class EnumUtil
    {
        /// <summary>
        /// This will return the value of the DescriptionAttribute attribute on the given enum value. If the attribute is not found, then it will return an empty string.
        /// </summary>
        /// <param name="value">The enum to grab the description from.</param>
        /// <returns></returns>
        public static string GetEnumDescription(object value)
        {
            try
            {
                FieldInfo? fi = value.GetType().GetRuntimeField(value.ToString() ?? string.Empty);

                if (fi != null)
                {
                    DescriptionAttribute[] attributes =
                    (DescriptionAttribute[])fi.GetCustomAttributes(
                    typeof(DescriptionAttribute),
                    false);

                    if (attributes != null &&
                        attributes.Length > 0)
                        return attributes[0].Description;
                    else
                        return string.Empty;
                }
                else
                {
                    return string.Empty;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }

        public static string? GetEnumDisplayName(object value)
        {
            try
            {
                FieldInfo? fi = value.GetType().GetRuntimeField(value.ToString() ?? string.Empty);

                if (fi != null)
                {
                    DisplayAttribute[] attributes =
                    (DisplayAttribute[])fi.GetCustomAttributes(
                    typeof(DisplayAttribute),
                    false);

                    if (attributes != null && attributes.Length > 0)
                    {
                        return attributes[0].Name;
                    }
                }

                return value.ToString();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }        

        public static IEnumerable<T> GetValues<T>()
        {
            return Enum.GetValues(typeof(T)).Cast<T>();
        }
    }
}
