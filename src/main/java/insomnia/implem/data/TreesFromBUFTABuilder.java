package insomnia.implem.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.creational.TreeBuilder;

final class TreesFromBUFTABuilder<VAL, LBL> implements Iterable<ITree<VAL, LBL>>
{
	private IBUFTA<VAL, LBL> automaton;
	private IGFPA<VAL, LBL>  gfpa;
	private int              maxRecursiveDepth;

	TreesFromBUFTABuilder(IBUFTA<VAL, LBL> automaton)
	{
		this.automaton = automaton;
		this.gfpa      = automaton.getGFPA();
		setMaxRecursiveDepth(-1);
	}

	public TreesFromBUFTABuilder<VAL, LBL> avoidMaxRecursiveDepth()
	{
		maxRecursiveDepth = -1;
		return this;
	}

	/**
	 * @param val if set to {@code -1}: avoid recursion
	 * @return this
	 */
	public TreesFromBUFTABuilder<VAL, LBL> setMaxRecursiveDepth(int val)
	{
		if (val < -1)
			throw new IllegalArgumentException("maxRecursiveDepth cannot be less than 0; have " + val);

		maxRecursiveDepth = val;
		return this;
	}

	@Override
	public Iterator<ITree<VAL, LBL>> iterator()
	{
		return new Iterator<>()
		{
			final class Data
			{
				private Data            parentData;
				private INode<VAL, LBL> currentBuilderNode;

				private IFSAState<VAL, LBL> theState;
				private boolean             isInitial;
				private IFTAEdge<VAL, LBL>  theFTAEdge;
				private IFSAEdge<VAL, LBL>  theSimpleEdge;

				private Iterator<IFTAEdge<VAL, LBL>> ftaEdges;
				private Iterator<IFSAEdge<VAL, LBL>> simpleEdges;

				private Data       simpleEdgeData;
				private List<Data> parentsData;

				Data(IFSAState<VAL, LBL> initialState, ITreeBuilder<VAL, LBL> tbuilder, Data parent)
				{
					theState   = initialState;
					parentData = parent;
					var states = IGFPA.getEpsilonClosureTo(gfpa, Collections.singleton(initialState));
					isInitial          = states.stream().anyMatch(automaton.getGFPA()::isInitial);
					ftaEdges           = automaton.getFTAEdgesTo(states).iterator();
					simpleEdges        = automaton.getGFPA().getEdgesTo(states).iterator();
					parentsData        = new ArrayList<>();
					currentBuilderNode = tbuilder.getCurrentNode();
				}

				Bag<IFSAState<VAL, LBL>> getProcessedStates()
				{
					Bag<IFSAState<VAL, LBL>> ret = new HashBag<>();

					Data d = this;

					while (true)
					{
						ret.add(d.theState);

						if (null == d.parentData)
							break;

						d = d.parentData;
					}
					return ret;
				}

				private boolean nextFTAChildEdge(ITreeBuilder<VAL, LBL> tbuilder)
				{
					if (null == theFTAEdge)
						return false;

					var parents = theFTAEdge.getParents();

					for (int i = 0, c = parentsData.size(); i < c; i++)
					{
						var data = parentsData.get(i);

						if (data.nextEdge(tbuilder))
							return true;

						parentsData.set(i, createData(parents.get(i), this, tbuilder));
					}
					tbuilder.setCurrentNode(currentBuilderNode);
					tbuilder.removeChilds();
					return false;
				}

				private boolean nextFTAEdge(ITreeBuilder<VAL, LBL> tbuilder)
				{
					if (nextFTAChildEdge(tbuilder))
						return true;

					for (;;)
					{
						if (!ftaEdges.hasNext())
							return false;

						tbuilder.setCurrentNode(currentBuilderNode);
						theFTAEdge = ftaEdges.next();
						parentsData.clear();

						// Avoid {q} -> q hyper-transition
						if (Objects.equals(theFTAEdge.getParents(), Collections.singletonList(theFTAEdge.getChild())))
							continue;

						var parents = theFTAEdge.getParents();
						tbuilder.removeChilds();

						for (int i = 0, c = parents.size(); i < c; i++)
						{
							var s = parents.get(i);

							tbuilder.setCurrentNode(currentBuilderNode).addChildDown(null);
							var data = createData(s, this, tbuilder);
							tbuilder.goUp();

							parentsData.add(data);
						}
						break;
					}
					return true;
				}

				private boolean nextSimpleChildEdge(ITreeBuilder<VAL, LBL> tbuilder)
				{
					if (null == simpleEdgeData)
						return false;

					tbuilder.goDown();
					var ret = simpleEdgeData.nextEdge(tbuilder);

					if (ret)
						return true;

					simpleEdgeData = null;
					return false;
				}

				private boolean nextSimpleEdge(ITreeBuilder<VAL, LBL> tbuilder)
				{
					if (nextSimpleChildEdge(tbuilder))
						return true;

					tbuilder.setCurrentNode(currentBuilderNode);

					if (!simpleEdges.hasNext())
					{
						if (!parentIsFTA() && tbuilder.getCurrentNode() != tbuilder.getRoot())
							tbuilder.removeUp();

						if (isInitial && !parentIsFTA())
						{
							isInitial = false;
							return true;
						}
						return false;
					}
					theSimpleEdge = simpleEdges.next();
					var labels = theSimpleEdge.getLabelCondition().getLabels();
					var values = theSimpleEdge.getParent().getValueCondition().getValues();

					if (labels.size() > 1)
						throw new IllegalArgumentException(String.format("Can't handle en edge condition with multiple labels: %s", labels));
					if (values.size() > 1)
						throw new IllegalArgumentException(String.format("Can't handle en edge condition with multiple values: %s", values));

					tbuilder //
						.setLabel(CollectionUtils.extractSingleton(labels)) //
						.setValue(CollectionUtils.extractSingleton(values));
					simpleEdgeData = createData(theSimpleEdge.getParent(), this, tbuilder);

					return true;
				}

				private boolean parentIsFTA()
				{
					return null != parentData && null != parentData.ftaEdges;
				}

				private boolean nextEdge(ITreeBuilder<VAL, LBL> tbuilder)
				{
					if (null != ftaEdges)
					{
						if (nextFTAEdge(tbuilder))
							return true;
						else
						{
							ftaEdges = null;

							if (!parentIsFTA())
							{
								tbuilder.setCurrentNode(currentBuilderNode);
								tbuilder.addChildDown(null);
								currentBuilderNode = tbuilder.getCurrentNode();
							}
						}
					}
					return nextSimpleEdge(tbuilder);
				}
			} // End Of Data

			// ======================================================================

			private Data createData(IFSAState<VAL, LBL> theState, Data parent, ITreeBuilder<VAL, LBL> tbuilder)
			{
				if (null != parent)
				{
					int processed = parent.getProcessedStates().getCount(theState);

					if (processed >= 1)
					{
						if (maxRecursiveDepth == -1)
							throw new IllegalArgumentException("Can't handle a BUFTA with an infinite language");

						if (processed >= maxRecursiveDepth)
							return null;
					}
				}
				var ret = new Data(theState, tbuilder, parent);
				return ret.nextEdge(tbuilder) ? ret : null;
			}

			// ======================================================================
			// Iterator Definition

			private ITreeBuilder<VAL, LBL> tbuilder;
			private Data                   data;

			private Iterator<IFSAState<VAL, LBL>> finalStates;

			// Iterator init
			{
				finalStates = automaton.getGFPA().getEpsilonClosure(automaton.getGFPA().getFinalStates(), s -> true).iterator();
				tbuilder    = new TreeBuilder<>();
			}

			// ======================================================================

			private boolean nextData()
			{
				for (;;)
				{
					if (!finalStates.hasNext())
						return false;

					tbuilder.reset();
					data = createData(finalStates.next(), null, tbuilder);

					if (data != null)
						return true;
				}
			}

			@Override
			public boolean hasNext()
			{
				if (null == data)
					return nextData();

				if (data.nextEdge(tbuilder))
					return true;

				return nextData();// throw new IllegalArgumentException("Can't handle a BUFTA with an infinite language");

			}

			@Override
			public ITree<VAL, LBL> next()
			{
				return tbuilder;
			}
		};
	}
}
