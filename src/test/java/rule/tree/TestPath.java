package rule.tree;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import insomnia.rule.tree.Path;

class TestPath
{
	@ParameterizedTest
	@CsvSource({
			"a.b, a.a.b"
	})
	void include(String h, String b)
	{
		Path head = new Path(h);
		Path body = new Path(b);
		assertTrue(head.isIncluded(body));
	}

}
