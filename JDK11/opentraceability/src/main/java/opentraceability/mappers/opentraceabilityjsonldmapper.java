package opentraceability.mappers;

import opentraceability.interfaces.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.utility.*;
import opentraceability.models.common.*;
import opentraceability.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class OpenTraceabilityJsonLDMapper
{
	/** 
	 Converts an object into JSON.
	*/

	public static Object ToJson(Object value, java.util.HashMap<String, String> namespacesReversed)
	{
		return ToJson(value, namespacesReversed, false);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static System.Nullable<Object> ToJson(object? value, Dictionary<string, string> namespacesReversed, bool required = false)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public static Object ToJson(Object value, HashMap<String, String> namespacesReversed, boolean required) throws IllegalAccessException {
		if (value != null)
		{
			JSONObject json = new JSONObject();
			JSONObject jpointer = json;

			Type t = value.getClass();
			OTMappingTypeInformation typeInfo = OTMappingTypeInformation.getJsonTypeInfo(t);
			for (var property : typeInfo.properties.stream().filter(p -> p.version == null || p.version == EPCISVersion.V2).collect(Collectors.toList()))
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = property.Property.get(value);
				Object obj = property.field.get(value);
				if (obj != null)
				{
					Object jvaluepointer = jpointer;
					String xchildname = property.name;

					if (property.isQuantityList)
					{
						ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
						products = products.stream().filter(p -> p.Quantity != null && p.Type == property.ProductType).collect(Collectors.toList());
						if (!products.isEmpty())
						{
							JSONArray xQuantityList = new JSONArray();
							for (var product : products)
							{
								if (product.EPC != null && product.Quantity != null)
								{
									JSONObject jQuantity = new JSONObject();
									jQuantity["epcClass"] = product.EPC.toString();
									jQuantity["quantity"] = product.Quantity.getValue();
									if (!Objects.equals(product.Quantity.getUoM().getUNCode(), "EA"))
									{
										jQuantity["uom"] = product.Quantity.getUoM().getUNCode();
									}
									xQuantityList.Add(xQuantity);
								}
							}
							jvaluepointer[xchildname] = xQuantityList;
						}
					}
					else if (property.IsEPCList)
					{
						ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
						products = products.stream().filter(p -> p.Quantity == null && p.Type == property.ProductType).collect(Collectors.toList());
						if (!products.isEmpty() || property.Required)
						{
							JSONArray xEPCList = new JSONArray();
							for (var product : products)
							{
								if (product.EPC != null)
								{
									xEPCList.Add(product.EPC.toString());
								}
							}
							jvaluepointer[xchildname] = xEPCList;
						}
					}
					else if (property.IsArray)
					{
						List list = (List)obj;
						JSONArray xlist = new JSONArray();

						if (!list.isEmpty() || property.Required == true)
						{
							if (property.IsRepeating && list.size() == 1)
							{
								Object jt = WriteObjectToJToken(list.get(0));
								if (jt != null)
								{
									jvaluepointer[xchildname] = jt;
								}
							}
							else
							{
								for (var o : list)
								{
									if (property.IsObject)
									{
										Object xchild = ToJson(o, namespacesReversed, property.Required);
										if (xchild != null)
										{
											xlist.Add(xchild);
										}
									}
									else
									{
										Object jt = WriteObjectToJToken(o);
										if (jt != null)
										{
											xlist.Add(jt);
										}
									}
								}

								jvaluepointer[xchildname] = xlist;
							}
						}
					}
					else if (property.IsObject)
					{
						Object xchild = ToJson(obj, namespacesReversed, property.Required);
						if (xchild != null)
						{
							jvaluepointer[xchildname] = xchild;
						}
					}
					else
					{
						Object jt = WriteObjectToJToken(obj);
						if (jt != null)
						{
							jvaluepointer[xchildname] = jt;
						}
					}
				}
			}

			if (typeInfo.extensionKDEs != null)
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = typeInfo.ExtensionKDEs.get(value);
				Object obj = typeInfo.extensionKDEs.get(value);
				if (obj != null && obj instanceof List<IEventKDE>)
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList<IEventKDE>? kdes = obj instanceof java.util.List<IEventKDE> ? (java.util.List<IEventKDE>)obj : null;
					List<IEventKDE> kdes = obj instanceof List<IEventKDE> ? (List<IEventKDE>)obj : null;
					if (kdes != null)
					{
						for (var kde : kdes)
						{
							Object xchild = kde.getJson();
							if (xchild != null)
							{
								String name = kde.getName();
								if (kde.namespaces != null)
								{
									if (!namespacesReversed.containsKey(kde.getNamespace()))
									{
										throw new RuntimeException(String.format("The namespace %1$s is not recognized in the EPCIS Document / EPCIS Query Document.", kde.getNamespace()));
									}
									name = namespacesReversed.get(kde.getNamespace()) + ":" + name;
								}

								jpointer[name] = xchild;
							}
						}
					}
				}
			}

			if (typeInfo.getExtensionAttributes() != null)
			{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = typeInfo.ExtensionAttributes.get(value);
				Object obj = typeInfo.getExtensionAttributes().get(value);
				if (obj != null && obj instanceof List<IEventKDE>)
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList<IEventKDE>? kdes = obj instanceof java.util.List<IEventKDE> ? (java.util.List<IEventKDE>)obj : null;
					List<IEventKDE> kdes = obj instanceof List<IEventKDE> ? (List<IEventKDE>)obj : null;
					if (kdes != null)
					{
						for (IEventKDE kde : kdes)
						{
							Object xchild = kde.GetJson();
							if (xchild != null)
							{
								String name = kde.getName();
								if (kde.namespaces != null)
								{
									if (!namespacesReversed.containsKey(kde.getNamespace()))
									{
										throw new RuntimeException(String.format("The namespace %1$s is not recognized in the EPCIS Document / EPCIS Query Document.", kde.getNamespace()));
									}
									name = namespacesReversed.get(kde.getNamespace()) + ":" + name;
								}

								jpointer[name] = xchild;
							}
						}
					}
				}
			}

			return json;
		}
		else
		{
			return null;
		}
	}

	/** 
	 Converts a JSON object into the generic type specified.
	*/
	public static <T> T FromJson(Object json, HashMap<String, String> namespaces)
	{
		T o = (T)FromJson(json, T.class, namespaces);
		return o;
	}

	/** 
	 Converts a JSON object into the type specified.
	*/
	public static Object FromJson(Object json, Type type, HashMap<String, String> namespaces) throws Exception {
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: object value = Activator.CreateInstance(type) ?? throw new Exception("Failed to create instance of type " + type.FullName);
		Object value = ReflectionUtility.constructType(type);

		OTMappingTypeInformation typeInfo = OTMappingTypeInformation.getJsonTypeInfo(type);

		ArrayList<IEventKDE> extensionKDEs = null;
		ArrayList<IEventKDE> extensionAttributes = null;

		if (typeInfo.extensionAttributes != null)
		{
			extensionAttributes = new ArrayList<IEventKDE>();
		}

		if (typeInfo.extensionKDEs != null)
		{
			extensionKDEs = new ArrayList<IEventKDE>();
		}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OTMappingTypeInformationProperty? mappingProp;
		OTMappingTypeInformationProperty mappingProp;

		JSONObject jobj = json instanceof JSONObject ? (JSONObject)json : null;
		if (jobj != null)
		{
			for (String jprop : jobj.keySet())
			{
				mappingProp = typeInfo.get(jprop);

				if (jobj.has(jprop))
				{
					Object jchild = jobj.get(jprop);
					if (mappingProp != null)
					{
						ReadPropertyMapping(mappingProp, jchild, value, namespaces);
					}
					else if (extensionKDEs != null)
					{
						IEventKDE kde = ReadKDE(jprop, jchild, namespaces);
						extensionKDEs.add(kde);
					}
					else if (extensionAttributes != null)
					{
						IEventKDE kde = ReadKDE(jprop, jchild, namespaces);
						extensionAttributes.add(kde);
					}
				}
			}
		}

		if (typeInfo.extensionAttributes != null)
		{
			typeInfo.extensionAttributes.set(value, extensionAttributes);
		}

		if (typeInfo.extensionKDEs != null)
		{
			typeInfo.extensionKDEs.set(value, extensionKDEs);
		}

		return value;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static System.Nullable<Object> WriteObjectToJToken(object? obj)
	private static Object WriteObjectToJToken(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof ArrayList<LanguageString>)
		{
			String json = JsonConvert.SerializeObject(obj);
			return JSONArray.Parse(json);
		}
		else if (obj instanceof OffsetDateTime)
		{
			OffsetDateTime dt = (OffsetDateTime)obj;
			return dt.format(DateTimeFormatter.ISO_DATE_TIME);
		}
		else if (obj instanceof UOM)
		{
			UOM uom = (UOM)obj;
			return uom.UNCode;
		}
		else if (obj instanceof Double)
		{
			return obj;
		}
		else if (obj instanceof Boolean)
		{
			return obj;
		}
		else if (obj instanceof Country)
		{
			Country b = (Country)obj;
			return b.abbreviation;
		}
		else if (obj instanceof Duration)
		{
			Duration timespan = (Duration)obj;
			String timeStr = (new Double(timespan.toHoursPart())).toString() + ":" + (new Integer(timespan.Minutes)).toString("00");
			if (timespan.isNegative())
			{
				return "-" + timeStr;
			}
			else
			{
				return "-" + timeStr;
			}
		}
		else
		{
			return obj.toString() != null ? obj.toString() : "";
		}
	}

	private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, Object json, Object value, HashMap<String, String> namespaces) throws Exception {
		if (mappingProp.isQuantityList)
		{
			IEvent e = (IEvent)value;
			JSONArray jQuantityList = json instanceof JSONArray ? (JSONArray)json : null;
			if (jQuantityList != null)
			{
				for (Object o : jQuantityList)
				{
					if (o instanceof JSONObject)
					{
						JSONObject jQuantity = (JSONObject) o;
						EPC epc = new EPC((jQuantity.getString("epcClass"));

						EventProduct product = new EventProduct(epc);
						product.Type = mappingProp.productType;
						product.Quantity = JSONExtensions.readMeasurement(jQuantity);

						e.addProduct(product);
					}
				}
			}
		}
		else if (mappingProp.isEPCList)
		{
			IEvent e = (IEvent)value;
			JSONArray jEPCList = json instanceof JSONArray ? (JSONArray)json : null;
			if (jEPCList != null)
			{
				for (Object jEPC : jEPCList)
				{
					EPC epc = new EPC(jEPC.toString());
					EventProduct product = new EventProduct(epc);
					product.Type = mappingProp.productType;
					e.addProduct(product);
				}
			}
		}
		else if (mappingProp.isArray)
		{
			List list = (List)ReflectionUtility.constructType(mappingProp.field.getType());
			Type itemType = ReflectionUtility.getItemType(mappingProp.field.getType());

			if (mappingProp.isRepeating && !(json instanceof JSONArray))
			{
				String v = json.toString();
				if (!(v == null || v.isBlank()))
				{
					Object o = ReadObjectFromString(v, itemType);
					list.add(o);
				}
			}
			else
			{
				JSONArray jArr = json instanceof JSONArray ? (JSONArray)json : null;
				if (jArr != null)
				{
					for (Object j : jArr)
					{
						if (mappingProp.isObject)
						{
							Object o = FromJson(j, itemType, namespaces);
							list.add(o);
						}
						else
						{
							Object o = ReadObjectFromString(j.toString(), itemType);
							list.add(o);
						}
					}
				}
			}
		}
		else if (mappingProp.isObject)
		{
			Object o = FromJson(json, mappingProp.field.getType(), namespaces);
			mappingProp.field.set(value, o);
		}
		else if (mappingProp.field.getType() == ArrayList<LanguageString>.class)
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<LanguageString>? languageStrings = JsonConvert.DeserializeObject<List<LanguageString>>(json.ToString());
			ArrayList<LanguageString> languageStrings = JsonConvert.<ArrayList<LanguageString>>DeserializeObject(json.toString());
			if (languageStrings != null)
			{
				mappingProp.field.set(value, languageStrings);
			}
		}
		else
		{
			String v = json.toString();
			if (!(v == null || v.isBlank()))
			{
				Object o = ReadObjectFromString(v, mappingProp.field.getType());
				mappingProp.field.set(value, o);
			}
		}
	}

	private static Object ReadObjectFromString(String value, Type t)
	{
		return ReflectionUtility.parseFromString(t, value);
	}

	private static IEventKDE ReadKDE(String name, Object value, HashMap<String, String> namespaces) throws Exception {
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IEventKDE? kde = null;
		IEventKDE kde = null;

		//if not, check if it is a simple value or an object
		if (kde == null)
		{
			String ns = "";
			if (name.contains(":"))
			{
				ns = StringExtensions.First(name.split(java.util.regex.Pattern.quote(":"), -1));
				name = StringExtensions.Last(name.split(java.util.regex.Pattern.quote(":"), -1));

				if (!namespaces.containsKey(ns))
				{
					throw new RuntimeException("The KDE has a namespace prefix, but there is no such namespace in the dictionary. " + ns);
				}
				ns = namespaces.get(ns);
			}

			if (value instanceof JSONObject || value instanceof JSONArray)
			{
				kde = new EventKDEObject(ns, name);
			}
			//else if simple value, then we will consume it as a string
			else
			{
				kde = new EventKDEString(ns, name);
			}
		}

		if (kde != null)
		{
			kde.setFromJson(value);
		}
		else
		{
			throw new RuntimeException("Failed to initialize KDE from JSON = " + value.toString());
		}

		return kde;
	}
}
