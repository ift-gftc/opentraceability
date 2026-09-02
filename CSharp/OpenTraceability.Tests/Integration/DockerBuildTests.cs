using System.Diagnostics;
using System.Net.Http;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using NUnit.Framework;

namespace OpenTraceability.Tests.Integration;

[TestFixture]
[Category("Docker")]
public class DockerBuildTests
{
    /// <summary>
    /// How many seed dataset files OpenTraceability.TestServerPackage ships. Asserted rather than
    /// merely spot-checked, because a .dockerignore rule that swallowed part of SeedData would
    /// leave the server starting up cleanly with datasets silently missing.
    /// </summary>
    private const int ExpectedSeedDataFileCount = 8;

    /// <summary>
    /// Fragments that can only have come from the developer's machine, never from a build inside
    /// the container. Debug, net7.0 and net9.0 stand in for host bin/obj output because the
    /// container builds Release for net10.0/netstandard2.0 only.
    /// </summary>
    private static readonly string[] HostOnlyArtefacts =
    {
        ".vs/",
        ".git/",
        "epcis.db",
        "appsettings.Development.json",
        "README.md",
        "GDST_README.md",
        "README_TESTSERVER.md",
        "PublishProfiles",
        "ServiceDependencies",
        ".csproj.user",
        "launchSettings.json",
        "obj/Debug",
        "bin/Debug",
        "net7.0",
        "net9.0"
    };

    /// <summary>
    /// Everything legitimately published to /app: framework-dependent build output, the runtime
    /// configuration, the seed data, the SQLitePCLRaw native assets, and the satellite culture
    /// folders that ship with Humanizer.
    /// </summary>
    private static readonly Regex[] AllowedAppEntries =
    {
        new Regex(@"^[A-Za-z0-9._-]+\.dll$"),
        new Regex(@"^[A-Za-z0-9._-]+\.pdb$"),
        new Regex(@"^OpenTraceability\.TestServer\.(deps|runtimeconfig|staticwebassets\.endpoints)\.json$"),
        new Regex(@"^appsettings\.json$"),
        new Regex(@"^web\.config$"),
        new Regex(@"^SeedData$"),
        new Regex(@"^runtimes$"),
        new Regex(@"^[a-z]{2}(-[A-Za-z]+){0,2}$")
    };

    /// <summary>
    /// Files that must never reach the runtime image: developer configuration, NuGet packaging
    /// artefacts, source-control leftovers and local databases.
    /// </summary>
    private static readonly string[] ForbiddenPublishedFiles =
    {
        "appsettings.Development.json",
        "README.md",
        "GDST_README.md",
        "README_TESTSERVER.md",
        ".git",
        ".pubxml",
        ".user",
        ".db"
    };

    /// <summary>
    /// Probes whether a docker daemon is reachable, so the Docker-category tests can skip rather
    /// than fail on agents without one.
    /// </summary>
    /// <returns>True when "docker version" succeeds within eight seconds.</returns>
    private static bool DockerAvailable()
    {
        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = RuntimeInformation.IsOSPlatform(OSPlatform.Windows) ? "docker.exe" : "docker",
                Arguments = "version --format '{{.Server.Version}}'",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };
            using var p = Process.Start(psi)!;
            if (!p.WaitForExit(8000))
            {
                try { p.Kill(); } catch { }
                return false;
            }
            return p.ExitCode == 0;
        }
        catch
        {
            return false;
        }
    }

    [Test]
    [Category("Docker")] // Allows filtering e.g. dotnet test --filter Category=Docker
    public async Task BuildAndRun_DiagnosticsTool_Image()
    {
        if (!DockerAvailable())
        {
            Assert.Ignore("Docker is not available on this machine / CI agent.");
        }

        string solutionRoot = GetSolutionRoot();
        string projectDir = Path.Combine(solutionRoot, "DiagnosticsTool");
        string dockerfile = Path.Combine(projectDir, "Dockerfile");
        Assert.That(File.Exists(dockerfile), "Dockerfile not found for DiagnosticsTool.");

        string imageTag = "diagnosticstool-test:latest";
        string containerName = "diagnosticstool-test-container";

        // Clean previous container if exists
        RunDocker($"rm -f {containerName}", ignoreErrors: true);
        RunDocker($"rmi {imageTag}", ignoreErrors: true);

        // Build image using repository root as context so referenced projects are available
        string repoRoot = Directory.GetParent(solutionRoot)?.FullName ?? solutionRoot;
        RunDocker($"build -f \"{dockerfile}\" -t {imageTag} \"{repoRoot}\"");

        // Run container mapping host port 5089 -> container 8080 and enable Development env for Swagger
        int hostPort = 5089;
        RunDocker($"run -d --name {containerName} -e ASPNETCORE_ENVIRONMENT=Development -p {hostPort}:8080 {imageTag}");

        try
        {
            using HttpClient client = new();
            var baseUrl = $"http://localhost:{hostPort}";
            var deadline = DateTime.UtcNow.AddSeconds(90);
            HttpResponseMessage? resp = null;
            Exception? lastEx = null;
            while (DateTime.UtcNow < deadline)
            {
                try
                {
                    resp = await client.GetAsync(baseUrl + "");
                    if (resp.IsSuccessStatusCode) break;
                }
                catch (Exception ex) { lastEx = ex; }
                await Task.Delay(1500);
            }

            Assert.That(resp, Is.Not.Null, "Did not receive any HTTP response from container.");
            Assert.That(resp!.IsSuccessStatusCode, Is.True, $"DiagnosticsTool swagger endpoint not reachable. Last exception: {lastEx}");
            string swaggerJson = await resp.Content.ReadAsStringAsync();
            Assert.That(swaggerJson, Does.Contain("<!DOCTYPE html>"), "HTML doc returned as expected.");
        }
        finally
        {
            // Collect basic logs for debugging if failing
            try { RunDocker($"logs {containerName}", ignoreErrors: true); } catch { }
            RunDocker($"rm -f {containerName}", ignoreErrors: true);
        }
    }

    [Test]
    [Category("Docker")] // Allows filtering e.g. dotnet test --filter Category=Docker
    public async Task BuildAndRun_TestServer_Image()
    {
        if (!DockerAvailable())
        {
            Assert.Ignore("Docker is not available on this machine / CI agent.");
        }

        string solutionRoot = GetSolutionRoot();
        string projectDir = Path.Combine(solutionRoot, "OpenTraceability.TestServer");
        string dockerfile = Path.Combine(projectDir, "Dockerfile");
        Assert.That(File.Exists(dockerfile), "Dockerfile not found for TestServer.");

        string imageTag = "testserver-test:latest";
        string containerName = "testserver-test-container";

        // Clean previous container if exists
        RunDocker($"rm -f {containerName}", ignoreErrors: true);
        RunDocker($"rmi {imageTag}", ignoreErrors: true);

        // Build image using the repository root as context so the OpenTraceability library can embed
        // schema resources from docs/ (referenced as ..\..\docs, which lives outside the CSharp/ folder).
        string repoRoot = Directory.GetParent(solutionRoot)?.FullName ?? solutionRoot;
        RunDocker($"build -f \"{dockerfile}\" -t {imageTag} \"{repoRoot}\"");

        // Run container mapping host port 5089 -> container 8080
        int hostPort = 5089;
        RunDocker($"run -d --name {containerName} -e ASPNETCORE_ENVIRONMENT=Development -p {hostPort}:8080 {imageTag}");

        try
        {
            using HttpClient client = new();
            var healthUrl = $"http://localhost:{hostPort}/health";
            var deadline = DateTime.UtcNow.AddSeconds(90);
            HttpResponseMessage? resp = null;
            Exception? lastEx = null;
            while (DateTime.UtcNow < deadline)
            {
                try
                {
                    resp = await client.GetAsync(healthUrl);
                    if (resp.IsSuccessStatusCode) break;
                }
                catch (Exception ex) { lastEx = ex; }
                await Task.Delay(1500);
            }

            Assert.That(resp, Is.Not.Null, "Did not receive any HTTP response from container.");
            Assert.That(resp!.IsSuccessStatusCode, Is.True, $"TestServer health endpoint not reachable. Last exception: {lastEx}");
            string body = await resp.Content.ReadAsStringAsync();
            Assert.That(body, Does.Contain("Healthy"), "Health endpoint did not report a healthy status.");
        }
        finally
        {
            // Collect basic logs for debugging if failing
            try { RunDocker($"logs {containerName}", ignoreErrors: true); } catch { }
            RunDocker($"rm -f {containerName}", ignoreErrors: true);
        }
    }

    /// <summary>
    /// Builds the Dockerfile's "build" stage and asserts that Dockerfile.dockerignore trimmed the
    /// repository-root context down to the TestServer's build closure.
    /// </summary>
    /// <remarks>
    /// The context is the repository root, which carries roughly 1.1 GB the image never needs -
    /// .git, Java/, .vs/, and the bin/obj of all eight C# projects. This test is the guard that the
    /// allowlist keeps that out: it asserts the exact top level of /src rather than probing for a
    /// handful of known offenders, so a pattern that silently stops matching fails here.
    /// </remarks>
    [Test]
    [Category("Docker")]
    public void BuildStage_TestServerImage_ContainsOnlyTheBuildClosure()
    {
        if (!DockerAvailable())
        {
            Assert.Ignore("Docker is not available on this machine / CI agent.");
        }

        // Arrange
        string solutionRoot = GetSolutionRoot();
        string repoRoot = Directory.GetParent(solutionRoot)?.FullName ?? solutionRoot;
        string dockerfile = Path.Combine(solutionRoot, "OpenTraceability.TestServer", "Dockerfile");
        Assert.That(File.Exists(dockerfile + ".dockerignore"), Is.True, "Dockerfile.dockerignore is missing, so the build context is not scoped.");

        string imageTag = "testserver-buildstage:probe";

        // Act - the build stage is where COPY CSharp/ and COPY docs/ land.
        DockerResult build = RunDocker($"build --progress=plain --target build -f \"{dockerfile}\" -t {imageTag} \"{repoRoot}\"");
        foreach (string line in build.StdErr.Split('\n').Where(l => l.Contains("transferring context")))
        {
            TestContext.Out.WriteLine(line.Trim());
        }

        IReadOnlyList<string> srcTop = ListImagePaths(imageTag, "/src", maxDepth: 1);
        IReadOnlyList<string> srcAll = ListImagePaths(imageTag, "/src");
        IReadOnlyList<string> docs = ListImagePaths(imageTag, "/docs", filesOnly: true);

        // Assert
        Assert.Multiple(() =>
        {
            Assert.That(srcTop, Is.EquivalentTo(new[]
            {
                "OpenTraceability",
                "OpenTraceability.GDST",
                "OpenTraceability.TestServer",
                "OpenTraceability.TestServerPackage"
            }), $"Unexpected top level of /src:{Environment.NewLine}{string.Join(Environment.NewLine, srcTop)}");

            Assert.That(docs, Is.EquivalentTo(new[]
            {
                "beefleather/beefleather_openapi.yaml",
                "epcis/epcis_schema.json",
                "gdst/gdst_json_schema.json",
                "gdst/gdst_openapi.yaml"
            }), "/docs must hold exactly the four schema documents embedded by OpenTraceability.csproj.");

            // Host-only artefacts. A bare "obj" is deliberately absent from this list because the
            // container's own restore creates one; Debug, net7.0 and net9.0 are the discriminating
            // markers, since the container only ever builds Release for net10.0/netstandard2.0.
            foreach (string forbidden in HostOnlyArtefacts)
            {
                Assert.That(srcAll.Any(path => path.Contains(forbidden, StringComparison.OrdinalIgnoreCase)), Is.False, $"'{forbidden}' reached the build context.");
            }

            foreach (string required in new[]
            {
                "OpenTraceability/OpenTraceability.csproj",
                "OpenTraceability.GDST/OpenTraceability.GDST.csproj",
                "OpenTraceability.TestServerPackage/OpenTraceability.TestServerPackage.csproj",
                "OpenTraceability.TestServer/OpenTraceability.TestServer.csproj",
                "OpenTraceability.TestServer/appsettings.json",
                "OpenTraceability/Utility/Data/uoms.json",
                "OpenTraceability/Mappers/EPCIS/mappings.json",
                "OpenTraceability.TestServerPackage/SeedData/default/testdata01.json"
            })
            {
                Assert.That(srcAll, Does.Contain(required), $"'{required}' is missing from the build context.");
            }

            int seedFiles = srcAll.Count(path => path.StartsWith("OpenTraceability.TestServerPackage/SeedData/", StringComparison.Ordinal) && Path.HasExtension(path));
            Assert.That(seedFiles, Is.EqualTo(ExpectedSeedDataFileCount), "All seed data files must reach the build context.");
        });
    }

    /// <summary>
    /// Asserts that the published image's /app carries only runtime assets, and that it runs as the
    /// non-root "app" user.
    /// </summary>
    /// <remarks>
    /// The allowlist is expressed as patterns rather than a literal file list so that ordinary
    /// dependency changes do not churn the test, while anything of a genuinely new shape - a stray
    /// readme, a developer appsettings file, a checked-in database - still fails it.
    /// </remarks>
    [Test]
    [Category("Docker")]
    public void FinalImage_TestServerApp_ContainsOnlyRuntimeAssets()
    {
        if (!DockerAvailable())
        {
            Assert.Ignore("Docker is not available on this machine / CI agent.");
        }

        // Arrange
        string solutionRoot = GetSolutionRoot();
        string repoRoot = Directory.GetParent(solutionRoot)?.FullName ?? solutionRoot;
        string dockerfile = Path.Combine(solutionRoot, "OpenTraceability.TestServer", "Dockerfile");
        string imageTag = "testserver-appprobe:latest";

        // Act
        RunDocker($"build -f \"{dockerfile}\" -t {imageTag} \"{repoRoot}\"");
        IReadOnlyList<string> app = ListImagePaths(imageTag, "/app");
        IReadOnlyList<string> top = ListImagePaths(imageTag, "/app", maxDepth: 1);
        DockerResult size = RunDocker($"image inspect {imageTag} --format {{{{.Size}}}}");
        DockerResult identity = RunDocker($"run --rm --entrypoint id {imageTag}");
        TestContext.Out.WriteLine($"Image size (bytes): {size.StdOut.Trim()}");

        // Assert
        Assert.Multiple(() =>
        {
            string[] unexpected = top.Where(entry => !AllowedAppEntries.Any(rule => rule.IsMatch(entry))).ToArray();
            Assert.That(unexpected, Is.Empty, $"Unexpected entries at the top of /app:{Environment.NewLine}{string.Join(Environment.NewLine, unexpected)}");

            foreach (string forbidden in ForbiddenPublishedFiles)
            {
                Assert.That(app.Any(path => path.Contains(forbidden, StringComparison.OrdinalIgnoreCase)), Is.False, $"'{forbidden}' was published into the image.");
            }

            foreach (string required in new[]
            {
                "OpenTraceability.TestServer.dll",
                "OpenTraceability.TestServer.deps.json",
                "OpenTraceability.TestServer.runtimeconfig.json",
                "OpenTraceability.dll",
                "OpenTraceability.GDST.dll",
                "OpenTraceability.TestServerPackage.dll",
                "appsettings.json",
                "SeedData/default/testdata01.json"
            })
            {
                Assert.That(app, Does.Contain(required), $"'{required}' is missing from the image.");
            }

            int seedFiles = app.Count(path => path.StartsWith("SeedData/", StringComparison.Ordinal) && Path.HasExtension(path));
            Assert.That(seedFiles, Is.EqualTo(ExpectedSeedDataFileCount), "All seed datasets must ship in the image.");

            Assert.That(identity.StdOut, Does.Contain("uid=1654(app)"), "The image must run as the non-root 'app' user.");
        });
    }

    /// <summary>
    /// Runs the docker CLI, streaming both output streams to the test log and returning them so
    /// callers can assert against what the command actually printed.
    /// </summary>
    /// <param name="arguments">Arguments passed verbatim to the docker executable.</param>
    /// <param name="ignoreErrors">When true a non-zero exit code does not fail the test.</param>
    /// <returns>The exit code and both captured streams.</returns>
    private static DockerResult RunDocker(string arguments, bool ignoreErrors = false)
    {
        var psi = new ProcessStartInfo
        {
            FileName = RuntimeInformation.IsOSPlatform(OSPlatform.Windows) ? "docker.exe" : "docker",
            Arguments = arguments,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };

        var stdoutBuilder = new StringBuilder();
        var stderrBuilder = new StringBuilder();

        TestContext.Out.WriteLine($"$ docker {arguments}");

        using var p = Process.Start(psi)!;

        p.OutputDataReceived += (_, e) =>
        {
            if (e.Data is null) return;
            stdoutBuilder.AppendLine(e.Data);
            TestContext.Out.WriteLine(e.Data);
        };

        p.ErrorDataReceived += (_, e) =>
        {
            if (e.Data is null) return;
            stderrBuilder.AppendLine(e.Data);
            TestContext.Out.WriteLine(e.Data);
        };

        p.BeginOutputReadLine();
        p.BeginErrorReadLine();
        p.WaitForExit();

        string stdout = stdoutBuilder.ToString();
        string stderr = stderrBuilder.ToString();

        if (p.ExitCode != 0 && !ignoreErrors)
        {
            Assert.Fail($"Docker command failed: docker {arguments}\nExitCode: {p.ExitCode}\nSTDOUT: {stdout}\nSTDERR: {stderr}");
        }

        return new DockerResult(p.ExitCode, stdout, stderr);
    }

    /// <summary>
    /// Lists every entry beneath <paramref name="root"/> inside an image, as paths relative to
    /// <paramref name="root"/>.
    /// </summary>
    /// <remarks>
    /// GNU find is invoked as the entrypoint rather than through "sh -c" so that no shell sits
    /// between <see cref="Process"/> and the container. That matters on Windows hosts: a shell
    /// would add a quoting layer, and running the suite from Git Bash would let MSYS2 rewrite an
    /// argument like "/app" into a Windows path before docker ever saw it. find is Priority:
    /// required in Debian, so it is present in both the sdk and aspnet base images.
    /// </remarks>
    /// <param name="imageTag">The image to inspect.</param>
    /// <param name="root">An absolute directory inside the image, for example "/app".</param>
    /// <param name="maxDepth">Optional find -maxdepth; pass 1 for a top-level listing.</param>
    /// <param name="filesOnly">When true, only regular files are listed.</param>
    /// <returns>Relative, forward-slash separated paths, ordinally sorted.</returns>
    private static IReadOnlyList<string> ListImagePaths(string imageTag, string root, int? maxDepth = null, bool filesOnly = false)
    {
        string depth = maxDepth is null ? string.Empty : $" -maxdepth {maxDepth}";
        string type = filesOnly ? " -type f" : string.Empty;

        DockerResult result = RunDocker($"run --rm --entrypoint find {imageTag} {root} -mindepth 1{depth}{type}");

        return result.StdOut
            .Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries)
            .Select(line => line.Trim())
            .Where(line => line.StartsWith(root + "/", StringComparison.Ordinal))
            .Select(line => line.Substring(root.Length + 1))
            .OrderBy(path => path, StringComparer.Ordinal)
            .ToList();
    }

    /// <summary>
    /// Walks up from the test output directory to the CSharp solution folder.
    /// </summary>
    /// <remarks>
    /// Internal rather than private so <see cref="DockerIgnoreClosureTests"/> can locate the same
    /// folder without duplicating the walk-up loop.
    /// </remarks>
    /// <returns>The absolute path of the folder containing the C# projects.</returns>
    /// <exception cref="DirectoryNotFoundException">Thrown when the folder cannot be located.</exception>
    internal static string GetSolutionRoot()
    {
        // Starting from test directory, walk up until we find the folder that contains DiagnosticsTool project
        var dir = new DirectoryInfo(TestContext.CurrentContext.TestDirectory);
        for (int i = 0; i < 10 && dir != null; i++)
        {
            if (Directory.Exists(Path.Combine(dir.FullName, "DiagnosticsTool")) && File.Exists(Path.Combine(dir.FullName, "DiagnosticsTool", "DiagnosticsTool.csproj")))
            {
                return dir.FullName;
            }
            dir = dir.Parent!;
        }
        throw new DirectoryNotFoundException("Failed to locate solution root containing DiagnosticsTool directory.");
    }
}
