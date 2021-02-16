package insomnia.implem.fsa.fpa.graphchunk.modifier;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import insomnia.data.IPath;
import insomnia.fsa.IFSAState;
import insomnia.implem.data.Paths;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fpa.graphchunk.modifier.IGraphChunkModifier.Environment;
import insomnia.implem.rule.PathRules;
import insomnia.rule.IPathRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.grd.IGRD;
import insomnia.unifier.IPathUnifier;

/**
 * Apply rules to a graph chunk.
 * 
 * @author zuri
 */
public class GCPathRuleApplierSimple<VAL, LBL>
{
	int maxDepth;

	public GCPathRuleApplierSimple(int maxDepth)
	{
		this.maxDepth = maxDepth;
	}

	int currentDepth = 0;

	GraphChunk<VAL, LBL> mainChunk;

	IGRD<VAL, LBL>        grd;
	Environment<VAL, LBL> env;

	/**
	 * gChunk must represent a simple path.
	 * 
	 * @param gChunk
	 * @param grd
	 */
	public void apply(//
		GraphChunk<VAL, LBL> gChunk, //
		IGRD<VAL, LBL> grd, //
		Environment<VAL, LBL> env //
	)
	{
		GraphChunk<VAL, LBL> gclean = gChunk.cleanLimits();

		IPath<VAL, LBL> queryPath = Paths.pathFromGFPA(gclean);

		if (null == queryPath)
			throw new InvalidParameterException("graph chunk does not represent a path");

		IPathRule<VAL, LBL> queryRule = PathRules.create(queryPath, Paths.create(), false);

		Collection<IDependency<VAL, LBL>> dependencies = grd.getDependencies(queryRule);

		this.env       = env;
		this.mainChunk = gChunk;
		this.grd       = grd;
		build(gclean, dependencies);
	}

	private void build(GraphChunk<VAL, LBL> gchunk, Collection<IDependency<VAL, LBL>> dependencies)
	{
		currentDepth++;
		List<GraphChunk<VAL, LBL>>              newChunks = new ArrayList<>();
		List<Collection<IDependency<VAL, LBL>>> newDeps   = new ArrayList<>();

		for (IDependency<VAL, LBL> dependency : dependencies)
		{
			IPathRule<VAL, LBL>               rule       = (IPathRule<VAL, LBL>) dependency.getRuleTarget();
			IPathUnifier<VAL, LBL>            unifier    = (IPathUnifier<VAL, LBL>) dependency.getUnifier();
			Collection<GraphChunk<VAL, LBL>>  unifiables = getUnifiables(gchunk, dependency);
			Collection<IDependency<VAL, LBL>> deps       = grd.getDependencies(rule);

			for (GraphChunk<VAL, LBL> unifiable : unifiables)
			{
				GraphChunk<VAL, LBL> modGC = apply(unifiable, unifier, rule);

				if (deps.isEmpty())
					continue;

				newChunks.add(modGC);
				newDeps.add(deps);
			}
		}

		if (currentDepth <= maxDepth)
		{
			for (int i = 0, c = newChunks.size(); i < c; i++)
				build(newChunks.get(i), newDeps.get(i));
		}
		currentDepth--;
	}

	private GraphChunk<VAL, LBL> apply(GraphChunk<VAL, LBL> gchunk, IPathUnifier<VAL, LBL> unifier, IPathRule<VAL, LBL> rule)
	{
		Collection<GraphChunk<VAL, LBL>> gcstart = gchunk.getValidChunks(gchunk.getStart(), unifier.getPrefixBody().getLabels());
		Collection<GraphChunk<VAL, LBL>> gcend   = gchunk.getValidChunks_reverse(gchunk.getEnd(), unifier.getSuffixBody().getLabels());
		assert (gcstart.size() == 1);
		assert (gcend.size() == 1);

		IFSAState<VAL, LBL> start = gcstart.iterator().next().getEnd();
		IFSAState<VAL, LBL> end   = gcend.iterator().next().getStart();

		if (rule.isExistential())
		{
			Collection<GraphChunk<VAL, LBL>> bodyChunks = mainChunk.getValidChunks(start, rule.getBody().getLabels());

			bodyChunks.stream().filter((gc) -> {
				IFSAState<VAL, LBL> endState = gc.getEnd();
				return gc.isFinal(endState) && gc.isTerminal(endState) == rule.getBody().isTerminal() && endState.getValueCondition().test(rule.getBody().getLeaf().getValue());
			}).findFirst();

			if (!bodyChunks.isEmpty())
				return bodyChunks.iterator().next();

			return env.gluePath(mainChunk, start, rule.getBody());
		}
		else
		{
			Optional<GraphChunk<VAL, LBL>> bodyChunk = mainChunk.getFirstValidChunk(start, end, rule.getBody().getLabels());

			if (bodyChunk.isPresent())
				return bodyChunk.get();

			return env.gluePath(mainChunk, start, end, rule.getBody());
		}
	}

	private Collection<GraphChunk<VAL, LBL>> getUnifiables(GraphChunk<VAL, LBL> gchunk, IDependency<VAL, LBL> dep)
	{
		IPathUnifier<VAL, LBL> unifier = (IPathUnifier<VAL, LBL>) dep.getUnifier();

		if (unifier.getHead().isEmpty())
			return Collections.singleton(gchunk);

		Collection<GraphChunk<VAL, LBL>> startChunks, endChunks;
		boolean                          prefixEmpty = unifier.getPrefixHead().isEmpty();
		boolean                          suffixEmpty = unifier.getSuffixHead().isEmpty();

		// Unread head prefix
		if (prefixEmpty)
			startChunks = Collections.emptyList();
		else
			startChunks = mainChunk.getValidChunks_reverse(gchunk.getStart(), unifier.getPrefixHead().getLabels());

		if (suffixEmpty)
			endChunks = Collections.emptyList();
		else
			endChunks = mainChunk.getValidChunks(gchunk.getEnd(), unifier.getSuffixHead().getLabels());

		// Not able to read: no chunk presents
		if ((!prefixEmpty && startChunks.isEmpty()) || (!suffixEmpty && endChunks.isEmpty()))
			return Collections.emptyList();

		if (!startChunks.isEmpty() && !endChunks.isEmpty())
		{
			Collection<GraphChunk<VAL, LBL>> ret = new ArrayList<>();

			for (GraphChunk<VAL, LBL> start : startChunks)
			{
				for (GraphChunk<VAL, LBL> end : endChunks)
				{
					GraphChunk<VAL, LBL> gc = start.copy();
					gc.union(end);
					ret.add(gc);
				}
			}
			return ret;
		}
		else if (!startChunks.isEmpty())
		{
			startChunks.forEach((gc) -> gc.union(gchunk));
			return startChunks;
		}
		else
		{
			endChunks.forEach((gc) -> gc.union(gchunk));
			return endChunks;
		}
	}

	public IGraphChunkModifier<VAL, LBL> getGraphChunkModifier(IGRD<VAL, LBL> grd)
	{
		return (gchunk, env) -> {
			apply(gchunk, grd, env);
		};
	}
}
