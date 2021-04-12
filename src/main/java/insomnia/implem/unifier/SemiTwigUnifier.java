package insomnia.implem.unifier;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.builder.ToStringBuilder;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.implem.data.Trees;
import insomnia.rule.IRule;
import insomnia.unifier.ISemiTwigUnifier;

final class SemiTwigUnifier<VAL, LBL> implements ISemiTwigUnifier<VAL, LBL>
{
	private final IRule<VAL, LBL>                       rule;
	private final ITree<VAL, LBL>                       tree, treeSemiTwig, subHead;
	private final Map<INode<VAL, LBL>, INode<VAL, LBL>> bodyToHead;

	public SemiTwigUnifier(IRule<VAL, LBL> rule, ITree<VAL, LBL> tree, ITree<VAL, LBL> treeSemiTwig, Map<INode<VAL, LBL>, INode<VAL, LBL>> bodyToHead)
	{
		this.rule         = rule;
		this.tree         = tree;
		this.treeSemiTwig = treeSemiTwig;
		this.bodyToHead   = MapUtils.unmodifiableMap(new DualHashBidiMap<>(bodyToHead));

		subHead = Trees.subTree(rule.getHead(), rule.getHead().getRoot(), bodyToHead.values());
	}

	@Override
	public IRule<VAL, LBL> rule()
	{
		return rule;
	}

	@Override
	public ITree<VAL, LBL> subHead()
	{
		return subHead;
	}

	@Override
	public ITree<VAL, LBL> tree()
	{
		return tree;
	}

	@Override
	public ITree<VAL, LBL> treeSemiTwig()
	{
		return treeSemiTwig;
	}

	@Override
	public Map<INode<VAL, LBL>, INode<VAL, LBL>> treeToHead()
	{
		return bodyToHead;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this) //
			.append("rule", rule) //
			.append("tree", ITree.toString(tree)) //
			.append("subHead", ITree.toString(subHead)) //
			.append("semiTwig", ITree.toString(treeSemiTwig)) //
			.append("mapping", bodyToHead) //
			.toString();
	}
}
