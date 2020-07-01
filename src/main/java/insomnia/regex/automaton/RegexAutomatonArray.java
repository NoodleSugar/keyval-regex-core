package insomnia.regex.automaton;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import insomnia.regex.automaton.RegexAutomatonBuilder.EdgeData;

public class RegexAutomatonArray
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

	private RegexAutomatonBuilder builder;
	private StateBijection bijection;
	private HashMap<Integer, HashMap<Symbol, ArrayList<Integer>>> lines;

	public RegexAutomatonArray(RegexAutomatonBuilder builder)
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

		// On parcourt tous les états de la case
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
