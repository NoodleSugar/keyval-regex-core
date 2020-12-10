package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

public abstract class Edge implements IEdge<String>
{
	protected IState<String> parent;
	protected IState<String> child;

	protected Edge(IState<String> parent, IState<String> child)
	{
		this.parent = parent;
		this.child = child;
	}

	@Override
	public IState<String> getParent()
	{
		return parent;
	}
	
	@Override
	public IState<String> getChild()
	{
		return child;
	}
}
