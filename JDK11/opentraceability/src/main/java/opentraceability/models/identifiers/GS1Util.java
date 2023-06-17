package opentraceability.models.identifiers;

import opentraceability.*;
import java.util.*;

public final class GS1Util
{
	private static boolean IsEven(int i)
	{
        return (i % 2) == 0;
	}
	public static int CharToInt32(char charInt)
	{
		try
		{
			int iRet = 0;
			switch (charInt)
			{
				case '0':
					iRet = 0;
					break;
				case '1':
					iRet = 1;
					break;
				case '2':
					iRet = 2;
					break;
				case '3':
					iRet = 3;
					break;
				case '4':
					iRet = 4;
					break;
				case '5':
					iRet = 5;
					break;
				case '6':
					iRet = 6;
					break;
				case '7':
					iRet = 7;
					break;
				case '8':
					iRet = 8;
					break;
				case '9':
					iRet = 9;
					break;
				default:
					throw new IllegalArgumentException("Must give a single digit numeral string.");
			}
			return (iRet);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
	}
	public static char Int32ToChar(int charInt)
	{
		try
		{
			char iRet = '0';
			switch (charInt)
			{
				case 0:
					iRet = '0';
					break;
				case 1:
					iRet = '1';
					break;
				case 2:
					iRet = '2';
					break;
				case 3:
					iRet = '3';
					break;
				case 4:
					iRet = '4';
					break;
				case 5:
					iRet = '5';
					break;
				case 6:
					iRet = '6';
					break;
				case 7:
					iRet = '7';
					break;
				case 8:
					iRet = '8';
					break;
				case 9:
					iRet = '9';
					break;
				default:
					throw new IllegalArgumentException("Must give a single digit numeral string.");
			}
			return (iRet);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
	}
	public static int[] BreakIntoDigits(String strInt)
	{
		ArrayList<Integer> rtnInts = new ArrayList<Integer>();

		for (int i = 0; i < strInt.length(); i++)
		{
			rtnInts.add(CharToInt32(strInt.charAt(i)));
		}

		return tangible.IntegerLists.toArray(rtnInts);
	}
	public static char CalculateGTIN14CheckSum(String strGS)
	{
		if (strGS == null)
		{
			throw new NullPointerException("strGS");
		}

		if (strGS.length() == 14)
		{
			strGS = strGS.substring(0, 13);
		}

		int[] gsDigits = BreakIntoDigits(strGS);
		int sum = 0;

		for (int i = 0; i < (gsDigits.length); i++)
		{
			if (IsEven(i))
			{
				sum += gsDigits[i] * 3;
			}
			else
			{
				sum += gsDigits[i];
			}
		}

		// Get the higher multiple of ten;
		int higherMultipleOfTen = 10;
		while (higherMultipleOfTen < sum)
		{
			higherMultipleOfTen += 10;
		}
		if (sum == 0)
		{
			higherMultipleOfTen = 0;
		}

		int determinedCheckSum = higherMultipleOfTen - sum;
		char charCheckSum = Int32ToChar(determinedCheckSum);
		return (charCheckSum);
	}
	public static char CalculateGLN13CheckSum(String strGS)
	{
		if (strGS == null)
		{
			throw new NullPointerException("strGS");
		}

		if (strGS.length() == 13)
		{
			strGS = strGS.substring(0, 12);
		}

		int[] gsDigits = BreakIntoDigits(strGS);
		int sum = 0;

		for (int i = 0; i < (gsDigits.length); i++)
		{
			if (IsEven(i))
			{
				sum += gsDigits[i];
			}
			else
			{
				sum += gsDigits[i] * 3;
			}
		}

		// Get the higher multiple of ten;
		int higherMultipleOfTen = 10;
		while (higherMultipleOfTen < sum)
		{
			higherMultipleOfTen += 10;
		}

		int determinedCheckSum = higherMultipleOfTen - sum;
		char charCheckSum = Int32ToChar(determinedCheckSum);
		return (charCheckSum);
	}
}
