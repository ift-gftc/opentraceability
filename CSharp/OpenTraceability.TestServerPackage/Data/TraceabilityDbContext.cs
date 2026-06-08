using Microsoft.EntityFrameworkCore;
using OpenTraceability.TestServer.Core.Data.Entities;

namespace OpenTraceability.TestServer.Core.Data
{
    /// <summary>
    /// EF Core context for the traceability store. Backed by SQLite (file-based for the real
    /// server, in-memory for the WireMock test host).
    /// </summary>
    public class TraceabilityDbContext : DbContext
    {
        public TraceabilityDbContext(DbContextOptions<TraceabilityDbContext> options) : base(options)
        {
        }

        public DbSet<TraceabilityEvent> Events { get; set; } = null!;
        public DbSet<EventSearchEntry> EventSearchEntries { get; set; } = null!;
        public DbSet<MasterDataRecord> MasterDataRecords { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<TraceabilityEvent>(e =>
            {
                e.HasIndex(x => new { x.DatasetId, x.EventId }).IsUnique().HasDatabaseName("IX_Events_Dataset_EventId");
                e.HasIndex(x => x.DatasetId).HasDatabaseName("IX_Events_Dataset");
                e.HasIndex(x => x.BizStep).HasDatabaseName("IX_Events_BizStep");
            });

            modelBuilder.Entity<EventSearchEntry>(e =>
            {
                e.HasIndex(x => x.DatasetId).HasDatabaseName("IX_Search_Dataset");
                e.HasIndex(x => x.EventId).HasDatabaseName("IX_Search_EventId");
                e.HasIndex(x => x.EPC).HasDatabaseName("IX_Search_EPC");
                e.HasIndex(x => x.ProductGTIN).HasDatabaseName("IX_Search_ProductGTIN");
                e.HasIndex(x => x.LocationGLN).HasDatabaseName("IX_Search_LocationGLN");
                e.HasIndex(x => x.PartyPGLN).HasDatabaseName("IX_Search_PartyPGLN");
                e.HasIndex(x => x.BizStep).HasDatabaseName("IX_Search_BizStep");
                e.HasIndex(x => x.Action).HasDatabaseName("IX_Search_Action");
            });

            modelBuilder.Entity<MasterDataRecord>(e =>
            {
                e.HasIndex(x => new { x.DatasetId, x.ElementId }).IsUnique().HasDatabaseName("IX_MasterData_Dataset_ElementId");
                e.HasIndex(x => x.DatasetId).HasDatabaseName("IX_MasterData_Dataset");
            });
        }
    }
}
