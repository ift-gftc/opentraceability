using Json.Schema;
using Newtonsoft.Json.Linq;
using OpenTraceability.Utility;
using System.Collections.ObjectModel;
using System.Linq;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json.Nodes;
using System.Xml;
using System.Xml.Linq;

namespace OpenTraceability.Kotlin
{
    public static class NetToKotlinConverter
    {
        public static string ConvertToKotlinClass(Type csharpType)
        {
            try
            {
                string name = csharpType.Name;

                StringBuilder kotlinClass = new StringBuilder();

                // Class name
                if (csharpType.IsGenericType)
                {
                    kotlinClass.AppendLine($"class {csharpType.Name.Split('`').First()}<T> {{");
                }
                else
                {
                    kotlinClass.AppendLine($"class {csharpType.Name} {{");
                }

                object? target = null;
                if (!(csharpType.IsClass && csharpType.IsSealed && csharpType.IsAbstract))
                {
                    try
                    {
                        target = Activator.CreateInstance(csharpType);
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Failed to make instance of: " + csharpType.FullName);
                    }
                }

                // Fields
                FieldInfo[] fields = csharpType.GetFields(BindingFlags.Instance);
                foreach (var field in fields)
                {
                    string line = WriteField(field, "    ", target);
                    kotlinClass.AppendLine(line);
                }

                // Properties
                PropertyInfo[] properties = csharpType.GetProperties();
                foreach (var property in properties)
                {
                    string propName = property.Name;
                    string line = WriteProperty(property, "    ", target);
                    kotlinClass.AppendLine(line);
                }

                // Methods
                MethodInfo[] methods = csharpType.GetMethods(BindingFlags.Instance);
                foreach (var method in methods)
                {
                    WriteMethod(kotlinClass, method, "    ");
                }

                // Static stuff...

                // Fields
                kotlinClass.AppendLine($"    companion object{{");
                FieldInfo[] static_fields = csharpType.GetFields(BindingFlags.Public | BindingFlags.Static);
                foreach (var field in static_fields)
                {
                    string line = WriteField(field, "        ");
                    kotlinClass.AppendLine(line);
                }

                // Properties
                PropertyInfo[] static_properties = csharpType.GetProperties(BindingFlags.Public | BindingFlags.Static);
                foreach (var property in static_properties)
                {
                    string propName = property.Name;
                    string line = WriteProperty(property, "        ");
                    kotlinClass.AppendLine(line);
                }
                kotlinClass.AppendLine($"    }}");

                // Methods
                MethodInfo[] static_methods = csharpType.GetMethods(BindingFlags.Public | BindingFlags.Static);
                foreach (var method in static_methods)
                {
                    WriteMethod(kotlinClass, method, "        ");
                }

                kotlinClass.AppendLine("}");


                WriteImports(kotlinClass);
                WriteNamespace(csharpType, kotlinClass);
                RemoveWords(kotlinClass);


                var output = kotlinClass.ToString();
                if (output.Contains("<T>"))
                {
                    output = output.Replace($"class {csharpType.Name} ", $"class {csharpType.Name}<T> ");
                }


                return output;
            }
            catch (Exception ex)
            {
                throw new Exception($"Failed to build kotlin class for {csharpType}", ex);
            }
        }

        public static string ConvertToKotlinInterface(Type csharpInterface)
        {
            StringBuilder kotlinInterface = new StringBuilder();

            // Interface name
            kotlinInterface.AppendLine($"interface {csharpInterface.Name} {{");

            // Methods
            MethodInfo[] methods = csharpInterface.GetMethods();
            foreach (var method in methods)
            {
                // Method signature

                string returnType = ConvertType(method.ReturnType);
                if (returnType == "T")
                {
                    kotlinInterface.AppendLine($"    fun<{returnType}> {method.Name}({ConvertParameters(method.GetParameters())}): {returnType}");
                }
                else
                {
                    kotlinInterface.AppendLine($"    fun {method.Name}({ConvertParameters(method.GetParameters())}): {returnType}");
                }
            }

            kotlinInterface.AppendLine("}");

            WriteImports(kotlinInterface);
            WriteNamespace(csharpInterface, kotlinInterface);
            RemoveWords(kotlinInterface);



            var output = kotlinInterface.ToString();
            if (output.Contains("<T>"))
            {
                output = output.Replace($"interface {csharpInterface.Name} ", $"interface {csharpInterface.Name}<T> ");
            }


            return output;
        }

        public static string ConvertToKotlinEnum(Type csharpEnum)
        {
            StringBuilder kotlinEnum = new StringBuilder();

            // Enum name
            kotlinEnum.AppendLine($"enum class {csharpEnum.Name} {{");

            // Enum values
            Array enumValues = Enum.GetValues(csharpEnum);
            foreach (var value in enumValues)
            {
                kotlinEnum.AppendLine($"    {value},");
            }

            kotlinEnum.AppendLine("}");

            WriteNamespace(csharpEnum, kotlinEnum);

            return kotlinEnum.ToString();
        }

        private static string ConvertType(Type type)
        {
            string returnData = string.Empty;

            string suffix = "";

            Type? nullableType = Nullable.GetUnderlyingType(type);
            if (nullableType != null)
            {
                returnData = ConvertType(nullableType) + "?";
            }
            else if (type.IsGenericType == true)
            {
                string genArgs = string.Join(",", type.GetGenericArguments().Select(a => ConvertType(a)));
                if (type.GetGenericTypeDefinition() == typeof(List<>) || type.GetGenericTypeDefinition() == typeof(ReadOnlyCollection<>))
                {
                    return $"List<{genArgs}>" ?? throw new Exception("Failed to build List Convert Type.");
                }
                else
                {
                    return $"{type.FullName?.Split('`').FirstOrDefault()}<{genArgs}>" ?? throw new Exception("Failed to build List Convert Type.");
                }
            }
            else
            {
                //if (type.GetCustomAttributes().Any(a => a.GetType().FullName == "System.Runtime.CompilerServices.NullableAttribute"))
                //{
                //    suffix = "?";
                //}

                if (type == typeof(int))
                {
                    returnData = "Int";
                }
                else if (type == typeof(bool))
                {
                    returnData = "Boolean";
                }
                else if (type == typeof(string) || type == typeof(XNamespace))
                {
                    returnData = "String";
                }
                else if (type == typeof(DateTimeOffset))
                {
                    returnData = "OffsetDateTime";
                }
                else if (type == typeof(TimeSpan))
                {
                    returnData = "Duration";
                }
                else if (type == typeof(Uri))
                {
                    returnData = "URI?";
                }
                else if (type == typeof(Int16))
                {
                    returnData = "Short";
                }
                else if (type == typeof(Int64))
                {
                    returnData = "Long";
                }
                else if (type == typeof(XElement))
                {
                    returnData = "XmlElement";
                }
                else if (type == typeof(XDocument))
                {
                    returnData = "XmlDocument";
                }
                else if (type == typeof(JToken))
                {
                    returnData = "JsonToken";
                }
                else if (type == typeof(JObject))
                {
                    returnData = "JsonObject";
                }
                else if (type.Name.Contains("XDocument&"))
                {
                    returnData = "XmlDocument?";
                }
                else
                {
                    // Handle other types as needed
                    returnData = type.Name;
                }

                returnData += suffix;
            }

            returnData = returnData.Replace("&", string.Empty);

            return returnData;
        }

        private static string ConvertParameters(ParameterInfo[] parameters)
        {
            List<string> convertedParameters = new List<string>();
            foreach (var parameter in parameters)
            {
                string convertedType = ConvertType(parameter.ParameterType);

                convertedParameters.Add($"{parameter.Name}: {convertedType}");
            }
            return string.Join(", ", convertedParameters);
        }

        private static string WriteField(FieldInfo field, string spacing, object? target = null)
        {
            try
            {
                string constLabel = (field.IsLiteral && !field.IsInitOnly) ? "const val" : "var";

                string line = $"{spacing}{constLabel} {field.Name}: {ConvertType(field.FieldType)}";

                if (field.FieldType.ContainsGenericParameters == false)
                {
                    // if this is STATIC or our TARGET is not null...
                    if (field.IsStatic || target != null)
                    {
                        object? v = field.GetValue(target);
                        if (v != null)
                        {
                            if (v is string || v is XNamespace)
                            {
                                line += $" = \"{v.ToString()}\"";
                            }
                            else
                            {
                                line += $" = {v.ToString()}()";
                            }

                        }
                    }
                }

                return line;
            }
            catch (Exception ex)
            {
                string nullText = (target == null) ? "NULL" : "NOT NULL";
                throw new Exception($"Failed to read field {field.Name}:{field.FieldType}. target is {nullText}.", ex);
            }
        }

        private static string WriteProperty(PropertyInfo property, string spacing, object? target = null)
        {
            try
            {
                bool addedInitializer = false;
                string line = $"{spacing}var {property.Name}: {ConvertType(property.PropertyType)}";

                // if this is STATIC or our TARGET is not null...
                if (property.GetAccessors(true).Any(a => a.IsStatic) || target != null)
                {
                    object? v = property.GetValue(target);
                    if (v != null)
                    {
                        if (property.PropertyType == typeof(string) || property.PropertyType == typeof(XNamespace))
                        {
                            addedInitializer = true;
                            line += $" = \"{v.ToString()}\"";
                        }
                    }
                }

                if (!addedInitializer)
                {
                    if (Nullable.GetUnderlyingType(property.PropertyType) != null)
                    {
                        line += " = null";
                    }
                    else
                    {
                        if (property.PropertyType.IsGenericType && (property.PropertyType.GetGenericTypeDefinition() == typeof(List<>) || property.PropertyType.GetGenericTypeDefinition() == typeof(ReadOnlyCollection<>)))
                        {
                            string genArgs = string.Join(",", property.PropertyType.GetGenericArguments().Select(a => ConvertType(a)));
                            line += $" = ArrayList<{genArgs}>()";
                        }
                        else
                        {
                            line += $" = {ConvertType(property.PropertyType)}()";
                        }
                    }
                }

                return line;
            }
            catch (Exception ex)
            {
                string nullText = (target == null) ? "NULL" : "NOT NULL";
                throw new Exception($"Failed to read property {property.Name}:{property.PropertyType}. target is {nullText}.", ex);
            }
        }

        private static void WriteMethod(StringBuilder kotlinClass, MethodInfo method, string spacing)
        {
            if (method.Name == "GetType" || method.Name == "GetHashCode" || method.Name == "ToString" || method.Name == "Invoke" || method.Name == "BeginInvoke" || method.Name == "EndInvoke")
            {
                return;
            }

            // Skip special methods like getters/setters or constructors
            if (method.IsSpecialName || method.IsConstructor)
                return;

            // Method signature
            kotlinClass.AppendLine($"");
            kotlinClass.AppendLine($"{spacing}// [OT_AutoGenerated_FROM]: " + method.Name);
            kotlinClass.AppendLine($"{spacing}// [OT_AutoGenerated_FROM]: " + method.Name);
            kotlinClass.AppendLine($"{spacing}// [OT_AutoGenerated_HASH]: " + GenerateHashFromMethodInfoBody(method));


            string returnType = ConvertType(method.ReturnType);

            if (!string.IsNullOrEmpty(returnType) && returnType == "Void")
            {
                returnType = string.Empty;
            }
            var returnTypeString = (!string.IsNullOrEmpty(returnType) ? $": {returnType}" : string.Empty);

            if (returnType == "T")
            {
                kotlinClass.AppendLine($"{spacing}fun<{returnType}> {method.Name}({ConvertParameters(method.GetParameters())}){returnTypeString} {{");
            }
            else if (method.ReturnType.Name.Contains("`2"))
            {
                Type listItemType1 = method.ReturnType.GetGenericArguments()[0];
                Type listItemType2 = method.ReturnType.GetGenericArguments()[1];

                kotlinClass.AppendLine($"{spacing}fun<{listItemType1},{listItemType2}> {method.Name}({ConvertParameters(method.GetParameters())}){returnTypeString} {{");
            }
            else
            {
                kotlinClass.AppendLine($"{spacing}fun {method.Name}({ConvertParameters(method.GetParameters())}){returnTypeString} {{");
            }

            kotlinClass.AppendLine($"{spacing}    // Method body goes here");

            if (!string.IsNullOrEmpty(returnType) && returnType != "Void")
            {
                if (returnType == "Boolean" || returnType == "Boolean?")
                {
                    kotlinClass.AppendLine($"{spacing}    return 1==1");
                }
                else if (returnType == "Double" || returnType == "Double?")
                {
                    kotlinClass.AppendLine($"{spacing}    return 0.0");
                }
                else if (returnType == "Char" || returnType == "Char?")
                {
                    kotlinClass.AppendLine($"{spacing}    return ' '");
                }
                else if (returnType == "Short" || returnType == "Short?")
                {
                    kotlinClass.AppendLine($"{spacing}    return 0");
                }
                else if (returnType == "Long" || returnType == "Long?")
                {
                    kotlinClass.AppendLine($"{spacing}    return 0");
                }
                else if (returnType == "Int" || returnType == "Int?")
                {
                    kotlinClass.AppendLine($"{spacing}    return 0");
                }
                else if (returnType == "URI" || returnType == "URI?")
                {
                    kotlinClass.AppendLine($"{spacing}    return URI(\"\")");
                }
                else if (returnType == "OffsetDateTime" || returnType == "OffsetDateTime?")
                {
                    kotlinClass.AppendLine($"{spacing}    return OffsetDateTime.now()");
                }
                else if (returnType == "List<HttpClient>" || returnType == "List<HttpClient>?")
                {
                    kotlinClass.AppendLine($"{spacing}    return listOf(HttpClient.newHttpClient())");
                }
                else if (returnType == "HttpClient" || returnType == "HttpClient?")
                {
                    kotlinClass.AppendLine($"{spacing}    return HttpClient.newHttpClient()");
                }
                else if (returnType == "List<T>" || returnType == "List<T>?")
                {
                    kotlinClass.AppendLine($"{spacing}    return ArrayList<T>()");
                }
                else if (returnType == "List<String>" || returnType == "List<String>?")
                {
                    kotlinClass.AppendLine($"{spacing}    return ArrayList<String>()");
                }
                else if (returnType == "List<URI>" || returnType == "List<URI>?")
                {
                    kotlinClass.AppendLine($"{spacing}    return listOf(URI(\"\"))");
                }
                else if (returnType == "List<EPCISQueryResults>" || returnType == "List<EPCISQueryResults>?")
                {
                    kotlinClass.AppendLine($"{spacing}    return ArrayList<EPCISQueryResults>()");
                }
                else
                {
                    kotlinClass.AppendLine($"{spacing}    return {returnType}()");
                }
            }


            kotlinClass.AppendLine($"{spacing}}}");
        }

        private static string GenerateHashFromMethodInfoBody(MethodInfo methodInfo)
        {
            // generate a SHA-256 hash from the method info body
            using (SHA256 sha256Hash = SHA256.Create())
            {
                // get the method body
                MethodBody? methodBody = methodInfo.GetMethodBody();
                if (methodBody == null)
                {
                    throw new Exception($"Failed to get method body for {methodInfo.Name}");
                }
                // get the IL bytes
                byte[]? ilBytes = methodBody.GetILAsByteArray();
                if (ilBytes == null)
                {
                    throw new Exception($"Failed to get IL bytes for {methodInfo.Name}");
                }
                // hash the bytes
                byte[] hashBytes = sha256Hash.ComputeHash(ilBytes);
                // convert to hex string
                StringBuilder sb = new StringBuilder();
                foreach (byte b in hashBytes)
                {
                    sb.Append(b.ToString("x2"));
                }
                return sb.ToString();
            }
        }


        private static void WriteImports(StringBuilder sb)
        {
            string fileText = sb.ToString();

            if (fileText.Contains("OffsetDateTime"))
            {
                sb.Insert(0, "import java.time.OffsetDateTime" + Environment.NewLine);
            }

            if (fileText.Contains("URI"))
            {
                sb.Insert(0, "import java.net.URI" + Environment.NewLine);
            }

            if (fileText.Contains("Type"))
            {
                sb.Insert(0, "import java.lang.reflect.Type" + Environment.NewLine);
            }

            if (fileText.Contains("Duration"))
            {
                sb.Insert(0, "import java.time.Duration" + Environment.NewLine);
            }

            if (fileText.Contains("EPCISDocument") || fileText.Contains("EPCISQueryDocument") || fileText.Contains("SensorElement")
                 || fileText.Contains("EPCISBaseDocument") || fileText.Contains("EPCISVersion"))
            {
                if (!sb.ToString().Contains("import models.events.*"))
                {
                    sb.Insert(0, "import models.events.*" + Environment.NewLine);
                }
            }

            if (fileText.Contains("CertificationList"))
            {
                sb.Insert(0, "import models.events.kdes.CertificationList" + Environment.NewLine);
            }

            if (fileText.Contains("EPC") || fileText.Contains("PGLN"))
            {
                sb.Insert(0, "import models.identifiers.*" + Environment.NewLine);
            }


            if (fileText.Contains("HttpClient"))
            {
                sb.Insert(0, "import java.net.http.HttpClient" + Environment.NewLine);
            }

            if (fileText.Contains("Dictionary<") || fileText.Contains("ArrayList"))
            { 
                sb.Insert(0, "import java.util.*" + Environment.NewLine);
            }


            if (fileText.Contains("XmlElement"))
            {
                sb.Insert(0, "import javax.xml.bind.annotation.*" + Environment.NewLine);
            }


            if (fileText.Contains("XmlDocument"))
            {
                sb.Insert(0, "import com.intellij.psi.xml.XmlDocument" + Environment.NewLine);
            }

            if (fileText.Contains("JsonToken"))
            {
                sb.Insert(0, "import com.fasterxml.jackson.core.JsonToken" + Environment.NewLine);
            }

            if (fileText.Contains("JsonObject"))
            {
                sb.Insert(0, "import com.intellij.json.psi.JsonObject" + Environment.NewLine);
            }
        }

        private static void WriteNamespace(Type type, StringBuilder sb)
        {
            var arr = type.Namespace.Split('.');

            if (arr.Length > 1)
            {
                string convertedNamespace = string.Join(".", arr).ToLowerInvariant();
                convertedNamespace = convertedNamespace.Replace("opentraceability.", string.Empty);

                sb.Insert(0, "package " + convertedNamespace + Environment.NewLine);
            }
        }

        private static void RemoveWords(StringBuilder sb)
        {
            sb.Replace("System.String,System.String", "String");
            sb.Replace("OpenTraceability.", string.Empty);
            sb.Replace("Mappers.", string.Empty);


        }
    }
}