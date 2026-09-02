using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;

namespace DiagnosticsTool
{
    public static class WebServiceFactory
    {
        public static Uri? TestBaseAddress = null;

        public static IHost Create(string url, IConfiguration config)
        {
            TestBaseAddress = new Uri(url);
            var host = Host.CreateDefaultBuilder(args: new string[] { })
                           .ConfigureWebHostDefaults(webBuilder =>
                           {
                               webBuilder.UseStartup<Startup>()
                                         .UseEnvironment("Test")
                                         .UseConfiguration(config)
                                         .UseKestrel()
                                         .UseUrls(TestBaseAddress.ToString());
                           })
                           .Build();


            host.Start();
            return host;
        }
    }
}
