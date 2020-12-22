package insomnia.implem.kv.rule.grd;

import java.util.ArrayList;
import java.util.Collection;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyValidation;

public class KVGRDFactory
{
	private Collection<IRule<KVValue, KVLabel>>     rules;
	private IDependencyValidation<KVValue, KVLabel> validation;

	public KVGRDFactory(Collection<? extends IRule<KVValue, KVLabel>> rules, IDependencyValidation<KVValue, KVLabel> validation)
	{
		this.rules      = new ArrayList<>(rules);
		this.validation = validation;
	}

	public KVGRD get()
	{
		KVGRD grd = new KVGRD(validation);
		build(grd);
		return grd;
	}

	private void registerDependencies(KVGRD grd, //
		Collection<IDependency<KVValue, KVLabel>> dependencies, //
		IRule<KVValue, KVLabel> a, //
		IRule<KVValue, KVLabel> b)
	{
		for (IDependency<KVValue, KVLabel> dependency : dependencies)
		{
			grd.addVertex(a);
			grd.addVertex(b);
			grd.addEdge(a, b, dependency);
		}
	}

	private void build(KVGRD grd)
	{
		for (IRule<KVValue, KVLabel> a : rules)
		{
			for (IRule<KVValue, KVLabel> b : rules)
			{
				if (false == validation.test(a, b))
					continue;

				registerDependencies(grd, validation.getDependencies(a, b), a, b);
			}
		}
	}
}