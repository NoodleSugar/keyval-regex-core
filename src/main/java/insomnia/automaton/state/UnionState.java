package insomnia.automaton.state;

import java.util.ArrayList;
import java.util.List;

public class UnionState extends AState implements IUnionState<String>
{
	private static final long serialVersionUID = 7429083129003010155L;
	ArrayList<IState<String>> waitings;

	public UnionState(int id)
	{
		this.id = id;
		isInitial = false;
		isFinal = false;
		waitings = new ArrayList<>();
	}

	public void addWaiting(IState<String> state)
	{
		waitings.add(state);
	}

	@Override
	public List<IState<String>> getWaitings()
	{
		return waitings;
	}

	@Override
	public String toString()
	{
		return "<" + id + ">";
	}
}
