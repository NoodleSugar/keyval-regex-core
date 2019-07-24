package insomnia.regex.automaton;

import java.security.InvalidParameterException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import insomnia.automaton.AutomatonException;
import insomnia.regex.automaton.RegexAutomaton.Builder.BuilderException;
import insomnia.regex.automaton.RegexAutomaton.Builder.EdgeData;
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
		RegexAutomaton.Builder builder = recursiveConstruct(elements);
		determinize(builder);
		return builder.build();
	}

	private static class StateBijection
	{
		private HashMap<Integer, ArrayList<Integer>> bijection;

		public StateBijection()
		{
			bijection = new HashMap<>();
		}

		public void add(int alias, ArrayList<Integer> states)
		{
			bijection.put(alias, states);
		}

		public int getAlias(ArrayList<Integer> states)
		{
			for(Map.Entry<Integer, ArrayList<Integer>> entry : bijection.entrySet())
			{
				ArrayList<Integer> currentStates = entry.getValue();
				if(states.size() == currentStates.size())
				{
					boolean equal = true;
					for(int state : states)
					{
						if(!currentStates.contains(state))
						{
							equal = false;
							break;
						}
					}
					if(equal)
						return entry.getKey();
				}
			}
			return -1;
		}

		@Override
		public String toString()
		{
			StringBuffer s = new StringBuffer();
			for(Map.Entry<Integer, ArrayList<Integer>> entry : bijection.entrySet())
			{
				s.append(entry.getKey()).append(" = {");
				for(int state : entry.getValue())
					s.append(" ").append(state);
				s.append("}\n");
			}
			return s.toString();
		}
	}

	private static class AutomatonArray
	{
		private class Symbol
		{
			public String str;
			public EdgeData.Type type;

			public Symbol(EdgeData.Type type, String str)
			{
				this.type = type;
				this.str = str;
			}

			@Override
			public boolean equals(Object o)
			{
				if(o instanceof Symbol)
				{
					Symbol e = (Symbol) o;
					if(e.type == type && e.str.equals(str))
						return true;
				}

				return false;
			}

			@Override
			public int hashCode()
			{
				return 7 * str.hashCode() + 13 * type.hashCode();
			}

			@Override
			public String toString()
			{
				if(type == EdgeData.Type.REGEX)
					return "~" + str + "~";
				return str;
			}
		};

		private RegexAutomaton.Builder builder;
		private StateBijection bijection;
		private HashMap<Integer, HashMap<Symbol, ArrayList<Integer>>> lines;

		public AutomatonArray(RegexAutomaton.Builder builder)
		{
			this.builder = builder;
			bijection = new StateBijection();
			lines = new HashMap<>();

			for(Map.Entry<Integer, ArrayList<EdgeData>> entry : builder.edges.entrySet())
			{
				int currentState = entry.getKey();
				ArrayList<EdgeData> edges = entry.getValue();

				HashMap<Symbol, ArrayList<Integer>> line = new HashMap<>();
				for(EdgeData edge : edges)
				{
					Symbol symbol = new Symbol(edge.type, edge.str);
					ArrayList<Integer> arrayCase = line.get(symbol);
					if(arrayCase == null)
						arrayCase = new ArrayList<Integer>();

					arrayCase.add(edge.endState);
					line.put(symbol, arrayCase);
				}
				lines.put(currentState, line);
			}
		}

		public void determinize()
		{
			ArrayDeque<Map.Entry<Integer, HashMap<Symbol, ArrayList<Integer>>>> queue = new ArrayDeque<>();
			for(Map.Entry<Integer, HashMap<Symbol, ArrayList<Integer>>> line : lines.entrySet())
				// On ajoute toutes les lignes du tableau à la queue
				queue.offer(line);

			// Tant que la queue n'est pas vide
			while(!queue.isEmpty())
			{
				Map.Entry<Integer, HashMap<Symbol, ArrayList<Integer>>> line = queue.poll();
				int currentState = line.getKey();

				// Pour chaque case de la ligne
				for(Map.Entry<Symbol, ArrayList<Integer>> arrayCase : line.getValue().entrySet())
				{
					Symbol symbol = arrayCase.getKey();
					ArrayList<Integer> nextStates = arrayCase.getValue();

					// Si cette case contient plusieurs états successeurs
					// on créé un nouvel état qui est la fusion de ces précédents
					if(nextStates.size() > 1)
					{
						// On vérifie si l'état fusion existe déja
						int newState = bijection.getAlias(nextStates);

						// Si il n'existe pas on le créé
						if(newState == -1)
						{
							newState = builder.addState();
							HashMap<Symbol, ArrayList<Integer>> newArrayCases = mergeLines(newState, nextStates);

							Map.Entry<Integer, HashMap<Symbol, ArrayList<Integer>>> newLine = new AbstractMap.SimpleEntry<Integer, HashMap<Symbol, ArrayList<Integer>>>(
									newState, newArrayCases);
							queue.offer(newLine);
						}

						// On supprime ensuite les arcs portant sur le même mot
						// puis on ajoute l'arc vers le nouvel état fusion
						ArrayList<EdgeData> edges = builder.edges.get(currentState);
						edges.removeIf(edge -> symbol.str.equals(edge.str) && symbol.type == edge.type);
						edges.add(new EdgeData(currentState, newState, symbol.str, symbol.type));
					}
				}
			}
		}

		private HashMap<Symbol, ArrayList<Integer>> mergeLines(int newState, ArrayList<Integer> caseStates)
		{
			// Les cases de la nouvelle ligne
			HashMap<Symbol, ArrayList<Integer>> newArrayCases = new HashMap<>();
			ArrayList<EdgeData> edges = new ArrayList<>();
			bijection.add(newState, caseStates);

			// On parcourt tous les �tats de la case
			for(int state : caseStates)
			{
				// On récupère la ligne correspondant à l'état actuel
				HashMap<Symbol, ArrayList<Integer>> line = lines.get(state);
				if(line == null)
					continue;

				// On parcourt les cases de la ligne actuelle
				for(Map.Entry<Symbol, ArrayList<Integer>> arrayCase : line.entrySet())
				{
					Symbol symbol = arrayCase.getKey();
					ArrayList<Integer> states = arrayCase.getValue();

					ArrayList<Integer> newStates = newArrayCases.get(symbol);
					if(newStates == null)
					{
						newStates = new ArrayList<Integer>();
						newArrayCases.put(symbol, newStates);
					}
					for(int s : states)
					{
						if(!newStates.contains(s))
						{
							edges.add(new EdgeData(newState, s, symbol.str, symbol.type));
							newStates.add(s);
						}
						if(builder.finalState.contains(s))
							builder.addFinalState(newState);
					}
				}
			}
			lines.put(newState, newArrayCases);
			builder.edges.put(newState, edges);
			return newArrayCases;
		}

		@Override
		public String toString()
		{
			StringBuffer s = new StringBuffer();
			for(Map.Entry<Integer, HashMap<Symbol, ArrayList<Integer>>> entry1 : lines.entrySet())
			{
				s.append(entry1.getKey()).append("|");
				for(Map.Entry<Symbol, ArrayList<Integer>> entry2 : entry1.getValue().entrySet())
				{
					s.append(entry2.getKey().str).append(" :");
					for(int state : entry2.getValue())
						s.append(" ").append(state);
					s.append("| ");
				}
				s.append("\n");
			}
			s.append(bijection);
			return s.toString();
		}
	}

	private static void determinize(RegexAutomaton.Builder builder)
	{
		// Remplacement des epsilons transitions
		cleanEpsilon(builder);

		// Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);

		// Gestion des collisions entre symbol et regex
		determinizeRegex(builder);

		// Déterminisation
		AutomatonArray array = new AutomatonArray(builder);
		array.determinize();

		// Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);
	}

	private static void determinizeRegex(RegexAutomaton.Builder builder)
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

	private static void cleanEpsilon(RegexAutomaton.Builder builder)
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

	private static void cleanInaccessible(RegexAutomaton.Builder builder)
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

	private static void getAccessibles(RegexAutomaton.Builder builder, List<Integer> accessibles, int state)
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

	private static RegexAutomaton.Builder recursiveConstruct(IElement element) throws BuilderException
	{
		RegexAutomaton.Builder builder = new RegexAutomaton.Builder();

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
				RegexAutomaton.Builder newBuilder = recursiveConstruct(e);
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
				RegexAutomaton.Builder newBuilder = recursiveConstruct(e);
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
			RegexAutomaton.Builder quantifiedBuilder = new RegexAutomaton.Builder();
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
