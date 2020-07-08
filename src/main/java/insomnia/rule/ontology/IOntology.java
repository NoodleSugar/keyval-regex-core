package insomnia.rule.ontology;

import insomnia.rule.IRule;

public interface IOntology<R extends IRule<?>> extends Iterable<R>
{
	IGRD<R> getGRD();
}
