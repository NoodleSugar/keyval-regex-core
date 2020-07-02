package insomnia.automaton.algorithm;

import java.util.List;

import insomnia.automaton.IAutomaton;

public class DeterministicValidation<E> implements IValidation<E>
{

	@Override
	public boolean check(IAutomaton<E> automaton, List<E> elements)
	{
		int initialState = automaton.getInitialStates().get(0);
		automaton.goToState(initialState);
		List<Integer> finalStates = automaton.getFinalStates();

		if(automaton.isSynchronous())
		{
			for(E elt : elements)
			{
				List<Integer> nexts = automaton.nextStates(elt);
				if(nexts.isEmpty())
					return false;
				automaton.goToState(nexts.get(0));
			}
		}
		else
		{
			for(int i = 0; i < elements.size(); i++)
			{
				E elt = elements.get(i);

				List<Integer> nextEpsilons = automaton.nextEpsilonStates();
				if(!nextEpsilons.isEmpty())
				{
					automaton.goToState(nextEpsilons.get(0));
					i--;
				}
				else
				{
					List<Integer> nexts = automaton.nextStates(elt);
					if(nexts.isEmpty())
						return false;

					automaton.goToState(nexts.get(0));
				}
			}
		}

		return finalStates.contains(automaton.getCurrentState());
	}

}
