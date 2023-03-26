using System;
namespace OpenTraceability.Interfaces
{
	public interface IMasterDataMapper
	{
		string Map(IVocabularyElement vocab);
        IVocabularyElement Map(Type T, string value);
		IVocabularyElement Map<T>(string value) where T : IVocabularyElement;
	}
}

