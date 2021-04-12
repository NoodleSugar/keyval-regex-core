package insomnia.data;

import java.util.Collection;

public interface ITreeErasure<VAL, LBL>
{
	ITree<VAL, LBL> original();

	ITree<VAL, LBL> erase();

	ITree<VAL, LBL> top();

	Collection<ITree<VAL, LBL>> side();

	Collection<ITree<VAL, LBL>> bottom();
}
