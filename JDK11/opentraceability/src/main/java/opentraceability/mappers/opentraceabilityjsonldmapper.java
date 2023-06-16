package opentraceability.mappers;

import opentraceability.interfaces.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.utility.attributes.*;
import opentraceability.utility.*;
import Newtonsoft.Json.Linq.*;
import Newtonsoft.Json.*;
import opentraceability.models.common.*;
import opentraceability.*;
import java.util.*;

public final class OpenTraceabilityJsonLDMapper
{
	/** 
	 Converts an object into JSON.
	*/

	public static JToken ToJson(Object value, java.util.HashMap<String, String> namespacesReversed)
	{
		return ToJson(value, namespacesReversed, false);
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: public static System.Nullable<JToken> ToJson(object? value, Dictionary<string, string> namespacesReversed, bool required = false)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public static JToken ToJson(Object value, HashMap<String, String> namespacesReversed, boolean required)
	{
		try
		{
			if (value != null)
			{
				JToken json = new JObject();
				JToken jpointer = json;

				java.lang.Class t = value.getClass();
				OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetJsonTypeInfo(t);
				for (var property : typeInfo.getProperties().Where(p -> p.Version == null || p.Version == EPCISVersion.V2))
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = property.Property.GetValue(value);
					Object obj = property.Property.GetValue(value);
					if (obj != null)
					{
						JToken jvaluepointer = jpointer;
						String xchildname = property.Name;

						if (property.IsQuantityList)
						{
							ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
							products = products.stream().filter(p -> p.Quantity != null && p.Type == property.ProductType).collect(Collectors.toList());
							if (!products.isEmpty())
							{
								JArray xQuantityList = new JArray();
								for (var product : products)
								{
									if (product.getEPC() != null && product.getQuantity() != null)
									{
										JObject xQuantity = new JObject();
										xQuantity["epcClass"] = product.getEPC().toString();
										xQuantity["quantity"] = product.getQuantity().getValue();
										if (!Objects.equals(product.getQuantity().getUoM().getUNCode(), "EA"))
										{
											xQuantity["uom"] = product.getQuantity().getUoM().getUNCode();
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
								JArray xEPCList = new JArray();
								for (var product : products)
								{
									if (product.getEPC() != null)
									{
										xEPCList.Add(product.getEPC().toString());
									}
								}
								jvaluepointer[xchildname] = xEPCList;
							}
						}
						else if (property.IsArray)
						{
							List list = (List)obj;
							JArray xlist = new JArray();

							if (!list.isEmpty() || property.Required == true)
							{
								if (property.IsRepeating && list.size() == 1)
								{
									JToken jt = WriteObjectToJToken(list.get(0));
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
											JToken xchild = ToJson(o, namespacesReversed, property.Required);
											if (xchild != null)
											{
												xlist.Add(xchild);
											}
										}
										else
										{
											JToken jt = WriteObjectToJToken(o);
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
							JToken xchild = ToJson(obj, namespacesReversed, property.Required);
							if (xchild != null)
							{
								jvaluepointer[xchildname] = xchild;
							}
						}
						else
						{
							JToken jt = WriteObjectToJToken(obj);
							if (jt != null)
							{
								jvaluepointer[xchildname] = jt;
							}
						}
					}
				}

				if (typeInfo.getExtensionKDEs() != null)
				{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: object? obj = typeInfo.ExtensionKDEs.GetValue(value);
					Object obj = typeInfo.getExtensionKDEs().GetValue(value);
					if (obj != null && obj instanceof List<IEventKDE>)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList<IEventKDE>? kdes = obj instanceof java.util.List<IEventKDE> ? (java.util.List<IEventKDE>)obj : null;
						List<IEventKDE> kdes = obj instanceof List<IEventKDE> ? (List<IEventKDE>)obj : null;
						if (kdes != null)
						{
							for (var kde : kdes)
							{
								JToken xchild = kde.GetJson();
								if (xchild != null)
								{
									String name = kde.getName();
									if (kde.getNamespace() != null)
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
//ORIGINAL LINE: object? obj = typeInfo.ExtensionAttributes.GetValue(value);
					Object obj = typeInfo.getExtensionAttributes().GetValue(value);
					if (obj != null && obj instanceof List<IEventKDE>)
					{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList<IEventKDE>? kdes = obj instanceof java.util.List<IEventKDE> ? (java.util.List<IEventKDE>)obj : null;
						List<IEventKDE> kdes = obj instanceof List<IEventKDE> ? (List<IEventKDE>)obj : null;
						if (kdes != null)
						{
							for (IEventKDE kde : kdes)
							{
								JToken xchild = kde.GetJson();
								if (xchild != null)
								{
									String name = kde.getName();
									if (kde.getNamespace() != null)
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
		catch (RuntimeException ex)
		{
			RuntimeException e = new RuntimeException(String.format("Failed to parse json. value=%1$s", value), ex);
			OTLogger.Error(e);
			throw e;
		}
	}

	/** 
	 Converts a JSON object into the generic type specified.
	*/
	public static <T> T FromJson(JToken json, HashMap<String, String> namespaces)
	{
		T o = (T)FromJson(json, T.class, namespaces);
		return o;
	}

	/** 
	 Converts a JSON object into the type specified.
	*/
	public static Object FromJson(JToken json, java.lang.Class type, HashMap<String, String> namespaces)
	{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: object value = Activator.CreateInstance(type) ?? throw new Exception("Failed to create instance of type " + type.FullName);
		Object value = type.newInstance() != null ? type.newInstance() : throw new RuntimeException("Failed to create instance of type " + type.FullName);

		try
		{
			OTMappingTypeInformation typeInfo = OTMappingTypeInformation.GetJsonTypeInfo(type);

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<IEventKDE>? extensionKDEs = null;
			ArrayList<IEventKDE> extensionKDEs = null;
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<IEventKDE>? extensionAttributes = null;
			ArrayList<IEventKDE> extensionAttributes = null;

			if (typeInfo.getExtensionAttributes() != null)
			{
				extensionAttributes = new ArrayList<IEventKDE>();
			}

			if (typeInfo.getExtensionKDEs() != null)
			{
				extensionKDEs = new ArrayList<IEventKDE>();
			}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: OTMappingTypeInformationProperty? mappingProp;
			OTMappingTypeInformationProperty mappingProp;

			JObject jobj = json instanceof JObject ? (JObject)json : null;
			if (jobj != null)
			{
				for (JProperty jprop : jobj.Properties())
				{
					mappingProp = typeInfo.get(jprop.Name);

					if (mappingProp != null && mappingProp.getProperty().SetMethod == null)
					{
						continue;
					}

					JToken jchild = jobj[jprop.Name];
					if (jchild != null)
					{
						if (mappingProp != null)
						{
							ReadPropertyMapping(mappingProp, jchild, value, namespaces);
						}
						else if (extensionKDEs != null)
						{
							IEventKDE kde = ReadKDE(jprop.Name, jchild, namespaces);
							extensionKDEs.add(kde);
						}
						else if (extensionAttributes != null)
						{
							IEventKDE kde = ReadKDE(jprop.Name, jchild, namespaces);
							extensionAttributes.add(kde);
						}
					}
				}
			}

			if (typeInfo.getExtensionAttributes() != null)
			{
				typeInfo.getExtensionAttributes().SetValue(value, extensionAttributes);
			}

			if (typeInfo.getExtensionKDEs() != null)
			{
				typeInfo.getExtensionKDEs().SetValue(value, extensionKDEs);
			}
		}
		catch (RuntimeException ex)
		{
			OTLogger.Error(ex);
			throw ex;
		}

		return value;
	}

//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: private static System.Nullable<JToken> WriteObjectToJToken(object? obj)
	private static JToken WriteObjectToJToken(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof ArrayList<LanguageString>)
		{
			String json = JsonConvert.SerializeObject(obj);
			return JArray.Parse(json);
		}
		else if (obj instanceof DateTimeOffset)
		{
			DateTimeOffset dt = (DateTimeOffset)obj;
			return dt.toString("O");
		}
		else if (obj instanceof UOM)
		{
			UOM uom = (UOM)obj;
			return uom.getUNCode();
		}
		else if (obj instanceof Double)
		{
			return JToken.FromObject(obj);
		}
		else if (obj instanceof Boolean)
		{
			return JToken.FromObject(obj);
		}
		else if (obj instanceof Country)
		{
			Country b = (Country)obj;
			return b.getAbbreviation();
		}
		else if (obj instanceof TimeSpan)
		{
			TimeSpan timespan = (TimeSpan)obj;
			if (timespan.Ticks < 0)
			{
				return "-" + (new Double(timespan.Negate().TotalHours)).toString("#00") + ":" + (new Integer(timespan.Minutes)).toString("00");
			}
			else
			{
				return "+" + (new Double(timespan.TotalHours)).toString("#00") + ":" + (new Integer(timespan.Minutes)).toString("00");
			}
		}
		else
		{
			return obj.toString() != null ? obj.toString() : "";
		}
	}

	private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, JToken json, Object value, HashMap<String, String> namespaces)
	{
		if (mappingProp.isQuantityList())
		{
			IEvent e = (IEvent)value;
			JArray jQuantityList = json instanceof JArray ? (JArray)json : null;
			if (jQuantityList != null)
			{
				for (JObject jQuantity : jQuantityList)
				{
					EPC epc = new EPC((jQuantity["epcClass"] == null ? null : jQuantity["epcClass"].<String>Value()) != null ? (jQuantity["epcClass"] == null ? null : jQuantity["epcClass"].<String>Value()) : "");

					EventProduct product = new EventProduct(epc);
					product.setType(mappingProp.getProductType());

					double quantity = jQuantity.<Double>Value("quantity");
					String uom = jQuantity.<String>Value("uom") != null ? jQuantity.<String>Value("uom") : "EA";
					product.setQuantity(new Measurement(quantity, uom));

					e.AddProduct(product);
				}
			}
		}
		else if (mappingProp.isEPCList())
		{
			IEvent e = (IEvent)value;
			JArray jEPCList = json instanceof JArray ? (JArray)json : null;
			if (jEPCList != null)
			{
				for (JToken jEPC : jEPCList)
				{
					EPC epc = new EPC(jEPC.toString());
					EventProduct product = new EventProduct(epc);
					product.setType(mappingProp.getProductType());
					e.AddProduct(product);
				}
			}
		}
		else if (mappingProp.isArray())
		{
			Object tempVar = mappingProp.getProperty().GetValue(value);
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IList? list = tempVar instanceof java.util.List ? (java.util.List)tempVar : null;
			List list = tempVar instanceof List ? (List)tempVar : null;
			if (list == null)
			{
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: list = (IList)(Activator.CreateInstance(mappingProp.Property.PropertyType) ?? throw new Exception("Failed to create instance of " + mappingProp.Property.PropertyType.FullName));
				list = (List)(mappingProp.getProperty().PropertyType.newInstance() != null ? mappingProp.getProperty().PropertyType.newInstance() : throw new RuntimeException("Failed to create instance of " + mappingProp.getProperty().PropertyType.FullName));

				mappingProp.getProperty().SetValue(value, list);
			}

			java.lang.Class itemType = list.getClass().GenericTypeArguments[0];

			if (mappingProp.isRepeating() && !(json instanceof JArray))
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
				JArray jArr = json instanceof JArray ? (JArray)json : null;
				if (jArr != null)
				{
					for (JToken j : jArr)
					{
						if (mappingProp.isObject())
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
		else if (mappingProp.isObject())
		{
			Object o = FromJson(json, mappingProp.getProperty().PropertyType, namespaces);
			mappingProp.getProperty().SetValue(value, o);
		}
		else if (mappingProp.getProperty().PropertyType == ArrayList<LanguageString>.class)
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: List<LanguageString>? languageStrings = JsonConvert.DeserializeObject<List<LanguageString>>(json.ToString());
			ArrayList<LanguageString> languageStrings = JsonConvert.<ArrayList<LanguageString>>DeserializeObject(json.toString());
			if (languageStrings != null)
			{
				mappingProp.getProperty().SetValue(value, languageStrings);
			}
		}
		else
		{
			String v = json.toString();
			if (!(v == null || v.isBlank()))
			{
				Object o = ReadObjectFromString(v, mappingProp.getProperty().PropertyType);
				mappingProp.getProperty().SetValue(value, o);
			}
		}
	}

	private static Object ReadObjectFromString(String value, java.lang.Class t)
	{
		try
		{
			if (t == DateTimeOffset.class || t == DateTimeOffset.class)
			{
				DateTimeOffset tempVar = StringExtensions.TryConvertToDateTimeOffset(value);
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: DateTimeOffset dt = value.TryConvertToDateTimeOffset() ?? throw new Exception("Failed to convert string to datetimeoffset where value = " + value);
				DateTimeOffset dt = tempVar != null ? tempVar : throw new RuntimeException("Failed to convert string to datetimeoffset where value = " + value);
				return dt;
			}
			else if (t == UOM.class)
			{
				UOM uom = UOM.LookUpFromUNCode(value);
				return uom;
			}
			else if (t == Boolean.class || t == Boolean.class)
			{
				boolean v = Boolean.parseBoolean(value);
				return v;
			}
			else if (t == Double.class || t == Double.class)
			{
				double v = Double.parseDouble(value);
				return v;
			}
			else if (t == Uri.class)
			{
				Uri v = new Uri(value);
				return v;
			}
			else if (t == TimeSpan.class || t == TimeSpan.class)
			{
				if (value.startsWith("+"))
				{
					value = value.substring(1);
				}
				TimeSpan ts = TimeSpan.Parse(value);
				return ts;
			}
			else if (t == EventAction.class || t == EventAction.class)
			{
				EventAction action = Enum.<EventAction>Parse(value);
				return action;
			}
			else if (t == PGLN.class)
			{
				PGLN pgln = new PGLN(value);
				return pgln;
			}
			else if (t == GLN.class)
			{
				GLN gln = new GLN(value);
				return gln;
			}
			else if (t == GTIN.class)
			{
				GTIN gtin = new GTIN(value);
				return gtin;
			}
			else if (t == EPC.class)
			{
				EPC epc = new EPC(value);
				return epc;
			}
			else if (t == Country.class)
			{
				Country c = Countries.Parse(value);
				return c;
			}
			else
			{
				return value;
			}
		}
		catch (RuntimeException ex)
		{
			RuntimeException e = new RuntimeException(String.format("Failed to convert string into object. value=%1$s and t=%2$s", value, t), ex);
			OTLogger.Error(e);
			throw e;
		}
	}

	private static IEventKDE ReadKDE(String name, JToken json, HashMap<String, String> namespaces)
	{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: IEventKDE? kde = null;
		IEventKDE kde = null;

		//if not, check if it is a simple value or an object
		if (kde == null)
		{
			String ns = "";
			if (name.contains(":"))
			{
				ns = name.split(java.util.regex.Pattern.quote(":"), -1).First();
				name = name.split(java.util.regex.Pattern.quote(":"), -1).Last();

				if (!namespaces.containsKey(ns))
				{
					throw new RuntimeException("The KDE has a namespace prefix, but there is no such namespace in the dictionary. " + ns);
				}
				ns = namespaces.get(ns);
			}

			if (json instanceof JObject || json instanceof JArray)
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
			kde.SetFromJson(json);
		}
		else
		{
			throw new RuntimeException("Failed to initialize KDE from JSON = " + json.toString());
		}

		return kde;
	}
}
