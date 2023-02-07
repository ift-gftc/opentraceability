using DSUtil;
using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.Models.Identifiers
{
    public static class GS1Util
    {
        static private bool IsEven(int i)
        {
            try
            {
                if ((i % 2) == 0)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        internal static int CharToInt32(char charInt)
        {
            try
            {
                int iRet = 0;
                switch (charInt)
                {
                    case '0': iRet = 0; break;
                    case '1': iRet = 1; break;
                    case '2': iRet = 2; break;
                    case '3': iRet = 3; break;
                    case '4': iRet = 4; break;
                    case '5': iRet = 5; break;
                    case '6': iRet = 6; break;
                    case '7': iRet = 7; break;
                    case '8': iRet = 8; break;
                    case '9': iRet = 9; break;
                    default: throw new ArgumentException("Must give a single digit numeral string.");
                }
                return (iRet);
            }
            catch (Exception)
            {
                throw;
            }
        }
        internal static char Int32ToChar(Int32 charInt)
        {
            try
            {
                char iRet = '0';
                switch (charInt)
                {
                    case 0: iRet = '0'; break;
                    case 1: iRet = '1'; break;
                    case 2: iRet = '2'; break;
                    case 3: iRet = '3'; break;
                    case 4: iRet = '4'; break;
                    case 5: iRet = '5'; break;
                    case 6: iRet = '6'; break;
                    case 7: iRet = '7'; break;
                    case 8: iRet = '8'; break;
                    case 9: iRet = '9'; break;
                    default: throw new ArgumentException("Must give a single digit numeral string.");
                }
                return (iRet);
            }
            catch (Exception)
            {
                throw;
            }
        }
        internal static int[] BreakIntoDigits(string strInt)
        {
            try
            {
                List<int> rtnInts = new List<int>();

                for (int i = 0; i < strInt.Length; i++)
                {
                    rtnInts.Add(CharToInt32(strInt[i]));
                }

                return rtnInts.ToArray();
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        public static char CalculateGTIN14CheckSum(string strGS)
        {
            try
            {
                if (strGS == null)
                {
                    throw new ArgumentNullException("strGS");
                }

                if (strGS.Length == 14)
                {
                    strGS = strGS.Substring(0, 13);
                }

                int[] gsDigits = BreakIntoDigits(strGS);
                int sum = 0;

                for (int i = 0; i < (gsDigits.Length); i++)
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
            catch (ArgumentNullException anEx)
            {
                DSLogger.Log(0, anEx);
                throw;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
        public static char CalculateGLN13CheckSum(string strGS)
        {
            try
            {
                if (strGS == null)
                {
                    throw new ArgumentNullException("strGS");
                }

                if (strGS.Length == 13)
                {
                    strGS = strGS.Substring(0, 12);
                }

                int[] gsDigits = BreakIntoDigits(strGS);
                int sum = 0;

                for (int i = 0; i < (gsDigits.Length); i++)
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
            catch (ArgumentNullException anEx)
            {
                DSLogger.Log(0, anEx);
                throw;
            }
            catch (Exception Ex)
            {
                OTLogger.Error(Ex);
                throw;
            }
        }
    }
}
