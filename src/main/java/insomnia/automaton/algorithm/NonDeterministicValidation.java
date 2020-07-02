package insomnia.automaton.algorithm;

import java.util.ArrayDeque;
import java.util.List;

import insomnia.automaton.IAutomaton;

public class NonDeterministicValidation<E> implements IValidation<E>
{
	private ArrayDeque<Integer> indexs;
	private ArrayDeque<Integer> states;

	@Override
	public boolean check(IAutomaton<E> automaton, List<E> elements)
	{
		indexs = new ArrayDeque<>();
		states = new ArrayDeque<>();

		List<Integer> finalStates = automaton.getFinalStates();

		indexs.add(0);
		states.add(automaton.getInitialStates().get(0));

		int n = elements.size();
		while(!states.isEmpty())
		{
			int state = states.poll();
			int index = indexs.poll();
			automaton.goToState(state);

			if(finalStates.contains(state))
				return true;

			List<Integer> nextEpsilons = automaton.nextEpsilonStates();
			states.addAll(nextEpsilons);
			for(int i = 0; i < nextEpsilons.size(); i++)
				indexs.add(index);

			if(index < n)
			{
				List<Integer> nexts = automaton.nextStates(elements.get(index));
				states.addAll(nexts);
				for(int i = 0; i < nexts.size(); i++)
					indexs.add(index + 1);
			}
		}
		return false;
	}
}
