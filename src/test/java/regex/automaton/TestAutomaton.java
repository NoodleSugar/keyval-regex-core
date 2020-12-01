package regex.automaton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import insomnia.automaton.AutomatonException;
import insomnia.automaton.algorithm.DeterministicValidation;
import insomnia.kv.regex.RegexParser;
import insomnia.kv.regex.automaton.RegexAutomaton;
import insomnia.kv.regex.automaton.RegexAutomatonBuilder;
import insomnia.kv.regex.automaton.RegexAutomatonBuilder.BuilderException;
import insomnia.kv.regex.element.IElement;

public class TestAutomaton
{
	static String regex = "a*.b?.c+|(d.(e|f)[2,5]).~r*e?g+~";
	static RegexAutomaton automaton;
	static RegexAutomaton dautomaton;
	static DeterministicValidation<String> valid = new DeterministicValidation<String>();

	@BeforeAll
	static void init()
	{
		RegexParser parser = new RegexParser();
		IElement elements;
		RegexAutomatonBuilder builder;
		try
		{
			elements = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
			builder = new RegexAutomatonBuilder(elements);
			automaton = builder.build();
			dautomaton = builder.determinize().build();
		}
		catch(IOException | ParseException | AutomatonException | BuilderException e)
		{
			fail(e.getMessage());
		}
	}

	ArrayList<String> array(String path)
	{
		ArrayList<String> a = new ArrayList<String>();

		for(String s : path.split("\\."))
		{
			a.add(s);
		}
		return a;
	}

	@Test
	void synch()
	{
		assertFalse(automaton.isSynchronous());
		assertTrue(dautomaton.isSynchronous());
	}

	@Test
	void deter()
	{
		assertFalse(automaton.isDeterministic());
		assertTrue(dautomaton.isDeterministic());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"a.b.c", //
			"b.c.c.c.c.c", //
			"a.a.a.c.c", //
			"c", //
			"d.e.f.e.e.f.reg", //
			"d.f.f.gggg", //
			"d.e.e.rrrrrrreg", //
			"c"
	})
	void match(String s)
	{
		try
		{
			ArrayList<String> a = array(s);
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"a.b.b.c", //
			"a.b", //
			"d.e.reg", //
			"e.e.reg", //
			"d.f.f.re", //
			"a.b.c.e"
	})
	void matchNot(String s)
	{
		try
		{
			ArrayList<String> a = array(s);
			assertFalse(automaton.run(a));
			assertFalse(dautomaton.run(a));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
}
