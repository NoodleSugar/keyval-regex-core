package insomnia.fsa.fpa.factory;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fpa.algorithm.GFPAValidation;
import insomnia.fsa.fpa.algorithm.IGFPAValidation;
import insomnia.implem.fsa.fpa.FPAProperties;

/**
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 * @param <RET> The constructed type
 */
public abstract class AbstractGFPABuilder<VAL, LBL, RET extends IGFPA<VAL, LBL>> implements IGFPABuilder<VAL, LBL>
{
	protected Collection<IFSAState<VAL, LBL>> states, initialStates, finalStates, rootedStates, terminalStates;

	protected Collection<IFSAEdge<VAL, LBL>> edges;

	protected IFPAProperties properties;

	protected IGFPAValidation<VAL, LBL> validation;

	// ========================================================================

	public AbstractGFPABuilder()
	{
		clear();
	}
	// ========================================================================

	@Override
	public AbstractGFPABuilder<VAL, LBL, RET> clear()
	{
		states     = initialStates = finalStates = rootedStates = terminalStates = Collections.emptyList();
		edges      = Collections.emptyList();
		properties = new FPAProperties(false, false);
		validation = new GFPAValidation<>();
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL, RET> set(Collection<IFSAState<VAL, LBL>> states, Collection<IFSAState<VAL, LBL>> initialStates, Collection<IFSAState<VAL, LBL>> finalStates, Collection<IFSAEdge<VAL, LBL>> edges)
	{
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.edges         = edges;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL, RET> setProperties(IFPAProperties properties)
	{
		this.properties = properties;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL, RET> setRootedStates(Collection<IFSAState<VAL, LBL>> rootedStates)
	{
		this.rootedStates = rootedStates;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL, RET> setTerminalStates(Collection<IFSAState<VAL, LBL>> terminalStates)
	{
		this.terminalStates = terminalStates;
		return this;
	}

	@Override
	public AbstractGFPABuilder<VAL, LBL, RET> setValidation(IGFPAValidation<VAL, LBL> validation)
	{
		this.validation = validation;
		return this;
	}

	// ========================================================================

	@Override
	public abstract RET create();

	@Override
	public RET create( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFPAProperties properties, //
		IGFPAValidation<VAL, LBL> validation)
	{
		return clear() //
			.set(terminalStates, initialStates, finalStates, edges) //
			.setProperties(properties) //
			.setRootedStates(rootedStates).setTerminalStates(terminalStates) //
			.setValidation(validation) //
			.create();
	}
}
