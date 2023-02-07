using OpenTraceability.Utility;

namespace OpenTraceability.Models.Common
{
    public class Attachment
    {
        public AttachmentType AttachmentType { get; set; }
        public string FileName { get; set; }
        public Uri URI { get; set; }
        public Uri URL { get; set; }
        public Measurement Size { get; set; }
        public DateTime? StartDate { get; set; }
        public DateTime? EndDate { get; set; }
        public string Description { get; set; }
        public int PixelWidth { get; set; }
        public int PixelHeight { get; set; }
    }
}