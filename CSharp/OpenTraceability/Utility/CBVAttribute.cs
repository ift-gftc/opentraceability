using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    [AttributeUsage(AttributeTargets.All, AllowMultiple = true, Inherited = true)]
    public class CBVAttribute : System.Attribute
    {
        public string Value { get; set; }

        public CBVAttribute(string value)
        {
            Value = value;
        }
    }
}
