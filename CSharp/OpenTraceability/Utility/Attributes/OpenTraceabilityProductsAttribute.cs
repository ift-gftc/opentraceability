using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility.Attributes
{
    public enum OpenTraceabilityProductsListType
    {
        EPCList,
        QuantityList
    }

    [AttributeUsage(AttributeTargets.Property, AllowMultiple = true)]
    public class OpenTraceabilityProductsAttribute : Attribute
    {
        public string Name { get; set; }
        public EPCISVersion? Version { get; set; }
        public EventProductType ProductType { get; set; }
        public int? SequenceOrder { get; set; }
        public OpenTraceabilityProductsListType ListType { get; set; }
        public bool Required { get; set; } = false;

        public OpenTraceabilityProductsAttribute(string name, EPCISVersion version, EventProductType productType, int SequenceOrder, OpenTraceabilityProductsListType listType)
        {
            this.Name = name;
            this.Version = version;
            this.ProductType = productType;
            this.SequenceOrder = SequenceOrder;
            this.ListType = listType;
        }

        public OpenTraceabilityProductsAttribute(string name, EventProductType productType, int SequenceOrder, OpenTraceabilityProductsListType listType)
        {
            this.Name = name;
            this.ProductType = productType;
            this.SequenceOrder = SequenceOrder;
            this.ListType = listType;
        }
    }
}
