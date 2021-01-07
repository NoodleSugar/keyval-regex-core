package insomnia.implem.kv.data;

import java.util.Optional;

import insomnia.data.INode;

final class KVNodes
{
	private KVNodes()
	{

	}

	// =========================================================================

	static abstract class KVNode implements INode<KVValue, KVLabel>
	{
		private int pos;

		private Optional<KVValue> value;

		KVNode(int pos, Optional<KVValue> value)
		{
			this.pos   = pos;
			this.value = value;
		}

		@Override
		public Optional<KVValue> getValue()
		{
			return value;
		}

		int getPos()
		{
			return pos;
		}
	}

	// =========================================================================

	public static KVNode create(int pos, boolean isRooted, boolean isTerminal, Optional<KVValue> value)
	{
		return new KVNode(pos, value)
		{
			@Override
			public boolean isTerminal()
			{
				return isTerminal;
			}

			@Override
			public boolean isRooted()
			{
				return isRooted;
			}
		};
	}
}
