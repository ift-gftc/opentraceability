/*
 * WELCOME!
 * 
 * This is a library used for automatically building some of the Kotlin (java) code
 * in the Java translated version of this library.
 */
using OpenTraceability.Kotlin;

Console.WriteLine("Getting started...");

// lets go through each class in the following namespaces and start building them in our kotlin code...
List<string> namespaces = new List<string>()
{
    "OpenTraceability"
};

//string rootKotlinPath = @"C:\GitHub\DuckScapePhilip\opentraceability\Java\OpenTraceability\src\main\";
string rootKotlinPath = @"C:\Projects\Philip\opentraceability\Java\OpenTraceability\src\main\";

foreach (var ns in namespaces)
{
    // go through each class
    foreach (var type in typeof(OpenTraceability.Setup).Assembly.GetTypes())
    {
        if (type.Namespace != null && type.Namespace.StartsWith("OpenTraceability"))
        {
            try
            {
                // start building our kotlin classes...
                string path = rootKotlinPath + type.Namespace.ToLower().Replace('.', '\\') + "\\";
                Directory.CreateDirectory(path);

                // build our file name
                string filename = path + type.Name.ToLowerInvariant() + ".kt";
                if (filename.Contains("<>") || filename.Contains(">d__"))
                {
                    continue;
                }

                // write the kotlin code
                if (type.IsInterface)
                {
                    string kotlinInterface = NetToKotlinConverter.ConvertToKotlinInterface(type);

                    // write the class
                    File.WriteAllText(filename, kotlinInterface);
                }
                else if (type.IsClass)
                {
                    string kotlinClass = NetToKotlinConverter.ConvertToKotlinClass(type);

                    // write the class
                    File.WriteAllText(filename, kotlinClass);
                }
                else if (type.IsEnum)
                {
                    string kotlinEnum = NetToKotlinConverter.ConvertToKotlinEnum(type);

                    // write the class
                    File.WriteAllText(filename, kotlinEnum);
                }
            }
            catch (Exception ex) 
            {
                Console.WriteLine("Failed to write class: " + type.FullName);
                Console.WriteLine(ex.ToString());
            }
        }
    }
}
