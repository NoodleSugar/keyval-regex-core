package insomnia.automaton.algorithm;

import java.util.ArrayDeque;
import java.util.List;

import insomnia.automaton.IAutomaton;
import insomnia.automaton.state.IState;

public class NonDeterministicValidation<E> implements IValidation<E>
{
	private ArrayDeque<Integer> indexs;
	private ArrayDeque<IState<E>> states;

	@Override
	public boolean test(IAutomaton<E> automaton, List<E> elements)
	{
		indexs = new ArrayDeque<>();
		states = new ArrayDeque<>();

		List<IState<E>> finalStates = automaton.getFinalStates();

		indexs.add(0);
		states.add(automaton.getInitialStates().get(0));

		int n = elements.size();
		while(!states.isEmpty())
		{
			IState<E> state = states.poll();
			int index = indexs.poll();
			automaton.goToState(state);

			List<IState<E>> nextEpsilons = automaton.nextEpsilonStates();
			states.addAll(nextEpsilons);
			for(int i = 0; i < nextEpsilons.size(); i++)
				indexs.add(index);

			if(index < n)
			{
				List<IState<E>> nexts = automaton.nextStates(elements.get(index));
				states.addAll(nexts);
				for(int i = 0; i < nexts.size(); i++)
					indexs.add(index + 1);
			}
			else if(finalStates.contains(state))
				return true;
		}
		return false;
	}
}
