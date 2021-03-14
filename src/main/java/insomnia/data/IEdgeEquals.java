package insomnia.data;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Help class to implement different equality strategies.
 * 
 * @author zuri
 * @param <VAL> type of node value
 * @param <LBL> type of edge label
 */
interface IEdgeEquals<VAL, LBL> extends IEdge<VAL, LBL>
{
	IEdge<VAL, LBL> getSrc();

	// =========================================================================

	static <VAL, LBL> IEdgeEquals<VAL, LBL> create(IEdge<VAL, LBL> src, //
		BiPredicate<IEdge<VAL, LBL>, IEdge<VAL, LBL>> fequals, //
		Function<IEdge<VAL, LBL>, Integer> fhashCode)
	{
		return new IEdgeEquals<VAL, LBL>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public boolean equals(Object obj)
			{
				if (!(obj instanceof IEdgeEquals<?, ?>))
					return false;

				return fequals.test(src, ((IEdgeEquals<VAL, LBL>) obj).getSrc());
			}

			@Override
			public int hashCode()
			{
				return fhashCode.apply(this);
			}

			@Override
			public IEdge<VAL, LBL> getSrc()
			{
				return src;
			}

			@Override
			public LBL getLabel()
			{
				return src.getLabel();
			}

			@Override
			public INode<VAL, LBL> getParent()
			{
				return src.getParent();
			}

			@Override
			public INode<VAL, LBL> getChild()
			{
				return src.getChild();
			}

			@Override
			public String toString()
			{
				return src.toString();
			}
		};
	}
}
