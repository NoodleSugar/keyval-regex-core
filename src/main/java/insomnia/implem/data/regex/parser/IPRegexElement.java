package insomnia.implem.data.regex.parser;

import java.util.List;

/**
 * A part of a tree regular expression.
 * 
 * @author noodle
 * @author zuri
 */
public interface IPRegexElement
{
	public enum Type
	{
		EMPTY, KEY, SEQUENCE, DISJUNCTION, NODE
	};

	/**
	 * @return true if the element represents only a set of paths
	 */
	boolean isPath();

	/**
	 * The number of trees represented by the element.<br>
	 * The result may count duplicate trees.
	 * 
	 * @return the positive number of elements, or -1 if infinite
	 */
	int size();

	/**
	 * The number of trees represented by the element.<br>
	 * The result may count duplicate trees.
	 * 
	 * @return the positive number of elements, or -1 if infinite
	 */
	long longSize();

	boolean isRooted();

	boolean isTerminal();

	/**
	 * @return true if the element is rooted or contains a rooted element.
	 */
	boolean hasRootedElement();

	/**
	 * @return true if the element is terminal or contains a terminal element.
	 */
	boolean hasTerminalElement();

	String getLabel();

	String getValue();

	String getLabelDelimiters();

	String getValueDelimiters();

	Quantifier getQuantifier();

	Type getType();

	List<IPRegexElement> getElements();

	// ==========================================================================

	static boolean isEmpty(IPRegexElement element)
	{
		return element.getType() == Type.EMPTY;
	}

	static boolean isFullEmpty(IPRegexElement element)
	{
		return element.getType() == Type.EMPTY && !element.isRooted() && !element.isTerminal() && null == element.getValue();
	}
}
