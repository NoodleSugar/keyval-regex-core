package regex.automaton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.pregex.IPRegexElement;
import insomnia.implem.kv.pregex.PRegexParser;
import insomnia.implem.kv.pregex.fsa.PRegexVMFSA;
import insomnia.implem.kv.pregex.fsa.PRegexVMFSABuilder;

class TestVMAutomaton
{
	static String regex = "a*.b?.c+|(d.(e|f){2,5}).~r*e?g+~";

	static PRegexVMFSA<KVValue, KVLabel> automaton;

	@BeforeAll
	static void init()
	{
		PRegexParser       parser = new PRegexParser();
		IPRegexElement     elements;
		PRegexVMFSABuilder builder;
		try
		{
			elements  = parser.parse(new ByteArrayInputStream(regex.getBytes()));
			builder   = new PRegexVMFSABuilder(elements);
			automaton = builder.build();
		}
		catch (IOException | ParseException e)
		{
			fail(e.getMessage());
		}
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
		assertTrue(automaton.test(KVPaths.pathFromString(s)));
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
		assertFalse(automaton.test(KVPaths.pathFromString(s)));
	}
}
