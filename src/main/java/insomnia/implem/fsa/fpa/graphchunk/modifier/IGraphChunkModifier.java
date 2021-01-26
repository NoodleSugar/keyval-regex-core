package insomnia.implem.fsa.fpa.graphchunk.modifier;

import insomnia.data.IPath;
import insomnia.fsa.IFSAState;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;

@FunctionalInterface
public interface IGraphChunkModifier<VAL, LBL>
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
		GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IFSAState<VAL, LBL> start, IFSAState<VAL, LBL> end, IPath<VAL, LBL> path);

		/**
		 * Glue path at start.
		 * Useful for existential path.
		 */
		GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IFSAState<VAL, LBL> start, IPath<VAL, LBL> path);
	};

	void accept(GraphChunk<VAL, LBL> gchunk, Environment<VAL, LBL> env);
}
