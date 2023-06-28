package opentraceability.models.identifiers;


import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

public class GTIN implements java.lang.Comparable<GTIN>
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
		String error = GTIN.DetectGTINIssue(gtinStr);
		if (!(error == null || error.isBlank()))
		{
			throw new RuntimeException(String.format("The GTIN %1$s is invalid. %2$s", gtinStr, error));
		}
		this._gtinStr = gtinStr;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool TryParse(string? gtinStr, out System.Nullable<GTIN> gtin, out string? error)
	public static boolean TryParse(String gtinStr, tangible.OutObject<GTIN> gtin, tangible.OutObject<String> error)
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

	public final boolean IsGS1GTIN()
	{
		return _gtinStr.contains(":idpat:sgtin:");
	}

	public final String ToDigitalLinkURL()
	{
		if (_gtinStr == null)
		{
			return "";
		}
		else if (IsGS1GTIN())
		{
			String[] parts_step1 = _gtinStr.split("[:]", -1);
			String[] parts = parts_step1[parts_step1.length - 1].split("[.]", - 1);
			String gtin14 = parts[1].charAt(0) + parts[0] + parts[1].substring(1);
			gtin14 = gtin14 + GS1Util.CalculateGTIN14CheckSum(gtin14);
			return String.format("01/%1$s", gtin14);
		}
		else
		{
			return String.format("01/%1$s", this._gtinStr);
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
		if (tangible.StringHelper.isNullOrEmpty(gtinStr))
		{
			return ("GTIN is NULL or EMPTY.");
		}
		else if (!StringExtensions.isURI(gtinStr))
		{
			return ("The GTIN contains non-compatiable characters for a URI.");
		}
		else if (gtinStr.contains(" "))
		{
			return ("GTIN cannot contain spaces.");
		}
		else if (gtinStr.length() == 14 && StringExtensions.isOnlyDigits(gtinStr))
		{
			// validate the checksum
			char checksum = GS1Util.CalculateGTIN14CheckSum(gtinStr);
			if (checksum != gtinStr.toCharArray()[gtinStr.length() - 1])
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
			String[] pieces_1 = gtinStr.split("[:]", -1);
			String[] pieces = pieces_1[pieces_1.length - 1].split("[.]", -1);
			if (pieces.length < 2)
			{
				throw new RuntimeException(String.format("The GTIN %1$s is not valid.", gtinStr));
			}

			String lastPiece = pieces[0] + pieces[1];
			if (!StringExtensions.isOnlyDigits(lastPiece))
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

	/** 
	 This function detects if the GTIN is a valid GTIN str or not.
	 
	 @param gtinStr
	 @return 
	*/
	public static boolean IsGTIN(String gtinStr)
	{
		return DetectGTINIssue(gtinStr) == null;
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

		return obj1.IsEquals(obj2);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator !=(GTIN? obj1, GTIN? obj2)
	public static boolean opNotEquals(GTIN obj1, GTIN obj2)
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

		return !obj1.IsEquals(obj2);
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

		return this.IsEquals((GTIN)obj);
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
		if (this._gtinStr != null)
		{
			return this._gtinStr.toLowerCase();
		}
		else
		{
			return "";
		}
	}

	public final boolean equals(GTIN gtin)
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

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private bool IsEquals(GTIN? gtin)
	private boolean IsEquals(GTIN gtin)
	{
		if (null == gtin)
		{
			return false;
		}

		return this.toString().toLowerCase().equals(gtin.toString().toLowerCase());
	}

	public final int compareTo(GTIN gtin)
	{
		if (null == gtin)
		{
			throw new NullPointerException("gtin");
		}

		long myInt64Hash = HashCodeUtility.getInt64HashCode(this.toString());
		long otherInt64Hash = HashCodeUtility.getInt64HashCode(gtin.toString());

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
