using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Xml.Linq;

namespace OpenTraceability.Kotlin
{
    public static class NetToKotlinConverter
    {
        public static string ConvertToKotlinClass(Type csharpType)
        {
            try
            {

                StringBuilder kotlinClass = new StringBuilder();

                // Class name
                kotlinClass.AppendLine($"class {csharpType.Name} {{");

                object? target = null;
                if (csharpType.IsClass && csharpType.IsSealed && csharpType.IsAbstract)
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
                PropertyInfo[] properties = csharpType.GetProperties(BindingFlags.Instance);
                foreach (var property in properties)
                {
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

                return kotlinClass.ToString();
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
                kotlinInterface.AppendLine($"    fun {method.Name}({ConvertParameters(method.GetParameters())}): {ConvertType(method.ReturnType)}");
            }

            kotlinInterface.AppendLine("}");

            WriteImports(kotlinInterface);

            return kotlinInterface.ToString();
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

            return kotlinEnum.ToString();
        }

        private static string ConvertType(Type type)
        {
            Type? nullableType = Nullable.GetUnderlyingType(type);
            if (nullableType != null)
            {
                return ConvertType(nullableType) + "?";
            }
            else if (type == typeof(int))
            {
                return "Int";
            }
            else if (type == typeof(bool))
            {
                return "Boolean";
            }
            else if (type == typeof(string) || type == typeof(XNamespace))
            {
                return "String";
            }
            else if (type == typeof(DateTimeOffset))
            {
                return "OffsetDateTime";
            }
            else if (type == typeof(TimeSpan))
            {
                return "TimeSpan"; //TODO: Check this
            }
            else if (type == typeof(Uri))
            {
                return "URI";
            }
            else if (type.IsGenericType && type.GetGenericTypeDefinition() == typeof(List<>))
            {
                Type listItemType = type.GetGenericArguments()[0];
                return $"List<{ConvertType(listItemType)}>";
            }
            else
            {
                // Handle other types as needed
                return type.Name;
            }
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
                string line = $"{spacing}var {property.Name}: {ConvertType(property.PropertyType)}";
                if (property.PropertyType.ContainsGenericParameters == false)
                {
                    // if this is STATIC or our TARGET is not null...
                    if (property.GetAccessors(true).Any(a => a.IsStatic) || target != null)
                    {
                        object? v = property.GetValue(target);
                        if (v != null)
                        {
                            line += $" = \"{v.ToString()}\"";
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
            kotlinClass.AppendLine($"{spacing}fun {method.Name}({ConvertParameters(method.GetParameters())}): {ConvertType(method.ReturnType)} {{");
            kotlinClass.AppendLine($"{spacing}    // Method body goes here");
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
        }


    }
}