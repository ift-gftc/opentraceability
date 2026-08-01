# Build context must be the repository root so the OpenTraceability library can embed schema
# resources from docs/ (referenced as ..\..\docs from the project). The CSharp/ tree is copied to
# /src and docs/ to /docs, so the ..\..\docs links resolve to /docs (mirrors the DiagnosticsTool image).
# Dockerfile.dockerignore next to this file trims that root context down to the four projects in the
# TestServer's build closure plus the four embedded schema documents.
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base
WORKDIR /app

# The final stage runs as the non-root "app" user (uid 1654) that the official .NET images ship.
# /app must be writable by that user, not merely owned file-by-file: appsettings.json uses the
# relative connection string "Data Source=epcis.db" and the store calls EnsureCreatedAsync on
# startup, so the server creates /app/epcis.db on first run. /data is pre-created with the same
# ownership because CSharp/docker-compose.yml mounts a named volume there and points the connection
# string at /data/epcis.db - Docker seeds a fresh named volume from the image directory, ownership
# included, so without this the volume would arrive root-owned and unwritable.
RUN mkdir -p /data && chown app:app /app /data

EXPOSE 8080

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src

# Copy the entire CSharp source tree and the supporting docs (embedded schema resources).
COPY CSharp/ ./
COPY docs/ /docs/

RUN dotnet restore "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj"
RUN dotnet build "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish --chown=app:app /app/publish .
USER app

# Note - no HEALTHCHECK is declared here on purpose: the aspnet base image ships neither curl nor
# wget, so the conventional "curl -f http://localhost:8080/health" probe would exit 127 and mark the
# container permanently unhealthy. The /health endpoint is polled from outside by DockerBuildTests.
ENV ASPNETCORE_URLS=http://+:8080
ENV DISABLE_HTTPS_REDIRECTION=TRUE
ENTRYPOINT ["dotnet", "OpenTraceability.TestServer.dll"]
