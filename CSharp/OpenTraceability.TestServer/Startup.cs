using System.Runtime.CompilerServices;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using OpenTraceability.TestServer.Auth;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;
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
                services.AddScoped<DatasetContext>();
                services.AddScoped<DatasetResolutionFilter>();
                services.AddScoped<TracebackService>();

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

                // The "default" dataset backs the bare (non-prefixed) routes and X-Dataset-Id
                // header consumers. Its modules come from config; it is only created when absent
                // so operator edits via the /datasets API survive restarts.
                var configuredNames = Configuration.GetSection("Modules").Get<List<string>>() ?? new List<string>();
                if (!ModuleNames.TryParseStrict(configuredNames, out var defaultModules, out var invalidNames))
                {
                    throw new Exception($"appsettings 'Modules' contains unknown module names: {string.Join(", ", invalidNames)}");
                }
                if (store.GetDatasetAsync("default").GetAwaiter().GetResult() == null)
                {
                    store.UpsertDatasetAsync(new Dataset
                    {
                        DatasetId = "default",
                        Modules = defaultModules,
                        Description = "Default dataset (bare routes / X-Dataset-Id header)"
                    }).GetAwaiter().GetResult();
                }

                // seed bundled datasets (one dataset per folder under SeedData/)
                var seeder = scope.ServiceProvider.GetRequiredService<SeedingService>();
                seeder.SeedFromDirectoryAsync(Path.Combine(AppContext.BaseDirectory, "SeedData"), defaultModules)
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
