package opentraceability.mappers;

import opentraceability.models.events.*;
import opentraceability.utility.attributes.*;
import opentraceability.*;
import java.util.*;

public enum EPCISDataFormat
{
	XML,
	JSON;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static EPCISDataFormat forValue(int value)
	{
		return values()[value];
	}
}
