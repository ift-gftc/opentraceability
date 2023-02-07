using OpenTraceability.Utility;

namespace OpenTraceability.Models.Common
{
    public class Photo
    {
        public Uri URI { get; set; }
        public Uri URL { get; set; }
        public string FileName { get; set; }
        public Measurement Size { get; set; }
        public int PixelHeight { get; set; }
        public int PixelWidth { get; set; }
    }
}