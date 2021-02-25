package insomnia.implem.data.regex.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PRegexElements
{
	private PRegexElements()
	{
		throw new AssertionError();
	}

	// ==========================================================================

	private static class Key extends PRegexElement
	{
		String label;

		private Key(String label)
		{
			super(Type.KEY);
			this.label = label;
		}

		@Override
		public String getLabel()
		{
			return label;
		}
	}

	public static Key createKey(IPRegexElement e)
	{
		Key ret = new Key(null);
		ret.setRooted(e.isRooted());
		ret.setTerminal(e.isTerminal());
		ret.setQuantifier(e.getQuantifier());
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
		public Disjunction(List<IPRegexElement> elements)
		{
			super(Type.DISJUNCTION, elements);
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

	public static Disjunction createDisjunction(List<IPRegexElement> elements)
	{
		return new Disjunction(elements);
	}

	// =========================================================================

	/**
	 * Union element
	 */
	private static class Empty extends PRegexElement
	{
		public Empty(boolean isRooted, boolean isTerminal)
		{
			super(Type.EMPTY);
			setRooted(isRooted);
			setTerminal(isTerminal);
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

	/**
	 * Union element
	 */
	private static class Sequence extends MultipleElement
	{
		public Sequence(List<IPRegexElement> elements)
		{
			super(Type.SEQUENCE, elements);
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

	public static Sequence createSequence(List<IPRegexElement> elements)
	{
		return new Sequence(elements);
	}
	// =========================================================================

	static class PRegexElement implements IPRegexElement
	{
		String     value;
		Quantifier quantifier;
		String     labelDelimiters, valueDelimiters;
		boolean    rooted, terminal;

		Type type;

		public PRegexElement(Type type)
		{
			labelDelimiters = "";
			valueDelimiters = "";
			rooted          = terminal = false;
			this.type       = type;
			this.quantifier = Quantifier.from(1, 1);
		}

		public void setQuantifier(Quantifier quantifier)
		{
			this.quantifier = quantifier;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public void setRooted(boolean rooted)
		{
			this.rooted = rooted;
		}

		public void setTerminal(boolean terminal)
		{
			this.terminal = terminal;
		}

		@Override
		public boolean isRooted()
		{
			return rooted;
		}

		@Override
		public boolean isTerminal()
		{
			return terminal;
		}

		@Override
		public List<IPRegexElement> getElements()
		{
			return Collections.emptyList();
		}

		@Override
		public Type getType()
		{
			return type;
		}

		@Override
		public String getLabel()
		{
			return "";
		}

		@Override
		public String getValue()
		{
			return value;
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

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			if (isRooted())
				sb.append("^");

			if (null != getLabel())
				sb.append(getLabel());

			if (null != value)
				sb.append("=").append(value);

			if (isTerminal())
				sb.append("$");

			sb.append(quantifier);
			return sb.toString();
		}
	}

	/**
	 * Sequence|disjunction element
	 */
	static abstract class MultipleElement extends PRegexElement
	{
		List<IPRegexElement> elements;

		abstract String toString_separator();

		public MultipleElement(Type type, List<IPRegexElement> elements)
		{
			super(type);
			this.elements = new ArrayList<>(elements);
		}

		/**
		 * @return The modifiable {@link ArrayList} of elements.
		 */
		@Override
		public List<IPRegexElement> getElements()
		{
			return elements;
		}

		public String toString()
		{
			StringBuilder sb  = new StringBuilder();
			String        sep = toString_separator();

			sb.append("(");
			boolean first = true;
			for (IPRegexElement e : this.elements)
			{
				if (first)
					first = false;
				else
					sb.append(sep);
				sb.append(e);
			}
			sb.append(")").append(getQuantifier());
			return sb.toString();
		}
	}
}
