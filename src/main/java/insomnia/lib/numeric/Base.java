package insomnia.lib.numeric;

import java.util.Arrays;
import java.util.List;

public final class Base
{
	private int base[];

	private Base(int... base)
	{
		this.base = base;
	}

	private Base(List<? extends Number> base)
	{
		this.base = base.stream().mapToInt(Number::intValue).toArray();
	}

	// ==========================================================================

	/**
	 * Create a numeric base of a determined length with a unique base number for each element.
	 * 
	 * @param base   the base for each element
	 * @param length the size of a number
	 * @return
	 */
	public static Base simple(int base, int length)
	{
		int[] abase = new int[length];
		Arrays.fill(abase, base);
		return new Base(abase);
	}

	public static Base from(int... base)
	{
		return new Base(base);
	}

	public static Base from(List<? extends Number> base)
	{
		return new Base(base);
	}

	// ==========================================================================

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

	public int toInt(int num[])
	{
		int pos     = base.length - 1;
		int ret     = num[pos];
		int accBase = base[pos];

		while (pos-- != 0)
		{
			ret     += num[pos] * accBase;
			accBase *= base[pos + 1];
		}
		return ret;
	}


	private long max = -1;

	public int size()
	{
		return (int) longSize();
	}

	public long longSize()
	{
		if (max != -1)
			return max;
		if (base.length == 0)
		{
			max = 0;
			return max;
		}
		int pos = base.length - 1;
		max = base[pos];

		while (pos-- != 0)
			max *= base[pos];

		return max;
	}
}
