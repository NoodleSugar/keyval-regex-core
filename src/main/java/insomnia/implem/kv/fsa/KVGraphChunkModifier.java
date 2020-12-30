package insomnia.implem.kv.fsa;

import insomnia.data.IPath;
import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.fsa.graphchunk.IGCState;

public interface KVGraphChunkModifier<VAL, LBL>
{

	/**
	 * Data needed for the Modifier.
	 * 
	 * @author zuri
	 */
	public interface Environment<VAL, LBL>
	{
		GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IGCState<VAL> start, IGCState<VAL> end, IPath<VAL, LBL> path);
	};

	void accept(GraphChunk<VAL, LBL> gchunk, Environment<VAL, LBL> env);
}
