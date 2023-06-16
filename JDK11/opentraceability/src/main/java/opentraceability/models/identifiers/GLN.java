package opentraceability.models.identifiers;

import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

/** 
 Global Location Number - used for identifying SCE's in Full Chain Traceability.
*/
//C# TO JAVA CONVERTER TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [DataContract][JsonConverter(typeof(GLNConverter))] public class GLN : IEquatable<GLN>, IComparable<GLN>
public class GLN implements java.lang.Comparable<GLN>
{
	private String _glnStr = "";

	public GLN()
	{
	}

	public GLN(String glnStr)
	{
		String error = GLN.DetectGLNIssue(glnStr);
		if (!(error == null || error.isBlank()))
		{
			throw new RuntimeException(String.format("The GLN %1$s is invalid. %2$s", glnStr, error));
		}
		this._glnStr = glnStr;
	}

	public final boolean IsGS1GLN()
	{
		return (_glnStr.contains(":id:sgln:"));
	}

	public final String ToDigitalLinkURL()
	{
		if (IsGS1GLN())
		{
			String[] parts_step1 = _glnStr.split("[:]", -1);
			String[] parts = parts_step1[parts_step1.length - 1].split("[.]", - 1);
			String gln = parts[0] + parts[1];
			gln = gln + GS1Util.CalculateGLN13CheckSum(gln);
			return String.format("414/%1$s", gln);
		}
		else
		{
			return String.format("414/%1$s", this._glnStr);
		}
	}

	public static boolean IsGLN(String glnStr)
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

	public static String DetectGLNIssue(String glnStr)
	{
		if (tangible.StringHelper.isNullOrEmpty(glnStr))
		{
			return ("The GLN is NULL or EMPTY.");
		}
		else if (StringExtensions.isURI(glnStr) == false)
		{
			return ("The GLN contains non-compatible characters for a URI.");
		}
		else if (glnStr.contains(" "))
		{
			return ("GLN cannot contain spaces.");
		}
		else if (glnStr.length() == 13 && StringExtensions.isOnlyDigits(glnStr))
		{
			// validate the checksum
			char checksum = GS1Util.CalculateGLN13CheckSum(glnStr);
			if (checksum != glnStr.toCharArray()[glnStr.length() - 1])
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
			String[] pieces_1 = glnStr.split("[:]", -1);
			String[] pieces = pieces_1[pieces_1.length - 1].split("[.]", -1);
			if (pieces.length < 2)
			{
				throw new RuntimeException(String.format("The GLN %1$s is not valid.", glnStr));
			}

			String lastPiece = pieces[0] + pieces[1];
			if (!StringExtensions.isOnlyDigits(lastPiece))
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

	public static boolean TryParse(String glnStr, tangible.OutObject<GLN> gln, tangible.OutObject<String> error)
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

	public final Object Clone()
	{
		GLN gln = new GLN(this.toString());
		return gln;
	}

	public static boolean opEquals(GLN obj1, GLN obj2)
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

		return obj1.equals(obj2);
	}

	public static boolean opNotEquals(GLN obj1, GLN obj2)
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

		return this.IsEquals((GLN)obj);
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
		return _glnStr.toLowerCase();
	}

	public final boolean equals(GLN gln)
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

	private boolean IsEquals(GLN gln)
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

	public final int compareTo(GLN gln)
	{
		if (null == gln)
		{
			throw new NullPointerException("gln");
		}

		long myInt64Hash = HashCodeUtility.getInt64HashCode(this.toString());
		long otherInt64Hash = HashCodeUtility.getInt64HashCode(gln.toString());

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
