package insomnia.regex.automaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import insomnia.automaton.AutomatonException;
import insomnia.automaton.IAutomaton;
import insomnia.automaton.edge.EdgeEmpty;
import insomnia.automaton.edge.EdgeRegex;
import insomnia.automaton.edge.EdgeStringEqual;
import insomnia.automaton.edge.IEdge;
import insomnia.automaton.state.IState;
import insomnia.automaton.state.State;
import insomnia.regex.automaton.RegexAutomaton.Builder.EdgeData;
import insomnia.summary.ISummary;

public final class RegexAutomaton implements IAutomaton<String>
{
	public final static class Builder
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

		int initialState;
		int junctionState;
		ArrayList<Integer> finalState;
		TreeSet<Integer> states;
		HashMap<Integer, ArrayList<EdgeData>> edges;

		public Builder()
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
			if(!states.add(state))
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

		public int mergeBuilder(int start, int end, Builder builder) throws BuilderException
		{
			int last = states.last();

			// On supprime la jonction de fin du builder importé
			if(end != -1)
				builder.states.remove(builder.junctionState);

			// On ajout les états en réindixant
			for(int state : builder.states)
				states.add(state + last);

			// On ajoute les arcs en réindexant les états de début et fin
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

			// On remet la jonction de fin précédement supprimée
			if(end != -1)
				builder.states.add(builder.junctionState);

			return end != -1 ? end : builder.junctionState + last;
		}

		public RegexAutomaton build() throws AutomatonException
		{
			return new RegexAutomaton(this);
		}

		static class EdgeData
		{
			int startState;
			int endState;
			String str;
			Type type;

			enum Type
			{
				EMPTY, STRING_EQUALS, REGEX;
			};

			EdgeData(int start, int end, String str, Type type)
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

		@Override
		public String toString()
		{
			StringBuffer s = new StringBuffer();

			s.append("Initial : ").append(initialState).append("\nJunction : ").append(junctionState)
					.append("\nNodes : {");
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

	IState<String> initialState;
	IState<String> currentState;
	private ArrayList<IState<String>> states;

	private RegexAutomaton(Builder b) throws AutomatonException
	{
		states = new ArrayList<>();
		currentState = null;

		for(int id : b.states)
		{
			State state = new State();
			state.setId(id);
			if(b.initialState == id)
			{
				initialState = state;
				state.setInitial(true);
			}
			if(b.finalState.contains(id))
				state.setFinal(true);
			states.add(state);
		}
		for(Map.Entry<Integer, ArrayList<EdgeData>> entry : b.edges.entrySet())
		{
			int startId = entry.getKey();
			for(EdgeData d : entry.getValue())
			{
				State startState = (State) getState(startId);
				State endState = (State) getState(d.endState);
				IEdge<String> edge;
				if(d.type == EdgeData.Type.STRING_EQUALS)
					edge = new EdgeStringEqual(endState, d.str);
				else if(d.type == EdgeData.Type.REGEX)
					edge = new EdgeRegex(endState, d.str);
				else if(d.type == EdgeData.Type.EMPTY)
					edge = new EdgeEmpty(endState);
				else
					throw new AutomatonException("Invalid edge type : " + d.type);

				startState.add(edge);
			}
		}
	}

	private IState<String> getState(int id) throws AutomatonException
	{
		for(IState<String> state : states)
		{
			if(state.getId() == id)
				return state;
		}
		throw new AutomatonException("State '" + id + "' not found");
	}

	@Override
	public boolean stepForward(String word) throws AutomatonException
	{
		if(currentState == null)
			throw new AutomatonException("Automaton has no current state");

		IState<String> tempState = currentState;
		currentState = null;
		for(IEdge<String> edge : tempState)
		{
			if(edge.isValid(word))
			{
				currentState = edge.getNextState();
				return true;
			}
		}
		return false;
	}

	/**
	 * The automate need to have a finite number of paths (=no loop) And it must
	 * only contain EdgeStringEqual (i.e : no regex)
	 * 
	 * @throws AutomatonException
	 */
	public ArrayList<String> generatePaths() throws AutomatonException
	{
		ArrayList<String> paths = new ArrayList<>();
		generatePaths(initialState, new Path(), paths);
		return paths;
	}

	private void generatePaths(IState<String> state, Path path, ArrayList<String> paths) throws AutomatonException
	{
		for(IEdge<String> edge : state)
		{
			String word = ((EdgeStringEqual) edge).getWord();

			currentState = state;
			if(!stepForward(word))
				continue;

			path.push(word);
			if(currentState.isFinal())
				paths.add(path.getPath());
			generatePaths(currentState, path, paths);
			path.pop();
		}
	}

	/**
	 * jsonData obtained with JsonParser
	 * 
	 * @throws AutomatonException
	 */
	public boolean existPath(ISummary summary) throws AutomatonException
	{
		return getPathsFromSummary(summary).size() != 0;
	}

	public boolean isValidPath(String path) throws AutomatonException
	{
		String[] array = path.split("\\.");

		currentState = initialState;
		for(String word : array)
		{
			if(!stepForward(word))
				return false;
		}

		return true;
	}

	/**
	 * automatonSummary must only contain EdgeStringEqual (i.e : no regex)
	 */
	public ArrayList<String> getPathsFromAutomaton(RegexAutomaton automatonSummary) throws AutomatonException
	{
		ArrayList<String> queries = new ArrayList<>();
		automatonSummary.currentState = automatonSummary.initialState;
		getPathsFromAutomaton(automatonSummary, initialState, new Path(), queries);
		return queries;
	}

	private void getPathsFromAutomaton(RegexAutomaton automaton, IState<String> state, Path path,
			ArrayList<String> queries) throws AutomatonException
	{
		IState<String> currentRegexState = automaton.currentState;
		for(IEdge<String> edge : currentRegexState)
		{
			String word = ((EdgeStringEqual) edge).getWord();

			currentState = state;
			if(!stepForward(word))
				continue;
			automaton.currentState = edge.getNextState();

			path.push(word);
			if(currentState.isFinal())
				queries.add(path.getPath());
			getPathsFromAutomaton(automaton, currentState, path, queries);
			path.pop();
		}
	}

	public ArrayList<String> getPathsFromSummary(ISummary summary) throws AutomatonException
	{
		ArrayList<String> paths = new ArrayList<>();
		getPathsFromSummary(summary.getData(), initialState, new Path(), paths);
		return paths;
	}

	@SuppressWarnings("unchecked")
	private void getPathsFromSummary(Object data, IState<String> state, Path path, ArrayList<String> paths)
			throws AutomatonException
	{
		if(data instanceof Map)
		{
			Map<String, Object> map = (Map<String, Object>) data;
			for(Map.Entry<String, Object> entry : map.entrySet())
			{
				String word = entry.getKey();
				currentState = state;
				if(!stepForward(word))
					continue;

				path.push(word);
				if(currentState.isFinal())
				{
					String p = path.getPath();
					if(!paths.contains(p))
						paths.add(p);
				}
				getPathsFromSummary(entry.getValue(), currentState, path, paths);
				path.pop();
			}
		}
		else if(data instanceof List)
		{
			List<Object> array = (List<Object>) data;
			for(Object object : array)
				getPathsFromSummary(object, state, path, paths);
		}
	}

	@Override
	public String toString()
	{
		StringBuffer s1 = new StringBuffer();
		StringBuffer s2 = new StringBuffer();

		s1.append("Nodes : {");
		s2.append("Edges :\n");
		for(IState<String> state : states)
		{
			s1.append(state + " ");
			for(IEdge<String> edge : state)
				s2.append(state.toString() + edge.toString() + "\n");
		}
		s1.append("}\n");
		s1.append(s2);

		return s1.toString();
	}
}
