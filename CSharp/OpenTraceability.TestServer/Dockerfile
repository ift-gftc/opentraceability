# Build context must be the CSharp/ folder so the referenced projects are available.
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base
WORKDIR /app
EXPOSE 8080

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY . .
RUN dotnet restore "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj"
RUN dotnet build "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENV ASPNETCORE_URLS=http://+:8080
ENV DISABLE_HTTPS_REDIRECTION=TRUE
ENTRYPOINT ["dotnet", "OpenTraceability.TestServer.dll"]
