using System.Diagnostics;
using System.Net.Http;
using System.Runtime.InteropServices;
using System.Text;
using NUnit.Framework;

namespace OpenTraceability.Tests.Integration;

[TestFixture]
[Category("Docker")]
public class DockerBuildTests
{
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

    private static void RunDocker(string arguments, bool ignoreErrors = false)
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
    }

    private static string GetSolutionRoot()
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
