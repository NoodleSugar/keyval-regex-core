package insomnia.rule.tree;

import java.util.List;

import insomnia.rule.value.IValue;

public interface IPath extends ITree
{
	List<String> getKeys();
	
	boolean isValued();

	IValue getValue();
}