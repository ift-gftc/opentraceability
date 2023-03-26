using System;
using System.Runtime.CompilerServices;
using Microsoft.Data.Sqlite;
using OpenTraceability.TestServer.Services;
using OpenTraceability.TestServer.Services.Interfaces;

namespace OpenTraceability.TestServer
{
    /// <summary>
    /// Class for handling the startup of the web service.
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

                // Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
                services.AddEndpointsApiExplorer();
                services.AddSwaggerGen();

                if (Configuration.GetConnectionString("sqlite") != null)
                {
                    services.AddScoped<IEPCISBlobService, EPCISBlobSqlLiteService>();
                    ConfigureSqlite(Configuration);
                }

                services.AddCors(options =>
                {
                    options.AddPolicy(name: "myOrigins",
                                      builder =>
                                      {
                                          builder.WithOrigins("https://localhost:4001")
                                                 .AllowAnyMethod().AllowAnyHeader().AllowCredentials();
                                      });
                });
            }
            catch (Exception ex)
            {
                Error = ex.ToString();
                throw;
            }
        }


        void ConfigureSqlite(IConfiguration config)
        {
            string cs = config.GetConnectionString("sqlite") ?? throw new Exception("No connection string 'sqlite' found in appsettings.");

            string tableCmd = @"
                    CREATE TABLE IF NOT EXISTS data (
                        id varchar(128) PRIMARY KEY,
                        version int NOT NULL,
                        format int NOT NULL,
                        raw_data text NOT NULL,
                        created datetime NOT NULL)
                ";

            using var con = new SqliteConnection(cs);
            con.Open();

            using var cmd = new SqliteCommand(tableCmd, con);
            cmd.ExecuteNonQuery();
        }

        public void Configure(IApplicationBuilder app)
        {
            app.UseCors("myOrigins");

#if RELEASE
            app.UseHttpsRedirection();
#endif

            if (app is WebApplication)
            {
                var webApp = (app as WebApplication) ?? throw new Exception("Failed to cast app to WebApplication.");

                app.UseAuthentication();
                app.UseAuthorization();

                webApp.MapControllers();
            }
            else
            {
                app.UseRouting();

                app.UseAuthentication();
                app.UseAuthorization();

                app.UseEndpoints(builder =>
                {
                    builder.MapControllers();
                });
            }
        }
    }
}

