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
		this(num, Base.from(base));
	}

	public BaseNum(int num, Base base)
	{
		this.base = base;
		this.num  = new int[base.getBase().length];
	public BaseNum(Base base)
	{
		this(0, base);
	}

	public void increment()
	{
		base.increment(num);
	}

	public int[] getNum()
	{
		return num;
	}

	public int toInt()
	{
		return base.toInt(num);
	}

	public Base getBase()
	{
		return base;
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append(ArrayUtils.toString(num)).append("(").append(toInt()).append(")").toString();
	}
}
