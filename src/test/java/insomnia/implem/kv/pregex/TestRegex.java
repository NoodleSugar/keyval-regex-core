package insomnia.implem.kv.pregex;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.implem.data.regex.parser.RegexParser;

class TestRegex
{
	public static void pathFromString(String regex) throws ParseException, IOException
	{
		new RegexParser("''\"\"").parse(regex);
	}

	static List<Object> error()
	{
		List<Object> a = Arrays.asList( //
			"a$._", //
			"(a.b$).v", //
			"(a$).v", //
			"(a|(a$)).v", //
			"^a.^b", //
			".^b", //

			"a{10 20}", //"
			"a{10,20.b", //
			"a.b.(c", //
			"(a))", //
			"/a", //
			"a.\"b", //

			"(a,b)(c,d)", //
			",,", //
			"(a,)", //
			"(,$).", //
			".(,^)", //
			"^{2}", //
			"^${2}" //
		);
		return a;
	}

	@ParameterizedTest
	@MethodSource
	void error(String regex)
	{
		assertThrows(ParseException.class, () -> pathFromString(regex));
	}
}
