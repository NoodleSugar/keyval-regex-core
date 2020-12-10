package insomnia.kv.summary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Summary implements ISummary
{
	Object data;

	public static class Builder
	{
		Object root;
		ArrayDeque<Object> dataStack;

		public class BuilderException extends Exception
		{
			private static final long serialVersionUID = 1943576115610288468L;

			public BuilderException(String message)
			{
				super(message);
			}

			public BuilderException(Throwable cause)
			{
				super(cause);
			}

			public BuilderException(String message, Throwable cause)
			{
				super(message, cause);
			}
		}

		public enum RootType
		{
			OBJECT, ARRAY;
		}

		public Builder(RootType type)
		{
			dataStack = new ArrayDeque<>();
			if(type == RootType.OBJECT)
				root = new HashMap<>();
			else if(type == RootType.ARRAY)
				root = new ArrayList<>();

			if(root != null)
				dataStack.push(root);
		}

		public boolean goBack()
		{
			if(dataStack.size() > 1)
			{
				dataStack.pop();
				return true;
			}
			else
				return false;
		}

		// Add key into an object
		@SuppressWarnings("unchecked")
		public Builder addKey(String key) throws BuilderException
		{
			if(!(dataStack.peek() instanceof Map))
				throw new BuilderException("The current element is not an Object");

			Map<String, Object> map = (Map<String, Object>) dataStack.peek();

			if(!map.containsKey(key))
				map.put(key, null);

			return this;
		}

		// Add array into an object
		@SuppressWarnings("unchecked")
		public Builder addArray(String key) throws BuilderException
		{
			if(!(dataStack.peek() instanceof Map))
				throw new BuilderException("The current element is not an Object");

			Map<String, Object> object = (Map<String, Object>) dataStack.peek();
			
			Object oldValue = object.get(key);
			if(oldValue instanceof Map)
				throw new BuilderException("Inconsistent datas: same key '" + key + "' has type Object and Array");
			else if(oldValue == null)
			{
				ArrayList<Object> newArray = new ArrayList<>();
				object.put(key, newArray);
				dataStack.push(newArray);
			}
			else
				dataStack.push(oldValue);

			return this;
		}

		// Add object into an object
		@SuppressWarnings("unchecked")
		public Builder addObject(String key) throws BuilderException
		{
			if(!(dataStack.peek() instanceof Map))
				throw new BuilderException("The current element is not an Object");

			Map<String, Object> object = (Map<String, Object>) dataStack.peek();

			Object oldValue = object.get(key);
			if(oldValue instanceof List)
				throw new BuilderException("Inconsistent datas: same key '" + key + "' has type Array and Object");
			else if(oldValue == null)
			{
				HashMap<String, Object> newObject = new HashMap<>();
				object.put(key, newObject);
				dataStack.push(newObject);
			}
			else
				dataStack.push(oldValue);

			return this;
		}

		// Add object into an array
		@SuppressWarnings("unchecked")
		public Builder addObject() throws BuilderException
		{
			if(!(dataStack.peek() instanceof List))
				throw new BuilderException("The current element is not an Array");

			List<Object> array = (List<Object>) dataStack.peek();

			int size = array.size();
			if(size == 1)
			{
				Object oldValue = array.get(0);
				if(!(oldValue instanceof Map))
					throw new BuilderException("Unknow error made array in array");
				dataStack.push(oldValue);
			}
			else if(size == 0)
			{
				HashMap<String, Object> newObject = new HashMap<>();
				array.add(newObject);
				dataStack.push(newObject);
			}
			else
				throw new BuilderException("Unknow error made array with more than 1 element");

			return this;
		}

		public Summary build()
		{
			return new Summary(this);
		}
	}

	private Summary(Builder b)
	{
		data = b.root;
	}

	@Override
	public Object getData()
	{
		return data;
	}
}
