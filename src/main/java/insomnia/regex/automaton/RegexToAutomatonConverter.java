package insomnia.regex.automaton;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import insomnia.automaton.AutomatonException;
import insomnia.regex.automaton.RegexAutomatonBuilder.BuilderException;
import insomnia.regex.automaton.RegexAutomatonBuilder.EdgeData;
import insomnia.regex.element.IElement;
import insomnia.regex.element.Key;
import insomnia.regex.element.MultipleElement;
import insomnia.regex.element.OrElement;
import insomnia.regex.element.Quantifier;
import insomnia.regex.element.Regex;

public final class RegexToAutomatonConverter
{
	public static RegexAutomaton convert(IElement elements) throws AutomatonException, BuilderException
	{
		RegexAutomatonBuilder builder = recursiveConstruct(elements);
		determinize(builder);
		return builder.build();
	}

	private static void determinize(RegexAutomatonBuilder builder)
	{
		// On parcourt tous les états de l'automate
		for(int state : builder.states)
		{
			/*
			 * Gestion des epsilon transitions
			 */
			// On calcule la epsilon fermeture de l'état
			// puis on supprime ses epsilon transitions
			List<Integer> closure = new ArrayList<>();
			epsilonClosure(builder, state, closure);
			List<EdgeData> state_edges = builder.edges.get(state);
			state_edges.removeIf(e -> e.startState == state && e.type == EdgeData.Type.EPSILON);

			// Pour chaque état de la epsilon fermeture
			for(int s : closure)
			{
				// Pour chaque transition sortante de cet état
				for(EdgeData edge : builder.edges.get(s))
				{
					// Si ce n'est pas une epsilon transition
					if(edge.type != EdgeData.Type.EPSILON)
						// On ajoute la transition à l'état actuel
						state_edges.add(new EdgeData(state, edge.endState, edge.str, edge.type));
				}
			}

			/*
			 * Gestion collision entre label et label regex
			 */
			// Pour chaque transition sortante de l'état actuel
			for(EdgeData edge : state_edges)
			{
				// Si c'est une regex transition
				if(edge.type == EdgeData.Type.REGEX)
				{
					// Pour chaque autre transition non regex
					for(EdgeData e : state_edges)
					{
						// Si il y a collision
						if(e.type == EdgeData.Type.STRING_EQUALS && e.str.matches(edge.str))
						{
							// On ajoute une nouvelle transition (si elle n'existe pas déja)
							// du noeud de départ vers le noeuds d'arrivée de la regex transition
							// et ayant pour label celui de la collision
							EdgeData newEdge = new EdgeData(state, edge.endState, e.str, EdgeData.Type.STRING_EQUALS);
							if(!state_edges.contains(newEdge))
								state_edges.add(newEdge);
						}
					}
				}
			}
		}

		// Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);
	}

	// Calcule la epsilon fermeture de l'état state, privée de ce dernier
	private static void epsilonClosure(RegexAutomatonBuilder builder, int state, List<Integer> closure)
	{
		List<EdgeData> edges = builder.edges.get(state);
		for(EdgeData edge : edges)
		{
			// Si l'arc est une epsilon transition
			if(edge.type == EdgeData.Type.EPSILON)
			{
				// On ajoute l'état pointé par la transition dans la fermeture
				closure.add(edge.endState);
				epsilonClosure(builder, edge.endState, closure);
			}
		}
	}

	private static void cleanInaccessible(RegexAutomatonBuilder builder)
	{
		ArrayList<Integer> accessibleStates = new ArrayList<>();
		accessibleStates.add(builder.initialState);
		getAccessibles(builder, accessibleStates, builder.initialState);
		for(int state : builder.states)
		{
			if(!accessibleStates.contains(state))
				builder.edges.remove(state);
		}
		builder.states.removeIf(state -> !accessibleStates.contains(state));
	}

	private static void getAccessibles(RegexAutomatonBuilder builder, List<Integer> accessibles, int state)
	{
		List<EdgeData> edges = builder.edges.get(state);
		if(edges == null)
			return;
		for(EdgeData edge : edges)
		{
			int nextState = edge.endState;
			if(!accessibles.contains(nextState))
			{
				accessibles.add(nextState);
				getAccessibles(builder, accessibles, nextState);
			}
		}
	}

	private static RegexAutomatonBuilder recursiveConstruct(IElement element) throws BuilderException
	{
		RegexAutomatonBuilder builder = new RegexAutomatonBuilder();

		if(element instanceof Key)
		{
			Key key = (Key) element;
			builder.addFinalState(1);
			builder.addState(1);
			builder.addEdge(0, 1, key.getLabel(), EdgeData.Type.STRING_EQUALS);
		}
		else if(element instanceof Regex)
		{
			Regex regex = (Regex) element;
			builder.addFinalState(1);
			builder.addState(1);
			builder.addEdge(0, 1, regex.getRegex(), EdgeData.Type.REGEX);
		}
		else if(element instanceof OrElement)
		{
			OrElement oe = (OrElement) element;
			builder.addState(1);
			builder.addFinalState(1);
			for(IElement e : oe)
			{
				RegexAutomatonBuilder newBuilder = recursiveConstruct(e);
				int start = builder.addState();
				builder.addEdge(0, start, null, EdgeData.Type.EPSILON);
				builder.mergeBuilder(start, 1, newBuilder);
			}
		}
		else if(element instanceof MultipleElement)
		{
			MultipleElement me = (MultipleElement) element;
			int start;
			int end = 0;
			for(IElement e : me)
			{
				start = end;
				RegexAutomatonBuilder newBuilder = recursiveConstruct(e);
				end = builder.mergeBuilder(start, -1, newBuilder);
			}
			builder.addFinalState(end);
		}
		else
			throw new InvalidParameterException("Invalid type for parameter");

		Quantifier q = element.getQuantifier();
		int inf = q.getInf();
		int sup = q.getSup();

		if(inf != 1 || sup != 1)
		{
			RegexAutomatonBuilder quantifiedBuilder = new RegexAutomatonBuilder();
			int start = 0;
			for(int i = 0; i < inf; i++)
				start = quantifiedBuilder.mergeBuilder(start, -1, builder);

			if(sup == -1)
			{
				quantifiedBuilder.mergeBuilder(start, start, builder);
				int end = quantifiedBuilder.addState();
				quantifiedBuilder.addEdge(start, end, null, EdgeData.Type.EPSILON);
				start = end;
			}
			else
			{
				int end = quantifiedBuilder.addState();
				for(int i = 0; i < sup - inf - 1; i++)
				{
					quantifiedBuilder.addEdge(start, end, null, EdgeData.Type.EPSILON);
					start = quantifiedBuilder.mergeBuilder(start, -1, builder);
				}
				quantifiedBuilder.addEdge(start, end, null, EdgeData.Type.EPSILON);
				start = quantifiedBuilder.mergeBuilder(start, end, builder);
			}
			quantifiedBuilder.addFinalState(start);
			return quantifiedBuilder;
		}
		else
			return builder;
	}

}
