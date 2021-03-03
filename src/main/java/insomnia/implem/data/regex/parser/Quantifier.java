package insomnia.implem.data.regex.parser;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.WeakHashMap;

public final class Quantifier
{
	private final int inf;
	private final int sup;

	private static Map<Quantifier, WeakReference<Quantifier>> map = new WeakHashMap<>();

	public static Quantifier from(int inf, int sup)
	{
		Quantifier                tmp = new Quantifier(inf, sup);
		WeakReference<Quantifier> q   = map.get(tmp);

		if (q != null && q.get() != null)
			return q.get();

		map.put(tmp, new WeakReference<>(tmp));
		return tmp;
	}
	}

	public static Quantifier multiplication(Quantifier q1, Quantifier q2)
	{
		int inf = q1.inf * q2.inf;
		int sup;
		if (q1.sup == -1 || q2.sup == -1)
			sup = -1;
		else
			sup = q1.sup * q2.sup;

		return Quantifier.from(inf, sup);
	}

	/**
	 * On interdit le quantifier [0, 0]
	 * Par convention infini = -1
	 * Bien entendu il faut inf <= sup
	 */
	private Quantifier(int inf, int sup) throws InvalidParameterException
	{
		if (inf < 0 || (sup != -1 && (sup < inf || sup == 0)))
			throw new InvalidParameterException("Invalid quantifier");
		this.inf = inf;
		this.sup = sup;
	}

	public int getInf()
	{
		return inf;
	}

	public int getSup()
	{
		return sup;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof Quantifier))
			return false;

		Quantifier qo = (Quantifier) obj;
		return inf == qo.inf && sup == qo.sup;
	}

	@Override
	public int hashCode()
	{
		return inf + sup * 31;
	}

	@Override
	public String toString()
	{
		if (inf == 1 && sup == 1)
			return "";
		if (inf == 0 && sup == -1)
			return "*";
		if (inf == 1 && sup == -1)
			return "+";
		if (inf == 0 && sup == 1)
			return "?";
		else if (inf == sup)
			return "{" + inf + "}";
		else
			return "{" + inf + ", " + sup + "}";
	}
}
