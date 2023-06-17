package opentraceability.models.identifiers;

import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;
import java.util.regex.Pattern;

public class EPC
{
	private String _epcStr = "";

	private EPCType Type = EPCType.values()[0];
	public final EPCType getType()
	{
		return Type;
	}
	private void setType(EPCType value)
	{
		Type = value;
	}
	private GTIN GTIN;
	public final GTIN getGTIN()
	{
		return GTIN;
	}
	private void setGTIN(GTIN value)
	{
		GTIN = value;
	}
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public string? SerialLotNumber {get;private set;}
	private String SerialLotNumber;
	public final String getSerialLotNumber()
	{
		return SerialLotNumber;
	}
	private void setSerialLotNumber(String value)
	{
		SerialLotNumber = value;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public EPC(string? epcStr)
	public EPC(String epcStr) throws Exception {
		String error = EPC.DetectEPCIssue(epcStr);

		if (!(error == null || error.isBlank()))
		{
			throw new RuntimeException(String.format("The EPC %1$s is invalid. %2$s", epcStr, error));
		}
		else if (epcStr == null)
		{
			throw new NullPointerException("epcStr");
		}

		this._epcStr = epcStr;

		// if this is a GS1 class level epc (GS1 GTIN + Lot Number)
		if (epcStr.startsWith("urn:epc:id:sscc:"))
		{
			this.setType(EPCType.SSCC);
			this.setSerialLotNumber(StringExtensions.LastOrDefault(epcStr.split(java.util.regex.Pattern.quote(":"), -1)));
		}
		else if (epcStr.startsWith("urn:epc:class:lgtin:"))
		{
			this.setType(EPCType.Class);

			ArrayList<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(java.util.regex.Pattern.quote(":"), -1)));
			ArrayList<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("[.]", -1)));
			parts.remove(parts.size() - 1);

			String gtinStr = tangible.StringHelper.join(":", parts.toArray(new String[0])) + ":" + parts2.get(0) + "." + parts2.get(1);
			gtinStr = gtinStr.replace(":class:lgtin:", ":idpat:sgtin:");
			this.setSerialLotNumber(parts2.get(2));
			this.setGTIN(new GTIN(gtinStr));
		}
		// else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
		else if (epcStr.startsWith("urn:epc:id:sgtin:"))
		{
			this.setType(EPCType.Instance);

			ArrayList<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(java.util.regex.Pattern.quote(":"), -1)));
			ArrayList<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("[.]", -1)));
			parts.remove(parts.size() - 1);

			String gtinStr = tangible.StringHelper.join(":", parts.toArray(new String[0])) + ":" + parts2.get(0) + "." + parts2.get(1);
			gtinStr = gtinStr.replace(":id:sgtin:", ":idpat:sgtin:");
			this.setSerialLotNumber(parts2.get(2));
			this.setGTIN(new GTIN(gtinStr));
		}
		// else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
		else if (epcStr.startsWith("urn:") && epcStr.contains(":product:lot:class:"))
		{
			this.setType(EPCType.Class);

			ArrayList<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(java.util.regex.Pattern.quote(":"), -1)));
			ArrayList<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("[.]", -1)));
			parts.remove(parts.size() - 1);

			String gtinStr = tangible.StringHelper.join(":", parts.toArray(new String[0])) + ":" + parts2.get(0) + "." + parts2.get(1);
			gtinStr = gtinStr.replace(":product:lot:class:", ":product:class:");
			this.setSerialLotNumber(parts2.get(2));
			this.setGTIN(new GTIN(gtinStr));
		}
		// else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
		else if (epcStr.startsWith("urn:") && epcStr.contains(":product:serial:obj:"))
		{
			this.setType(EPCType.Instance);

			ArrayList<String> parts = new ArrayList<>(Arrays.asList(epcStr.split(java.util.regex.Pattern.quote(":"), -1)));
			ArrayList<String> parts2 = new ArrayList<>(Arrays.asList(parts.get(parts.size() - 1).split("[.]", -1)));
			parts.remove(parts.size() - 1);

			String gtinStr = tangible.StringHelper.join(":", parts.toArray(new String[0])) + ":" + parts2.get(0) + "." + parts2.get(1);
			gtinStr = gtinStr.replace(":product:serial:obj:", ":product:class:");
			this.setSerialLotNumber(parts2.get(2));
			this.setGTIN(new GTIN(gtinStr));
		}
		else if (epcStr.startsWith("urn:sscc:"))
		{
			this.setType(EPCType.SSCC);
		}
		else if (epcStr.startsWith("urn:") && epcStr.contains(":lpn:obj:"))
		{
			this.setType(EPCType.SSCC);
		}
		else if (epcStr.startsWith("urn:epc:id:bic:"))
		{
			this.setType(EPCType.SSCC);
		}
		else if (StringExtensions.isURI(epcStr) && epcStr.startsWith("http") && epcStr.contains("/obj/"))
		{
			this.setType(EPCType.Instance);
			this.setSerialLotNumber(StringExtensions.LastOrDefault(epcStr.split("[/]", -1)));
		}
		else if (StringExtensions.isURI(epcStr) && epcStr.startsWith("http") && epcStr.contains("/class/"))
		{
			this.setType(EPCType.Class);
			this.setSerialLotNumber(StringExtensions.LastOrDefault(epcStr.split("[/]", -1)));
		}
		else if (StringExtensions.isURI(epcStr))
		{
			this.setType(EPCType.URI);
		}
	}

	public EPC(EPCType type, GTIN gtin, String lotOrSerial)
	{
		if (type == EPCType.Class)
		{
			String epc = gtin.toString().toLowerCase() + "." + lotOrSerial;
			if (epc.contains(":product:class:"))
			{
				epc = epc.replace(":product:class:", ":product:lot:class:");
			}
			else if (epc.contains(":idpat:sgtin:"))
			{
				epc = epc.replace(":idpat:sgtin:", ":class:lgtin:");
			}
			else
			{
				throw new RuntimeException("Unrecognized GTIN pattern. " + gtin);
			}
			this.setType(type);
			this.setGTIN(gtin);
			this.setSerialLotNumber(lotOrSerial);
			this._epcStr = epc;
		}
		else if (type == EPCType.Instance)
		{
			String epc = gtin.toString().toLowerCase() + "." + lotOrSerial;
			if (epc.contains(":product:class:"))
			{
				epc = epc.replace(":product:class:", ":product:serial:obj:");
			}
			else if (epc.contains(":idpat:sgtin:"))
			{
				epc = epc.replace(":idpat:sgtin:", ":id:sgtin:");
			}
			else
			{
				throw new RuntimeException("Unrecognized GTIN pattern. " + gtin);
			}
			this.setType(type);
			this.setGTIN(gtin);
			this.setSerialLotNumber(lotOrSerial);
			this._epcStr = epc;
		}
		else
		{
			throw new RuntimeException("Cannot build EPC of type {type} with a GTIN and Lot/Serial number.");
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static string? DetectEPCIssue(string? epcStr)
	public static String DetectEPCIssue(String epcStr) throws Exception {
		if ((epcStr == null || epcStr.isBlank()))
		{
			return ("The EPC is a NULL or White Space string.");
		}

		// if this is a GS1 class level epc (GS1 GTIN + Lot Number)
		if (epcStr.startsWith("urn:epc:class:lgtin:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
			String[] parts2 = StringExtensions.Last(parts).split("[.]", -1);

			if (parts2.length < 3)
			{
				return String.format("The EPC %1$s is not in the right format. It doesn't contain a company prefix, item code, and lot number.", epcStr);
			}
			else
			{
				return "";
			}
		}
		// else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
		else if (epcStr.startsWith("urn:epc:id:sgtin:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
			String[] parts2 = StringExtensions.Last(parts).split("[.]", -1);

			if (parts2.length < 3)
			{
				return String.format("The EPC %1$s is not in the right format. It doesn't contain a company prefix, item code, and lot number.", epcStr);
			}
			else
			{
				return null;
			}
		}
		// else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
		else if (epcStr.startsWith("urn:") && epcStr.contains(":product:lot:class:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
			String[] parts2 = StringExtensions.Last(parts).split("[.]", -1);

			if (parts2.length < 3)
			{
				return String.format("The EPC %1$s is not in the right format. It doesn't contain a company prefix, item code, and serial number.", epcStr);
			}
			else
			{
				return null;
			}
		}
		// else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
		else if (epcStr.startsWith("urn:") && epcStr.contains(":product:serial:obj:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
			String[] parts2 = StringExtensions.Last(parts).split("[.]", -1);

			if (parts2.length < 3)
			{
				return String.format("The EPC %1$s is not in the right format. It doesn't contain a company prefix, item code, and a serial number.", epcStr);
			}
			else
			{
				return null;
			}
		}
		else if (epcStr.startsWith("urn:epc:id:sscc:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			return null;
		}
		else if (epcStr.startsWith("urn:epc:id:bic:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			return null;
		}
		else if (epcStr.startsWith("urn:") && epcStr.contains(":lpn:obj:"))
		{
			if (!StringExtensions.isURI(epcStr))
			{
				return ("The EPC contains non-compatiable characters for a URN format.");
			}

			return null;
		}
		else if (StringExtensions.isURI(epcStr) && epcStr.startsWith("http") && epcStr.contains("/obj/"))
		{
			return null;
		}
		else if (StringExtensions.isURI(epcStr) && epcStr.startsWith("http") && epcStr.contains("/class/"))
		{
			return null;
		}
		else if (StringExtensions.isURI(epcStr))
		{
			return null;
		}
		else
		{
			return "This EPC does not fit any of the allowed formats.";
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool TryParse(string? epcStr, out System.Nullable<EPC> epc, out string? error)
	public static boolean TryParse(String epcStr, tangible.OutObject<EPC> epc, tangible.OutObject<String> error) throws Exception {
		error.outArgValue = EPC.DetectEPCIssue(epcStr);
		if ((error.outArgValue == null || error.outArgValue.isBlank()))
		{
			epc.outArgValue = new EPC(epcStr);
			return true;
		}
		else
		{
			epc.outArgValue = null;
			return false;
		}
	}

	/** 
	 This method will perform a matching process with the EPC if both are either a Class/Instance type EPC.
	 If both EPCs are equal, it returns TRUE.
	 If the source contains a "*" for the serial/lot number, and the source and the target have matching GTINs, then it returns TRUE.
	*/
	public final boolean Matches(EPC targetEPC)
	{
		if (this.equals(targetEPC))
		{
			return true;
		}
		else return Objects.equals(this.getSerialLotNumber(), "*") && opentraceability.models.identifiers.GTIN.opEquals(this.getGTIN(), targetEPC.getGTIN());
	}

	public final Object Clone() throws Exception {
		EPC epc = new EPC(this.toString());
		return epc;
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region Overrides

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator ==(EPC? obj1, EPC? obj2)
	public static boolean opEquals(EPC obj1, EPC obj2)
	{
		if (null == obj1 && null == obj2)
		{
			return true;
		}

		if (null != obj1 && null == obj2)
		{
			return false;
		}

		if (null == obj1 && null != obj2)
		{
			return false;
		}

		if (obj1 == null)
		{
			return false;
		}

		return obj1.equals(obj2);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator !=(EPC? obj1, EPC? obj2)
	public static boolean opNotEquals(EPC obj1, EPC obj2)
	{
		if (null == obj1 && null == obj2)
		{
			return false;
		}

		if (null != obj1 && null == obj2)
		{
			return true;
		}

		if (null == obj1 && null != obj2)
		{
			return true;
		}

		if (obj1 == null)
		{
			return true;
		}

		return !obj1.equals(obj2);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public override bool Equals(object? obj)
	@Override
	public boolean equals(Object obj)
	{
		if (null == obj)
		{
			return false;
		}

		if (this == obj)
		{
			return true;
		}

		if (obj.getClass() != this.getClass())
		{
			return false;
		}

		return this.IsEquals((EPC)obj);
	}

	@Override
	public int hashCode()
	{
		int hash = HashCodeUtility.getInt32HashCode(this.toString());
		return hash;
	}

	@Override
	public String toString()
	{
		return _epcStr.toLowerCase();
	}

	public final boolean equals(EPC epc)
	{
		if (null == epc)
		{
			return false;
		}

		if (this == epc)
		{
			return true;
		}

		return this.IsEquals(epc);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private bool IsEquals(EPC? epc)
	private boolean IsEquals(EPC epc)
	{
		if (null == epc)
		{
			return false;
		}

		return Objects.equals(this.toString().toLowerCase(), epc.toString().toLowerCase());
	}

	public final int CompareTo(EPC epc)
	{
		if (null == epc)
		{
			throw new NullPointerException("epc");
		}

		long myInt64Hash = HashCodeUtility.getInt64HashCode(this.toString());
		long otherInt64Hash = HashCodeUtility.getInt64HashCode(epc.toString());

		if (myInt64Hash > otherInt64Hash)
		{
			return -1;
		}
		if (myInt64Hash == otherInt64Hash)
		{
			return 0;
		}

		return 1;
	}
}
