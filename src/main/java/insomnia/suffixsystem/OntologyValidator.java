package insomnia.suffixsystem;

import insomnia.rule.PathRule;
import insomnia.rule.ontology.IOntology;

public class OntologyValidator implements IOntologyValidator<PathRule>
{
	@Override
	public boolean test(IOntology<PathRule> ontology)
	{
		for (PathRule rule : ontology)
		{
			if (!isValid(rule))
				return false;
		}
		return true;
	}

	private boolean isValid(PathRule rule)
	{
		// Vérification existentielle (c)
		// Vérification clé -> clé (a)
		// Vérification suffixe (b)
		return (rule.isExistential() && rule.getHead().isTerminal() == false) //
			|| (rule.getBody().size() == 1 && rule.getHead().size() == 1) //
			|| (rule.isExistential() == false && rule.getBody().isTerminal()) //
		;
	}
}
