using System;
using OpenTraceability.TestServer.Models;

namespace OpenTraceability.TestServer.Services.Interfaces
{
	/// <summary>
	/// The dependency service used for loading and saving the epcis data
	/// to a persisted data storage.
	/// </summary>
	public interface IEPCISBlobService
	{
		Task<bool> SaveBlob(EPCISBlob blob);
		Task<EPCISBlob?> LoadBlob(string id);
	}
}

