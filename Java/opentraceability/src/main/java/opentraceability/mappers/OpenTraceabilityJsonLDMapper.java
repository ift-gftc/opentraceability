package opentraceability.mappers;

import opentraceability.interfaces.*;
import opentraceability.models.events.kdes.*;
import opentraceability.models.events.*;
import opentraceability.models.identifiers.*;
import opentraceability.utility.*;
import opentraceability.models.common.*;
import opentraceability.*;
import org.apache.commons.codec.language.bm.Lang;
import org.json.JSONArray;
import org.json.JSONObject;
import tangible.StringHelper;


import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class OpenTraceabilityJsonLDMapper
{
	public static Object ToJson(Object value, Map<String, String> namespacesReversed, Boolean isGs1WebVocab, boolean required) throws Exception {
		if (value != null)
		{
			JSONObject json = new JSONObject();
			JSONObject jpointer = json;

			Class t = value.getClass();
			OTMappingTypeInformation typeInfo = OTMappingTypeInformation.getJsonTypeInfo(t);
			for (var property : typeInfo.properties.stream().filter(p -> p.version.equals(EPCISVersion.Any) || p.version == EPCISVersion.V2).collect(Collectors.toList()))
			{
				Object obj = property.field.get(value);
				if (obj != null)
				{
					JSONObject jvaluepointer = jpointer;
					String jChildName = property.name;

					if (property.isQuantityList)
					{
						ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
						var quantityProducts = products.stream().filter(p -> p.Quantity != null && p.Type == property.productType).collect(Collectors.toList());
						if (!quantityProducts.isEmpty())
						{
							JSONArray jQuantityList = new JSONArray();
							for (var product : quantityProducts)
							{
								if (product.EPC != null && product.Quantity != null)
								{
									JSONObject jQuantity = new JSONObject();
									jQuantity.put("epcClass", product.EPC.toString());
									jQuantity.put("quantity", product.Quantity.value);
									if (!Objects.equals(product.Quantity.uom.UNCode, "EA"))
									{
										jQuantity.put("uom", product.Quantity.uom.UNCode);
									}
									jQuantityList.put(jQuantity);
								}
							}
							JSONExtensions.put(jvaluepointer, jChildName, jQuantityList);
						}
					}
					else if (property.isEPCList)
					{
						ArrayList<EventProduct> products = (ArrayList<EventProduct>)obj;
						var epcProducts = products.stream().filter(p -> p.Quantity == null && p.Type == property.productType).collect(Collectors.toList());
						if (!epcProducts.isEmpty() || property.required)
						{
							JSONArray jEPCList = new JSONArray();
							for (var product : epcProducts)
							{
								if (product.EPC != null)
								{
									JSONExtensions.put(jEPCList, product.EPC.toString());
								}
							}
							JSONExtensions.put(jvaluepointer, jChildName, jEPCList);
						}
					}
					else if (property.isArray)
					{
						List list = (List)obj;
						JSONArray jList = new JSONArray();

						if (!list.isEmpty() || property.required)
						{
							if (property.isRepeating && list.size() == 1)
							{
								Object jt = WriteObjectToJToken(list.get(0), isGs1WebVocab);
								if (jt != null)
								{
									JSONExtensions.put(jvaluepointer, jChildName, jt);
								}
							}
							else
							{
								for (var o : list)
								{
									if (property.isObject)
									{
										Object jListChild = ToJson(o, namespacesReversed, isGs1WebVocab, property.required);
										if (jListChild != null)
										{
											JSONExtensions.put(jList, jListChild);
										}
									}
									else if (property.itemType == LanguageString.class && isGs1WebVocab)
									{
										LanguageString ls = (LanguageString)o;
										JSONObject j = ls.toJSON();
										JSONExtensions.put(jList, j);
									}
									else
									{
										Object jt = WriteObjectToJToken(o, isGs1WebVocab);
										if (jt != null)
										{
											JSONExtensions.put(jList, jt);
										}
									}
								}

								JSONExtensions.put(jvaluepointer, jChildName, jList);
							}
						}
					}
					else if (property.isObject)
					{
						Object jChild = ToJson(obj, namespacesReversed, isGs1WebVocab, property.required);
						if (jChild != null)
						{
							JSONExtensions.put(jvaluepointer, jChildName, jChild);
						}
					}
					else
					{
						Object jt = WriteObjectToJToken(obj, isGs1WebVocab);
						if (jt != null)
						{
							JSONExtensions.put(jvaluepointer, jChildName, jt);
						}
					}
				}
			}

			if (typeInfo.extensionKDEs != null)
			{
				Object obj = typeInfo.extensionKDEs.get(value);
				if (obj != null)
				{
					ArrayList<IEventKDE> kdes = (ArrayList<IEventKDE>)obj;
					if (kdes != null)
					{
						for (var kde : kdes)
						{
							Object jChild = kde.getJson();
							if (jChild != null)
							{
								String name = kde.name;
								if (!StringHelper.isNullOrEmpty(kde.namespace))
								{
									if (!namespacesReversed.containsKey(kde.namespace))
									{
										throw new RuntimeException(String.format("The namespace %1$s is not recognized in the EPCIS Document / EPCIS Query Document.", kde.namespace));
									}
									name = namespacesReversed.get(kde.namespace) + ":" + name;
								}

								JSONExtensions.put(jpointer, name, jChild);
							}
						}
					}
				}
			}

			if (typeInfo.extensionAttributes != null)
			{
				Object obj = typeInfo.extensionAttributes.get(value);
				if (obj != null)
				{
					ArrayList<IEventKDE> kdes = (ArrayList<IEventKDE>)obj;
						if (kdes != null)
						{
							for (IEventKDE kde : kdes)
							{
								Object jChild = kde.getJson();
								if (jChild != null)
								{
									String name = kde.name;
									if (kde.namespace != null)
									{
										if (!namespacesReversed.containsKey(kde.namespace))
										{
											throw new RuntimeException(String.format("The namespace %1$s is not recognized in the EPCIS Document / EPCIS Query Document.", kde.namespace));
										}
										name = namespacesReversed.get(kde.namespace) + ":" + name;
									}

									JSONExtensions.put(jpointer, name, jChild);
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
	 Converts a JSON object into the type specified.
	*/
	public static Object FromJson(Object json, Class type, Map<String, String> namespaces) throws Exception {
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

		OTMappingTypeInformationProperty mappingProp;

		JSONObject jobj = json instanceof JSONObject ? (JSONObject)json : null;
		if (jobj != null)
		{
			for (String jprop : jobj.keySet())
			{
				mappingProp = typeInfo.get(jprop, null);

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

	private static Object WriteObjectToJToken(Object obj, Boolean isGs1WebVocab)
	{
		if (obj == null)
		{
			return null;
		}
		// check if obj is ArrayList<LanguageString>...
		else if (obj instanceof ArrayList)
		{
			ArrayList l = (ArrayList)obj;
			if (!l.isEmpty())
			{
				JSONArray jArr = new JSONArray();
				for (Object o: l)
				{
					if (o instanceof LanguageString)
					{
						JSONObject j = ((LanguageString)o).toJSON();
						JSONExtensions.put(jArr, j);
					}
					else {
						JSONExtensions.put(jArr, o);
					}
				}
				return jArr;
			}
			else
			{
				return null;
			}
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
			return StringExtensions.fromDuration((Duration)obj);
		}
		else
		{
			return obj.toString() != null ? obj.toString() : "";
		}
	}

	private static void ReadPropertyMapping(OTMappingTypeInformationProperty mappingProp, Object json, Object value, Map<String, String> namespaces) throws Exception {
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
						if (!jQuantity.has("epcClass"))
						{
							throw new Exception("Cannot deserialize quantityElement because epcClass not on JSON object.");
						}

						EPC epc = new EPC((jQuantity.getString("epcClass")));

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

			if (mappingProp.isRepeating && !(json instanceof JSONArray))
			{
				String v = json.toString();
				if (!(v == null || v.isBlank()))
				{
					Object o = ReadObjectFromString(v, mappingProp.itemType);
					list.add(o);
				}
			}
			else
			{
				JSONArray jArr = json instanceof JSONArray ? (JSONArray)json : null;
				if (jArr != null)
				{
					if (mappingProp.itemType == LanguageString.class)
					{
						list = LanguageString.fromJSON(jArr);
					}
					else
					{
						for (Object j : jArr)
						{
							if (mappingProp.isObject)
							{
								Object o = FromJson(j, mappingProp.itemType, namespaces);
								list.add(o);
							}
							else
							{
								Object o = ReadObjectFromString(j.toString(), mappingProp.itemType);
								list.add(o);
							}
						}
					}
				}
			}

			mappingProp.field.set(value, list);
		}
		else if (mappingProp.isObject)
		{
			Object o = FromJson(json, mappingProp.field.getType(), namespaces);
			mappingProp.field.set(value, o);
		}
		else if (ReflectionUtility.isListOf(mappingProp.field.getType(), LanguageString.class))
		{

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

	private static Object ReadObjectFromString(String value, Class t) throws Exception {
		return ReflectionUtility.parseFromString(t, value);
	}

	private static IEventKDE ReadKDE(String name, Object value, Map<String, String> namespaces) throws Exception {

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
