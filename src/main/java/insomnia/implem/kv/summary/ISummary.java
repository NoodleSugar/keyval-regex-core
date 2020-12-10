package insomnia.kv.summary;


public interface ISummary
{
	/**
	 * getData must return the summary datas root under the form of an 
	 * abstract syntaxic tree with these constraints:
	 *   - Object  : Map
	 *   - Array   : List
	 *   - Key     : String
	 *   
	 *   Arrays must contain 0 or 1 element
	 *   Arrays can only contain an Object
	 */
	public Object getData();
}
