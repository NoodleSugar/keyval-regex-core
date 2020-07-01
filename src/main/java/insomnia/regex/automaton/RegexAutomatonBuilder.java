package insomnia.regex.automaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import insomnia.automaton.AutomatonException;

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

	protected int initialState;
	protected int junctionState;
	protected ArrayList<Integer> finalState;
	protected TreeSet<Integer> states;
	protected HashMap<Integer, ArrayList<EdgeData>> edges;

	public RegexAutomatonBuilder()
	{
		initialState = 0;
		junctionState = -1;
		finalState = new ArrayList<Integer>();
		states = new TreeSet<Integer>();
		states.add(0);
		edges = new HashMap<Integer, ArrayList<EdgeData>>();
	}

	public void addFinalState(int end)
	{
		finalState.add(end);
		junctionState = end;
	}

	public void addState(int state) throws BuilderException
	{
		if (!states.add(state))
			throw new BuilderException("State " + state + " already in");
	}

	public int addState()
	{
		int state = states.last() + 1;
		states.add(state);
		return state;
	}

	public void addEdge(int startState, int endState, String str, EdgeData.Type type) throws BuilderException
	{
		EdgeData edge = new EdgeData(startState, endState, str, type);
		ArrayList<EdgeData> stateEdges;
		if (edges.containsKey(startState))
		{
			stateEdges = edges.get(startState);
			if (stateEdges.contains(edge))
				throw new BuilderException("Edge " + edge + " already in");
		} else
		{
			stateEdges = new ArrayList<EdgeData>();
			edges.put(startState, stateEdges);
		}
		stateEdges.add(edge);
	}

	public int mergeBuilder(int start, int end, RegexAutomatonBuilder builder) throws BuilderException
	{
		int last = states.last();

		// On supprime la jonction de fin du builder import�
		if (end != -1)
			builder.states.remove(builder.junctionState);

		// On ajout les �tats en r�indixant
		for (int state : builder.states)
			states.add(state + last);

		// On ajoute les arcs en r�indexant les �tats de d�but et fin
		int startState;
		int endState;
		for (Map.Entry<Integer, ArrayList<EdgeData>> entry : builder.edges.entrySet())
		{
			int edgeStartState = entry.getKey();
			for (EdgeData d : entry.getValue())
			{
				if (edgeStartState == builder.initialState)
					startState = start;
				else if (end != -1 && edgeStartState == builder.junctionState)
					startState = end;
				else
					startState = edgeStartState + last;

				if (d.endState == builder.initialState)
					endState = start;
				else if (end != -1 && d.endState == builder.junctionState)
					endState = end;
				else
					endState = d.endState + last;

				addEdge(startState, endState, d.str, d.type);
			}
		}

		// On remet la jonction de fin pr�c�dement supprim�e
		if (end != -1)
			builder.states.add(builder.junctionState);

		return end != -1 ? end : builder.junctionState + last;
	}

	public RegexAutomaton build() throws AutomatonException
	{
		return new RegexAutomaton(this);
	}

	public static class EdgeData
	{
		protected int startState;
		protected int endState;
		protected String str;
		protected Type type;

		public enum Type
		{
			EMPTY, STRING_EQUALS, REGEX;
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
			if (o == null)
				return false;
			if (o instanceof EdgeData)
			{
				EdgeData d = (EdgeData) o;
				if (startState == d.startState && endState == d.endState
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

	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer();

		s.append("Initial : ").append(initialState).append("\nJunction : ").append(junctionState).append("\nNodes : {");
		for (int state : states)
			s.append(state).append(" ");
		s.append("}\n");

		s.append("Edges :\n");
		for (Map.Entry<Integer, ArrayList<EdgeData>> entry : edges.entrySet())
		{
			for (EdgeData d : entry.getValue())
				s.append(d).append("\n");
		}

		return s.toString();
	}
}
