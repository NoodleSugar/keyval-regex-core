package insomnia.regex.automaton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import insomnia.automaton.AutomatonException;
import insomnia.automaton.IAutomaton;
import insomnia.automaton.edge.EdgeEmpty;
import insomnia.automaton.edge.EdgeRegex;
import insomnia.automaton.edge.EdgeStringEqual;
import insomnia.automaton.edge.IEdge;
import insomnia.automaton.state.IState;
import insomnia.automaton.state.State;
import insomnia.regex.automaton.RegexAutomatonBuilder.EdgeData;
import insomnia.summary.ISummary;

public final class RegexAutomaton implements IAutomaton<String>
{
	private IState<String> initialState;
	private IState<String> currentState;
	private ArrayList<IState<String>> states;

	protected RegexAutomaton(RegexAutomatonBuilder b) throws AutomatonException
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
			State startState = (State) getState(startId);
			ArrayList<EdgeRegex> regexEdges = new ArrayList<>();
			for(EdgeData d : entry.getValue())
			{
				State endState = (State) getState(d.endState);
				if(d.type == EdgeData.Type.STRING_EQUALS)
					startState.add(new EdgeStringEqual(endState, d.str));
				else if(d.type == EdgeData.Type.EMPTY)
					startState.add(new EdgeEmpty(endState));
				else if(d.type == EdgeData.Type.REGEX)
					regexEdges.add(new EdgeRegex(endState, d.str));
				else
					throw new AutomatonException("Invalid edge type : " + d.type);
			}
			startState.addAll(regexEdges);
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

	private boolean stepForward(String word) throws AutomatonException
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
	
	@Override
	public boolean run(String path) throws AutomatonException
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
