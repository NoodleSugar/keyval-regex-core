package insomnia.regex.automaton;

import java.security.InvalidParameterException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
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
		// Remplacement des epsilons transitions
		cleanEpsilon(builder);

		// Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);

		// Gestion des collisions entre symbol et regex
		determinizeRegex(builder);

		// Déterminisation
		RegexAutomatonArray array = new RegexAutomatonArray(builder);
		array.determinize();

		// Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);
	}

	private static void determinizeRegex(RegexAutomatonBuilder builder)
	{

		for(Map.Entry<Integer, ArrayList<EdgeData>> entry : builder.edges.entrySet())
		{
			ArrayList<EdgeData> edges = entry.getValue();
			ArrayList<EdgeData> newEdges = new ArrayList<>();

			// On parcourt chaque arcs du noeuds courant
			for(EdgeData edge : edges)
			{
				// Si l'arc est de type regex
				if(edge.type == EdgeData.Type.REGEX)
				{
					// On regarde les arcs du noeud suivant
					ArrayList<EdgeData> nextEdges = builder.edges.get(edge.endState);
					if(nextEdges == null)
						continue;
					for(EdgeData nextEdge : nextEdges)
					{
						// Si l'arc courant du noeud suivant est de type string et est inclue dans regex
						if(nextEdge.type == EdgeData.Type.STRING_EQUALS && nextEdge.str.matches(edge.str))
							// On créé un nouvel arc du même type avec la même string du noeud de départ au
							// noeud de fin de l'arc regex
							newEdges.add(new EdgeData(edge.startState, edge.endState, nextEdge.str, nextEdge.type));
					}
				}
			}
			// On ajoute tous les nouveaux arcs à l'ensemble des arcs du builder
			edges.addAll(newEdges);
		}
	}

	private static void cleanEpsilon(RegexAutomatonBuilder builder)
	{
		ArrayDeque<EdgeData> epsilonEdges = new ArrayDeque<>();
		for(Map.Entry<Integer, ArrayList<EdgeData>> entry : builder.edges.entrySet())
		{
			for(EdgeData edge : entry.getValue())
			{
				if(edge.str == null)
					epsilonEdges.offer(edge);
			}
		}

		while(!epsilonEdges.isEmpty())
		{
			EdgeData epsilonEdge = epsilonEdges.poll();
			ArrayList<EdgeData> startStateEdges = builder.edges.get(epsilonEdge.startState);
			startStateEdges.remove(epsilonEdge);
			if(epsilonEdge.startState != epsilonEdge.endState)
			{
				ArrayList<EdgeData> endStateEdges = builder.edges.get(epsilonEdge.endState);
				// Héritage des arcs du noeud suivant la epsilon transition
				if(endStateEdges != null)
				{
					for(EdgeData edge : endStateEdges)
					{
						EdgeData newEdge = new EdgeData(epsilonEdge.startState, edge.endState, edge.str, edge.type);
						if(!startStateEdges.contains(newEdge))
							startStateEdges.add(newEdge);
						if(newEdge.str == null)
							epsilonEdges.offer(newEdge);
					}
				}
				// H�ritage du status final du noeud suivant la epsilon transition
				if(builder.finalState.contains(epsilonEdge.endState))
					builder.finalState.add(epsilonEdge.startState);
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
				builder.addEdge(0, start, null, EdgeData.Type.EMPTY);
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
				quantifiedBuilder.addEdge(start, end, null, EdgeData.Type.EMPTY);
				start = end;
			}
			else
			{
				int end = quantifiedBuilder.addState();
				for(int i = 0; i < sup - inf - 1; i++)
				{
					quantifiedBuilder.addEdge(start, end, null, EdgeData.Type.EMPTY);
					start = quantifiedBuilder.mergeBuilder(start, -1, builder);
				}
				quantifiedBuilder.addEdge(start, end, null, EdgeData.Type.EMPTY);
				start = quantifiedBuilder.mergeBuilder(start, end, builder);
			}
			quantifiedBuilder.addFinalState(start);
			return quantifiedBuilder;
		}
		else
			return builder;
	}

}
