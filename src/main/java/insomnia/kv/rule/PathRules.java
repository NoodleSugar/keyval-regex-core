package insomnia.kv.rule;

import java.util.List;

import insomnia.kv.suffixsystem.unifier.Unifier;

public final class PathRules
{
	public static boolean weakDependency(PathRule a, PathRule b)
	{
		return Unifier.weakUnifiers(a.getHead(), b.getBody(), true, a.isExistential()).size() > 0;
	}

	public static boolean strongDependency(PathRule a, PathRule b)
	{
		return Unifier.strongUnifiers(a.getHead(), b.getBody(), true).size() > 0;
	}

	public static boolean haveDependency(PathRule a, PathRule b)
	{
		return weakDependency(a, b) || strongDependency(a, b);
	}

	public static List<Unifier> weakUnifiers(PathRule a, PathRule b)
	{
		return Unifier.weakUnifiers(a.getHead(), b.getBody(), a.isExistential());
	}

	public static List<Unifier> strongUnifiers(PathRule a, PathRule b)
	{
		return Unifier.strongUnifiers(a.getHead(), b.getBody());
	}

	public static List<Unifier> allUnifiers(PathRule a, PathRule b)
	{
		List<Unifier> ret = weakUnifiers(a, b);
		ret.addAll(strongUnifiers(a, b));
		return ret;
	}
}
