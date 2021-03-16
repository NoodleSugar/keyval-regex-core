package insomnia.implem.data.regex.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;

/**
 * @author zuri
 */
public final class RegexElements
{
	private RegexElements()
	{
		throw new AssertionError();
	}

	// ==========================================================================

	/**
	 * Do the last computing of a size multiple element.
	 * It compute <code>size^q.inf + size^(q.inf+1) + ... + size^q.sup</code>.
	 * 
	 * @param quantifier the quantifier
	 * @param size       the element base size
	 * @return
	 */
	private static long size_sumQuantifierPower(Quantifier quantifier, long size)
	{
		long ret  = 0;
		long prod = size;
		long i    = 1;

		// Power of the quantifier inf value
		for (int c = quantifier.getInf(); i < c; i++)
			prod *= size;
		ret += prod;

		// Continue the power for the next quantifier values
		for (int c = quantifier.getSup(); i < c; i++)
		{
			prod *= size;
			ret  += prod;
		}

		if (quantifier.getInf() == 0)
			ret++;
		return ret;
	}

	// ==========================================================================

	private static class Key extends SimpleElement
	{
		String label;

		private Key(String label)
		{
			super(Type.KEY);
			this.label = label;
		}

		@Override
		public long longSize()
		{
			if (Quantifier.isInfinite(quantifier.getSup()))
				return -1;

			long ret = 0;

			for (int i = quantifier.getInf(), c = quantifier.getSup(); i <= c; i++)
				ret++;

			return ret;
		}

		@Override
		public String getLabel()
		{
			return label;
		}
	}

	public static Key createKey(IRegexElement e)
	{
		Key ret = new Key(null);
		ret.setRooted(e.isRooted());
		ret.setTerminal(e.isTerminal());
		ret.setQuantifier(e.getQuantifier());
		ret.setValue(e.getValue());
		return ret;
	}

	public static Key createKey(String label)
	{
		return new Key(label);
	}

	public static Key createKey(boolean isRooted, boolean isTerminal, String label)
	{
		Key ret = new Key(label);
		ret.setRooted(isRooted);
		ret.setTerminal(isTerminal);
		return ret;
	}

	// =========================================================================

	/**
	 * Union element
	 */
	private static class Disjunction extends MultipleElement
	{
		public Disjunction(List<IRegexElement> elements)
		{
			super(Type.DISJUNCTION, elements);
		}

		@Override
		public long longSize()
		{
			if (Quantifier.isInfinite(quantifier.getSup()))
				return -1;

			long base = 0;
			// Sum of each element size
			for (IRegexElement e : getElements())
			{
				long esize = e.longSize();

				if (Quantifier.isInfinite(esize))
					return -1;

				base += esize;
			}
			return size_sumQuantifierPower(quantifier, base);
		}

		@Override
		public boolean isRooted()
		{
			return IterableUtils.matchesAll(getElements(), IRegexElement::isRooted);
		}

		@Override
		public boolean isTerminal()
		{
			return super.isTerminal() || IterableUtils.matchesAll(getElements(), IRegexElement::isTerminal);
		}

		@Override
		String toString_separator()
		{
			return "|";
		}
	}

	public static Disjunction createDisjunction()
	{
		return createDisjunction(new ArrayList<>());
	}

	public static Disjunction createDisjunction(List<IRegexElement> elements)
	{
		return new Disjunction(elements);
	}

	// =========================================================================

	/**
	 * Union element
	 */
	private static class Empty extends SimpleElement
	{
		public Empty(boolean isRooted, boolean isTerminal)
		{
			super(Type.EMPTY);
			setRooted(isRooted);
			setTerminal(isTerminal);
		}

		@Override
		public long longSize()
		{
			if (quantifier.getInf() == 0)
				return 2;

			return 1;
		}

		@Override
		public String getLabel()
		{
			return null;
		}
	}

	public static Empty createEmpty()
	{
		return createEmpty(false, false);
	}

	public static Empty createEmpty(boolean isRooted, boolean isTerminal)
	{
		return new Empty(isRooted, isTerminal);
	}

	// =========================================================================

	private static class Sequence extends MultipleElement
	{
		public Sequence(List<IRegexElement> elements)
		{
			super(Type.SEQUENCE, elements);
		}

		@Override
		public boolean isRooted()
		{
			return getElements().size() > 0 && getElements().get(0).isRooted();
		}

		@Override
		void setTerminal(boolean terminal)
		{
			assert (elements.isEmpty() || terminal == true);

			((PRegexElement) elements.get(elements.size() - 1)).setTerminal(terminal);
			this.terminal = terminal;
		}

		@Override
		public boolean isTerminal()
		{
			int size = getElements().size();
			return super.isTerminal() || size > 0 && getElements().get(size - 1).isTerminal();
		}

		@Override
		String toString_separator()
		{
			return ".";
		}
	}

	public static Sequence createSequence()
	{
		return createSequence(new ArrayList<>());
	}

	public static Sequence createSequence(List<IRegexElement> elements)
	{
		return new Sequence(elements);
	}

	// =========================================================================

	private static class Node extends MultipleElement
	{
		public Node(List<IRegexElement> elements)
		{
			super(Type.NODE, elements);
		}

		@Override
		public boolean isPath()
		{
			return getElements().stream().filter(e -> !IRegexElement.isEmpty(e)).count() <= 1;
		}

		@Override
		public boolean isRooted()
		{
			return hasRootedElement();
		}

		@Override
		public boolean isTerminal()
		{
			return super.isTerminal() || IterableUtils.matchesAll(getElements(), IRegexElement::isTerminal);
		}

		@Override
		String toString_separator()
		{
			return ",";
		}
	}

	public static Node createNode()
	{
		return createNode(new ArrayList<>());
	}

	public static Node createNode(List<IRegexElement> elements)
	{
		return new Node(elements);
	}

	// =========================================================================

	static abstract class PRegexElement implements IRegexElement
	{
		boolean    terminal;
		Quantifier quantifier;
		String     labelDelimiters, valueDelimiters;

		Type type;

		abstract void setValue(String value);

		void setTerminal(boolean terminal)
		{
			this.terminal = terminal;
		}

		@Override
		public boolean isTerminal()
		{
			return terminal;
		}

		public PRegexElement(Type type)
		{
			terminal = false;

			labelDelimiters = "";
			valueDelimiters = "";
			this.type       = type;
			this.quantifier = Quantifier.from(1, 1);
		}

		public void setQuantifier(Quantifier quantifier)
		{
			this.quantifier = quantifier;
		}

		@Override
		public int size()
		{
			return (int) longSize();
		}

		@Override
		public Type getType()
		{
			return type;
		}

		@Override
		public Quantifier getQuantifier()
		{
			return quantifier;
		}

		@Override
		public String getValueDelimiters()
		{
			return valueDelimiters;
		}

		@Override
		public String getLabelDelimiters()
		{
			return labelDelimiters;
		}
	}

	// =========================================================================

	static abstract class SimpleElement extends PRegexElement
	{
		String  value;
		boolean rooted;

		public SimpleElement(Type type)
		{
			super(type);
			rooted = terminal = false;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public void setRooted(boolean rooted)
		{
			this.rooted = rooted;
		}

		@Override
		public String getValue()
		{
			return value;
		}

		@Override
		public boolean isPath()
		{
			return true;
		}

		@Override
		public boolean isRooted()
		{
			return rooted;
		}

		@Override
		public boolean hasRootedElement()
		{
			return rooted;
		}

		@Override
		public boolean hasTerminalElement()
		{
			return terminal;
		}

		@Override
		public List<IRegexElement> getElements()
		{
			return Collections.emptyList();
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			if (isRooted())
				sb.append("^");
			if (getType() == Type.EMPTY)
				sb.append("ε");

			if (null != getLabel())
				sb.append(getLabel());

			if (null != value)
				sb.append("=").append(value);

			if (isTerminal())
				sb.append("$");

			if (!getQuantifier().equals(Quantifier.from(1, 1)))
				sb.append(quantifier);
			return sb.toString();
		}
	}

	// =========================================================================

	/**
	 * Sequence|disjunction element
	 */
	static abstract class MultipleElement extends PRegexElement
	{
		List<IRegexElement> elements;

		abstract String toString_separator();

		public MultipleElement(Type type, List<IRegexElement> elements)
		{
			super(type);
			this.elements = new ArrayList<>(elements);
		}

		@Override
		public boolean isPath()
		{
			for (IRegexElement e : getElements())
				if (!e.isPath())
					return false;
			return true;
		}

		@Override
		void setTerminal(boolean terminal)
		{
			assert (elements.isEmpty() || terminal == true);

			for (IRegexElement ie : elements)
				((PRegexElement) ie).setTerminal(terminal);

			this.terminal = terminal;
		}

		@Override
		public long longSize()
		{
			if (Quantifier.isInfinite(quantifier.getSup()))
				return -1;

			long base = 1;

			// Product of each element
			for (IRegexElement e : getElements())
			{
				long esize = e.longSize();

				if (Quantifier.isInfinite(esize))
					return -1;

				base *= esize;
			}
			return size_sumQuantifierPower(quantifier, base);
		}

		@Override
		public boolean hasRootedElement()
		{
			return IterableUtils.matchesAny(getElements(), IRegexElement::hasRootedElement);
		}

		@Override
		public boolean hasTerminalElement()
		{
			return terminal || IterableUtils.matchesAny(getElements(), IRegexElement::hasTerminalElement);
		}

		@Override
		public String getValue()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getLabel()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		void setValue(String value)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @return The modifiable {@link ArrayList} of elements.
		 */
		@Override
		public List<IRegexElement> getElements()
		{
			return elements;
		}

		public String toString()
		{
			StringBuilder sb  = new StringBuilder();
			String        sep = toString_separator();

			if (isRooted())
				sb.append("^");

			if (getElements().size() <= 1)
				sb.append("[").append(toString_separator()).append("]");

			sb.append("(");

			boolean first = true;
			for (IRegexElement e : this.elements)
			{
				if (first)
					first = false;
				else
					sb.append(sep);
				sb.append(e);
			}
			sb.append(")");

			if (!getQuantifier().equals(Quantifier.from(1, 1)))
				sb.append(getQuantifier());

			if (isTerminal())
				sb.append("$");

			return sb.toString();
		}
	}
}
