package insomnia.implem.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import insomnia.data.IPath;
import insomnia.implem.data.regex.parser.IPRegexElement;
import insomnia.implem.data.regex.parser.Quantifier;
import insomnia.lib.Side;
import insomnia.lib.help.HelpLists;

/**
 * Build a path from a {@link IPRegexElement}.
 * 
 * @author zuri
 * @param <VAL> type of a node value
 * @param <LBL> type of an edge label
 */
final class PathFromPRegexElementBuilder<VAL, LBL>
{
	private Function<String, VAL> mapValue;
	private Function<String, LBL> mapLabel;

	private List<LBL> labels = new ArrayList<>();
	private List<VAL> values = new ArrayList<>();

	/**
	 * @param mapValue a function to map each {@link String} value from an {@link IPRegexElement} to a VAL
	 * @param mapLabel a function to map each {@link String} label from an {@link IPRegexElement} to a LBL
	 */
	public PathFromPRegexElementBuilder(Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		this.mapLabel = mapLabel;
		this.mapValue = mapValue;
	}

	private void checkQuantifier(Quantifier q)
	{
		if (q.getInf() != q.getSup())
			throw new IllegalArgumentException("A path can't have the quantifier: " + q + " in " + element);
	}

	private IPRegexElement element;
	private boolean        isRooted, isTerminal;

	/**
	 * Create a path from an {@link IPRegexElement}
	 * 
	 * @param element the element representing a path
	 * @return the represented path
	 * @throws IllegalArgumentException if element does not represent a {@link IPath}
	 */
	public IPath<VAL, LBL> create(IPRegexElement element)
	{
		this.element = element;
		isRooted     = false;
		isTerminal   = false;
		values.add(null);
		doit(element);
		return new Path<VAL, LBL>(isRooted, isTerminal, labels, values, Side.LEFT);
	}

	private void doit(IPRegexElement element)
	{
		switch (element.getType())
		{
		case EMPTY:
			empty(element);
			break;
		case SEQUENCE:
			sequence(element);
			break;
		case KEY:
			key(element);
			break;
		case DISJUNCTION:
		{
			int nb = element.getElements().size();
			if (nb == 0)
				break;
			if (nb == 1)
			{
				doit(element.getElements().get(0));
				break;
			}
		}
		default:
			throw new IllegalArgumentException(element + " does not represent a path");
		}
	}

	private void empty(IPRegexElement key)
	{
		checkQuantifier(key.getQuantifier());
		VAL value = mapValue.apply(key.getValue());
		values.set(values.size() - 1, value);

		if (key.isTerminal())
			isTerminal = true;
		if (key.isRooted())
			isRooted = true;
	}

	private void key(IPRegexElement key)
	{
		assert (!key.isRooted());
		checkQuantifier(key.getQuantifier());
		LBL label = mapLabel.apply(key.getLabel());
		VAL value = mapValue.apply(key.getValue());
		int i     = key.getQuantifier().getInf();

		if (key.isTerminal())
			isTerminal = true;

		while (i != 0)
		{
			labels.add(label);
			values.add(value);
			i--;
		}
	}

	private void sequence(IPRegexElement sequence)
	{
		checkQuantifier(sequence.getQuantifier());
		int offset = labels.size();
		int i      = sequence.getQuantifier().getInf();

		if (i == 0)
			return;

		for (IPRegexElement e : sequence.getElements())
			doit(e);
		i--;

		List<LBL> subLabels = HelpLists.staticList(labels.subList(offset, labels.size()));
		List<VAL> subValues = HelpLists.staticList(values.subList(offset + 1, values.size()));

		while (i != 0)
		{
			labels.addAll(subLabels);
			values.addAll(subValues);
			i--;
		}
	}
}
