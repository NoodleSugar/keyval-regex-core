package insomnia.implem.rule.grd.creational;

import java.util.ArrayList;
import java.util.Collection;

import org.jgrapht.graph.DefaultDirectedGraph;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyValidation;
import insomnia.rule.grd.IGRD;

class GRD<VAL, LBL> extends DefaultDirectedGraph<IRule<VAL, LBL>, IDependency<VAL, LBL>> implements IGRD<VAL, LBL>
{
	private static final long               serialVersionUID = 1L;
	private IDependencyValidation<VAL, LBL> validation;

	public GRD(IDependencyValidation<VAL, LBL> validation)
	{
		super(null, null, false);
		this.validation = validation;
	}

	@Override
	public Collection<IDependency<VAL, LBL>> getDependencies(IRule<VAL, LBL> rule)
	{
		if (containsVertex(rule))
			return outgoingEdgesOf(rule);

		Collection<IDependency<VAL, LBL>> ret = new ArrayList<>();

		for (IRule<VAL, LBL> grdRule : vertexSet())
			ret.addAll(validation.getDependencies(rule, grdRule));

		return ret;
	}
}