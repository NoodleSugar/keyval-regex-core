package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatcher;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.implem.fsa.fpa.GFPAMatchers;

/**
 * A simple {@link IGFPA} implementation which use simple collections for storage.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public abstract class AbstractSimpleGFPA<VAL, LBL> //
	extends AbstractGFPA<VAL, LBL> //
	implements IGFPA<VAL, LBL>
{
	private IFPAProperties properties;

	private Collection<IFSAState<VAL, LBL>> rootedStates;
	private Collection<IFSAState<VAL, LBL>> terminalStates;

	private Collection<IFSAState<VAL, LBL>> initialStates;
	private Collection<IFSAState<VAL, LBL>> finalStates;

	private Collection<IFSAState<VAL, LBL>> states;
	private Collection<IFSAEdge<VAL, LBL>>  edges;
	private Collection<IFSAEdge<VAL, LBL>>  epsilonEdges;

	private Map<IFSAState<VAL, LBL>, Collection<IFSAEdge<VAL, LBL>>> edgesOf;
	private Map<IFSAState<VAL, LBL>, Collection<IFSAEdge<VAL, LBL>>> edgesTo;
	private Map<IFSAState<VAL, LBL>, Collection<IFSAEdge<VAL, LBL>>> epsilonEdgesOf;
	private Map<IFSAState<VAL, LBL>, Collection<IFSAEdge<VAL, LBL>>> epsilonEdgesTo;

	protected AbstractSimpleGFPA( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> allEdges, //
		IFPAProperties properties //
	)
	{
		this.states         = states;
		this.initialStates  = initialStates;
		this.finalStates    = finalStates;
		this.properties     = properties;
		this.rootedStates   = rootedStates;
		this.terminalStates = terminalStates;

		this.edges   = new ArrayList<>();
		this.edgesOf = new HashMap<>();
		this.edgesTo = new HashMap<>();

		if (properties.isSynchronous())
		{
			epsilonEdgesTo = Collections.emptyMap();
			epsilonEdgesOf = Collections.emptyMap();
			epsilonEdges   = Collections.emptyList();

			for (IFSAEdge<VAL, LBL> edge : allEdges)
			{
				edges.add(edge);
				edgesOf.computeIfAbsent(edge.getParent(), e -> new ArrayList<>()).add(edge);
				edgesTo.computeIfAbsent(edge.getChild(), e -> new ArrayList<>()).add(edge);
			}
		}
		else
		{
			epsilonEdges   = new ArrayList<>();
			epsilonEdgesOf = new HashMap<>();
			epsilonEdgesTo = new HashMap<>();

			for (IFSAEdge<VAL, LBL> edge : allEdges)
			{
				if (IFSAEdge.isEpsilon(edge))
				{
					epsilonEdges.add(edge);
					epsilonEdgesOf.computeIfAbsent(edge.getParent(), e -> new ArrayList<>()).add(edge);
					epsilonEdgesTo.computeIfAbsent(edge.getChild(), e -> new ArrayList<>()).add(edge);
				}
				else
				{
					edges.add(edge);
					edgesOf.computeIfAbsent(edge.getParent(), e -> new ArrayList<>()).add(edge);
					edgesTo.computeIfAbsent(edge.getChild(), e -> new ArrayList<>()).add(edge);
				}
			}
		}
	}

	@Override
	public IPathMatcher<VAL, LBL> matcher(IPath<VAL, LBL> element)
	{
		return GFPAMatchers.create(this, element);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getFinalStates()
	{
		return Collections.unmodifiableCollection(finalStates);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getInitialStates()
	{
		return Collections.unmodifiableCollection(initialStates);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getRootedStates()
	{
		return Collections.unmodifiableCollection(rootedStates);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getTerminalStates()
	{
		return Collections.unmodifiableCollection(terminalStates);
	}

	@Override
	public IFPAProperties getProperties()
	{
		return properties;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getStates()
	{
		return Collections.unmodifiableCollection(states);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getEpsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAState<VAL, LBL>> ret = new ArrayList<>();
		ret.addAll(states);

		if (!properties.isSynchronous())
			IGFPA.epsilonClosureOf(this, ret);

		return ret;
	}

	// =========================================================================

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges()
	{
		return Collections.unmodifiableCollection(edges);
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdges()
	{
		return Collections.unmodifiableCollection(epsilonEdges);
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdges()
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new ArrayList<>(edges.size() + epsilonEdges.size());
		ret.addAll(edges);
		ret.addAll(epsilonEdges);
		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new HashSet<>();

		for (IFSAState<VAL, LBL> state : states)
			ret.addAll(epsilonEdgesTo.getOrDefault(state, Collections.emptySet()));

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new HashSet<>();

		for (IFSAState<VAL, LBL> state : states)
			ret.addAll(edgesTo.getOrDefault(state, Collections.emptySet()));

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new HashSet<>();

		for (IFSAState<VAL, LBL> state : states)
		{
			ret.addAll(edgesTo.getOrDefault(state, Collections.emptySet()));
			ret.addAll(epsilonEdgesTo.getOrDefault(state, Collections.emptySet()));
		}
		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new HashSet<>();

		for (IFSAState<VAL, LBL> state : states)
			ret.addAll(epsilonEdgesOf.getOrDefault(state, Collections.emptySet()));

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new HashSet<>();

		for (IFSAState<VAL, LBL> state : states)
			ret.addAll(edgesOf.getOrDefault(state, Collections.emptySet()));

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new HashSet<>();

		for (IFSAState<VAL, LBL> state : states)
		{
			ret.addAll(edgesOf.getOrDefault(state, Collections.emptySet()));
			ret.addAll(epsilonEdgesOf.getOrDefault(state, Collections.emptySet()));
		}
		return ret;
	}
}
