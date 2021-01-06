package regex;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

import insomnia.implem.kv.pregex.PRegexParser;

class TestRegex
{
	PRegexParser parser = new PRegexParser();

	String regex;

	@Test
	void errorElement()
	{
		regex = "a.";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Expected element");
	}

	@Test
	void errorSymbol()
	{
		regex = "()";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Expected symbol");
	}

	@Test
	void errorNumber()
	{
		regex = "20e";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Expected number");
	}

	@Test
	void errorComma()
	{
		regex = "a[10 20]";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Expected ','");
	}

	@Test
	void errorCloseRBracket()
	{
		regex = "a[10,20.b";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Expected ']'");
	}

	@Test
	void errorCloseBracket()
	{
		regex = "a.b.(c";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Expected ')'");
	}

	@Test
	void errorInvalidRegex()
	{
		regex = "(a))";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Invalid regex");
	}

	@Test
	void errorInvalidCharacter()
	{
		regex = "/a";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "Invalid character '/'");
	}

	@Test
	void errorEOF()
	{
		regex = "a.\"b";
		assertThrows(ParseException.class, () -> parser.parse(new ByteArrayInputStream(regex.getBytes())), "EOF while reading word");
	}
}
