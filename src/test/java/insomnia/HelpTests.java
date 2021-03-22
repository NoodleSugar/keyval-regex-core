package insomnia;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;

public abstract class HelpTests
{
	private HelpTests()
	{
		throw new AssertionError();
	}

	// ==========================================================================

	public static Stream<Map<String, String>> loadResourceCSVAsMap(String... paths)
	{
		Map<String, String> map = new HashMap<>();

		return Arrays.stream(paths).flatMap(path -> {
			try
			{
				var resourceStream = HelpTests.class.getResourceAsStream(path);
				var records        = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(resourceStream));
				return StreamSupport.stream(records.spliterator(), false).map(r -> {
					var     current = r.toMap();
					boolean empty   = true;
					for (var k : current.keySet())
					{
						if (!current.get(k).isEmpty())
						{
							empty = false;
							map.put(k, current.get(k));
						}
					}
					if (empty)
						map.clear();
					return map;
				}).filter(r -> !r.isEmpty());
			}
			catch (Exception e)
			{
				throw new AssertionError(e);
			}
		});
	}
}
