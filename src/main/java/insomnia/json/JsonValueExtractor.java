package insomnia.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonValueExtractor
{
	static public ArrayList<Object> getValues(Object jsonData, List<String> paths)
	{
		ArrayList<Object> values = new ArrayList<>();
		for(String path : paths)
			getValues(jsonData, path, values);
		return values;
	}
	
	@SuppressWarnings("unchecked")
	static private void getValues(Object jsonData, String path, List<Object> values)
	{
		if(jsonData instanceof Map)
		{
			String[] splitted = path.split("\\.", 2);
			String word = splitted[0];
			
			Map<String, Object> map = (Map<String, Object>) jsonData;
			for(Map.Entry<String, Object> entry : map.entrySet())
			{
				if(entry.getKey().equals(word))
				{
					if(splitted.length > 1)
						getValues(entry.getValue(), splitted[1], values);
					else
						values.add(entry.getValue());
				}
			}
		}
		else if(jsonData instanceof List)
		{
			List<Object> list = (List<Object>) jsonData;
			for(Object obj : list)
				getValues(obj, path, values);
		}
	}
}
