package insomnia.implem.kv.pregex.element;

import java.util.Collection;

public interface IElement
{
	public enum Type
	{
		KEY, SEQUENCE, DISJUNCTION, REGEX, VALUE,
	};

	Quantifier getQuantifier();

	Type getType();

	Collection<IElement> getElements();

	void setQuantifier(Quantifier q);
}
