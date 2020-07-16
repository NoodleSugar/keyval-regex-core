package insomnia.rule.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import insomnia.rule.PathRule;

public class PathOntology implements IOntology<PathRule>
{
	List<PathRule> rules;

	public PathOntology(Collection<PathRule> col)
	{
		rules = new ArrayList<>();
		rules.addAll(col);
	}

	@Override
	public Iterator<PathRule> iterator()
	{
		return rules.iterator();
	}
}
