using System;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;

namespace OpenTraceability.TestServer
{
    /// <summary>
    /// This class will host an instance of the web service for testing purposes. We use Kestral here
    /// because the Identity Server requires the ability to talk to itself and the standard Testing Host Server 
    /// will refuse the connection.
    /// </summary>
    public static class WebServiceFactory
    {
        public static Uri? TestBaseAddress = null;

        public static bool IsTesting { get; private set; }

        public static IHost Create(string url, IConfiguration config)
        {
            IsTesting = true;
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

