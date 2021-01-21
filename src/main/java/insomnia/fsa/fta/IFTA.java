package insomnia.fsa.fta;

import java.util.function.Predicate;

import insomnia.data.ITree;

@FunctionalInterface
public interface IFTA<VAL, LBL> extends Predicate<ITree<VAL, LBL>>
{
	@Override
	boolean test(ITree<VAL, LBL> tree);

}
