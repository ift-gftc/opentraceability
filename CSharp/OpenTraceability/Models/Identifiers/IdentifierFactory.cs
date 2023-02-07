using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models.Identifiers;
using GS1.Models.Identifiers;
using DSUtil;

namespace OpenTraceability.Models.Identifiers
{
    public static class IdentifierFactory
    {
        public static IGTIN ParseGTIN(string gtinStr, out string error)
        {
            error = null;
            IGTIN gtin = null;
            GTIN.TryParse(gtinStr, out gtin, out error);
            return gtin;
        }

        public static IGTIN ParseGTIN(string gtinStr)
        {
            IGTIN gtin = null;
            GTIN.TryParse(gtinStr, out gtin, out string error);
            if (!string.IsNullOrWhiteSpace(error))
            {
                throw new Exception($"Failed to parse GTIN {gtinStr} due to errors. {error}");
            }
            return gtin;
        }

        public static EPC ParseEPC(string epcStr, out string error)
        {
            error = null;
            EPC epc = null;
            EPC.TryParse(epcStr, out epc, out error);
            return epc;
        }

        public static EPC ParseEPC(string epcStr)
        {
            EPC epc = null;
            EPC.TryParse(epcStr, out epc, out string error);
            if (!string.IsNullOrWhiteSpace(error))
            {
                throw new Exception($"Failed to parse EPC {epcStr} due to errors. {error}");
            }
            return epc;
        }

        public static IGLN ParseGLN(string glnStr, out string error)
        {
            error = null;
            IGLN gln = null;
            GLN.TryParse(glnStr, out gln, out error);
            return gln;
        }

        public static IGLN ParseGLN(string glnStr)
        {
            IGLN gln = null;
            GLN.TryParse(glnStr, out gln, out string error);
            if (!string.IsNullOrWhiteSpace(error))
            {
                throw new Exception("Failed to parse GLN. " + error);
            }
            return gln;
        }

        public static PGLN ParsePGLN(string pglnStr, out string error)
        {
            error = null;
            PGLN pgln = null;
            PGLN.TryParse(pglnStr, out pgln, out error);
            return pgln;
        }

        public static PGLN ParsePGLN(string pglnStr)
        {
            PGLN pgln = null;
            PGLN.TryParse(pglnStr, out pgln, out string error);
            if (!string.IsNullOrWhiteSpace(error))
            {
                throw new Exception("Failed to parse PGLN. " + error);
            }
            return pgln;
        }
    }
}
