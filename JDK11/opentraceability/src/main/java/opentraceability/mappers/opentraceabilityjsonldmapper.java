package opentraceability.mappers;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import kotlin.reflect.KClass;
import kotlin.reflect.KMutableProperty;
import kotlin.reflect.Type;
import kotlin.reflect.full.KClassify;
import kotlin.reflect.full.KTypeProjection;
import kotlin.reflect.full.KTypes;
import kotlin.reflect.full.starProjectedType;
import kotlin.reflect.jvm.JvmClassMappingKt;
import opentraceability.OTLogger;
