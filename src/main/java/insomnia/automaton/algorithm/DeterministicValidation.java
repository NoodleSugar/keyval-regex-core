package insomnia.automaton.algorithm;

import java.util.List;

import insomnia.automaton.IGAutomaton;
import insomnia.automaton.state.IState;

public class DeterministicValidation<E> implements IValidation<E>
{
	@Override
	public boolean test(IGAutomaton<E> automaton, List<E> elements)
	{
		// Initialisation de l'automate à son état de départ
		IState<E> initialState = automaton.getInitialStates().get(0);
		automaton.goToState(initialState);

		if(automaton.isSynchronous())
		{
			for(E elt : elements)
			{
				// Calcul du prochain état
				List<IState<E>> nexts = automaton.nextStates(elt);
				// Si il n'y en a pas c'est que le mot n'est pas valide
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

				// Vérification des epsilon transitions
				List<IState<E>> nextEpsilons = automaton.nextEpsilonStates();
				if(!nextEpsilons.isEmpty())
				{
					automaton.goToState(nextEpsilons.get(0));
				}
				// Vérification des transition classiques
				else
				{
					// Calcul du prochain état
					List<IState<E>> nexts = automaton.nextStates(elt);
					// Si il n'y en a pas c'est que le mot n'est pas valide
					if(nexts.isEmpty())
						return false;

					// Déplacemenet de l'état courrant et incrémentation de l'indice de l'élément
					// courrant
					automaton.goToState(nexts.get(0));
					i++;
				}
			}
		}
		
		// Vérification de la finalité du dernier état parcourru
		List<IState<E>> finalStates = automaton.getFinalStates();
		return finalStates.contains(automaton.getCurrentState());
	}

}
