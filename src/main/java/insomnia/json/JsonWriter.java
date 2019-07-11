package insomnia.json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class JsonWriter
{
	/**
	 * 
	 * @param stream
	 * @param jsonDatas with the same types that JsonParser uses
	 * @throws IOException
	 */
	public static void writeJson(OutputStream stream, Object jsonDatas, boolean compact) throws IOException
	{
		if(compact)
			writeJsonCompact(stream, jsonDatas);
		else
			writeJson(stream, jsonDatas, 0);
	}

	@SuppressWarnings("unchecked")
	private static void writeJson(OutputStream stream, Object o, int depth) throws IOException
	{
		String space = StringUtils.repeat("\t", depth);
		String newSpace = space + "\t";

		if(o instanceof Map)
		{
			Map<String, Object> h = (Map<String, Object>) o;
			if(h.size() > 0)
			{
				boolean firstTurn = true;
				stream.write(("\n" + space + "{\n").getBytes(StandardCharsets.UTF_8));
				for(Entry<String, Object> entry : h.entrySet())
				{
					if(!firstTurn)
						stream.write((",\n").getBytes(StandardCharsets.UTF_8));
					else
						firstTurn = false;

					stream.write(
							(newSpace + "\"" + entry.getKey().replaceAll("\"", "\\\\\"").replaceAll("\\\\", "\\\\\\\\")
									+ "\"" + " : ").getBytes(StandardCharsets.UTF_8));
					writeJson(stream, entry.getValue(), depth + 1);
				}
				stream.write(("\n" + space + "}").getBytes(StandardCharsets.UTF_8));
			}
			else
				stream.write(("{}").getBytes(StandardCharsets.UTF_8));

		}
		else if(o instanceof List)
		{
			List<Object> array = (List<Object>) o;

			boolean firstTurn = true;
			stream.write(("[").getBytes(StandardCharsets.UTF_8));
			for(Object elt : array)
			{
				if(!firstTurn)
					stream.write((", ").getBytes(StandardCharsets.UTF_8));
				else
					firstTurn = false;
				writeJson(stream, elt, depth + 1);
			}
			stream.write(("]").getBytes(StandardCharsets.UTF_8));
		}
		else if(o instanceof String)
		{
			stream.write(("\"" + o.toString() + "\"").getBytes(StandardCharsets.UTF_8));
		}
		else if(o == null)
			stream.write("null".getBytes(StandardCharsets.UTF_8));
		else
			stream.write(o.toString().getBytes(StandardCharsets.UTF_8));
	}

	@SuppressWarnings("unchecked")
	private static void writeJsonCompact(OutputStream stream, Object o) throws IOException
	{
		if(o instanceof Map)
		{
			Map<String, Object> h = (Map<String, Object>) o;
			if(h.size() > 0)
			{
				boolean firstTurn = true;
				stream.write(("{").getBytes(StandardCharsets.UTF_8));
				for(Entry<String, Object> entry : h.entrySet())
				{
					if(!firstTurn)
						stream.write((",").getBytes(StandardCharsets.UTF_8));
					else
						firstTurn = false;

					stream.write(("\"" + entry.getKey().replaceAll("\"", "\\\\\"").replaceAll("\\\\", "\\\\\\\\") + "\""
							+ ":").getBytes(StandardCharsets.UTF_8));
					writeJsonCompact(stream, entry.getValue());
				}
				stream.write(("}").getBytes(StandardCharsets.UTF_8));
			}
			else
				stream.write(("{}").getBytes(StandardCharsets.UTF_8));

		}
		else if(o instanceof List)
		{
			List<Object> array = (List<Object>) o;

			boolean firstTurn = true;
			stream.write(("[").getBytes(StandardCharsets.UTF_8));
			for(Object elt : array)
			{
				if(!firstTurn)
					stream.write((",").getBytes(StandardCharsets.UTF_8));
				else
					firstTurn = false;
				writeJsonCompact(stream, elt);
			}
			stream.write(("]").getBytes(StandardCharsets.UTF_8));
		}
		else if(o instanceof String)
		{
			stream.write(("\"" + o.toString() + "\"").getBytes(StandardCharsets.UTF_8));
		}
		else if(o == null)
			stream.write("null".getBytes(StandardCharsets.UTF_8));
		else
			stream.write(o.toString().getBytes(StandardCharsets.UTF_8));
	}
}
