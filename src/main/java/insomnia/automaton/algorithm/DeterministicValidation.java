package insomnia.automaton.algorithm;

import java.util.List;

import insomnia.automaton.IAutomaton;
import insomnia.automaton.state.IState;

public class DeterministicValidation<E> implements IValidation<E>
{

	@Override
	public boolean test(IAutomaton<E> automaton, List<E> elements)
	{
		IState<E> initialState = automaton.getInitialStates().get(0);
		automaton.goToState(initialState);
		List<IState<E>> finalStates = automaton.getFinalStates();

		if(automaton.isSynchronous())
		{
			for(E elt : elements)
			{
				List<IState<E>> nexts = automaton.nextStates(elt);
				if(nexts.isEmpty())
					return false;
				automaton.goToState(nexts.get(0));
			}
		}
		else
		{
			int n = elements.size();
			for(int i = 0; i < n;)
			{
				E elt = elements.get(i);

				List<IState<E>> nextEpsilons = automaton.nextEpsilonStates();
				if(!nextEpsilons.isEmpty())
				{
					automaton.goToState(nextEpsilons.get(0));
				}
				else
				{
					List<IState<E>> nexts = automaton.nextStates(elt);
					if(nexts.isEmpty())
						return false;

					automaton.goToState(nexts.get(0));
					i++;
				}
			}
		}

		return finalStates.contains(automaton.getCurrentState());
	}

}
