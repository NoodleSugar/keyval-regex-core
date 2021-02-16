package insomnia.data;

/**
 * An edge in a data.
 * 
 * @author zuri
 * @param <VAL> Type of the value (store in a node)
 * @param <LBL> Type of the label (store in an edge)
 */
public interface IEdge<VAL, LBL>
{
	LBL getLabel();

	INode<VAL, LBL> getParent();

	INode<VAL, LBL> getChild();

	// =========================================================================

	public static String toString(IEdge<?, ?> edge)
	{
		return new StringBuilder() //
			.append(edge.getParent()) //
			.append("--[").append(edge.getLabel()).append("]-->") //
			.append(edge.getChild()) //
			.toString();
	}
}
