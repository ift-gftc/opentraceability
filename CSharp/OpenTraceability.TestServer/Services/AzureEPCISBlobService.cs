using System;
using OpenTraceability.TestServer.Models;
using OpenTraceability.TestServer.Services.Interfaces;

namespace OpenTraceability.TestServer.Services
{
	/// <summary>
	/// An implementation of the IEPCISBlobService that uses an Azure Storage
	/// for storing the blobs.
	/// </summary>
	public class AzureEPCISBlobService : IEPCISBlobService
	{
		IConfiguration _config;

        public AzureEPCISBlobService(IConfiguration config)
        {
            _config = config;
        }

        public Task<EPCISBlob> LoadBlob(string id)
        {
            throw new NotImplementedException();
        }

        public Task<bool> SaveBlob(EPCISBlob blob)
        {
            throw new NotImplementedException();
        }
    }
}

