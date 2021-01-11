package insomnia.implem.kv.fsa.fpa;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fpa.graphchunk.IGCState;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.fsa.fpa.KVGraphChunkModifier.Environment;
import insomnia.implem.kv.rule.KVPathRule;
import insomnia.implem.kv.rule.dependency.KVBetaDependencyValidation;
import insomnia.implem.kv.rule.grd.KVGRDFactory;
import insomnia.rule.IPathRule;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.grd.IGRD;
import insomnia.unifier.IPathUnifier;

/**
 * Apply rules to a graph chunk.
 * 
 * @author zuri
 */
public class KVGraphChunkPathRuleApplierSimple
{
	int maxDepth;

	public KVGraphChunkPathRuleApplierSimple(int maxDepth)
	{
		this.maxDepth = maxDepth;
	}

	static private IGRD<KVValue, KVLabel> rules2grd(Collection<IRule<KVValue, KVLabel>> rules)
	{
		KVGRDFactory grdFactory = new KVGRDFactory(rules, new KVBetaDependencyValidation());
		return grdFactory.create();
	}

	int currentDepth = 0;

	GraphChunk<KVValue, KVLabel> mainChunk;

	IGRD<KVValue, KVLabel>        grd;
	Environment<KVValue, KVLabel> env;

	/**
	 * gChunk must represent a simple path.
	 * 
	 * @param gChunk
	 * @param grd
	 */
	public void apply(//
		GraphChunk<KVValue, KVLabel> gChunk, //
		IGRD<KVValue, KVLabel> grd, //
		Environment<KVValue, KVLabel> env //
	)
	{
		GraphChunk<KVValue, KVLabel> gclean = gChunk.cleanLimits();

		KVPath queryPath;
		{
			Optional<KVPath> opt = KVPaths.pathFromGraphChunk(gclean);

			if (!opt.isPresent())
				throw new InvalidParameterException("graph chunk does not represent a path");

			queryPath = opt.get();
		}
		KVPathRule queryRule = KVPathRule.create(queryPath, KVPaths.create(), false);

		Collection<IDependency<KVValue, KVLabel>> dependencies = grd.getDependencies(queryRule);

		this.env       = env;
		this.mainChunk = gChunk;
		this.grd       = grd;
		build(gclean, dependencies);
	}

	private void build(GraphChunk<KVValue, KVLabel> gchunk, Collection<IDependency<KVValue, KVLabel>> dependencies)
	{
		currentDepth++;
		List<GraphChunk<KVValue, KVLabel>>              newChunks = new ArrayList<>();
		List<Collection<IDependency<KVValue, KVLabel>>> newDeps   = new ArrayList<>();

		for (IDependency<KVValue, KVLabel> dependency : dependencies)
		{
			IPathRule<KVValue, KVLabel>               rule       = (IPathRule<KVValue, KVLabel>) dependency.getRuleTarget();
			IPathUnifier<KVValue, KVLabel>            unifier    = (IPathUnifier<KVValue, KVLabel>) dependency.getUnifier();
			Collection<GraphChunk<KVValue, KVLabel>>  unifiables = getUnifiables(gchunk, dependency);
			Collection<IDependency<KVValue, KVLabel>> deps       = grd.getDependencies(rule);

			for (GraphChunk<KVValue, KVLabel> unifiable : unifiables)
			{
				GraphChunk<KVValue, KVLabel> modGC = apply(unifiable, unifier, rule);

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

	private GraphChunk<KVValue, KVLabel> apply(GraphChunk<KVValue, KVLabel> gchunk, IPathUnifier<KVValue, KVLabel> unifier, IPathRule<KVValue, KVLabel> rule)
	{
		Collection<GraphChunk<KVValue, KVLabel>> gcstart = gchunk.getValidChunks(gchunk.getStart(), unifier.getPrefixBody().getLabels());
		Collection<GraphChunk<KVValue, KVLabel>> gcend   = gchunk.getValidChunks_reverse(gchunk.getEnd(), unifier.getSuffixBody().getLabels());
		assert (gcstart.size() == 1);
		assert (gcend.size() == 1);

		IGCState<KVValue> start = gcstart.iterator().next().getEnd();
		IGCState<KVValue> end   = gcend.iterator().next().getStart();

		if (rule.isExistential())
		{
			Collection<GraphChunk<KVValue, KVLabel>> bodyChunks = mainChunk.getValidChunks(start, rule.getBody().getLabels());

			bodyChunks.stream().filter((gc) -> {
				IGCState<KVValue> endState = gc.getEnd();
				return endState.isFinal() && endState.isTerminal() == rule.getBody().isTerminal() && endState.getValueCondition().test(rule.getBody().getValue().orElse(null));
			}).findFirst();

			if (!bodyChunks.isEmpty())
				return bodyChunks.iterator().next();

			return env.gluePath(mainChunk, start, rule.getBody());
		}
		else
		{
			Optional<GraphChunk<KVValue, KVLabel>> bodyChunk = mainChunk.getFirstValidChunk(start, end, rule.getBody().getLabels());

			if (bodyChunk.isPresent())
				return bodyChunk.get();

			return env.gluePath(mainChunk, start, end, rule.getBody());
		}
	}

	private Collection<GraphChunk<KVValue, KVLabel>> getUnifiables(GraphChunk<KVValue, KVLabel> gchunk, IDependency<KVValue, KVLabel> dep)
	{
		IPathUnifier<KVValue, KVLabel> unifier = (IPathUnifier<KVValue, KVLabel>) dep.getUnifier();

		if (unifier.getHead().isEmpty())
			return Collections.singleton(gchunk);

		Collection<GraphChunk<KVValue, KVLabel>> startChunks, endChunks;
		boolean                                  prefixEmpty = unifier.getPrefixHead().isEmpty();
		boolean                                  suffixEmpty = unifier.getSuffixHead().isEmpty();

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
			Collection<GraphChunk<KVValue, KVLabel>> ret = new ArrayList<>();

			for (GraphChunk<KVValue, KVLabel> start : startChunks)
			{
				for (GraphChunk<KVValue, KVLabel> end : endChunks)
				{
					GraphChunk<KVValue, KVLabel> gc = start.copy();
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

	public KVGraphChunkModifier<KVValue, KVLabel> getGraphChunkModifier(Collection<IRule<KVValue, KVLabel>> rules)
	{
		return getGraphChunkModifier(rules2grd(rules));
	}

	public KVGraphChunkModifier<KVValue, KVLabel> getGraphChunkModifier(IGRD<KVValue, KVLabel> grd)
	{
		return (gchunk, env) -> {
			apply(gchunk, grd, env);
		};
	}
}
