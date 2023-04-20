using Force.Crc32;
using System.Text;

namespace OpenTraceability.Utility
{
    public static class ObjectExtensions
    {
        public static Int32 GetInt32HashCode(this string strText)
        {
            Int32 hashCode = 0;
            if (!string.IsNullOrEmpty(strText))
            {
                hashCode = (int)Crc32Algorithm.Compute(Encoding.UTF8.GetBytes(strText));
            }
            return (hashCode);
        }

        public static Int16 GetInt16HashCode(this string strText)
        {
            Int64 i64Hash = strText.GetInt64HashCode();
            Int16 hash = (Int16)(i64Hash & 0xFFFF);
            hash ^= (Int16)((i64Hash >> 16) & 0xFFFF);
            hash ^= (Int16)((i64Hash >> 32) & 0xFFFF);
            hash ^= (Int16)((i64Hash >> 48) & 0xFFFF);
            return hash;
        }

        public static Int64 GetInt64HashCode(this string strText)
        {
            Int64 hashCode = 0;
            if (!string.IsNullOrEmpty(strText))
            {
                //Unicode Encode Covering all characterset
                byte[] byteContents = Encoding.Unicode.GetBytes(strText);
#pragma warning disable SYSLIB0021 // Type or member is obsolete
                System.Security.Cryptography.SHA256 hash = new System.Security.Cryptography.SHA256CryptoServiceProvider();
#pragma warning restore SYSLIB0021 // Type or member is obsolete
                byte[] hashText = hash.ComputeHash(byteContents);
                //32Byte hashText separate
                //hashCodeStart = 0~7  8Byte
                //hashCodeMedium = 8~23  8Byte
                //hashCodeEnd = 24~31  8Byte
                //and Fold
                Int64 hashCodeStart = BitConverter.ToInt64(hashText, 0);
                Int64 hashCodeMedium = BitConverter.ToInt64(hashText, 8);
                Int64 hashCodeEnd = BitConverter.ToInt64(hashText, 24);
                hashCode = hashCodeStart ^ hashCodeMedium ^ hashCodeEnd;
            }
            return (hashCode);
        }
    }
}