package insomnia.implem.fsa.fta.edgeCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdgeCondition;
import insomnia.lib.numeric.Base;

public class FTAEdgeConditions
{

	private static class FTAEdgeCondition<VAL, LBL> implements IFTAEdgeCondition<VAL, LBL>
	{
		private List<IFSAState<VAL, LBL>> parentStates;

		public FTAEdgeCondition(List<IFSAState<VAL, LBL>> states)
		{
			this.parentStates = states;
		}

		@Override
		public boolean test(List<IFSAState<VAL, LBL>> states)
		{
			if (states.size() < parentStates.size())
				return false;

			return test_solve(states);
		}

		/**
		 * Solve the problem as a naive CSP
		 */
		private boolean test_solve(List<IFSAState<VAL, LBL>> states)
		{
			List<IFSAState<VAL, LBL>> variables = parentStates;
			Bag<IFSAState<VAL, LBL>>  values    = new HashBag<>(states);

			for (IFSAState<VAL, LBL> var : variables)
				values.remove(var, 1);

			return values.isEmpty();
		}

		@Override
		public boolean testND(List<Collection<IFSAState<VAL, LBL>>> multiStates)
		{
			// Remove empty subStates
			multiStates = multiStates.stream().filter(c -> !c.isEmpty()).collect(Collectors.toList());

			if (multiStates.size() < parentStates.size())
				return false;

			// TODO: specific algorithm for not testing all the cases
			int                             nbMultiStates = multiStates.size();
			List<List<IFSAState<VAL, LBL>>> lmultiStates  = new ArrayList<>(nbMultiStates);
			Base                            base;

			// Init base & lmultiStates
			{
				int ibase[] = new int[nbMultiStates];
				int i       = 0;

				for (Collection<IFSAState<VAL, LBL>> states : multiStates)
				{
					ibase[i++] = states.size();
					lmultiStates.add(new ArrayList<>(states));
				}

				base = new Base(ibase);
			}
			List<IFSAState<VAL, LBL>> states = new ArrayList<>(nbMultiStates);
			int                       num[]  = new int[nbMultiStates];

			// Test all combination of states
			for (int i = 0, c = base.max(); i < c; i++)
			{
				for (int j = 0; j < nbMultiStates; j++)
					states.add(lmultiStates.get(j).get(num[j]));

				if (test_solve(states))
					return true;

				states.clear();
				base.increment(num);
			}
			return false;
		}

		@Override
		public String toString()
		{
			return "∀";
		}
	}

	/**
	 * Create a state that must contain at least the 'parents' states.
	 */
	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> createInclusive(List<IFSAState<VAL, LBL>> states)
	{
		return new FTAEdgeCondition<>(states);
	}
}
