package insomnia.lib.numeric;

import org.apache.commons.lang3.ArrayUtils;

public final class BaseNum
{
	private Base  base;
	private int[] num;

	public BaseNum(BaseNum src)
	{
		base = src.base;
		num  = src.num.clone();
	}

	public BaseNum(int num, int... base)
	{
		this((long) num, base);
	}

	public BaseNum(int num, Base base)
	{
		this((long) num, base);
	}

	public BaseNum(long num, int... base)
	{
		this(num, Base.from(base));
		setLong(num);
	}

	public BaseNum(long num, Base base)
	{
		this.base = base;
		this.num  = new int[base.getBase().length];
		setLong(num);
	}

	public BaseNum(Base base)
	{
		this(0, base);
	}
	// ==========================================================================

	public void increment()
	{
		base.increment(num);
	}

	public int[] getNum()
	{
		return num;
	}

	public void setLong(long i)
	{
		base.toNum(i, num);
	}

	public void setInt(int i)
	{
		base.toNum(i, num);
	}

	public long toLong()
	{
		return base.toLong(num);
	}

	public int toInt()
	{
		return base.toInt(num);
	}

	public Base getBase()
	{
		return base;
	}
	// ==========================================================================

	@Override
	public String toString()
	{
		return new StringBuilder().append(ArrayUtils.toString(num)).append("(").append(toLong()).append(")").toString();
	}
}
