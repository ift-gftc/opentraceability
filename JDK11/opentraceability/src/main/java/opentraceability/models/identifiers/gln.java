package opentraceability.models.identifiers;

import Newtonsoft.Json.*;
import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

/** 
 Global Location Number - used for identifying SCE's in Full Chain Traceability.
*/
//C# TO JAVA CONVERTER TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [DataContract][JsonConverter(typeof(GLNConverter))] public class GLN : IEquatable<GLN>, IComparable<GLN>
public class GLN implements IEquatable<GLN>, java.lang.Comparable<GLN>
{
	private String _glnStr = "";

	public GLN()
	{
	}

	public GLN(String glnStr)
	{
		try
		{
			String error = GLN.DetectGLNIssue(glnStr);
			if (!(error == null || error.isBlank()))
			{
				throw new RuntimeException(String.format("The GLN %1$s is invalid. %2$s", glnStr, error));
			}
			this._glnStr = glnStr;
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	public final boolean IsGS1PGLN()
	{
		return (_glnStr.contains(":id:sgln:"));
	}

	public final String ToDigitalLinkURL()
	{
		try
		{
			if (IsGS1PGLN())
			{
				String[] gtinParts = _glnStr.split("[:]", -1).Last().split("[.]", -1);
				String pgln = gtinParts[0] + gtinParts[1];
				pgln = pgln + GS1Util.CalculateGLN13CheckSum(pgln);
				return String.format("414/%1$s", pgln);
			}
			else
			{
				return String.format("414/%1$s", this._glnStr);
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	public static boolean IsGLN(String glnStr)
	{
		try
		{
			if (DetectGLNIssue(glnStr) == null)
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

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static string? DetectGLNIssue(string glnStr)
	public static String DetectGLNIssue(String glnStr)
	{
		try
		{
			if (tangible.StringHelper.isNullOrEmpty(glnStr))
			{
				return ("The GLN is NULL or EMPTY.");
			}
			else if (StringExtensions.IsURICompatibleChars(glnStr) == false)
			{
				return ("The GLN contains non-compatiable characters for a URI.");
			}
			else if (glnStr.contains(" "))
			{
				return ("GLN cannot contain spaces.");
			}
			else if (glnStr.length() == 13 && StringExtensions.IsOnlyDigits(glnStr))
			{
				// we don't care about validating the company prefix anymore
				// string cp = GCPLookUp.DetermineCompanyPrefix(glnStr);
				// if (string.IsNullOrWhiteSpace(cp))
				// {
				//     return "Invalid Company Prefix.";
				// }

				// validate the checksum
				char checksum = GS1Util.CalculateGLN13CheckSum(glnStr);
				if (checksum != glnStr.toCharArray().Last())
				{
					return String.format("The check sum did not calculate correctly. The expected check sum was %1$s. " + "Please make sure to validate that you typed the GLN correctly. It's possible the check sum " + "was typed correctly but another number was entered wrong.", checksum);
				}

				return (null);
			}
			else if (glnStr.startsWith("urn:") && glnStr.contains(":location:loc:"))
			{
				return (null);
			}
			else if (glnStr.startsWith("urn:") && glnStr.contains(":location:extension:loc:"))
			{
				return (null);
			}
			else if (glnStr.contains(":id:sgln:"))
			{
				String[] pieces = glnStr.split("[:]", -1).Last().split("[.]", -1);
				if (pieces.length < 2)
				{
					throw new RuntimeException(String.format("The GLN %1$s is not valid.", glnStr));
				}

				String lastPiece = pieces[0] + pieces[1];
				if (!StringExtensions.IsOnlyDigits(lastPiece))
				{
					return ("This is supposed to be a GS1 GLN based on the System Prefix and " + "Data Type Prefix. That means the Company Prefix and Serial Numbers " + "should only be digits. Found non-digit characters in the Company Prefix " + "or Serial Number.");
				}
				else if (lastPiece.length() != 12)
				{
					return ("This is supposed to be a GS1 GLN based on the System Prefix and Data Type " + "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " + "total of 12 digits between the two. The total number of digits when combined " + "is " + lastPiece.length() + ".");
				}

				return (null);
			}
			else
			{
				return ("The GLN is not in a valid EPCIS URI format or in GS1 GLN-13 format. Value = " + glnStr);
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool TryParse(string glnStr, out GLN? gln, out System.Nullable<string> error)
	public static boolean TryParse(String glnStr, tangible.OutObject<GLN> gln, tangible.OutObject<String> error)
	{
		try
		{
			error.outArgValue = GLN.DetectGLNIssue(glnStr);
			if ((error.outArgValue == null || error.outArgValue.isBlank()))
			{
				gln.outArgValue = new GLN(glnStr);
				return true;
			}
			else
			{
				gln.outArgValue = null;
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
		GLN gln = new GLN(this.toString());
		return gln;
	}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region Overrides

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator ==(GLN? obj1, GLN? obj2)
	public static boolean opEquals(GLN obj1, GLN obj2)
	{
		try
		{
			if (null == obj1 && null == obj2)
			{
				return true;
			}

			if (null == obj1 && null == obj2)
			{
				return false;
			}

			if (null == obj1 && null != obj2)
			{
				return false;
			}

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
///#pragma warning disable CS8602 // Dereference of a possibly null reference.
			return obj1.equals(obj2);
//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
///#pragma warning restore CS8602 // Dereference of a possibly null reference.
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static bool operator !=(GLN? obj1, GLN? obj2)
	public static boolean opNotEquals(GLN obj1, GLN obj2)
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

			return this.IsEquals((GLN)obj);
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
			return _glnStr.toLowerCase();
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
		///#region IEquatable<GLN>

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public bool Equals(GLN? gln)
	public final boolean equals(GLN gln)
	{
		try
		{
			if (null == gln)
			{
				return false;
			}

			if (this == gln)
			{
				return true;
			}

			if (gln == null)
			{
				return false;
			}
			else
			{
				return Objects.equals(this.toString(), gln.toString());
			}
		}
		catch (RuntimeException Ex)
		{
			OTLogger.Error(Ex);
			throw Ex;
		}
	}

	private boolean IsEquals(GLN gln)
	{
		try
		{
			if (null == gln)
			{
				return false;
			}

			if (Objects.equals(this.toString().toLowerCase(), gln.toString().toLowerCase()))
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
		///#endregion IEquatable<GLN>

//C# TO JAVA CONVERTER TASK: There is no preprocessor in Java:
		///#region IComparable

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public int CompareTo(GLN? gln)
	public final int compareTo(GLN gln)
	{
		try
		{
			if (null == gln)
			{
				throw new NullPointerException("gln");
			}

			long myInt64Hash = ObjectExtensions.GetInt64HashCode(this.toString());
			long otherInt64Hash = ObjectExtensions.GetInt64HashCode(gln.toString());

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
