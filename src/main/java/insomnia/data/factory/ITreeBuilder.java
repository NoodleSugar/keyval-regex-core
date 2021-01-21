package insomnia.data.factory;

import insomnia.data.ITree;

public interface ITreeBuilder<VAL, LBL> extends ITree<VAL, LBL>
{
	ITreeBuilder<VAL, LBL> setRooted(boolean rooted);

	@Override
	boolean isRooted();

	ITreeBuilder<VAL, LBL> add(LBL label);

	/**
	 * End on an existential leaf.
	 */
	ITreeBuilder<VAL, LBL> end();

	/**
	 * End on a terminal valued leaf.
	 */
	ITreeBuilder<VAL, LBL> endTerminal(VAL value);

	/**
	 * End on a terminal leaf.
	 */
	ITreeBuilder<VAL, LBL> endTerminal();

	/**
	 * Set the builder to an empty tree.
	 */
	ITreeBuilder<VAL, LBL> clear();

	// =========================================================================
}
