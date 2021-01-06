package insomnia.implem.kv.rule.grd;

import java.util.ArrayList;
import java.util.Collection;

import org.jgrapht.graph.DefaultDirectedGraph;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyValidation;
import insomnia.rule.grd.IGRD;

class KVGRD extends DefaultDirectedGraph<IRule<KVValue, KVLabel>, IDependency<KVValue, KVLabel>> implements IGRD<KVValue, KVLabel>
{
	private static final long                       serialVersionUID = 1L;
	private IDependencyValidation<KVValue, KVLabel> validation;

	public KVGRD(IDependencyValidation<KVValue, KVLabel> validation)
	{
		super(null, null, false);
		this.validation = validation;
	}

	@Override
	public Collection<IDependency<KVValue, KVLabel>> getDependencies(IRule<KVValue, KVLabel> rule)
	{
		if (containsVertex(rule))
			return outgoingEdgesOf(rule);

		Collection<IDependency<KVValue, KVLabel>> ret = new ArrayList<>();

		for (IRule<KVValue, KVLabel> grdRule : vertexSet())
			ret.addAll(validation.getDependencies(rule, grdRule));

		return ret;
	}
}