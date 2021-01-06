package insomnia.implem.kv.pregex;

import java.util.Collection;

public interface IPRegexElement
{
	public enum Type
	{
		KEY, SEQUENCE, DISJUNCTION, REGEX, VALUE,
	};

	Quantifier getQuantifier();

	Type getType();

	Collection<IPRegexElement> getElements();

	void setQuantifier(Quantifier q);
}
