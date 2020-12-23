package insomnia.implem.kv.pregex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import insomnia.implem.kv.data.KVValue;

public final class PRegexElements
{
	public static class Key extends AbstractElement
	{
		String label;

		public Key(String label)
		{
			super(Type.KEY);
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		public String toString()
		{
			return label + quantifier;
		}
	}

	public static IPRegexElement createKey(String label)
	{
		return new Key(label);
	}

	// =========================================================================

	public static class Value extends AbstractElement
	{
		KVValue value;

		public Value(KVValue value)
		{
			super(Type.VALUE);
			this.value = value;
		}

		public KVValue getValue()
		{
			return value;
		}

		public String toString()
		{
			return value.toString() + quantifier;
		}
	}

	public static Value createValue(KVValue value)
	{
		return new Value(value);
	}

	// =========================================================================

	/**
	 * Regex element (delimited by ~)
	 * Two different regex can have intersection area,
	 * the user must take care of their use because of the determinization algorithm.
	 */
	public static class Regex extends AbstractElement
	{
		String regex;

		public Regex(String regex)
		{
			super(Type.REGEX);
			this.regex = regex;
		}

		public String getRegex()
		{
			return regex;
		}

		public String toString()
		{
			return regex + quantifier;
		}
	}

	public static Regex createRegex(String regex)
	{
		return new Regex(regex);
	}

	// =========================================================================

	/**
	 * Union element
	 */
	public static class Disjunction extends MultipleElement
	{
		public Disjunction(Collection<IPRegexElement> elements)
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

	public static Disjunction createDisjunction(Collection<IPRegexElement> elements)
	{
		return new Disjunction(elements);
	}

	// =========================================================================

	/**
	 * Union element
	 */
	public static class Sequence extends MultipleElement
	{
		public Sequence(Collection<IPRegexElement> elements)
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

	public static Sequence createSequence(Collection<IPRegexElement> elements)
	{
		return new Sequence(elements);
	}
	// =========================================================================

	static abstract class AbstractElement implements IPRegexElement
	{
		Quantifier quantifier;

		Type type;

		public AbstractElement(Type type)
		{
			this.type = type;
		}

		public void setQuantifier(Quantifier quantifier)
		{
			this.quantifier = quantifier;
		}

		@Override
		public Collection<IPRegexElement> getElements()
		{
			return Collections.emptyList();
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
	}

	/**
	 * Sequence|disjunction element
	 */
	public static abstract class MultipleElement extends AbstractElement
	{
		Collection<IPRegexElement> elements;

		abstract String toString_separator();

		public MultipleElement(Type type, Collection<IPRegexElement> elements)
		{
			super(type);
			this.elements = elements;
		}

		/**
		 * @return The modifiable {@link ArrayList} of elements.
		 */
		@Override
		public Collection<IPRegexElement> getElements()
		{
			return elements;
		}

		public String toString()
		{
			StringBuffer buffer = new StringBuffer();

			boolean hasQuantifier = quantifier.getInf() != 1 || quantifier.getSup() != 1;

			if (hasQuantifier)
				buffer.append('(');

			String sep = toString_separator();

			boolean first = true;
			for (IPRegexElement e : this.elements)
			{
				if (first)
					first = false;
				else
					buffer.append(sep);

				if (e.getType() == Type.KEY)
					buffer.append(e);
				else
					buffer.append('(').append(e).append(')');
			}
			if (hasQuantifier)
				buffer.append(')').append(quantifier);

			return buffer.toString();
		}
	}
}
