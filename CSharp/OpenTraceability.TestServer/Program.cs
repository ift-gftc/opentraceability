using Microsoft.Data.Sqlite;
using OpenTraceability.TestServer;

var builder = WebApplication.CreateBuilder(args);
Startup startup = new Startup(builder.Configuration);
startup.ConfigureServices(builder.Services);

var app = builder.Build();
startup.Configure(app);
app.Run();
