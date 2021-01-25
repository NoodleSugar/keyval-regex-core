package insomnia.fsa.fpa.creational;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.AbstractGFPA;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.implem.fsa.fpa.FPAProperties;

/**
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public abstract class AbstractGFPABuilder<VAL, LBL> extends AbstractGFPA<VAL, LBL> implements IGFPABuilder<VAL, LBL>
{
	protected Collection<IFSAState<VAL, LBL>> states, initialStates, finalStates, rootedStates, terminalStates;

	protected Collection<IFSAEdge<VAL, LBL>> edges;

	protected IFPAProperties properties;

	// ========================================================================

	public AbstractGFPABuilder()
	{
		super();
		clear();
	}
	// ========================================================================

	@Override
	public Collection<IFSAState<VAL, LBL>> getStates()
	{
		return states;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getInitialStates()
	{
		return initialStates;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getFinalStates()
	{
		return finalStates;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getRootedStates()
	{
		return rootedStates;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getTerminalStates()
	{
		return terminalStates;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges()
	{
		return edges;
	}

	@Override
	public IFPAProperties getProperties()
	{
		return properties;
	}

	// ========================================================================

	@Override
	public AbstractGFPABuilder<VAL, LBL> clear()
	{
		states     = initialStates = finalStates = rootedStates = terminalStates = Collections.emptyList();
		edges      = Collections.emptyList();
		properties = new FPAProperties(false, false);
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL> set(Collection<IFSAState<VAL, LBL>> states, Collection<IFSAState<VAL, LBL>> initialStates, Collection<IFSAState<VAL, LBL>> finalStates, Collection<IFSAEdge<VAL, LBL>> edges)
	{
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.edges         = edges;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL> setProperties(IFPAProperties properties)
	{
		this.properties = properties;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL> setRootedStates(Collection<IFSAState<VAL, LBL>> rootedStates)
	{
		this.rootedStates = rootedStates;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL> setTerminalStates(Collection<IFSAState<VAL, LBL>> terminalStates)
	{
		this.terminalStates = terminalStates;
		return this;
	}

	// ========================================================================

}
