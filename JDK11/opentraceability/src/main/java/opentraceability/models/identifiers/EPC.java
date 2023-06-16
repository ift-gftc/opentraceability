package opentraceability.models.identifiers;

import Newtonsoft.Json.*;
import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

//C# TO JAVA CONVERTER TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [JsonConverter(typeof(EPCConverter))] public class EPC
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
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public GTIN? GTIN {get;private set;}
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
	public EPC(String epcStr)
	{
		try
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? error = EPC.DetectEPCIssue(epcStr);
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
				this.setSerialLotNumber(epcStr.split(java.util.regex.Pattern.quote(":"), -1).LastOrDefault());
			}
			else if (epcStr.startsWith("urn:epc:class:lgtin:"))
			{
				this.setType(EPCType.Class);

				ArrayList<String> parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1).ToList();
				ArrayList<String> parts2 = parts.get(parts.size() - 1).split("[.]", -1).ToList();
				parts.remove(parts.size() - 1);

				String gtinStr = tangible.StringHelper.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
				gtinStr = gtinStr.replace(":class:lgtin:", ":idpat:sgtin:");
				this.setSerialLotNumber(parts2.get(2));
				this.setGTIN(new GTIN(gtinStr));
			}
			// else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
			else if (epcStr.startsWith("urn:epc:id:sgtin:"))
			{
				this.setType(EPCType.Instance);

				ArrayList<String> parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1).ToList();
				ArrayList<String> parts2 = parts.get(parts.size() - 1).split("[.]", -1).ToList();
				parts.remove(parts.size() - 1);

				String gtinStr = tangible.StringHelper.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
				gtinStr = gtinStr.replace(":id:sgtin:", ":idpat:sgtin:");
				this.setSerialLotNumber(parts2.get(2));
				this.setGTIN(new GTIN(gtinStr));
			}
			// else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
			else if (epcStr.startsWith("urn:") && epcStr.contains(":product:lot:class:"))
			{
				this.setType(EPCType.Class);

				ArrayList<String> parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1).ToList();
				ArrayList<String> parts2 = parts.get(parts.size() - 1).split("[.]", -1).ToList();
				parts.remove(parts.size() - 1);

				String gtinStr = tangible.StringHelper.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
				gtinStr = gtinStr.replace(":product:lot:class:", ":product:class:");
				this.setSerialLotNumber(parts2.get(2));
				this.setGTIN(new GTIN(gtinStr));
			}
			// else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
			else if (epcStr.startsWith("urn:") && epcStr.contains(":product:serial:obj:"))
			{
				this.setType(EPCType.Instance);

				ArrayList<String> parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1).ToList();
				ArrayList<String> parts2 = parts.get(parts.size() - 1).split("[.]", -1).ToList();
				parts.remove(parts.size() - 1);

				String gtinStr = tangible.StringHelper.join(":", parts) + ":" + parts2.get(0) + "." + parts2.get(1);
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
			else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.startsWith("http") && epcStr.contains("/obj/"))
			{
				this.setType(EPCType.Instance);
				this.setSerialLotNumber(epcStr.split("[/]", -1).LastOrDefault());
			}
			else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.startsWith("http") && epcStr.contains("/class/"))
			{
				this.setType(EPCType.Class);
				this.setSerialLotNumber(epcStr.split("[/]", -1).LastOrDefault());
			}
			else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute))
			{
				this.setType(EPCType.URI);
			}
		}
		catch (RuntimeException Ex)
		{
			RuntimeException exception = new RuntimeException("The EPC is not in a valid format and could not be parsed. EPC=" + epcStr, Ex);
			OTLogger.Error(Ex);
			throw exception;
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
				throw new RuntimeException("Unrecognized GTIN pattern. " + gtin.toString());
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
				throw new RuntimeException("Unrecognized GTIN pattern. " + gtin.toString());
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
	public static String DetectEPCIssue(String epcStr)
	{
		try
		{
			if ((epcStr == null || epcStr.isBlank()))
			{
				return ("The EPC is a NULL or White Space string.");
			}

			// if this is a GS1 class level epc (GS1 GTIN + Lot Number)
			if (epcStr.startsWith("urn:epc:class:lgtin:"))
			{
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
				String[] parts2 = parts.Last().split("[.]", -1);

				if (parts2.Count() < 3)
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
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
				String[] parts2 = parts.Last().split("[.]", -1);

				if (parts2.Count() < 3)
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
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
				String[] parts2 = parts.Last().split("[.]", -1);

				if (parts2.Count() < 3)
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
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				String[] parts = epcStr.split(java.util.regex.Pattern.quote(":"), -1);
				String[] parts2 = parts.Last().split("[.]", -1);

				if (parts2.Count() < 3)
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
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				return null;
			}
			else if (epcStr.startsWith("urn:epc:id:bic:"))
			{
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				return null;
			}
			else if (epcStr.startsWith("urn:") && epcStr.contains(":lpn:obj:"))
			{
				if (!StringExtensions.IsURICompatibleChars(epcStr))
				{
					return ("The EPC contains non-compatiable characters for a URN format.");
				}

				return null;
			}
			else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.startsWith("http") && epcStr.contains("/obj/"))
			{
				return null;
			}
			else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute) && epcStr.startsWith("http") && epcStr.contains("/class/"))
			{
				return null;
			}
			else if (Uri.IsWellFormedUriString(epcStr, UriKind.Absolute))
			{
				return null;
			}
			else
			{
				return "This EPC does not fit any of the allowed formats.";
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool TryParse(string? epcStr, out System.Nullable<EPC> epc, out string? error)
	public static boolean TryParse(String epcStr, tangible.OutObject<EPC> epc, tangible.OutObject<String> error)
	{
		try
		{
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
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
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
		else if (Objects.equals(this.getSerialLotNumber(), "*") && opentraceability.models.identifiers.GTIN.opEquals(this.getGTIN(), targetEPC.getGTIN()))
		{
			return true;
		}
		return false;
	}

	public final Object Clone()
	{
		EPC epc = new EPC(this.toString());
		return epc;
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region Overrides

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator ==(EPC? obj1, EPC? obj2)
	public static boolean opEquals(EPC obj1, EPC obj2)
	{
		try
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
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator !=(EPC? obj1, EPC? obj2)
	public static boolean opNotEquals(EPC obj1, EPC obj2)
	{
		try
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
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public override bool Equals(object? obj)
	@Override
	public boolean equals(Object obj)
	{
		try
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
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	@Override
	public int hashCode()
	{
		try
		{
			int hash = ObjectExtensions.GetInt32HashCode(this.toString());
			return hash;
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	@Override
	public String toString()
	{
		try
		{
			return _epcStr.toLowerCase();
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#endregion Overrides

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region IEquatable<EPC>

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public bool Equals(EPC? epc)
	public final boolean equals(EPC epc)
	{
		try
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
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private bool IsEquals(EPC? epc)
	private boolean IsEquals(EPC epc)
	{
		try
		{
			if (null == epc)
			{
				return false;
			}

			if (Objects.equals(this.toString().toLowerCase(), epc.toString().toLowerCase()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#endregion IEquatable<EPC>

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region IComparable

	public final int CompareTo(EPC epc)
	{
		try
		{
			if (null == epc)
			{
				throw new NullPointerException("epc");
			}

			long myInt64Hash = ObjectExtensions.GetInt64HashCode(this.toString());
			long otherInt64Hash = ObjectExtensions.GetInt64HashCode(epc.toString());

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
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#endregion IComparable
}
