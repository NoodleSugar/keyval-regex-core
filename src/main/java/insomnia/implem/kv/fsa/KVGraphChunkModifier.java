package insomnia.implem.kv.fsa;

import insomnia.data.IPath;
import insomnia.implem.fsa.gbuilder.GCState;
import insomnia.implem.fsa.gbuilder.GraphChunk;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;

public interface KVGraphChunkModifier
{

	/**
	 * Data needed for the Modifier.
	 * 
	 * @author zuri
	 */
	public interface Environment
	{
		GraphChunk gluePath(GraphChunk gchunk, GCState start, GCState end, IPath<KVValue, KVLabel> path);
	};

	void accept(GraphChunk gchunk, Environment env);
}
