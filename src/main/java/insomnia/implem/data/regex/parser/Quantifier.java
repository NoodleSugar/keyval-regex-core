package insomnia.implem.data.regex.parser;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An element {@link Quantifier} store its repetition informations as a lower (inf) and an upper (sup) bound.
 * <br>
 * The bound values may represent infinite repetitions if set to {@link Quantifier#infinite}.
 * <br>
 * A {@link Quantifier} guarantee that {@code (0 <= inf && inf <= sup)} if no infinite values are involved, if not then {@code (sup == Quantifier#infinite && (0 <= inf || inf == sup))}.
 * 
 * @author zuri noodle
 * @author zuri
 */
public final class Quantifier
{
	/**
	 * The value for the infinite semantic
	 */
	public static final long infinite = -1;
	private final int        inf;
	private final int        sup;

	private static Map<Quantifier, WeakReference<Quantifier>> map = new WeakHashMap<>();

	// ==========================================================================

	private Quantifier(int inf, int sup) throws InvalidParameterException
	{
		if (!(0 <= inf && (sup == infinite || inf <= sup) || (inf == sup && sup == infinite)))
			throw new IllegalArgumentException(String.format("Invalid quantifier {%d,%d}", inf, sup));
		this.inf = inf;
		this.sup = sup;
	}

	// ==========================================================================

	/**
	 * Create a new {@link Quantifier}.
	 * 
	 * @param inf lower bound
	 * @param sup upper bound
	 * @return a quantifier with the requested bounds
	 */
	public static Quantifier from(int inf, int sup)
	{
		Quantifier                tmp = new Quantifier(inf, sup);
		WeakReference<Quantifier> q   = map.get(tmp);

		if (q != null && q.get() != null)
			return q.get();

		map.put(tmp, new WeakReference<>(tmp));
		return tmp;
	}
	// ==========================================================================

	public static boolean isInfinite(long val)
	{
		return val == infinite;
	}

	public static boolean isInfinite(int val)
	{
		return val == infinite;
	}

	public int getInf()
	{
		return inf;
	}

	public int getSup()
	{
		return sup;
	}
	// ==========================================================================

	/**
	 * Make the sum of two quantifier.
	 * <br>
	 * Note that an infinite value summed always result in an infinite value.
	 * 
	 * @param q1 first quantifier
	 * @param q2 second quantifier
	 * @return a quantifier where the bounds of the two previous ones are summed
	 */
	public static Quantifier add(Quantifier q1, Quantifier q2)
	{
		int inf = q1.inf + q2.inf;
		int sup;

		if (isInfinite(q1.sup) || isInfinite(q2.sup))
			sup = (int) infinite;
		else
			sup = q1.sup + q2.sup;

		return Quantifier.from(inf, sup);
	}

	/**
	 * Make the product of two quantifier.
	 * <br>
	 * Note that an infinite value multiplication always result in an infinite value.
	 * 
	 * @param q1 first quantifier
	 * @param q2 second quantifier
	 * @return a quantifier where the bounds of the two previous ones are multiplied
	 */
	public static Quantifier mul(Quantifier q1, Quantifier q2)
	{
		int inf = q1.inf * q2.inf;
		int sup;

		if (isInfinite(q1.sup) || isInfinite(q2.sup))
			sup = (int) infinite;
		else
			sup = q1.sup * q2.sup;

		return Quantifier.from(inf, sup);
	}
	// ==========================================================================

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
		if (inf == 0 && isInfinite(sup))
			return "*";
		if (inf == 1 && isInfinite(sup))
			return "+";
		if (inf == 0 && sup == 1)
			return "?";
		else if (inf == sup)
			return "{" + inf + "}";
		else
			return "{" + inf + ", " + sup + "}";
	}
}
