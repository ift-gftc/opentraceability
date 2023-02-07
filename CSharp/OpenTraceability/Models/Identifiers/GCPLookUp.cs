using DSUtil;
using DSUtil.Extensions;
using DSUtil.ObjectPooling;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Web;

namespace OpenTraceability.Models.Identifiers
{
    public static class GCPLookUp
    {
        private static LimitedPool<HttpClient> _httpClientPool = new LimitedPool<HttpClient>(() =>
        {
            return new HttpClient();
        },
        (client) =>
        {
            client.Dispose();
            client = null;
        }, new TimeSpan(0, 10, 0));

        public static string DetermineCompanyPrefix(string gtinOrgln)
        {
            try
            {
                // check the arguments
                if (string.IsNullOrEmpty(gtinOrgln))
                {
                    throw new ArgumentNullException("gtinOrgln is null. Cannot look up company prefix with a null string.");
                }

                if (gtinOrgln.Length != 13 && gtinOrgln.Length != 14)
                {
                    throw new Exception("gtinOrgln has an invalid length, must be eiterh 13 or 14 digits long.");
                }

                if (!gtinOrgln.IsOnlyDigits())
                {
                    throw new Exception("glnStr is not only digits. This is not a proper GS1 GLN-13.");
                }

                // if we didn't find the company prefix then we are going to query
                using (LimitedPoolItem<HttpClient> limitedItem = _httpClientPool.Get())
                {
                    HttpClient client = limitedItem.Value;
                    NameValueCollection queryString = HttpUtility.ParseQueryString(string.Empty);
                    client.DefaultRequestHeaders.Add("APIKey", "432fc706285d4fe9a7d594114df639cf");
                    var uri = string.Format("https://api.gs1us.org/company/v3/company/GTIN/{0}?", gtinOrgln) + queryString;
                    if (gtinOrgln.Length == 13)
                    {
                        uri = string.Format("https://api.gs1us.org/company/v3/company/GLN/{0}?", gtinOrgln) + queryString;
                    }
                    try
                    {
                        string jsonString = client.GetAsync(uri).Result.Content.ReadAsStringAsync().Result;
                        List<GS1PrefixInfo> prefixInfo = JsonConvert.DeserializeObject<List<GS1PrefixInfo>>(jsonString);
                        if (prefixInfo.Count > 0)
                        {
                            GS1PrefixInfo gS1PrefixInfo = prefixInfo.First();
                            return gS1PrefixInfo.Prefixes.GS1Prefix;
                        }
                    }
                    catch (Exception Ex)
                    {
                        DSLogger.Log(0, "Failed to query the GS1 Data Hub for the GTIN or GLN. gtinOrGLN=" + gtinOrgln + " and Exception=" + Ex.Message);
                        return null;
                    }
                }
            }
            catch (Exception ex)
            {
                DSLogger.Log(0, ex);
            }
            return null;
        }

        public static async Task<string> DetermineCompanyPrefixAsync(string gtinOrgln)
        {
            try
            {
                // check the arguments
                if (string.IsNullOrEmpty(gtinOrgln))
                {
                    throw new ArgumentNullException("gtinOrgln is null. Cannot look up company prefix with a null string.");
                }

                if (gtinOrgln.Length != 13 && gtinOrgln.Length != 14)
                {
                    throw new Exception("gtinOrgln has an invalid length, must be eiterh 13 or 14 digits long.");
                }

                if (!gtinOrgln.IsOnlyDigits())
                {
                    throw new Exception("glnStr is not only digits. This is not a proper GS1 GLN-13.");
                }

                // if we didn't find the company prefix then we are going to query
                using (LimitedPoolItem<HttpClient> limitedItem = _httpClientPool.Get())
                {
                    HttpClient client = limitedItem.Value;
                    NameValueCollection queryString = HttpUtility.ParseQueryString(string.Empty);
                    client.DefaultRequestHeaders.Add("APIKey", "432fc706285d4fe9a7d594114df639cf");
                    var uri = string.Format("https://api.gs1us.org/company/v3/company/GTIN/{0}?", gtinOrgln) + queryString;
                    if (gtinOrgln.Length == 13)
                    {
                        uri = string.Format("https://api.gs1us.org/company/v3/company/GLN/{0}?", gtinOrgln) + queryString;
                    }
                    try
                    {
                        string jsonString = await (await client.GetAsync(uri)).Content.ReadAsStringAsync();
                        List<GS1PrefixInfo> prefixInfo = JsonConvert.DeserializeObject<List<GS1PrefixInfo>>(jsonString);
                        if(prefixInfo.Count > 0)
                        {
                            GS1PrefixInfo gS1PrefixInfo = prefixInfo.First();
                            return gS1PrefixInfo.Prefixes.GS1Prefix;
                        }
                    }
                    catch (Exception Ex)
                    {
                        DSLogger.Log(0, "Failed to query the GS1 Data Hub for the GTIN or GLN. gtinOrGLN=" + gtinOrgln + " and Exception=" + Ex.Message);
                        return null;
                    }
                }
            }
            catch(Exception ex)
            {
                DSLogger.Log(0, ex);
            }
            return null;
        }

        public static async Task<bool> IsValidCompanyPrefixAsync(string companyPrefix)
        {
            if (string.IsNullOrWhiteSpace(companyPrefix)) return false;

            string gtin = "0" + companyPrefix.PadRight(12, '0');
            gtin = gtin + GS1Util.CalculateGTIN14CheckSum(gtin);

            if (string.IsNullOrWhiteSpace(await DetermineCompanyPrefixAsync(gtin)))
            {
                return false;
            }

            return true;
        }
    }
}
