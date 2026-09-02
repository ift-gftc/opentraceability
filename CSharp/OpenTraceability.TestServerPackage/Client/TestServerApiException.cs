using System;
using System.Net;

namespace OpenTraceability.TestServer.Core.Client
{
    /// <summary>
    /// Thrown by <see cref="TestServerClient"/> when the test server returns a non-success status
    /// code. Carries the status code and the response body for diagnostics.
    /// </summary>
    public class TestServerApiException : Exception
    {
        public HttpStatusCode StatusCode { get; }

        public string ResponseBody { get; }

        public TestServerApiException(HttpStatusCode statusCode, string method, string url, string responseBody)
            : base($"{method} {url} failed with {(int)statusCode} {statusCode}: {responseBody}")
        {
            StatusCode = statusCode;
            ResponseBody = responseBody;
        }
    }
}
