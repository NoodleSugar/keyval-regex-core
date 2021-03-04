package insomnia.implem.data;

import java.util.Iterator;
import java.util.function.Function;

import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.data.regex.parser.IPRegexElement;
import insomnia.implem.data.regex.parser.IPRegexElement.Type;

/**
 * Build a tree from a {@link IPRegexElement}.
 * 
 * @author zuri
 * @param <VAL> type of a node value
 * @param <LBL> type of an edge label
 */
final class TreeFromPRegexElementBuilder<VAL, LBL>
{
	private Function<String, VAL> mapValue;
	private Function<String, LBL> mapLabel;
	ITreeBuilder<VAL, LBL>        treeBuilder;

	/**
	 * @param mapValue a function to map each {@link String} value from an {@link IPRegexElement} to a VAL
	 * @param mapLabel a function to map each {@link String} label from an {@link IPRegexElement} to a LBL
	 */
	public TreeFromPRegexElementBuilder(Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		this.mapLabel = mapLabel;
		this.mapValue = mapValue;
		treeBuilder   = new TreeBuilder<>();
	}

	/**
	 * Create a tree from an {@link IPRegexElement}
	 * 
	 * @param element the element representing a path
	 * @return the represented tree
	 * @throws IllegalArgumentException if element does not represent a {@link ITree}
	 */
	public ITree<VAL, LBL> create(IPRegexElement element)
	{
		long size = element.longSize();

		if (size > 1)
			throw new IllegalArgumentException(String.format("Can't handle a size of %s for %s", size, element));

		treeBuilder.setRooted(element.isRooted());
		doit(element, true, false);
		return Trees.create(treeBuilder);
	}

	private void doit(IPRegexElement element, boolean canBeTerminal, boolean mustBeTerminal)
	{
		mustBeTerminal |= element.isTerminal();

		switch (element.getType())
		{
		case EMPTY:
			empty(element, canBeTerminal, mustBeTerminal);
			break;
		case SEQUENCE:
			sequence(element, canBeTerminal, mustBeTerminal);
			break;
		case NODE:
			node(element, canBeTerminal, mustBeTerminal);
			break;
		case KEY:
			key(element, canBeTerminal, mustBeTerminal);
			break;
		case DISJUNCTION:
		{
			int nb = element.getElements().size();
			if (nb == 0)
				break;
			if (nb == 1)
				doit(element.getElements().get(0), canBeTerminal, mustBeTerminal);
			else
				throw new IllegalArgumentException(String.format("%s represents %s trees", element, element.size()));
			break;
		}
		default:
			throw new IllegalArgumentException(element + " does not represent a tree");
		}
	}

	private void empty(IPRegexElement empty, boolean canBeTerminal, boolean mustBeTerminal)
	{
		VAL value = mapValue.apply(empty.getValue());

		if (null != value)
			treeBuilder.setValue(value);

		if (empty.isRooted())
			treeBuilder.setRooted(true);
		if (canBeTerminal && mustBeTerminal)
			treeBuilder.setTerminal(true);
	}

	private void key(IPRegexElement key, boolean canBeTerminal, boolean mustBeTerminal)
	{
		int i = key.getQuantifier().getInf();

		if (i == 0)
			return;

		LBL label = mapLabel.apply(key.getLabel());
		VAL value = mapValue.apply(key.getValue());

		while (i-- != 0)
			treeBuilder.addChildDown(label, value);

		if (canBeTerminal && mustBeTerminal)
			treeBuilder.setTerminal(true);
	}

	private void node(IPRegexElement node, boolean canBeTerminal, boolean mustBeTerminal)
	{
		int i = node.getQuantifier().getInf();

		if (i == 0)
			return;

		int coords[] = treeBuilder.getCurrentCoordinates();

		while (i-- != 0)
			for (IPRegexElement e : node.getElements())
			{
				doit(e, canBeTerminal && e.getType() != Type.EMPTY, mustBeTerminal);
				treeBuilder.setCurrentCoordinates(coords);
			}
	}

	private void sequence(IPRegexElement sequence, boolean canBeTerminal, boolean mustBeTerminal)
	{
		int i = sequence.getQuantifier().getInf();

		if (i == 0)
			return;

		Iterator<IPRegexElement> it = sequence.getElements().iterator();

		if (!it.hasNext())
			return;

		while (i-- != 0)
		{
			IPRegexElement e = it.next();

			while (it.hasNext())
			{
				doit(e, false, false);
				e = it.next();
			}
			doit(e, canBeTerminal && i == 0, mustBeTerminal);
			it = sequence.getElements().iterator();
		}
	}
}
