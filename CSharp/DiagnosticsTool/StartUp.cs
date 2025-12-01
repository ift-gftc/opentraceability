using DiagnosticsTool.Models;
using DiagnosticsTool.Services;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.OpenApi.Models;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System.Security.Claims;

namespace DiagnosticsTool
{
    /// <summary>
    /// Due to unit testing, we use the traditional startup class from before ASP.NET 6.
    /// </summary>
    public class Startup
    {
        internal static string Error = "";
        public IConfiguration Configuration { get; set; }

        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public void ConfigureServices(IServiceCollection services)
        {
            // Add services to the container
            services.AddControllers().AddNewtonsoftJson(o =>
            {
                o.SerializerSettings.Converters.Add(new StringEnumConverter());
                o.SerializerSettings.NullValueHandling = Newtonsoft.Json.NullValueHandling.Ignore;
            });

            // Add Razor Pages + Server-Side Blazor home page support
            services.AddRazorPages();
            services.AddServerSideBlazor();

            // Register diagnostics envelope cache singleton
            services.AddSingleton<IDiagnosticsEnvelopeCache, DiagnosticsEnvelopeCache>();
            services.AddSingleton<ITestService, TestService>();

            services.Configure<DiagnosticsToolOptions>(Configuration.GetSection("DiagnosticsTool"));

            services.AddHttpClient("default")
                .ConfigureHttpClient(c =>
                {
                    c.Timeout = TimeSpan.FromSeconds(100);
                    c.DefaultRequestHeaders.UserAgent.ParseAdd("DiagnosticsTool/1.0");
                });

            services.AddEndpointsApiExplorer();
            services.AddSwaggerGen(c =>
            {
                c.SwaggerDoc("v1", new OpenApiInfo
                {
                    Title = "DiagnosticsTool API",
                    Version = "v1",
                    Description = "Diagnostics endpoints wrapping EPCIS traceability resolver functions with detailed diagnostics output"
                });
            });

            services.AddCors(o =>
            {
                o.AddPolicy("Default", p => p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod());
            });
        }

        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            // Initialize for the event profile mappings.
            OpenTraceability.Setup.Initialize();

            app.UseWebSockets();

            if (env.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI(c =>
                {
                    c.SwaggerEndpoint("/swagger/v1/swagger.json", "DiagnosticsTool API v1");
                    c.RoutePrefix = "swagger"; // keep swagger off root so Blazor home page can render at '/'
                });
            }

            if (!env.IsDevelopment())
            {
                app.UseHttpsRedirection();
            }

            app.UseCors("Default");
            app.UseStaticFiles();

            if (app is WebApplication)
            {
                var webApp = app as WebApplication ?? throw new Exception("Failed to cast app to WebApplication.");

                webApp.MapControllers();
                webApp.MapBlazorHub();
                webApp.MapFallbackToPage("/_Host"); // render Blazor home page for root and unmatched routes
            }
            else
            {
                app.UseRouting();

                app.UseAuthorization();

                app.UseEndpoints(builder =>
                {
                    builder.MapControllers();
                    builder.MapBlazorHub();
                    builder.MapFallbackToPage("/_Host"); // render Blazor home page for root and
                });
            }
        }
    }
}