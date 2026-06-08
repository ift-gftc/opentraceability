using System.Runtime.CompilerServices;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using OpenTraceability.TestServer.Auth;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.TestServer.Infrastructure;
using OpenTraceability.TestServer.Services;

namespace OpenTraceability.TestServer
{
    /// <summary>
    /// Configures and boots the GDST 2.0 test traceability server.
    /// </summary>
    public class Startup
    {
        [ModuleInitializer]
        public static void Init()
        {
            OpenTraceability.Setup.Initialize();
            OpenTraceability.GDST.Setup.Initialize();
        }

        public static string Error = "";

        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        public void ConfigureServices(IServiceCollection services)
        {
            try
            {
                services.AddControllers();
                services.AddEndpointsApiExplorer();
                services.AddSwaggerGen();
                services.AddHttpClient();

                // ---- persistence (SQLite) ----
                string connectionString = Configuration.GetConnectionString("sqlite") ?? "Data Source=epcis.db";
                services.AddDbContextFactory<TraceabilityDbContext>(options => options.UseSqlite(connectionString));
                services.AddScoped<ITraceabilityStore, TraceabilityStore>();

                // ---- core services (shared with the WireMock host) ----
                services.AddSingleton<DigitalLinkService>();
                services.AddScoped<EpcisQueryService>();
                services.AddScoped<MasterDataService>();
                services.AddScoped<IngestionService>();
                services.AddScoped<SeedingService>();

                // ---- host-only services ----
                services.AddSingleton<SupportedModules>();
                services.AddScoped<TracebackService>();
                services.AddScoped<CapabilityTestClientService>();

                // ---- API key authentication (all endpoints) ----
                services.AddSingleton<IApiKeyStore, InMemoryApiKeyStore>();
                services.AddAuthentication(ApiKeyAuthenticationOptions.SchemeName)
                        .AddScheme<ApiKeyAuthenticationOptions, ApiKeyAuthenticationHandler>(
                            ApiKeyAuthenticationOptions.SchemeName, _ => { });

                services.AddAuthorization(options =>
                {
                    // require an authenticated user by default for every endpoint
                    options.FallbackPolicy = new AuthorizationPolicyBuilder()
                        .RequireAuthenticatedUser()
                        .Build();
                });

                services.AddCors(options =>
                {
                    options.AddPolicy("myOrigins", builder =>
                        builder.AllowAnyOrigin().AllowAnyMethod().AllowAnyHeader());
                });
            }
            catch (Exception ex)
            {
                Error = ex.ToString();
                throw;
            }
        }

        public void Configure(IApplicationBuilder app)
        {
            app.UseCors("myOrigins");

#if RELEASE
            if (Environment.GetEnvironmentVariable("DISABLE_HTTPS_REDIRECTION") != "TRUE")
            {
                app.UseHttpsRedirection();
            }
#endif

            // ensure the database/schema exists
            using (var scope = app.ApplicationServices.CreateScope())
            {
                var store = scope.ServiceProvider.GetRequiredService<ITraceabilityStore>();
                store.InitializeAsync().GetAwaiter().GetResult();

                // seed bundled datasets (one dataset per folder under SeedData/)
                var seeder = scope.ServiceProvider.GetRequiredService<SeedingService>();
                seeder.SeedFromDirectoryAsync(Path.Combine(AppContext.BaseDirectory, "SeedData"))
                      .GetAwaiter().GetResult();
            }

            if (app is WebApplication webApp)
            {
                app.UseAuthentication();
                app.UseAuthorization();
                webApp.MapControllers();
            }
            else
            {
                app.UseRouting();
                app.UseAuthentication();
                app.UseAuthorization();
                app.UseEndpoints(builder => builder.MapControllers());
            }
        }
    }
}
