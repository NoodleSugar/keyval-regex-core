package insomnia.implem.unifier;

import java.util.ArrayList;
import java.util.Collection;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.rule.IRule;
import insomnia.unifier.ISemiTwigUnifier;

public final class SemiTwigUnifiers
{
	private SemiTwigUnifiers()
	{
		throw new AssertionError();
	}

	public static <VAL, LBL> Collection<ISemiTwigUnifier<VAL, LBL>> compute(IRule<VAL, LBL> rulea, IRule<VAL, LBL> ruleb)
	{
		return compute(rulea, ruleb.getBody());
	}

	public static <VAL, LBL> Collection<ISemiTwigUnifier<VAL, LBL>> compute(IRule<VAL, LBL> rule, ITree<VAL, LBL> body)
	{
		var                                    ruleHead  = rule.getHead();
		Collection<ITreeMatchResult<VAL, LBL>> semiTwigs = ITree.getSemiTwigs(ruleHead, body);
		Collection<ISemiTwigUnifier<VAL, LBL>> ret       = new ArrayList<>();

		for (var semiTwig : semiTwigs)
		{
			var bodySemiTwig = semiTwig.group();
			var nodeMap      = semiTwig.nodeToOriginal();

			if (!INode.sameAs(nodeMap.get(bodySemiTwig.getRoot()), ruleHead.getRoot()))
				continue;

			for (var twigLeaf : semiTwig.group().getLeaves())
			{
				var headNode = nodeMap.get(twigLeaf);

				if (twigLeaf.isTerminal())
				{
					if (!rule.isFrontier(headNode))
						continue;
				}
				else if (ITree.isSeparator(body, bodySemiTwig, twigLeaf))
				{
					if (!rule.isFrontier(headNode) || headNode.isTerminal())
						continue;
				}
				ret.add(new SemiTwigUnifier<>(rule, body, bodySemiTwig, nodeMap));
			}
		}
		return ret;
	}
}
