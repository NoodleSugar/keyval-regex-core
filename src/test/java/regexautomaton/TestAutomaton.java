package regexautomaton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import insomnia.automaton.AutomatonException;
import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexAutomatonBuilder.BuilderException;
import insomnia.regex.automaton.RegexToAutomatonConverter;
import insomnia.regex.element.IElement;

public class TestAutomaton
{
	static String regex = "a*.b?.c+|(d.(e|f)[2,5]).~r*e?g+~";
	static RegexAutomaton automaton;
	
	@BeforeAll
	static void init()
	{
		RegexParser parser = new RegexParser();
		IElement elements;
		try
		{
			elements = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
			automaton = RegexToAutomatonConverter.convert(elements);
			System.out.println(elements);
			System.out.println(automaton);
		}
		catch(IOException | ParseException | AutomatonException | BuilderException e)
		{
			fail(e.getMessage());
		}
	}
	
	@Test
	void match1()
	{
		try
		{
			assertTrue(automaton.run("a.b.c"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match2()
	{
		try
		{
			assertTrue(automaton.run("b.c.c.c.c.c"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match3()
	{
		try
		{
			assertTrue(automaton.run("a.a.a.c.c"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match4()
	{
		try
		{
			assertTrue(automaton.run("c"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match5()
	{
		try
		{
			assertTrue(automaton.run("d.e.f.e.e.f.reg"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match6()
	{
		try
		{
			assertTrue(automaton.run("d.f.f.gggg"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match7()
	{
		try
		{
			assertTrue(automaton.run("d.e.e.rrrrrrreg"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match8()
	{
		try
		{
			assertTrue(automaton.run("c"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match9()
	{
		try
		{
			assertFalse(automaton.run("a.b.b.c"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match10()
	{
		try
		{
			assertFalse(automaton.run("a.b"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match11()
	{
		try
		{
			assertFalse(automaton.run("d.e.reg"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match12()
	{
		try
		{
			assertFalse(automaton.run("e.e.reg"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
	
	@Test
	void match13()
	{
		try
		{
			assertFalse(automaton.run("d.f.f.re"));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
}
