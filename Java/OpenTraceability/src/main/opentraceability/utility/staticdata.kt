package utility
class StaticData {
    companion object{
        fun ReadData(path: String): String {

            var result: String = ""

            /*
            using (Stream? stream = typeof(StaticData).Assembly.GetManifestResourceStream("OpenTraceability.Utility.Data." + path))
            {
                if (stream == null)
                {
                    throw new Exception("Failed to read static data from embedded resource at path " + path);
                }

                using (StreamReader sr = new StreamReader(stream))
                {
                    result = sr.ReadToEnd();
                }
            }
            */

            return result

            TODO("Not yet implemented")
        }
    }
}
