package insomnia.implem.data;

import java.util.Iterator;
import java.util.function.Function;

import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.data.regex.parser.IRegexElement;

/**
 * Build a tree from a {@link IRegexElement}.
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
	 * @param mapValue a function to map each {@link String} value from an {@link IRegexElement} to a VAL
	 * @param mapLabel a function to map each {@link String} label from an {@link IRegexElement} to a LBL
	 */
	public TreeFromPRegexElementBuilder(Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		this.mapLabel = mapLabel;
		this.mapValue = mapValue;
		treeBuilder   = new TreeBuilder<>();
	}

	/**
	 * Create a tree from an {@link IRegexElement}
	 * 
	 * @param element the element representing a path
	 * @return the represented tree
	 * @throws IllegalArgumentException if element does not represent a {@link ITree}
	 */
	public ITree<VAL, LBL> create(IRegexElement element)
	{
		long size = element.longSize();

		if (size > 1)
			throw new IllegalArgumentException(String.format("Can't handle a size of %s for %s", size, element));

		treeBuilder.setRooted(element.isRooted());
		doit(element);
		return Trees.create(treeBuilder);
	}

	private void doit(IRegexElement element)
	{
		switch (element.getType())
		{
		case EMPTY:
			empty(element);
			break;
		case SEQUENCE:
			sequence(element);
			break;
		case NODE:
			node(element);
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
				doit(element.getElements().get(0));
			else
				throw new IllegalArgumentException(String.format("%s represents %s trees", element, element.size()));
			break;
		}
		default:
			throw new IllegalArgumentException(element + " does not represent a tree");
		}
	}

	private void empty(IRegexElement empty)
	{
		VAL value = mapValue.apply(empty.getValue());

		if (null != value)
			treeBuilder.setValue(value);

		if (empty.isRooted())
			treeBuilder.setRooted(true);
		if (empty.isTerminal())
			treeBuilder.setTerminal(true);
	}

	private void key(IRegexElement key)
	{
		int i = key.getQuantifier().getInf();

		if (i == 0)
			return;

		if (treeBuilder.getCurrentNode().isTerminal())
			treeBuilder.setTerminal(false);

		LBL label = mapLabel.apply(key.getLabel());
		VAL value = mapValue.apply(key.getValue());

		while (i-- != 0)
			treeBuilder.addChildDown(label, value);

		if (key.isTerminal())
			treeBuilder.setTerminal(true);
	}

	private void node(IRegexElement node)
	{
		int i = node.getQuantifier().getInf();

		if (i == 0)
			return;

		int coords[] = treeBuilder.getCurrentCoordinates();

		while (i-- != 0)
			for (IRegexElement e : node.getElements())
			{
				doit(e);
				treeBuilder.setCurrentCoordinates(coords);
			}
	}

	private void sequence(IRegexElement sequence)
	{
		int i = sequence.getQuantifier().getInf();

		if (i == 0)
			return;

		Iterator<IRegexElement> it = sequence.getElements().iterator();

		if (!it.hasNext())
			return;

		while (i-- != 0)
		{
			IRegexElement e = it.next();

			while (it.hasNext())
			{
				doit(e);
				e = it.next();
			}
			doit(e);
			it = sequence.getElements().iterator();
		}
	}
}
