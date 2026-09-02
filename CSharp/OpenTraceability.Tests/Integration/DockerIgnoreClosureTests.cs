using System.Text.RegularExpressions;
using System.Xml.Linq;
using NUnit.Framework;

namespace OpenTraceability.Tests.Integration;

/// <summary>
/// Guards that CSharp/OpenTraceability.TestServer/Dockerfile.dockerignore stays in sync with the
/// TestServer's project graph.
/// </summary>
/// <remarks>
/// The ignore file is a strict allowlist: it denies the whole repository-root build context and
/// re-includes only the four projects in the TestServer's ProjectReference closure plus the four
/// schema documents OpenTraceability.csproj embeds through ..\..\docs\ links. That is precise, but
/// it fails badly if someone adds a reference and forgets the ignore file - the Docker build dies
/// several minutes into CI with an MSB3202 about a missing project file, which does not obviously
/// point at a .dockerignore. These tests turn that into a sub-second failure that names exactly
/// what is missing. They need no docker daemon, so they run in the ordinary unit test pass.
/// </remarks>
[TestFixture]
[Category("UnitTest")]
public class DockerIgnoreClosureTests
{
    /// <summary>Matches a re-included project, for example "!CSharp/OpenTraceability.GDST/**".</summary>
    private static readonly Regex ProjectReInclude = new Regex(@"^!CSharp/([^/]+)/\*\*$");

    /// <summary>Matches a re-included documentation file, for example "!docs/epcis/epcis_schema.json".</summary>
    private static readonly Regex DocsReInclude = new Regex(@"^!(docs/.+)$");

    private string _solutionRoot = null!;
    private string _ignoreFilePath = null!;

    /// <summary>
    /// Locates the solution folder and the ignore file before each test.
    /// </summary>
    [SetUp]
    public void Setup()
    {
        _solutionRoot = DockerBuildTests.GetSolutionRoot();
        _ignoreFilePath = Path.Combine(_solutionRoot, "OpenTraceability.TestServer", "Dockerfile.dockerignore");
    }

    /// <summary>
    /// Without the ignore file the build context silently reverts to the whole repository, so its
    /// absence is itself a failure rather than a reason to skip.
    /// </summary>
    [Test]
    public void DockerIgnore_ForTestServer_Exists()
    {
        Assert.That(File.Exists(_ignoreFilePath), Is.True, $"Expected a scoped ignore file at {_ignoreFilePath}.");
    }

    /// <summary>
    /// Every project the TestServer transitively references must be re-included, and nothing else -
    /// a stale extra entry quietly re-admits a project's sources to the image.
    /// </summary>
    [Test]
    public void DockerIgnore_ReIncludedProjects_MatchTheTestServerReferenceClosure()
    {
        // Arrange
        string testServerProject = Path.Combine(_solutionRoot, "OpenTraceability.TestServer", "OpenTraceability.TestServer.csproj");

        // Act
        HashSet<string> reIncluded = ReadReIncluded(ProjectReInclude);
        HashSet<string> closure = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        CollectProjectClosure(testServerProject, closure);

        // Assert
        Assert.That(reIncluded, Is.EquivalentTo(closure), BuildDriftMessage("project", reIncluded, closure,
            "Add or remove the matching '!CSharp/<project>/**' line in Dockerfile.dockerignore."));
    }

    /// <summary>
    /// The docs/ files OpenTraceability.csproj embeds must all be re-included; a missing one fails
    /// the container build with an unresolved EmbeddedResource.
    /// </summary>
    [Test]
    public void DockerIgnore_ReIncludedDocs_MatchTheEmbeddedSchemaResources()
    {
        // Arrange
        string libraryProject = Path.Combine(_solutionRoot, "OpenTraceability", "OpenTraceability.csproj");

        // Act
        HashSet<string> reIncluded = ReadReIncluded(DocsReInclude);
        HashSet<string> embedded = ReadEmbeddedDocsResources(libraryProject);

        // Assert
        Assert.That(embedded, Is.Not.Empty, "Expected OpenTraceability.csproj to embed at least one resource from docs/.");
        Assert.That(reIncluded, Is.EquivalentTo(embedded), BuildDriftMessage("docs", reIncluded, embedded,
            "Add or remove the matching '!docs/<path>' line in Dockerfile.dockerignore."));
    }

    /// <summary>
    /// The allowlist only works if the deny-all line comes first and the re-exclusions come last.
    /// A re-inclusion accidentally appended below "CSharp/**/bin" would be overridden silently,
    /// because the last matching pattern wins.
    /// </summary>
    [Test]
    public void DockerIgnore_PatternOrdering_DeniesFirstAndReExcludesLast()
    {
        // Arrange
        List<string> patterns = ReadPatterns();

        // Act
        int denyAll = patterns.IndexOf("**");
        int lastReInclude = patterns.FindLastIndex(pattern => pattern.StartsWith("!", StringComparison.Ordinal));
        int firstReExclude = patterns.FindIndex(denyAll + 1, pattern => !pattern.StartsWith("!", StringComparison.Ordinal));

        // Assert
        Assert.Multiple(() =>
        {
            Assert.That(denyAll, Is.EqualTo(0), "The deny-all '**' must be the first pattern in the file.");
            Assert.That(firstReExclude, Is.GreaterThan(lastReInclude), "Every re-exclusion must sit below every re-inclusion, because the last matching pattern wins.");
        });
    }

    /// <summary>
    /// Reads the ignore file's significant lines, dropping comments and blanks.
    /// </summary>
    /// <returns>The patterns, in file order.</returns>
    private List<string> ReadPatterns()
    {
        return File.ReadAllLines(_ignoreFilePath)
            .Select(line => line.Trim())
            .Where(line => line.Length > 0 && !line.StartsWith("#", StringComparison.Ordinal))
            .ToList();
    }

    /// <summary>
    /// Extracts the first capture group of every pattern matching the given expression.
    /// </summary>
    /// <param name="expression">The re-inclusion shape to collect.</param>
    /// <returns>The captured values, case-insensitively de-duplicated.</returns>
    private HashSet<string> ReadReIncluded(Regex expression)
    {
        return ReadPatterns()
            .Select(pattern => expression.Match(pattern))
            .Where(match => match.Success)
            .Select(match => match.Groups[1].Value)
            .ToHashSet(StringComparer.OrdinalIgnoreCase);
    }

    /// <summary>
    /// Adds a project and everything it transitively references to <paramref name="closure"/>.
    /// </summary>
    /// <remarks>
    /// Projects are identified by their containing folder name, which is what the ignore file's
    /// '!CSharp/&lt;project&gt;/**' patterns key on. The visited set doubles as the recursion guard,
    /// so a reference cycle terminates instead of overflowing the stack.
    /// </remarks>
    /// <param name="projectPath">Absolute path of the .csproj to walk.</param>
    /// <param name="closure">Accumulator of project folder names.</param>
    private static void CollectProjectClosure(string projectPath, HashSet<string> closure)
    {
        string projectDirectory = Path.GetDirectoryName(projectPath)!;
        if (!closure.Add(new DirectoryInfo(projectDirectory).Name))
        {
            return;
        }

        XDocument document = XDocument.Load(projectPath);
        IEnumerable<string> references = document.Descendants()
            .Where(element => element.Name.LocalName == "ProjectReference")
            .Select(element => element.Attribute("Include")?.Value)
            .Where(include => !string.IsNullOrWhiteSpace(include))
            .Select(include => include!.Replace('\\', Path.DirectorySeparatorChar));

        foreach (string reference in references)
        {
            CollectProjectClosure(Path.GetFullPath(Path.Combine(projectDirectory, reference)), closure);
        }
    }

    /// <summary>
    /// Finds every EmbeddedResource in a project that points outside the CSharp tree into docs/.
    /// </summary>
    /// <param name="projectPath">Absolute path of the .csproj to inspect.</param>
    /// <returns>Repository-relative, forward-slash separated docs/ paths.</returns>
    private static HashSet<string> ReadEmbeddedDocsResources(string projectPath)
    {
        XDocument document = XDocument.Load(projectPath);

        return document.Descendants()
            .Where(element => element.Name.LocalName == "EmbeddedResource")
            .Select(element => element.Attribute("Include")?.Value)
            .Where(include => !string.IsNullOrWhiteSpace(include))
            .Select(include => include!.Replace('\\', '/'))
            .Where(include => include.StartsWith("../../docs/", StringComparison.OrdinalIgnoreCase))
            .Select(include => include.Substring("../../".Length))
            .ToHashSet(StringComparer.OrdinalIgnoreCase);
    }

    /// <summary>
    /// Builds an assertion message naming exactly which entries drifted, in either direction.
    /// </summary>
    /// <param name="kind">What the entries are, for example "project".</param>
    /// <param name="reIncluded">What the ignore file re-includes.</param>
    /// <param name="expected">What the project graph actually requires.</param>
    /// <param name="remedy">The fix to suggest.</param>
    /// <returns>A message listing the missing and the surplus entries.</returns>
    private static string BuildDriftMessage(string kind, IEnumerable<string> reIncluded, IEnumerable<string> expected, string remedy)
    {
        string[] missing = expected.Except(reIncluded, StringComparer.OrdinalIgnoreCase).ToArray();
        string[] surplus = reIncluded.Except(expected, StringComparer.OrdinalIgnoreCase).ToArray();

        return $"Dockerfile.dockerignore is out of sync with the {kind} graph. "
            + $"Missing re-inclusions: [{string.Join(", ", missing)}]. "
            + $"Surplus re-inclusions: [{string.Join(", ", surplus)}]. "
            + remedy;
    }
}
