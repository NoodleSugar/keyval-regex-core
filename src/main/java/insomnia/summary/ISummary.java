package insomnia.summary;


public interface ISummary
{
	/**
	 * getData must return the data under this specific arborescent form:
	 *   - Object  : Map
	 *   - Array   : List
	 *   - Key     : String
	 *   
	 *   Array must contain 0 or 1 element
	 *   Array can contain only an Object
	 */
	public Object getData();
}
