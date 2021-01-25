package insomnia.implem.rule.grd.creational;

import java.util.ArrayList;
import java.util.Collection;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyValidation;
import insomnia.rule.grd.IGRD;

public final class GRDFactory<VAL, LBL>
{
	private Collection<IRule<VAL, LBL>>     rules;
	private IDependencyValidation<VAL, LBL> validation;

	public GRDFactory(Collection<? extends IRule<VAL, LBL>> rules, IDependencyValidation<VAL, LBL> validation)
	{
		this.rules      = new ArrayList<>(rules);
		this.validation = validation;
	}

	public IGRD<VAL, LBL> create()
	{
		GRD<VAL, LBL> grd = new GRD<>(validation);
		build(grd);
		return grd;
	}

	private void registerDependencies(GRD<VAL, LBL> grd, //
		Collection<IDependency<VAL, LBL>> dependencies, //
		IRule<VAL, LBL> a, //
		IRule<VAL, LBL> b)
	{
		for (IDependency<VAL, LBL> dependency : dependencies)
		{
			grd.addVertex(a);
			grd.addVertex(b);
			grd.addEdge(a, b, dependency);
		}
	}

	private void build(GRD<VAL, LBL> grd)
	{
		for (IRule<VAL, LBL> a : rules)
		{
			grd.addVertex(a);

			for (IRule<VAL, LBL> b : rules)
			{
				if (false == validation.test(a, b))
					continue;

				registerDependencies(grd, validation.getDependencies(a, b), a, b);
			}
		}
	}
}