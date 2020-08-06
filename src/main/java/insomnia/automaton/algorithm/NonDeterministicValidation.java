package insomnia.automaton.algorithm;

import java.util.ArrayList;
import java.util.List;

import insomnia.automaton.IGAutomaton;
import insomnia.automaton.edge.EdgeEpsilon;
import insomnia.automaton.edge.IEdge;
import insomnia.automaton.state.IState;

public class NonDeterministicValidation<E> implements IValidation<E>
{
	@Override
	public boolean test(IGAutomaton<E> automaton, List<E> elements)
	{
		List<IState<E>> states = new ArrayList<>();
		List<IState<E>> nextStates = new ArrayList<>();

		nextStates.add(automaton.getInitialStates().get(0));
		epsilonClosure(nextStates);

		int index = 0;
		int n = elements.size();

		// Tant qu'il y aura des états à parcourir
		while(!nextStates.isEmpty())
		{
			E elt = elements.get(index);

			List<IState<E>> temp = states;
			states = nextStates;
			nextStates = temp;

			// Vérification des prochains états à parcourir
			for(IState<E> state : states)
			{
				automaton.goToState(state);
				nextStates.addAll(automaton.nextStates(elt));
			}
			epsilonClosure(nextStates);

			// Si l'entrée a été entièrement consommée
			if(++index == n)
			{
				// Si un des prochains états est final
				for(IState<E> s : nextStates)
				{
					if(s.isFinal())
						return true;
				}
				return false;
			}

			// Nettoyage des états parcourus
			states.clear();
		}
		return false;
	}

	private void epsilonClosure(List<IState<E>> states)
	{
		List<IState<E>> temp = new ArrayList<>(states);

		for(IState<E> state : temp)
			epsilonClosure(states, state);
	}

	private void epsilonClosure(List<IState<E>> states, IState<E> state)
	{
		for(IEdge<E> edge : state)
		{
			IState<E> s = edge.getChild();
			if(edge instanceof EdgeEpsilon && !states.contains(s))
			{
				states.add(s);
				epsilonClosure(states, s);
			}
		}
	}
}
