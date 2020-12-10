package insomnia.kv.suffixsystem;

import java.util.function.Predicate;

import insomnia.kv.rule.IRule;
import insomnia.kv.rule.ontology.IOntology;

public interface IOntologyValidator<R extends IRule<?>> extends Predicate<IOntology<R>>
{
}
