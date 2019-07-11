package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

public abstract class Edge implements IEdge<String>
{
	protected IState<String> nextState;

	protected Edge(IState<String> nextState)
	{
		this.nextState = nextState;
	}

	@Override
	public IState<String> getNextState()
	{
		return nextState;
	}
}
