package insomnia.implem.kv.fsa;

import insomnia.data.IPath;
import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.fsa.graphchunk.IGCState;

@FunctionalInterface
public interface KVGraphChunkModifier<VAL, LBL>
{

	/**
	 * Data needed for the Modifier.
	 * 
	 * @author zuri
	 */
	public interface Environment<VAL, LBL>
	{
		/**
		 * Glue path to 'gchunk' between 'start' and 'end'.
		 * 
		 * @param gchunk
		 * @param start
		 * @param end
		 * @param path
		 * @return The subGraphChunk just added to gchunk from start to end.
		 */
		GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IGCState<VAL> start, IGCState<VAL> end, IPath<VAL, LBL> path);

		/**
		 * Glue path at start.
		 * Useful for existential path.
		 */
		GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IGCState<VAL> start, IPath<VAL, LBL> path);
	};

	void accept(GraphChunk<VAL, LBL> gchunk, Environment<VAL, LBL> env);
}
