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
		HashMap<Integer, ArrayList<Integer>> bijection;
		
		StateBijection()
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
		RegexAutomaton.Builder builder;
		StateBijection bijection;
		HashMap<Integer, HashMap<String, ArrayList<Integer>>> lines;
		
		AutomatonArray(RegexAutomaton.Builder builder)
		{
			this.builder = builder;
			bijection = new StateBijection();
			lines = new HashMap<>();
			
			for(Map.Entry<Integer, ArrayList<EdgeData>> entry : builder.edges.entrySet())
			{
				int currentState = entry.getKey();
				ArrayList<EdgeData> edges = entry.getValue();
				
				HashMap<String, ArrayList<Integer>> line = new HashMap<>();
				for(EdgeData edge : edges)
				{
					String word = edge.str;
					ArrayList<Integer> arrayCase = line.get(word);
					if(arrayCase == null)
						arrayCase = new ArrayList<Integer>();
					
					arrayCase.add(edge.endState);
					line.put(word, arrayCase);
				}
				lines.put(currentState, line);
			}
		}
		
		void determinize()
		{
			//On ajoute toutes les lignes du tableau à la queue
			ArrayDeque<Map.Entry<Integer, HashMap<String, ArrayList<Integer>>>> queue = new ArrayDeque<>();
			for(Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> line : lines.entrySet())
				queue.offer(line);
			
			//Tant que la queue n'est pas vide
			while(!queue.isEmpty())
			{
				Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> line = queue.poll();
				int currentState = line.getKey();
				
				//Pour chaque case de la ligne
				for(Map.Entry<String, ArrayList<Integer>> arrayCase : line.getValue().entrySet())
				{
					String word = arrayCase.getKey();
					ArrayList<Integer> nextStates = arrayCase.getValue();
					
					//Si cette case contient plusieurs états successeurs
					//on créé un nouvel état qui est la fusion de ces précédents
					if(nextStates.size() > 1)
					{
						//On vérifie si l'état fusion existe déja
						int newState = bijection.getAlias(nextStates);
						
						//Si il n'existe pas on le créé
						if(newState == -1)
						{
							newState = builder.addState();
							HashMap<String, ArrayList<Integer>> newArrayCases = mergeLines(newState, nextStates);
							
							Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> newLine =
									new AbstractMap.SimpleEntry<Integer, HashMap<String, ArrayList<Integer>>>(newState, newArrayCases);
							queue.offer(newLine);
						}
						
						//On supprime ensuite les arcs portant sur le même mot
						//puis on ajoute l'arc vers le nouvel état fusion
						ArrayList<EdgeData> edges = builder.edges.get(currentState);
						edges.removeIf(edge -> word.equals(edge.str));
						edges.add(new EdgeData(currentState, newState, word, EdgeData.Type.STRING_EQUALS));
					}
				}
			}
		}
		
		HashMap<String, ArrayList<Integer>> mergeLines(int newState, ArrayList<Integer> caseStates)
		{
			//Les cases de la nouvelle ligne
			HashMap<String, ArrayList<Integer>> newArrayCases = new HashMap<>();
			ArrayList<EdgeData> edges = new ArrayList<>();
			bijection.add(newState, caseStates);
			
			//On parcourt tous les états de la case
			for(int state : caseStates)
			{
				//On récupère la ligne correspondant à l'état actuel
				HashMap<String, ArrayList<Integer>> line = lines.get(state);
				if(line == null)
					continue;
				
				//On parcourt les cases de la ligne actuelle
				for(Map.Entry<String, ArrayList<Integer>> arrayCase : line.entrySet())
				{
					String word = arrayCase.getKey();
					ArrayList<Integer> states = arrayCase.getValue();
					
					ArrayList<Integer> newStates = newArrayCases.get(word);
					if(newStates == null)
					{
						newStates = new ArrayList<Integer>();
						newArrayCases.put(word, newStates);
					}
					for(int s : states)
					{
						if(!newStates.contains(s))
						{
							edges.add(new EdgeData(newState, s, word, EdgeData.Type.STRING_EQUALS));
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
			for(Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> entry1 : lines.entrySet())
			{
				s.append(entry1.getKey()).append("|");
				for(Map.Entry<String, ArrayList<Integer>> entry2: entry1.getValue().entrySet())
				{
					s.append(entry2.getKey()).append(" :");
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
		//Remplacement des epsilons transitions
		cleanEpsilon(builder);
		
		//Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);
		
		//Déterminisation
		AutomatonArray array = new AutomatonArray(builder);
		array.determinize();
		
		//Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible(builder);
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
				//Héritage des arcs du noeud suivant la epsilon transition
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
				//Héritage du status final du noeud suivant la epsilon transition
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
