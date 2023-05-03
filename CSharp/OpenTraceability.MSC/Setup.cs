using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using OpenTraceability.MSC.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.MSC
{
    public static class Setup
    {
        private static object _locker = new object();
        private static bool _isInitialized = false;

        public static void Initialize()
        {
            lock (_locker)
            {
                if (!_isInitialized)
                {
                    OpenTraceability.GDST.Setup.Initialize();

                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCProcessingEvent), EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning"));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCProcessingEvent), EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning"));

                    _isInitialized = true;
                }
            }
        }
    }
}
