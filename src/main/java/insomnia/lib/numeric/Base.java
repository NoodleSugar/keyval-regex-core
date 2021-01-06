package insomnia.lib.numeric;

public final class Base
{
	private int base[];

	public Base(int... base)
	{
		this.base = base;
	}

	public int[] getBase()
	{
		return base.clone();
	}

	/**
	 * Increment 'num' considering the numeric base 'base'.
	 * 'num' represents a multi base number.
	 */
	public void increment(int num[])
	{
		assert (num.length == base.length);

		for (int i = num.length - 1; i >= 0; i--)
		{
			assert (base[i] != 0);
			num[i]++;

			if (num[i] % base[i] == 0)
				num[i] = 0;
			else
				break;
		}
	}
}
