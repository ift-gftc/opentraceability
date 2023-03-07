using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Interfaces
{
    public interface IILMDEvent<T> : IEvent where T : EventILMD
    {
        T ILMD { get; set; }
    }
}
