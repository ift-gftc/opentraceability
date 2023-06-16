package opentraceability.mappers.epcis;

import Newtonsoft.Json.*;
import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import java.util.*;

/** 
 This represents a KDE to mapping configuration to be used by the EPCIS mappers.
*/
public class EPCISMappingKDE
{
	/** STATIC **/

	private static Object _locker = new Object();
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static Dictionary<string, List<EPCISMappingKDE>>? _mappingKDEs = null;
	private static HashMap<String, ArrayList<EPCISMappingKDE>> _mappingKDEs = null;
	public static HashMap<String, ArrayList<EPCISMappingKDE>> getMappingKDEs()
	{
		if (_mappingKDEs == null)
		{
			synchronized (_locker)
			{
				_mappingKDEs = new HashMap<String, ArrayList<EPCISMappingKDE>>();
				EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
				String jsonStr = loader.ReadString("OpenTraceability", "OpenTraceability.Mappers.EPCIS.mappings.json");
				JSONObject json = JSONObject.Parse(jsonStr);
				JSONObject jEPCIS = json["EPCIS"] instanceof JSONObject ? (JSONObject)json["EPCIS"] : null;
				if (jEPCIS != null)
				{
					for (JProperty jprop : jEPCIS.Properties())
					{
						JSONArray jarr = jEPCIS[jprop.Name] instanceof JSONArray ? (JSONArray)jEPCIS[jprop.Name] : null;
						if (jarr != null)
						{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<EPCISMappingKDE>? kdes = JsonConvert.DeserializeObject<List<EPCISMappingKDE>>(jarr.ToString());
							ArrayList<EPCISMappingKDE> kdes = JsonConvert.<ArrayList<EPCISMappingKDE>>DeserializeObject(jarr.toString());
							if (kdes != null)
							{
								_mappingKDEs.put(jprop.Name, kdes);
							}
						}
					}
				}
			}
		}
		return _mappingKDEs;
	}

	/** PROPERTIES **/
	private String XPath = "";
	public final String getXPath()
	{
		return XPath;
	}
	public final void setXPath(String value)
	{
		XPath = value;
	}
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public string? Property {get;set;}
	private String Property;
	public final String getProperty()
	{
		return Property;
	}
	public final void setProperty(String value)
	{
		Property = value;
	}
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public string? Type {get;set;}
	private String Type;
	public final String getType()
	{
		return Type;
	}
	public final void setType(String value)
	{
		Type = value;
	}
	private boolean Required = false;
	public final boolean getRequired()
	{
		return Required;
	}
	public final void setRequired(boolean value)
	{
		Required = value;
	}
	private EPCISVersion Version = null;
	public final EPCISVersion getVersion()
	{
		return Version;
	}
	public final void setVersion(EPCISVersion value)
	{
		Version = value;
	}
}
