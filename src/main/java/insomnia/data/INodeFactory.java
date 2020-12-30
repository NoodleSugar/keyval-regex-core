package insomnia.data;

public interface INodeFactory<VAL, LBL>
{
	INode<VAL, LBL> get();

	INode<VAL, LBL> get(VAL value);

	INode<VAL, LBL> get(INode<VAL, LBL> parent);

	INode<VAL, LBL> get(INode<VAL, LBL> parent, VAL value);
}
