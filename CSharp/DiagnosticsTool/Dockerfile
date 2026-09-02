# Use the official .NET 10 runtime as the base image for the final stage
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

# Use the official .NET 10 SDK for building the application
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src

# Copy the project files needed for restore
COPY ["CSharp/DiagnosticsTool/DiagnosticsTool.csproj", "DiagnosticsTool/"]
COPY ["CSharp/OpenTraceability/OpenTraceability.csproj", "OpenTraceability/"]
RUN dotnet restore "DiagnosticsTool/DiagnosticsTool.csproj"

# Copy the entire source code and supporting docs
COPY CSharp/ ./
COPY docs/ /docs/
WORKDIR "/src/DiagnosticsTool"

# Build the application
RUN dotnet build "DiagnosticsTool.csproj" -c $BUILD_CONFIGURATION -o /app/build

# Publish the application
FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "DiagnosticsTool.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

# Final stage: Create the runtime image
FROM base AS final
WORKDIR /app

# Copy the published application and assign ownership to the built-in non-root user
COPY --from=publish --chown=app:app /app/publish .

# Switch to the built-in non-root user provided by official .NET images
USER app

# Set environment variables
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Define the entry point
ENTRYPOINT ["dotnet", "DiagnosticsTool.dll"]