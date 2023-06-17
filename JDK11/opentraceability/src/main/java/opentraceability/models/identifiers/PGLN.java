package opentraceability.models.identifiers;

import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

/** 
 Party Global Location Number - Used for identifying Trading Partners, Business Departments,
 Business Regions, Business Groups, and Private Trading Partners in Full Chain Traceability.
*/
//C# TO JAVA CONVERTER TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [DataContract][JsonConverter(typeof(PGLNConverter))] public class PGLN : IEquatable<PGLN>, IComparable<PGLN>
public class PGLN implements java.lang.Comparable<PGLN>
{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private string? _pglnStr = string.Empty;
	private String _pglnStr = "";

	public PGLN()
	{
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public PGLN(string? pglnStr)
	public PGLN(String pglnStr)
	{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? error = DetectPGLNIssue(pglnStr);
		String error = DetectPGLNIssue(pglnStr);
		if (!(error == null || error.isBlank()))
		{
			throw new RuntimeException(String.format("The PGLN %1$s is not valid. %2$s", pglnStr, error));
		}
		this._pglnStr = pglnStr;
	}

	public final boolean IsGS1PGLN()
	{
		return (_pglnStr.contains(":id:pgln:") || _pglnStr.contains(":id:sgln:"));
	}

	public final String ToDigitalLinkURL()
	{
		if (IsGS1PGLN())
		{
			String[] parts_step1 = _pglnStr.split("[:]", -1);
			String[] parts = parts_step1[parts_step1.length - 1].split("[.]", - 1);
			String pgln = parts[0] + parts[1];
			pgln = pgln + GS1Util.CalculateGLN13CheckSum(pgln);
			return String.format("417/%1$s", pgln);
		}
		else
		{
			return String.format("417/%1$s", this._pglnStr);
		}
	}

	public static boolean IsPGLN(String pglnStr)
	{
		return PGLN.DetectPGLNIssue(pglnStr) == null;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static string? DetectPGLNIssue(string? pglnStr)
	public static String DetectPGLNIssue(String pglnStr)
	{
		if (tangible.StringHelper.isNullOrEmpty(pglnStr))
		{
			return ("PGLN is NULL or EMPTY.");
		}
		else if (pglnStr.contains(" "))
		{
			return ("PGLN cannot contain spaces.");
		}
		else if (!StringExtensions.isURI(pglnStr))
		{
			return ("The PGLN contains non-compatiable characters for a URI.");
		}
		else if (pglnStr.length() == 13 && StringExtensions.isOnlyDigits(pglnStr))
		{
			// validate the checksum
			char checksum = GS1Util.CalculateGLN13CheckSum(pglnStr);
			if (checksum != pglnStr.toCharArray()[pglnStr.length() - 1])
			{
				return String.format("The check sum did not calculate correctly. The expected check sum was %1$s. " + "Please make sure to validate that you typed the PGLN correctly. It's possible the check sum " + "was typed correctly but another number was entered wrong.", checksum);
			}

			return (null);
		}
		else if (pglnStr.startsWith("urn:") && pglnStr.contains(":party:"))
		{
			return (null);
		}
		else if (pglnStr.startsWith("urn:") && (pglnStr.contains(":id:pgln:") || pglnStr.contains(":id:sgln:")))
		{
			String[] pieces_1 = pglnStr.split("[:]", -1);
			String[] pieces = pieces_1[pieces_1.length - 1].split("[.]", -1);
			if (pieces.length < 2)
			{
				return ("This is supposed to contain the company prefix and the location code. Did not find these two pieces.");
			}
			String lastPiece = pieces[0] + pieces[1];
			if (!StringExtensions.isOnlyDigits(lastPiece))
			{
				return ("This is supposed to be a GS1 PGLN based on the System Prefix and " + "Data Type Prefix. That means the Company Prefix and Serial Numbers " + "should only be digits. Found non-digit characters in the Company Prefix " + "or Serial Number.");
			}
			else if (lastPiece.length() != 12)
			{
				return ("This is supposed to be a GS1 PGLN based on the System Prefix and Data Type " + "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " + "total of 12 digits between the two. The total number of digits when combined " + "is " + lastPiece.length() + ".");
			}

			return (null);
		}
		else
		{
			return ("The PGLN is not in a valid EPCIS URI format or in GS1 (P)GLN-13 format. PGLN = " + pglnStr);
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool TryParse(string? pglnStr, out System.Nullable<PGLN> pgln, out string? error)
	public static boolean TryParse(String pglnStr, tangible.OutObject<PGLN> pgln, tangible.OutObject<String> error)
	{
		error.outArgValue = PGLN.DetectPGLNIssue(pglnStr);
		if ((error.outArgValue == null || error.outArgValue.isBlank()))
		{
			pgln.outArgValue = new PGLN(pglnStr);
			return true;
		}
		else
		{
			pgln.outArgValue = null;
			return false;
		}
	}

	public final Object Clone()
	{
		PGLN pgln = new PGLN(this.toString());
		return pgln;
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region Overrides

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator ==(PGLN? obj1, PGLN? obj2)
	public static boolean opEquals(PGLN obj1, PGLN obj2)
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
//ORIGINAL LINE: public static bool operator !=(PGLN? obj1, PGLN? obj2)
	public static boolean opNotEquals(PGLN obj1, PGLN obj2)
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
			return false;
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

		return this.IsEquals((PGLN)obj);
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
		return this._pglnStr.toLowerCase();
	}

	public final boolean equals(PGLN pgln)
	{
		if (null == pgln)
		{
			return false;
		}

		if (this == pgln)
		{
			return true;
		}

		return this.IsEquals(pgln);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private bool IsEquals(PGLN? pgln)
	private boolean IsEquals(PGLN pgln)
	{
		if (pgln == null)
		{
			throw new NullPointerException("pgln");
		}

		return Objects.equals(this.toString().toLowerCase(), pgln.toString().toLowerCase());
	}

	public final int compareTo(PGLN pgln)
	{
		if (pgln == null)
		{
			throw new NullPointerException("pgln");
		}

		long myInt64Hash = HashCodeUtility.getInt64HashCode(this.toString().toLowerCase());
		long otherInt64Hash = HashCodeUtility.getInt64HashCode(pgln.toString().toLowerCase());

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
