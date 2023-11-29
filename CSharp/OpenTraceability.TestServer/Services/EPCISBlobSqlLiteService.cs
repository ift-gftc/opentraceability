using System;
using System.Collections.Concurrent;
using Microsoft.Data.Sqlite;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.TestServer.Models;
using OpenTraceability.TestServer.Services.Interfaces;

namespace OpenTraceability.TestServer.Services
{
	/// <summary>
	/// An implementation of the IEPCISBlobService that uses SQL Lite to store
	/// the blobs.
	/// </summary>
	public class EPCISBlobSqlLiteService : IEPCISBlobService
	{
		IConfiguration _config;

        static object cacheLock = new object();
        static ConcurrentDictionary<string, (EPCISBlob blob, DateTime lastAccess)> _blobCache = new ConcurrentDictionary<string, (EPCISBlob blob, DateTime lastAccess)>();

		public EPCISBlobSqlLiteService(IConfiguration config)
		{
			_config = config;
		}

        public Task<EPCISBlob?> LoadBlob(string id)
        {
            try
            {
                if (_blobCache.TryGetValue(id, out var b))
                {
                    b.lastAccess = DateTime.UtcNow;
                    return Task.FromResult<EPCISBlob?>(b.blob);
                }

                string cs = _config.GetConnectionString("sqlite") ?? throw new Exception("No connection string 'sqlite' found in appsettings.");

                // TODO: connect to the database
                using var con = new SqliteConnection(cs);
                con.Open();

                // query the database by the id
                string stm = "SELECT id, format, version, raw_data, created FROM data WHERE id=@id";

                using var cmd = new SqliteCommand(stm, con);
                cmd.Parameters.AddWithValue("@id", id);

                using SqliteDataReader rdr = cmd.ExecuteReader();

                EPCISBlob? blob = null;
                while (rdr.Read())
                {
                    // read the blob
                    blob = new EPCISBlob();
                    blob.ID = rdr.GetString(0);
                    blob.Format = (EPCISDataFormat)rdr.GetInt32(1);
                    blob.Version = (EPCISVersion)rdr.GetInt32(2);
                    blob.RawData = rdr.GetString(3);
                    blob.Created = rdr.GetDateTime(4);
                    break;
                }

                if (blob != null)
                {
                    lock (cacheLock)
                    {
                        if (!_blobCache.ContainsKey(id) && _blobCache.Values.Count > 5)
                        {
                            var oldest = _blobCache.OrderBy(v => v.Value.lastAccess).First();
                            _blobCache.TryRemove(oldest.Key, out _);
                            _blobCache.TryAdd(id, (blob, DateTime.UtcNow));
                        }
                        else
                        {
                            _blobCache[id] = (blob, DateTime.UtcNow);
                        }
                    }
                }

                // asynchronously delete all blobs that are more than 1 month old...
                Task.Run(() =>
                {
                    string deleteCmd = "DELETE FROM data WHERE created < @date";
                    using var delete = new SqliteCommand(deleteCmd, con);
                    delete.Parameters.AddWithValue("@date", DateTime.UtcNow.AddMonths(-1));
                    delete.ExecuteNonQuery();
                });

                // return the blob
                return Task.FromResult(blob);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                throw;
            }
        }

        public Task<bool> SaveBlob(EPCISBlob blob)
        {
			string cs = _config.GetConnectionString("sqlite") ?? throw new Exception("No connection string 'sqlite' found in appsettings.");

            // TODO: connect to the database
            using var con = new SqliteConnection(cs);
            con.Open();

            // TODO: if blob already exists, then delete it
            {
                string stm = "DELETE FROM data WHERE ID=@id";
                using var cmd = new SqliteCommand(stm, con);
                cmd.Parameters.AddWithValue("@id", blob.ID);
                cmd.ExecuteNonQuery();
            }

            // TODO: write blob into database
            {
                string cmdText = @"INSERT INTO data(id, format, version, raw_data, created)
                                   VALUES(@id, @format, @version, @raw_data, @created)";

                using var cmd = new SqliteCommand(cmdText, con);

                cmd.Parameters.AddWithValue("@id", blob.ID);
                cmd.Parameters.AddWithValue("@format", (int)blob.Format);
                cmd.Parameters.AddWithValue("@version", (int)blob.Version);
                cmd.Parameters.AddWithValue("@raw_data", blob.RawData);
                cmd.Parameters.AddWithValue("@created", blob.Created);
                cmd.Prepare();

                cmd.ExecuteNonQuery();
            }

            return Task.FromResult(true);
        }
    }
}

