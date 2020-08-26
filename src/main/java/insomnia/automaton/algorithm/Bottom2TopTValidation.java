package insomnia.automaton.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import insomnia.automaton.ITAutomaton;
import insomnia.automaton.edge.IEdge;
import insomnia.automaton.state.IState;
import insomnia.automaton.state.IUnionState;
import insomnia.automaton.state.IValueState;
import insomnia.rule.tree.ITree;
import insomnia.rule.tree.node.ITreeNode;

public class Bottom2TopTValidation<E> implements ITValidation<E>
{
	private class NodeMatch
	{
		public ITreeNode<E> treeNode;
		public IState<E> automatonNode;

		public NodeMatch(ITreeNode<E> n, IState<E> s)
		{
			treeNode = n;
			automatonNode = s;
		}
	}

	@Override
	public boolean test(ITAutomaton<E> automaton, ITree<E> tree)
	{
		List<? extends ITreeNode<E>> leaves = tree.getLeaves();
		if(automaton.getLeaves().size() < leaves.size())
			return false;
		return matchLeaves(automaton, tree, leaves, new ArrayList<NodeMatch>(), 0);
	}

	private boolean matchLeaves(ITAutomaton<E> automaton, ITree<E> tree, //
			List<? extends ITreeNode<E>> leaves, List<NodeMatch> leafMatchs, int index)
	{
		List<IValueState<E>> initStates = automaton.getLeaves();
		IValueState<E> state = initStates.get(index);

		// Tant qu'il y a des feuilles de l'automate qui ne sont pas assignées
		// à une feuille de l'arbre
		if(index != initStates.size())
		{
			// Test avec toutes les combinaisons de feuilles possibles
			List<? extends ITreeNode<E>> newLeaves = new ArrayList<>(leaves);
			for(ITreeNode<E> leaf : leaves)
			{
				// Si les valeurs de la feuille de l'arbre et celle de l'automate ne
				// correspondent pas
				if(!leaf.getValue().getValue().equals(state.getValue()))
					continue;

				NodeMatch nm = new NodeMatch(leaf, state);
				leafMatchs.add(nm);

				// Si il reste plus de feuilles à assigner sur
				// l'automate que sur l'arbre
				if(initStates.size() - index < leaves.size())
					newLeaves = leaves;
				else
				{
					newLeaves = new ArrayList<>(leaves);
					newLeaves.remove(leaf);
				}

				if(matchLeaves(automaton, tree, newLeaves, leafMatchs, index + 1))
					return true;

				leafMatchs.remove(nm);
			}
		}
		else
		{
			Map<Integer, List<NodeMatch>> nodeMatchs = new HashMap<>();
			nodeMatchs.put(0, leafMatchs);
			return matchNodes(automaton, tree, nodeMatchs, 0);
		}

		return false;
	}

	private boolean matchNodes(ITAutomaton<E> automaton, ITree<E> tree, //
			Map<Integer, List<NodeMatch>> nodeMatchs, int level)
	{
		List<NodeMatch> matchs = nodeMatchs.get(level);
		List<NodeMatch> usedMatchs = new ArrayList<>();
		List<NodeMatch> unionMatchs = new ArrayList<>();
		List<IUnionState<E>> unions = new ArrayList<>();

		// Union States
		for(NodeMatch nm : matchs)
		{
			IUnionState<E> union = (IUnionState<E>) nm.automatonNode;
			if(!unions.contains(union))
			{
				NodeMatch unionMatch = new NodeMatch(null, union);
				unions.add(union);
				if(matchWaitNode(union, matchs, usedMatchs, unionMatch))
					unionMatchs.add(unionMatch);
			}
		}

		// Basic States
		List<NodeMatch> nextMatchs = new ArrayList<>();
		nodeMatchs.put(level + 1, nextMatchs);

		for(NodeMatch nm : matchs)
		{
			if(!usedMatchs.contains(nm))
				nextMatchs.add(nm);
		}
		
		// TODO

		return false;
	}

	private boolean matchWaitNode(IUnionState<E> waiter, List<NodeMatch> matchs, //
			List<NodeMatch> usedMatchs, NodeMatch unionMatch)
	{
		// TODO
		return false;
	}

}
