package queries

import models.identifiers.*
import org.intellij.markdown.lexer.push
import java.lang.reflect.Type
import java.net.URL

class EPCISQueryParameters {
    var queryType: EPCISQueryType = EPCISQueryType.events
    var query: EPCISQuery = EPCISQuery()


    constructor(epcs: ArrayList<EPC>) {
        epcs.forEach { epc ->
            if (epc.Type == EPCType.Class) {
                if (query.MATCH_anyEPCClass == null) {
                    query.MATCH_anyEPCClass = ArrayList<String>();
                }
                query.MATCH_anyEPCClass.push(epc.toString());
            } else {
                if (query.MATCH_anyEPC == null) {
                    query.MATCH_anyEPC = ArrayList<String>();
                }
                query.MATCH_anyEPC.push(epc.toString());
            }
        }
    }


    constructor(uri: URL) {
        // split into each query parameter
        if (uri.query != null && uri.query.length > 1) {
            var arr = uri.query.substring(1).split('&')

            arr.forEach { qp ->

                var key: String = qp.split('=').first();
                /*
                 string value = HttpUtility.UrlDecode(qp.Split('=').Last());

                     if (_prop_mapping.ContainsKey(key))
                     {
                         PropertyInfo prop = _prop_mapping[key];
                         if (prop != null)
                         {
                             if (prop.PropertyType == typeof(DateTimeOffset?))
                             {
                                 DateTimeOffset dt = DateTimeOffset.Parse(value);
                                 prop.SetValue(query, dt);
                             }
                             else if (prop.PropertyType == typeof(List<string>))
                             {
                                 List<string> values = value.Split('|').ToList();
                                 prop.SetValue(query, values);
                             }
                             else if (prop.PropertyType == typeof(List<Uri>))
                             {
                                 List<Uri> values = value.Split('|').Select(u => new Uri(u)).ToList();
                                 prop.SetValue(query, values);
                             }
                         }
                     }
                 */

            }
        }
    }

    fun IsValid(error: String?): Boolean {
        var error = null;
        return true;
    }

    fun ToJSON(): String {
        TODO("Not yet implemented")
        /*
            string json = JsonConvert.SerializeObject(this.query, new JsonSerializerSettings()
            {
                Formatting = Formatting.Indented,
                DateFormatString = "o",
                NullValueHandling = NullValueHandling.Ignore
            });

            JObject jobject = new JObject();
            jobject["queryType"] = this.queryType.ToString();
            jobject["query"] = JObject.Parse(json);

            return jobject.ToString();
         */

    }

    fun ToQueryParameters(): String {

        var queryParameters: ArrayList<String> = ArrayList<String>()
        TODO("Not yet implemented")

        /*
            // go through each property on the on the query
            foreach (var prop in typeof(EPCISQuery).GetProperties())
            {
                if (prop.PropertyType == typeof(DateTimeOffset?))
                {
                    DateTimeOffset? dateTime = (DateTimeOffset?)prop.GetValue(query);
                    if (dateTime != null)
                    {
                        string queryParam = $"{prop.Name}={HttpUtility.UrlEncode(dateTime.Value.ToString("o"))}";
                        queryParameters.Add(queryParam);
                    }
                }
                else if (prop.PropertyType == typeof(List<string>))
                {
                    List<string>? list = prop.GetValue(query) as List<string>;
                    if (list != null && list.Count > 0)
                    {
                        string queryParam = $"{prop.Name}={string.Join('|', list.Select(l => HttpUtility.UrlEncode(l)))}";
                        queryParameters.Add(queryParam);
                    }
                }
                else if (prop.PropertyType == typeof(List<Uri>))
                {
                    List<Uri>? list = prop.GetValue(query) as List<Uri>;
                    if (list != null && list.Count > 0)
                    {
                        string queryParam = $"{prop.Name}={string.Join('|', list.Select(l => HttpUtility.UrlEncode(l.ToString())))}";
                        queryParameters.Add(queryParam);
                    }
                }
            }
         */


        var q:String  = "?" + queryParameters.joinToString("&")
        return q;
    }



    fun Merge(queryParameters: EPCISQueryParameters) {
        TODO("Not yet implemented")
    }
}
