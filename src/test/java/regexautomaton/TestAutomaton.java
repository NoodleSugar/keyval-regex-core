package regexautomaton;

import java.io.ByteArrayInputStream;

import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexToAutomatonConverter;
import insomnia.regex.element.IElement;

public class TestAutomaton
{

	public static void main(String[] args) throws Exception
	{
		String regex = "a*.b?.c+|(d.(e|f)[2,5]).~reg~";
		RegexParser parser = new RegexParser();
		IElement elements = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
		
		RegexAutomaton automaton = RegexToAutomatonConverter.convert(elements);
		System.out.println(automaton);
	}
}
