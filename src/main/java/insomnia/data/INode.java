package insomnia.data;

/**
 * A node in a data.
 * 
 * @author zuri
 * @param <VAL> Type of the value (store in a node)
 * @param <LBL> Type of the label (store in an edge)
 */
public interface INode<VAL, LBL>
{
	VAL getValue();

	void setValue(VAL value);
}
