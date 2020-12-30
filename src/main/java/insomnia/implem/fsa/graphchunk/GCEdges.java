package insomnia.implem.fsa.graphchunk;

import java.util.Optional;

import insomnia.implem.kv.data.KVValue;

public final class GCEdges
{
	private GCEdges()
	{

	}

	static abstract class AbstractGCEdge<LBL> implements IGCEdge<LBL>
	{
		private Object obj;

		// =========================================================================

		public AbstractGCEdge(Object obj)
		{
			this.obj = obj;
		}

		public Object getObj()
		{
			return obj;
		}

		@Override
		public Optional<String> getLabelAsString()
		{
			return Optional.empty();
		}

		@Override
		public String toString()
		{
			return obj.toString();
		}
	}

	static class GCEdgeEpsilon<LBL> extends AbstractGCEdge<LBL>
	{
		public GCEdgeEpsilon()
		{
			super(null);
		}

		@Override
		public boolean test(LBL t)
		{
			return false;
		}

		@Override
		public IGCEdge<LBL> copy()
		{
			return new GCEdgeEpsilon<>();
		}
	}

	static class GCEdgeStringEq<LBL> extends AbstractGCEdge<LBL>
	{
		public GCEdgeStringEq(String s)
		{
			super(s);
		}

		@Override
		public boolean test(LBL t)
		{
			return t.toString().equals(getObj());
		}

		@Override
		public IGCEdge<LBL> copy()
		{
			return new GCEdgeStringEq<>((String) getObj());
		}

		@Override
		public Optional<String> getLabelAsString()
		{
			return Optional.of((String) getObj());
		}
	}

	static class GCEdgeNumberEq<LBL> extends AbstractGCEdge<LBL>
	{
		public GCEdgeNumberEq(Number nb)
		{
			super(nb);
		}

		@Override
		public boolean test(LBL t)
		{
			return t.equals(getObj());
		}

		@Override
		public IGCEdge<LBL> copy()
		{
			return new GCEdgeNumberEq<>((Number) getObj());
		}

		@Override
		public Optional<String> getLabelAsString()
		{
			return Optional.of(((Number) getObj()).toString());
		}
	}

	static class GCEdgeRegex<LBL> extends AbstractGCEdge<LBL>
	{
		public GCEdgeRegex(String regex)
		{
			super(regex);
		}

		@Override
		public boolean test(LBL t)
		{
			return false;
		}

		@Override
		public IGCEdge<LBL> copy()
		{
			return new GCEdgeRegex<>((String) getObj());
		}
	}

	// =========================================================================

	public static <LBL> boolean isEpsilon(IGCEdge<LBL> edge)
	{
		return edge instanceof GCEdgeEpsilon;
	}

	public static <LBL> boolean isStringEq(IGCEdge<LBL> edge)
	{
		return edge instanceof GCEdgeStringEq;
	}

	public static <LBL> boolean isNumber(IGCEdge<LBL> edge)
	{
		return edge instanceof GCEdgeNumberEq;
	}

	public static <LBL> boolean isRegex(IGCEdge<LBL> edge)
	{
		return edge instanceof GCEdgeRegex;
	}

	// =========================================================================

	public static <LBL> IGCEdge<LBL> createFromKVValue(KVValue value)
	{
		switch (value.getType())
		{
		case NUMBER:
			return createNumber(value.getNumber());
		case STRING:
			return createStringEq(value.getString());
		case NULL:
		default:
			return createEpsilon();
		}
	}

	public static <LBL> IGCEdge<LBL> createEpsilon()
	{
		return new GCEdgeEpsilon<>();
	}

	public static <LBL> IGCEdge<LBL> createStringEq(String s)
	{
		return new GCEdgeStringEq<>(s);
	}

	public static <LBL> IGCEdge<LBL> createRegex(String r)
	{
		return new GCEdgeRegex<>(r);
	}

	public static <LBL> IGCEdge<LBL> createNumber(Number nb)
	{
		return new GCEdgeNumberEq<>(nb);
	}
}