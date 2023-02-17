using System;
namespace OpenTraceability.Interfaces
{
	public interface IMasterDataMapper
	{
		string Map(IVocabularyElement vocab);

		IVocabularyElement Map(string value);
	}
}

