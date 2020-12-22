package insomnia.implem.kv.fsa;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import insomnia.data.IPath;
import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.fsa.KVGraphChunkModifier.Environment;
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
		return grdFactory.get();
	}

	int        currentDepth = 0;
	GraphChunk mainChunk;

	IGRD<KVValue, KVLabel> grd;
	Environment            env;

	/**
	 * gChunk must represent a simple path.
	 * 
	 * @param gChunk
	 * @param grd
	 */
	public void apply(//
		GraphChunk gChunk, //
		IGRD<KVValue, KVLabel> grd, //
		Environment env //
	)
	{
		KVPath queryPath;
		{
			Optional<KVPath> opt = KVPath.pathFromGraphChunk(gChunk);

			if (!opt.isPresent())
				throw new InvalidParameterException("graph chunk does not represent a path");

			queryPath = opt.get();
		}
		KVPathRule queryRule = new KVPathRule(queryPath, new KVPath(), false);

		Collection<IDependency<KVValue, KVLabel>> dependencies = grd.getDependencies(queryRule);

		this.env       = env;
		this.mainChunk = gChunk;
		this.grd       = grd;
		build(gChunk, dependencies);
	}

	private void build(GraphChunk gchunk, Collection<IDependency<KVValue, KVLabel>> dependencies)
	{
		currentDepth++;
		List<GraphChunk>                                newChunks = new ArrayList<>();
		List<Collection<IDependency<KVValue, KVLabel>>> newDeps   = new ArrayList<>();

		for (IDependency<KVValue, KVLabel> dependency : dependencies)
		{
			Collection<GraphChunk> unifiables = getUnifiables(gchunk, dependency);

			for (GraphChunk unifiable : unifiables)
			{
				IPathRule<KVValue, KVLabel> rule = (IPathRule<KVValue, KVLabel>) dependency.getRuleTarget();

				Collection<IDependency<KVValue, KVLabel>> deps = grd.getDependencies(rule);

				GraphChunk modGC = apply(unifiable, (IPathUnifier<KVValue, KVLabel>) dependency.getUnifier(), rule.getBody());

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

	private GraphChunk apply(GraphChunk gchunk, IPathUnifier<KVValue, KVLabel> unifier, IPath<KVValue, KVLabel> body)
	{
		Collection<GraphChunk> start = gchunk.validStates(gchunk.getStart(), unifier.getPrefixBody().getLabels());
		Collection<GraphChunk> end   = gchunk.validStates_reverse(gchunk.getEnd(), unifier.getSuffixBody().getLabels());
		assert (start.size() == 1);
		assert (end.size() == 1);

		GraphChunk ret = env.gluePath(mainChunk, //
			start.iterator().next().getEnd(), //
			end.iterator().next().getStart(), //
			body);

		return ret;
	}

	private Collection<GraphChunk> getUnifiables(GraphChunk gchunk, IDependency<KVValue, KVLabel> dep)
	{
//		Collection<GraphChunk> ret = new ArrayList<>();
//		Collection<GCState>    start;
//		Collection<GCState>    end;

		IPathUnifier<KVValue, KVLabel> unifier = (IPathUnifier<KVValue, KVLabel>) dep.getUnifier();

		if (unifier.getHead().isEmpty())
			return Collections.singleton(gchunk);

		throw new NotImplementedException("");

//		// Unread head prefix
//		if (unifier.getPrefixHead().isEmpty())
//			start = Collections.singleton(gchunk.getStart());
//		else
//			throw new NotImplementedException("");
////			start = gchunk.validStates_reverse(gchunk.getStart(), unifier.getPrefixHead().getLabels());
//
//		if (unifier.getSuffixHead().isEmpty())
//			end = Collections.singleton(gchunk.getEnd());
//		else
//			throw new NotImplementedException("");
//
//		return ret;
	}

	public KVGraphChunkModifier getGraphChunkModifier(Collection<IRule<KVValue, KVLabel>> rules)
	{
		return getGraphChunkModifier(rules2grd(rules));
	}

	public KVGraphChunkModifier getGraphChunkModifier(IGRD<KVValue, KVLabel> grd)
	{
		return (gchunk, env) -> {
			apply(gchunk, grd, env);
		};
	}
}
