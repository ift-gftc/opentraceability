using System;
namespace OpenTraceability.Interfaces
{
	public interface IMasterDataMapper
	{
		string Map(IVocabularyElement vocab);

		IVocabularyElement Map<T>(string value) where T : IVocabularyElement;
	}
}

