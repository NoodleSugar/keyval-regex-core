package insomnia.implem.rule;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.platform.commons.util.ToStringBuilder;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.implem.data.Trees;
import insomnia.implem.data.Variables;
import insomnia.implem.data.Variables.INamedVariable;
import insomnia.rule.IRule;

public final class Rules<VAL, LBL>
{
	private Rules()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class Rule<VAL, LBL> implements IRule<VAL, LBL>
	{
		private ITree<VAL, LBL>                       body, head;
		private Map<INode<VAL, LBL>, INode<VAL, LBL>> frontier;

		private Collection<INode<VAL, LBL>> existentialNodes;

		Rule(ITree<VAL, LBL> body, ITree<VAL, LBL> head, Map<INode<VAL, LBL>, INode<VAL, LBL>> frontier)
		{
			this.body        = body;
			this.head        = head;
			this.frontier    = MapUtils.unmodifiableMap(new HashMap<>(frontier));
			existentialNodes = Collections.unmodifiableCollection(CollectionUtils.select(head.getLeaves(), l -> !this.frontier.containsKey(l)));
		}

		@Override
		public ITree<VAL, LBL> getBody()
		{
			return body;
		}

		@Override
		public ITree<VAL, LBL> getHead()
		{
			return head;
		}

		@Override
		public boolean frontierIsTerminal()
		{
			return IRule.frontierIsTerminal(this);
		}

		@Override
		public boolean isExistential()
		{
			return !existentialNodes.isEmpty();
		}

		@Override
		public boolean isExistential(INode<VAL, LBL> node)
		{
			return existentialNodes.contains(node);
		}

		@Override
		public boolean isFrontier(INode<VAL, LBL> node)
		{
			return frontier.containsKey(node);
		}

		@Override
		public Collection<LBL> getVocabulary()
		{
			return new HashSet<>(FluentIterable.of(body.getVocabulary()).append(head.getVocabulary()).toList());
		}

		@Override
		public Map<INode<VAL, LBL>, INode<VAL, LBL>> getFontier()
		{
			return frontier;
		}

		@Override
		public Collection<INode<VAL, LBL>> getExistentialNodes()
		{
			return existentialNodes;
		}

		@Override
		public String toString()
		{
			return new ToStringBuilder(this) //
				.append("body", ITree.toString(body)) //
				.append("head", ITree.toString(head)) //
				.append("frontier", frontier) //
				.toString();
		}
	}
	// =========================================================================

	private static final String emptyName = "";

	private static Function<String, Object> mapVariable(Function<String, ?> mapValue)
	{
		return s -> {
			if (null == s || s.isEmpty() || s.charAt(0) != '?')
				return mapValue.apply(s);

			String[] split = s.substring(1).split(":", 2);
			Object   value;

			if (split.length == 2)
				value = mapValue.apply(split[1]);
			else
				value = mapValue.apply(null);

			return Variables.createNamed(split[0].trim(), value);
		};
	}

	public static IRule<String, String> fromString(String body, String head) throws ParseException
	{
		return fromString(body, head, Function.identity(), Function.identity());
	}

	@SuppressWarnings("unchecked")
	private static <VAL, LBL> ITree<VAL, LBL> processVariables(ITree<Object, LBL> tree, MultiValuedMap<String, INode<VAL, LBL>> variableMap_out)
	{
		for (var leaf : tree.getLeaves())
		{
			if (!(leaf.getValue() instanceof INamedVariable<?>))
				continue;

			INamedVariable<VAL> variable = (INamedVariable<VAL>) leaf.getValue();
			var                 varName  = variable.getName();
			leaf.setValue(variable.getValue());

			if (varName == emptyName)
				continue;

			variableMap_out.put(varName, (INode<VAL, LBL>) leaf);
		}
		return (ITree<VAL, LBL>) tree;
	}

	public static <VAL, LBL> IRule<VAL, LBL> fromString(String body, String head, Function<String, VAL> mapValue, Function<String, LBL> mapLabel) throws ParseException
	{
		MultiValuedMap<String, INode<VAL, LBL>> bodyRefLeaf = new ArrayListValuedHashMap<>();
		MultiValuedMap<String, INode<VAL, LBL>> headRefLeaf = new ArrayListValuedHashMap<>();
		ITree<VAL, LBL>                         tbody       = processVariables(Trees.treeFromString(body, mapVariable(mapValue), mapLabel), bodyRefLeaf);
		ITree<VAL, LBL>                         thead       = processVariables(Trees.treeFromString(head, mapVariable(mapValue), mapLabel), headRefLeaf);

		for (var k : bodyRefLeaf.keySet())
		{
			if (bodyRefLeaf.get(k).size() > 1)
				throw new IllegalArgumentException(String.format("The body variable %s cannot be repeated", k));
		}
		Map<INode<VAL, LBL>, INode<VAL, LBL>> frontier = new HashMap<>();

		for (var headEntry : headRefLeaf.entries())
		{
			if (!bodyRefLeaf.containsKey(headEntry.getKey()))
				throw new IllegalArgumentException(String.format("The head variable %s must appears as a leaf of the body", headEntry.getKey()));

			for (var bnode : bodyRefLeaf.get(headEntry.getKey()))
			{
				if (!INode.equals(bnode, headEntry.getValue()))
					throw new IllegalArgumentException(String.format("Each node with the variable %s must be equal, have: %s and %s", headEntry.getKey(), headEntry.getValue(), bnode));

				frontier.put(headEntry.getValue(), bnode);
			}
		}
		frontier.put(thead.getRoot(), tbody.getRoot());
		return new Rule<>(tbody, thead, frontier);
	}
}