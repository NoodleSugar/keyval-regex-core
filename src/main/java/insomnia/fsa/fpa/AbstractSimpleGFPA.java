package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatcher;
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

	private Map<IFSAState<VAL, LBL>, Collection<IFSAEdge<VAL, LBL>>> edgesOf;

	protected AbstractSimpleGFPA( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFPAProperties properties //
	)
	{
		this.edges         = edges;
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.properties    = properties;

		this.rootedStates   = rootedStates;
		this.terminalStates = terminalStates;

		this.edgesOf = new HashMap<>();

		for (IFSAEdge<VAL, LBL> edge : edges)
		{
			Collection<IFSAEdge<VAL, LBL>> coll = edgesOf.computeIfAbsent(edge.getParent(), e -> new ArrayList<>());
			coll.add(edge);
		}
	}

	@Override
	public ITreeMatcher<VAL, LBL> matcher(IPath<VAL, LBL> element)
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
		return rootedStates;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getTerminalStates()
	{
		return terminalStates;
	}

	@Override
	public int nbStates()
	{
		return states.size();
	}

	@Override
	public int nbEdges()
	{
		return edges.size();
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
	public Collection<IFSAEdge<VAL, LBL>> getEdges()
	{
		return Collections.unmodifiableCollection(edges);
	}

	@Override
	public int nbEdges(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		int ret = 0;

		for (IFSAState<VAL, LBL> state : states)
			ret += edgesOf.getOrDefault(state, Collections.emptyList()).size();

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		List<IFSAEdge<VAL, LBL>> ret = new ArrayList<>();

		for (IFSAState<VAL, LBL> state : states)
			ret.addAll(edgesOf.getOrDefault(state, Collections.emptyList()));

		return ret;
	}
}
