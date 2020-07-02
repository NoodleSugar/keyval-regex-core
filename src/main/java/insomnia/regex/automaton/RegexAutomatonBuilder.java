package insomnia.regex.automaton;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import insomnia.automaton.AutomatonException;
import insomnia.regex.element.IElement;
import insomnia.regex.element.Key;
import insomnia.regex.element.MultipleElement;
import insomnia.regex.element.OrElement;
import insomnia.regex.element.Quantifier;
import insomnia.regex.element.Regex;

public class RegexAutomatonBuilder
{
	public class BuilderException extends Exception
	{
		private static final long serialVersionUID = 7602220617759700956L;

		public BuilderException(String message)
		{
			super(message);
		}

		public BuilderException(Throwable cause)
		{
			super(cause);
		}

		public BuilderException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	protected static class EdgeData
	{
		protected int startState;
		protected int endState;
		protected String str;
		protected Type type;

		public enum Type
		{
			EPSILON, STRING_EQUALS, REGEX;
		};

		public EdgeData(int start, int end, String str, Type type)
		{
			startState = start;
			endState = end;
			this.str = str;
			this.type = type;
		}

		@Override
		public boolean equals(Object o)
		{
			if(o == null)
				return false;
			if(o instanceof EdgeData)
			{
				EdgeData d = (EdgeData) o;
				if(startState == d.startState && endState == d.endState
						&& ((str == null && d.str == null) || (str != null && str.equals(d.str))))
					return true;
			}
			return false;
		}

		@Override
		public String toString()
		{
			return startState + ":" + endState + " " + str;
		}
	}

	protected int initialState;
	protected int junctionState;
	protected TreeSet<Integer> finalState;
	protected TreeSet<Integer> states;
	protected HashMap<Integer, ArrayList<EdgeData>> edges;

	public RegexAutomatonBuilder(IElement elements) throws BuilderException
	{
		this();
		recursiveConstruct(elements, this);
	}
	
	private RegexAutomatonBuilder()
	{
		initialState = 0;
		junctionState = -1;
		finalState = new TreeSet<Integer>();
		states = new TreeSet<Integer>();
		states.add(0);
		edges = new HashMap<Integer, ArrayList<EdgeData>>();
	}
	
	private void copy(RegexAutomatonBuilder builder)
	{
		initialState = builder.initialState;
		junctionState = builder.junctionState;
		finalState = builder.finalState;
		states = builder.states;
		edges = builder.edges;
	}

	private void addFinalState(int end)
	{
		finalState.add(end);
		junctionState = end;
	}

	private void addState(int state) throws BuilderException
	{
		if(!states.add(state))
			throw new BuilderException("State " + state + " already in");
	}

	private int addState()
	{
		int state = states.last() + 1;
		states.add(state);
		return state;
	}

	private void addEdge(int startState, int endState, String str, EdgeData.Type type) throws BuilderException
	{
		EdgeData edge = new EdgeData(startState, endState, str, type);
		ArrayList<EdgeData> stateEdges;
		if(edges.containsKey(startState))
		{
			stateEdges = edges.get(startState);
			if(stateEdges.contains(edge))
				throw new BuilderException("Edge " + edge + " already in");
		}
		else
		{
			stateEdges = new ArrayList<EdgeData>();
			edges.put(startState, stateEdges);
		}
		stateEdges.add(edge);
	}

	private int mergeBuilder(int start, int end, RegexAutomatonBuilder builder) throws BuilderException
	{
		int last = states.last();

		// On supprime la jonction de fin du builder import�
		if(end != -1)
			builder.states.remove(builder.junctionState);

		// On ajout les �tats en r�indixant
		for(int state : builder.states)
			states.add(state + last);

		// On ajoute les arcs en r�indexant les �tats de d�but et fin
		int startState;
		int endState;
		for(Map.Entry<Integer, ArrayList<EdgeData>> entry : builder.edges.entrySet())
		{
			int edgeStartState = entry.getKey();
			for(EdgeData d : entry.getValue())
			{
				if(edgeStartState == builder.initialState)
					startState = start;
				else if(end != -1 && edgeStartState == builder.junctionState)
					startState = end;
				else
					startState = edgeStartState + last;

				if(d.endState == builder.initialState)
					endState = start;
				else if(end != -1 && d.endState == builder.junctionState)
					endState = end;
				else
					endState = d.endState + last;

				addEdge(startState, endState, d.str, d.type);
			}
		}

		// On remet la jonction de fin pr�c�dement supprim�e
		if(end != -1)
			builder.states.add(builder.junctionState);

		return end != -1 ? end : builder.junctionState + last;
	}

	public RegexAutomatonBuilder determinize()
	{
		// On parcourt tous les états de l'automate possédant des transitions
		for(Map.Entry<Integer, ArrayList<EdgeData>> entry : edges.entrySet())
		{
			int state = entry.getKey();
			List<EdgeData> stateEdges = entry.getValue();
			/*
			 * Gestion des epsilon transitions
			 */
			// On calcule la epsilon fermeture de l'état
			// puis on supprime ses epsilon transitions
			List<Integer> closure = new ArrayList<>();
			epsilonClosure(state, closure);
			stateEdges.removeIf(e -> e.startState == state && e.type == EdgeData.Type.EPSILON);

			// Pour chaque état de la epsilon fermeture
			for(int s : closure)
			{
				// Si cet état est final
				if(finalState.contains(s))
					// L'état actuel devient final
					finalState.add(state);

				// Pour chaque transition sortante de cet état
				ArrayList<EdgeData> nextEdges = edges.get(s);
				if(nextEdges == null)
					continue;
				for(EdgeData edge : nextEdges)
				{
					// Si ce n'est pas une epsilon transition
					if(edge.type != EdgeData.Type.EPSILON)
						// On ajoute la transition à l'état actuel
						stateEdges.add(new EdgeData(state, edge.endState, edge.str, edge.type));
				}
			}

			/*
			 * Gestion collision entre label et label regex
			 */
			// Pour chaque transition sortante de l'état actuel
			for(EdgeData edge : stateEdges)
			{
				// Si c'est une regex transition
				if(edge.type == EdgeData.Type.REGEX)
				{
					// Pour chaque autre transition non regex
					for(EdgeData e : stateEdges)
					{
						// Si il y a collision
						if(e.type == EdgeData.Type.STRING_EQUALS && e.str.matches(edge.str))
						{
							// On ajoute une nouvelle transition (si elle n'existe pas déja)
							// du noeud de départ vers le noeuds d'arrivée de la regex transition
							// et ayant pour label celui de la collision
							EdgeData newEdge = new EdgeData(state, edge.endState, e.str, EdgeData.Type.STRING_EQUALS);
							if(!stateEdges.contains(newEdge))
								stateEdges.add(newEdge);
						}
					}
				}
			}
		}

		// Suppression des noeuds innaccessibles et de leurs arcs
		cleanInaccessible();

		return this;
	}

	// Calcule la epsilon fermeture de l'état state, privée de ce dernier
	private void epsilonClosure(int state, List<Integer> closure)
	{
		List<EdgeData> stateEdges = edges.get(state);
		if(stateEdges == null)
			return;
		for(EdgeData edge : stateEdges)
		{
			// Si l'arc est une epsilon transition
			if(edge.type == EdgeData.Type.EPSILON)
			{
				// On ajoute l'état pointé par la transition dans la fermeture
				closure.add(edge.endState);
				epsilonClosure(edge.endState, closure);
			}
		}
	}

	private void cleanInaccessible()
	{
		ArrayList<Integer> accessibleStates = new ArrayList<>();
		accessibleStates.add(initialState);
		getAccessibles(accessibleStates, initialState);
		for(int state : states)
		{
			if(!accessibleStates.contains(state))
				edges.remove(state);
		}
		states.removeIf(state -> !accessibleStates.contains(state));
	}

	private void getAccessibles(List<Integer> accessibles, int state)
	{
		List<EdgeData> stateEdges = edges.get(state);
		if(stateEdges == null)
			return;
		for(EdgeData edge : stateEdges)
		{
			int nextState = edge.endState;
			if(!accessibles.contains(nextState))
			{
				accessibles.add(nextState);
				getAccessibles(accessibles, nextState);
			}
		}
	}

	private void recursiveConstruct(IElement element, RegexAutomatonBuilder builder) throws BuilderException
	{
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
				RegexAutomatonBuilder newBuilder = new RegexAutomatonBuilder();
				recursiveConstruct(e, newBuilder);
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
				RegexAutomatonBuilder newBuilder = new RegexAutomatonBuilder();
				recursiveConstruct(e, newBuilder);
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
			builder.copy(quantifiedBuilder);
		}
	}

	public RegexAutomaton build() throws AutomatonException
	{
		return new RegexAutomaton(this);
	}

	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer();

		s.append("Initial : ").append(initialState).append("\nJunction : ").append(junctionState).append("\nNodes : {");
		for(int state : states)
			s.append(state).append(" ");
		s.append("}\n");

		s.append("Edges :\n");
		for(Map.Entry<Integer, ArrayList<EdgeData>> entry : edges.entrySet())
		{
			for(EdgeData d : entry.getValue())
				s.append(d).append("\n");
		}

		return s.toString();
	}
}
