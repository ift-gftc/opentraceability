package opentraceability.models.identifiers;

import Newtonsoft.Json.*;
import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

//C# TO JAVA CONVERTER TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [DataContract][JsonConverter(typeof(GTINConverter))] public class GTIN : IEquatable<GTIN>, IComparable<GTIN>
public class GTIN implements IEquatable<GTIN>, java.lang.Comparable<GTIN>
{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private string? _gtinStr;
	private String _gtinStr;

	public GTIN()
	{
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public GTIN(string? gtinStr)
	public GTIN(String gtinStr)
	{
		try
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? error = GTIN.DetectGTINIssue(gtinStr);
			String error = GTIN.DetectGTINIssue(gtinStr);
			if (!(error == null || error.isBlank()))
			{
				throw new RuntimeException(String.format("The GTIN %1$s is invalid. %2$s", gtinStr, error));
			}
			this._gtinStr = gtinStr;
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool TryParse(string? gtinStr, out System.Nullable<GTIN> gtin, out string? error)
	public static boolean TryParse(String gtinStr, tangible.OutObject<GTIN> gtin, tangible.OutObject<String> error)
	{
		try
		{
			error.outArgValue = GTIN.DetectGTINIssue(gtinStr);
			if ((error.outArgValue == null || error.outArgValue.isBlank()))
			{
				gtin.outArgValue = new GTIN(gtinStr);
				return true;
			}
			else
			{
				gtin.outArgValue = null;
				return false;
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	public final boolean IsGS1GTIN()
	{
		return _gtinStr.contains(":idpat:sgtin:");
	}

	public final String ToDigitalLinkURL()
	{
		try
		{
			if (_gtinStr == null)
			{
				return "";
			}
			else if (IsGS1GTIN())
			{
				String[] gtinParts = _gtinStr.split("[:]", -1).Last().split("[.]", -1);
				String gtin14 = gtinParts[1].charAt(0) + gtinParts[0] + gtinParts[1].Skip(1);
				gtin14 = gtin14 + GS1Util.CalculateGTIN14CheckSum(gtin14);
				return String.format("01/%1$s", gtin14);
			}
			else
			{
				return String.format("01/%1$s", this._gtinStr);
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	/** 
	 This function will analyze a GTIN and try to return feedback with why a GTIN is not valid.
	 
	 @param gtinStr The GTIN string.
	 @return An error if a problem is detected, otherwise returns NULL if no problem detected and the GTIN is valid.
	*/
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static string? DetectGTINIssue(string? gtinStr)
	public static String DetectGTINIssue(String gtinStr)
	{
		try
		{
			if (tangible.StringHelper.isNullOrEmpty(gtinStr))
			{
				return ("GTIN is NULL or EMPTY.");
			}
			else if (StringExtensions.IsURICompatibleChars(gtinStr) == false)
			{
				return ("The GTIN contains non-compatiable characters for a URI.");
			}
			else if (gtinStr.contains(" "))
			{
				return ("GTIN cannot contain spaces.");
			}
			else if (gtinStr.length() == 14 && StringExtensions.IsOnlyDigits(gtinStr))
			{
				// validate the checksum
				char checksum = GS1Util.CalculateGTIN14CheckSum(gtinStr);
				if (checksum != gtinStr.toCharArray().Last())
				{
					return String.format("The check sum did not calculate correctly. The expected check sum was %1$s. " + "Please make sure to validate that you typed the GTIN correctly. It's possible the check sum " + "was typed correctly but another number was entered wrong.", checksum);
				}

				return (null);
			}
			if (gtinStr.startsWith("urn:") && gtinStr.contains(":product:class:"))
			{
				return (null);
			}
			else if (gtinStr.startsWith("urn:") && gtinStr.contains(":idpat:sgtin:"))
			{
				String lastPiece = gtinStr.split("[:]", -1).Last().Replace(".", "");
				if (!StringExtensions.IsOnlyDigits(lastPiece))
				{
					return ("This is supposed to be a GS1 GTIN based on the System Prefix and " + "Data Type Prefix. That means the Company Prefix and Serial Numbers " + "should only be digits. Found non-digit characters in the Company Prefix " + "or Serial Number.");
				}
				else if (lastPiece.length() != 13)
				{
					return ("This is supposed to be a GS1 GTIN based on the System Prefix and Data Type " + "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " + "total of 13 digits between the two. The total number of digits when combined " + "is " + lastPiece.length() + ".");
				}

				return (null);
			}
			else
			{
				return ("The GTIN is not in a valid EPCIS URI format or in GS1 GTIN-14 format.");
			}
		}
		catch (RuntimeException Ex)
		{
			RuntimeException exception = new RuntimeException("Failed to detect GTIN Issues. GTIN=" + gtinStr, Ex);
			OTLogger.Error(exception);
			throw Ex;
		}
	}

	/** 
	 This function detects if the GTIN is a valid GTIN str or not.
	 
	 @param gtinStr
	 @return 
	*/
	public static boolean IsGTIN(String gtinStr)
	{
		try
		{
			if (DetectGTINIssue(gtinStr) == null)
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

	public final Object Clone()
	{
		GTIN gtin = new GTIN(this.toString());
		return gtin;
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region Overrides

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator ==(GTIN? obj1, GTIN? obj2)
	public static boolean opEquals(GTIN obj1, GTIN obj2)
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

			return (obj1 == null ? null : obj1.equals(obj2)) != null ? (obj1 == null ? null : obj1.equals(obj2)) : false;
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator !=(GTIN? obj1, GTIN? obj2)
	public static boolean opNotEquals(GTIN obj1, GTIN obj2)
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

			return !(obj1 == null ? null : obj1.equals(obj2)) != null ? (obj1 == null ? null : obj1.equals(obj2)) : false;
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

			return this.IsEquals((GTIN)obj);
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
			return (this._gtinStr == null ? null : this._gtinStr.toLowerCase()) != null ? (this._gtinStr == null ? null : this._gtinStr.toLowerCase()) : "";
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
		///#region IEquatable<GTIN>

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public bool Equals(GTIN? gtin)
	public final boolean equals(GTIN gtin)
	{
		try
		{
			if (null == gtin)
			{
				return false;
			}

			if (this == gtin)
			{
				return true;
			}

			return this.IsEquals(gtin);
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private bool IsEquals(GTIN? gtin)
	private boolean IsEquals(GTIN gtin)
	{
		try
		{
			if (null == gtin)
			{
				return false;
			}

			if (Objects.equals(this.toString().toLowerCase(), gtin.toString().toLowerCase()))
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
		///#endregion IEquatable<GTIN>

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region IComparable

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public int CompareTo(GTIN? gtin)
	public final int compareTo(GTIN gtin)
	{
		try
		{
			if (null == gtin)
			{
				throw new NullPointerException("gtin");
			}

			long myInt64Hash = ObjectExtensions.GetInt64HashCode(this.toString());
			long otherInt64Hash = ObjectExtensions.GetInt64HashCode(gtin.toString());

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
