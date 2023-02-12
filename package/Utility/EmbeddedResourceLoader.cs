using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    public class EmbeddedResourceLoader
    {
        Dictionary<string, Assembly> m_assemblyMap;

        public EmbeddedResourceLoader()
        {
            m_assemblyMap = new Dictionary<string, Assembly>();
        }

        private Assembly GetAssembly(string assemblyName)
        {
            Assembly? assembly = null;
            if (m_assemblyMap.ContainsKey(assemblyName))
            {
                assembly = m_assemblyMap[assemblyName];
            }
            else
            {
                assembly = Assembly.Load(assemblyName);
                m_assemblyMap.Add(assemblyName, assembly);
            }
            return (assembly);
        }

        public byte[] ReadBytes(string assemblyName, string resourceName)
        {
            byte[]? raw = null;
            try
            {
                Assembly assembly = GetAssembly(assemblyName);
                using (Stream? stream = assembly.GetManifestResourceStream(resourceName))
                {
                    if (stream == null)
                    {
                        throw new Exception($"Failed to find the resource in the assembly {assemblyName} with the resource name {resourceName}.");
                    }
                    using (BinaryReader sr = new BinaryReader(stream))
                    {

                        raw = sr.ReadBytes((Int32)stream.Length);
                    }
                }
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }
            return raw;
        }

        public string ReadString(string assemblyName, string resourceName)
        {
            string result = string.Empty;
            try
            {
                Assembly assembly = GetAssembly(assemblyName);
                using (Stream? stream = assembly.GetManifestResourceStream(resourceName))
                {
                    if (stream == null)
                    {
                        throw new Exception($"Failed to find the resource in the assembly {assemblyName} with the resource name {resourceName}.");
                    }
                    using (StreamReader sr = new StreamReader(stream))
                    {
                        result = sr.ReadToEnd();
                    }
                }
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }
            return result;
        }

        public XDocument ReadXML(string assemblyName, string resourceName)
        {
            string xmlStr = ReadString(assemblyName, resourceName);
            XDocument xDoc = XDocument.Parse(xmlStr);
            return xDoc;
        }

        public Stream? ReadStream(string assemblyName, string resourceName)
        {
            Stream? stream = null;
            try
            {
                Assembly assembly = GetAssembly(assemblyName);
                stream = assembly.GetManifestResourceStream(resourceName);
            }
            catch (Exception ex)
            {
                OTLogger.Error(ex);
                throw;
            }
            return stream;
        }
    }
}
