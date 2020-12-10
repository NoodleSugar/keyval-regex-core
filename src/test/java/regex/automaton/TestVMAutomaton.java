package regex.automaton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import insomnia.FSA.FSAException;
import insomnia.implem.kv.regex.RegexParser;
import insomnia.implem.kv.regex.automaton.VMRegexAutomaton;
import insomnia.implem.kv.regex.automaton.VMRegexAutomatonBuilder;
import insomnia.implem.kv.regex.element.IElement;

class TestVMAutomaton
{
	static String regex = "a*.b?.c+|(d.(e|f){2,5}).~r*e?g+~";

	static VMRegexAutomaton automaton;

	@BeforeAll
	static void init()
	{
		RegexParser             parser = new RegexParser();
		IElement                elements;
		VMRegexAutomatonBuilder builder;
		try
		{
			elements  = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
			builder   = new VMRegexAutomatonBuilder(elements);
			automaton = builder.build();
		}
		catch (IOException | ParseException e)
		{
			fail(e.getMessage());
		}
	}

	ArrayList<String> array(String path)
	{
		ArrayList<String> a = new ArrayList<String>();

		for (String s : path.split("\\."))
		{
			a.add(s);
		}
		return a;
	}

	@ParameterizedTest
	@ValueSource(strings = { "a.b.c", //
			"b.c.c.c.c.c", //
			"a.a.a.c.c", //
			"c", //
			"d.e.f.e.e.f.reg", //
			"d.f.f.gggg", //
			"d.e.e.rrrrrrreg", //
			"c" })
	void match(String s)
	{
		ArrayList<String> a = array(s);
		try
		{
			assertTrue(automaton.test(a));
		}
		catch (FSAException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "a.b.b.c", //
			"a.b", //
			"d.e.reg", //
			"e.e.reg", //
			"d.f.f.re", //
			"a.b.c.e" })
	void matchNot(String s)
	{
		ArrayList<String> a = array(s);
		try
		{
			assertFalse(automaton.test(a));
		}
		catch (FSAException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
}
