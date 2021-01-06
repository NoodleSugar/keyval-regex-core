package insomnia.implem.kv.pregex;

import java.security.InvalidParameterException;
import java.util.HashMap;

public class Quantifier
{
	private final int inf;
	private final int sup;

	private static HashMap<String, Quantifier> map = new HashMap<>();

	public static Quantifier from(int inf, int sup)
	{
		String     key = inf + ":" + sup;
		Quantifier q   = map.get(key);

		if (q == null)
		{
			q = new Quantifier(inf, sup);
			map.put(key, q);
		}
		return q;
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
		else
			return "{" + inf + ", " + sup + "}";
	}
}
