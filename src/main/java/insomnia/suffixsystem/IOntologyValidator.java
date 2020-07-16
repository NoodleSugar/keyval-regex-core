package insomnia.suffixsystem;

import java.util.function.Predicate;

import insomnia.rule.IRule;
import insomnia.rule.ontology.IOntology;

public interface IOntologyValidator<R extends IRule<?>> extends Predicate<IOntology<R>>
{
}
