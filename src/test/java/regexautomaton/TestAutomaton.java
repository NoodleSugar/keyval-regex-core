package regexautomaton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import insomnia.automaton.AutomatonException;
import insomnia.automaton.algorithm.DeterministicValidation;
import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexAutomatonBuilder;
import insomnia.regex.automaton.RegexAutomatonBuilder.BuilderException;
import insomnia.regex.element.IElement;

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
		
		for(String s : 	path.split("\\."))
		{
			a.add(s);
		}
		return a;
	}
	
	@Test
	void match1()
	{
		try
		{
			ArrayList<String> a = array("a.b.c");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("b.c.c.c.c.c");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("a.a.a.c.c");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("c");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("d.e.f.e.e.f.reg");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("d.f.f.gggg");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("d.e.e.rrrrrrreg");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("c");
			assertTrue(automaton.run(a));
			assertTrue(dautomaton.run(a));
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
			ArrayList<String> a = array("a.b.b.c");
			assertFalse(automaton.run(a));
			assertFalse(dautomaton.run(a));
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
			ArrayList<String> a = array("a.b");
			assertFalse(automaton.run(a));
			assertFalse(dautomaton.run(a));
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
			ArrayList<String> a = array("d.e.reg");
			assertFalse(automaton.run(a));
			assertFalse(dautomaton.run(a));
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
			ArrayList<String> a = array("e.e.reg");
			assertFalse(automaton.run(a));
			assertFalse(dautomaton.run(a));
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
			ArrayList<String> a = array("d.f.f.re");
			assertFalse(automaton.run(a));
			assertFalse(dautomaton.run(a));
		}
		catch(AutomatonException e)
		{
			fail("Regex Automaton Run Error : " + e.getMessage());
		}
	}
}
