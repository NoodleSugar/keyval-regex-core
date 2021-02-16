package insomnia.implem.data;

import java.util.function.Function;

import insomnia.data.ITree;
import insomnia.implem.data.creational.TreeBuilder;

public final class Trees
{
	private Trees()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private final static ITree<?, ?> emptyTree = Tree.empty();

	/**
	 * Create an empty {@link Tree}.
	 * The {@link Tree} returned can be compare to other returned empty {@link Tree}s with {@link Tree#equals(Object)}.
	 * 
	 * @see TreeBuilder
	 */
	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITree<VAL, LBL> empty()
	{
		return (ITree<VAL, LBL>) emptyTree;
	}

	/**
	 * Create a new tree copying informations from 'src'.
	 * 
	 * @see TreeBuilder
	 */
	public static <VAL, LBL> ITree<VAL, LBL> create(ITree<VAL, LBL> src)
	{
		return new Tree<VAL, LBL>(src);
	}

	// =========================================================================

	public static <RVAL, RLBL, VAL, LBL> ITree<RVAL, RLBL> map(ITree<VAL, LBL> src, Function<VAL, RVAL> fmapVal, Function<LBL, RLBL> fmapLabel)
	{
		return Tree.map(src, fmapVal, fmapLabel);
	}

}
