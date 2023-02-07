using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models.Events;
using DSUtil.StaticData;

namespace OpenTraceability.Models.Events
{
    public class SensorReport : ISensorReport
    {
        public DateTime? TimeStamp { get; set; }
        public Uri Type { get; set; }
        public Measurement Measurement { get; set; }
    }
}
