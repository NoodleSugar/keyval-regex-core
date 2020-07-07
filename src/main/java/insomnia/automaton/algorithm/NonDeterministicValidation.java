package insomnia.automaton.algorithm;

import java.util.ArrayDeque;
import java.util.List;

import insomnia.automaton.IAutomaton;
import insomnia.automaton.state.IState;

public class NonDeterministicValidation<E> implements IValidation<E>
{
	@Override
	public boolean test(IAutomaton<E> automaton, List<E> elements)
	{
		ArrayDeque<Integer> indexs = new ArrayDeque<>();
		ArrayDeque<IState<E>> states = new ArrayDeque<>();

		List<IState<E>> finalStates = automaton.getFinalStates();

		indexs.add(0);
		states.add(automaton.getInitialStates().get(0));

		int n = elements.size();
		// Tant qu'il y a des états à parcourir
		while(!states.isEmpty())
		{
			// L'automate est mis dans l'état pioché
			IState<E> state = states.poll();
			int index = indexs.poll();
			automaton.goToState(state);

			// Vérification des epsilons transitions
			List<IState<E>> nextEpsilons = automaton.nextEpsilonStates();
			states.addAll(nextEpsilons);
			for(int i = 0; i < nextEpsilons.size(); i++)
				indexs.add(index);

			// Si il y a encore des éléments du mot à parcourir
			if(index < n)
			{
				// Vérification des transitions classiques
				List<IState<E>> nexts = automaton.nextStates(elements.get(index));
				states.addAll(nexts);
				for(int i = 0; i < nexts.size(); i++)
					indexs.add(index + 1);
			}
			// Sinon si l'état est final alors le mot est valide
			else if(finalStates.contains(state))
				return true;
		}
		return false;
	}
}
