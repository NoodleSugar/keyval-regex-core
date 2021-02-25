package insomnia.implem.data.regex.parser;

import java.util.List;

public interface IPRegexElement
{
	public enum Type
	{
		EMPTY, KEY, SEQUENCE, DISJUNCTION
	};

	boolean isRooted();

	boolean isTerminal();

	String getLabel();

	String getValue();

	String getLabelDelimiters();

	String getValueDelimiters();

	Quantifier getQuantifier();

	Type getType();

	List<IPRegexElement> getElements();
}
