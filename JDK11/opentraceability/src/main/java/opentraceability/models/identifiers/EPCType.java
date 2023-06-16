package opentraceability.models.identifiers;

import opentraceability.utility.*;
import opentraceability.*;
import java.util.*;

public enum EPCType
{
	Class(0),
	Instance(1),
	SSCC(2),
	URI(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, EPCType> mappings;
	private static java.util.HashMap<Integer, EPCType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (EPCType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, EPCType>();
				}
			}
		}
		return mappings;
	}

	private EPCType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static EPCType forValue(int value)
	{
		return getMappings().get(value);
	}
}
