package insomnia.implem.data.regex.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.Stack;

import org.apache.commons.io.IOUtils;

import insomnia.implem.data.regex.parser.IPRegexElement.Type;
import insomnia.implem.data.regex.parser.PLexer.LexerValue;
import insomnia.implem.data.regex.parser.PLexer.Token;
import insomnia.implem.data.regex.parser.PRegexElements.MultipleElement;
import insomnia.implem.data.regex.parser.PRegexElements.PRegexElement;

/**
 * A parser able to read tree regular expressions.
 * 
 * @see insomnia.implem.data.regex.parser
 * @author zuri
 * @author noodle
 */
public final class PRegexParser
{
	private enum ReaderState
	{
		END, //
		OR_ELEMENT, NODE_ELEMENT, ELEMENT, //
		OR_INIT, NODE_INIT, ELEMENT_INIT, //
		OR_ELEM_END, NODE_ELEM_END, ELEMENT_END, //
		ADD_IN_DISJUNCTION, ADD_IN_NODE, ADD_IN_SEQUENCE, //
		ROOT_CHECK_WORD, //
		QUANTIFIER, QUANT_END, QUANT_INF, QUANT_SUP, QUANT_SUP2, //
		TERMINAL, VALUE, VALUE2, //
		COMMA, CLOSE_PARENTH,
	};
	// ==========================================================================

	private Map<String, String> allDelimiters;
	private Collection<String>  nullLabels;

	// ==========================================================================

	/**
	 * Construct with empty delimiters and nullLabels contains
	 * <q>{@code _}</q>
	 */
	public PRegexParser()
	{
		allDelimiters = Collections.emptyMap();
		nullLabels    = Collections.singleton("_");
	}

	/**
	 * Create a regex parser where the delimiters are given as a String
	 * 
	 * @param delimiters contains pairs of char delimiters: <begin,end>.
	 */
	public PRegexParser(String delimiters)
	{
		this();
		setDelimiters(delimiters);
	}

	/**
	 * Create a regex parser with delimiters
	 * 
	 * @param valueDelimiters delimiters where a start char delimiter is associated with its end char delimiter
	 */
	public PRegexParser(Map<String, String> valueDelimiters)
	{
		this();
		setDelimiters(valueDelimiters);
	}

	// ==========================================================================

	/**
	 * Change the delimiters
	 * 
	 * @param delimiters contains pairs of char delimiters: <begin,end>.
	 */
	public PRegexParser setDelimiters(String delimiters)
	{
		assert (delimiters.length() % 2 == 0);
		this.allDelimiters = new HashMap<>();
		OfInt chars = delimiters.chars().iterator();

		while (chars.hasNext())
			this.allDelimiters.put(String.valueOf((char) chars.next().intValue()), String.valueOf((char) chars.next().intValue()));

		return this;
	}

	/**
	 * Change the delimiters
	 * 
	 * @param valueDelimiters delimiters where a start char delimiter is associated with its end char delimiter
	 */
	public PRegexParser setDelimiters(Map<String, String> delimiters)
	{
		this.allDelimiters = new HashMap<>(delimiters);
		return this;
	}

	public PRegexParser setNullLabels(Collection<String> labels)
	{
		nullLabels = new HashSet<>(labels);
		return this;
	}

	public Map<String, String> getDelimiters()
	{
		return Collections.unmodifiableMap(allDelimiters);
	}

	public Collection<String> getNullLabels()
	{
		return Collections.unmodifiableCollection(nullLabels);
	}

	// ==========================================================================

	public IPRegexElement parse(String regex) throws ParseException
	{
		try
		{
			return parse(IOUtils.toInputStream(regex, Charset.defaultCharset()));
		}
		catch (IOException e)
		{
			// Should never happens because the input is a String
			throw new AssertionError(e);
		}
	}

	public IPRegexElement parse(InputStream regexStream) throws IOException, ParseException
	{
		StringBuilder parsed = new StringBuilder();
		PLexer        lexer  = new PLexer(regexStream, allDelimiters);

		Stack<ReaderState>   readerStateStack = new Stack<>();
		Stack<PRegexElement> elementStack     = new Stack<>();

		readerStateStack.push(ReaderState.END);
		readerStateStack.push(ReaderState.NODE_INIT);

		ReaderState state;

		LexerValue v     = null;
		Token      token = null;
		String     data  = null;

		int quant_inf = 0;
		int quant_sup = 0;

		boolean skipLexer = false;
		while (true)
		{
			state = readerStateStack.pop();

			if (skipLexer)
				skipLexer = false;
			else
			{
				v     = lexer.nextToken();
				token = v.getToken();
				data  = v.getData();

				if (null != data)
					parsed.append(data).append(" ");
				else
					parsed.append(token).append(" ");
			}
			switch (state)
			{
			case NODE_INIT:
				elementStack.push(PRegexElements.createNode());
				readerStateStack.push(ReaderState.NODE_ELEMENT);
				skipLexer = true;
				break;
			case OR_INIT:
				elementStack.push(PRegexElements.createDisjunction());
				readerStateStack.push(ReaderState.OR_ELEMENT);
				skipLexer = true;
				break;
			case ELEMENT_INIT:
				elementStack.push(PRegexElements.createSequence());
				readerStateStack.push(ReaderState.ELEMENT);
				skipLexer = true;
				break;
			case NODE_ELEMENT:
				readerStateStack.push(ReaderState.NODE_ELEM_END);
				readerStateStack.push(ReaderState.OR_INIT);
				skipLexer = true;
				break;
			case OR_ELEMENT:
				readerStateStack.push(ReaderState.OR_ELEM_END);
				readerStateStack.push(ReaderState.ELEMENT_INIT);
				skipLexer = true;
				break;
			case ELEMENT:

				switch (token)
				{
				case WORD:
				{
					if (nullLabels.contains(data))
						data = null;
					PRegexElement element = PRegexElements.createKey(data);
					element.labelDelimiters = v.getDelimiters();
					elementStack.push(element);

					readerStateStack.push(ReaderState.ELEMENT_END);
					readerStateStack.push(ReaderState.TERMINAL);
					readerStateStack.push(ReaderState.QUANTIFIER);
					readerStateStack.push(ReaderState.VALUE);
					break;
				}
				case TERMINAL:
					elementStack.push(PRegexElements.createEmpty());
					readerStateStack.push(ReaderState.ADD_IN_SEQUENCE);
					readerStateStack.push(ReaderState.TERMINAL);
					skipLexer = true;
					break;
				case ROOTED:
					elementStack.push(PRegexElements.createEmpty(true, false));
					readerStateStack.push(ReaderState.ELEMENT_END);
					readerStateStack.push(ReaderState.TERMINAL);
					readerStateStack.push(ReaderState.VALUE);
					readerStateStack.push(ReaderState.ROOT_CHECK_WORD);
					break;
				case OPEN_PARENTH:
					readerStateStack.push(ReaderState.ELEMENT_END);
					readerStateStack.push(ReaderState.TERMINAL);
					readerStateStack.push(ReaderState.QUANTIFIER);
					readerStateStack.push(ReaderState.CLOSE_PARENTH);
					readerStateStack.push(ReaderState.NODE_INIT);
					break;
				default:
					elementStack.push(PRegexElements.createEmpty());
					readerStateStack.push(ReaderState.ELEMENT_END);
					readerStateStack.push(ReaderState.TERMINAL);
					readerStateStack.push(ReaderState.QUANTIFIER);
					readerStateStack.push(ReaderState.VALUE);
					skipLexer = true;
				}
				break;

			case ROOT_CHECK_WORD:
				if (token == Token.WORD || token == Token.OPEN_PARENTH)
				{
					int size = readerStateStack.size();
					readerStateStack.subList(size - 3, size).clear();
					readerStateStack.push(ReaderState.ELEMENT);
					readerStateStack.push(ReaderState.ADD_IN_SEQUENCE);
				}
				skipLexer = true;
				break;

			case TERMINAL:
				if (token == Token.TERMINAL)
				{
					PRegexElement element = elementStack.peek();
					element.setTerminal(true);
				}
				else
					skipLexer = true;
				break;

			case VALUE:
				if (token == Token.EQUALS)
					readerStateStack.push(ReaderState.VALUE2);
				else
					skipLexer = true;
				break;

			case VALUE2:
			{
				if (token != Token.WORD)
					throw new ParseException("Expected value", lexer.getOffset());
				PRegexElement element = elementStack.peek();
				element.setValue(data);
				element.valueDelimiters = v.getDelimiters();
				break;
			}

			case QUANTIFIER:
			{
				Quantifier q;

				if (token == Token.STAR)
					q = Quantifier.from(0, -1);
				else if (token == Token.PLUS)
					q = Quantifier.from(1, -1);
				else if (token == Token.QUESTION_MARK)
					q = Quantifier.from(0, 1);
				else if (token == Token.OPEN_BRACE)
				{
					readerStateStack.push(ReaderState.QUANT_SUP);
					readerStateStack.push(ReaderState.QUANT_INF);
					break;
				}
				else
				{
					skipLexer = true;
					break;
				}
				elementStack.peek().setQuantifier(q);
				break;
			}

			case QUANT_INF:
			{
				final String errorMsg = "Expected number";
				if (token != Token.WORD)
					throw new ParseException(errorMsg, lexer.getOffset());
				try
				{
					quant_inf = Integer.parseInt(data.trim());
				}
				catch (NumberFormatException e)
				{
					throw new ParseException(errorMsg + "; " + e.getMessage(), lexer.getOffset());
				}
				break;
			}

			case QUANT_SUP:
				if (token == Token.CLOSE_BRACE)
				{
					quant_sup = quant_inf;
					skipLexer = true;
					readerStateStack.push(ReaderState.QUANT_END);
				}
				else if (token == Token.COMMA)
				{
					readerStateStack.push(ReaderState.QUANT_END);
					readerStateStack.push(ReaderState.QUANT_SUP2);
				}
				else
					throw new ParseException("Expected number or '}'", lexer.getOffset());
				break;

			case QUANT_SUP2:
			{
				final String errorMsg = "Expected number";

				if (token != Token.WORD)
					throw new ParseException(errorMsg, lexer.getOffset());
				try
				{
					quant_sup = Integer.parseInt(data.trim());
				}
				catch (NumberFormatException e)
				{
					throw new ParseException(errorMsg + "; " + e.getMessage(), lexer.getOffset());
				}
				break;
			}

			case COMMA:
				if (token != Token.COMMA)
					throw new ParseException("Expected ','", lexer.getOffset());
				break;

			case QUANT_END:
				if (token != Token.CLOSE_BRACE)
					throw new ParseException("Expected '}'", lexer.getOffset());
				elementStack.peek().setQuantifier(Quantifier.from(quant_inf, quant_sup));
				break;

			case NODE_ELEM_END:
				if (token == Token.COMMA)
					readerStateStack.push(ReaderState.NODE_ELEMENT);
				else
					skipLexer = true;
				readerStateStack.push(ReaderState.ADD_IN_NODE);
				break;

			case ELEMENT_END:
				if (token == Token.POINT)
				{
					readerStateStack.push(ReaderState.ELEMENT);
				}
				else if (token == Token.OPEN_PARENTH)
				{
					readerStateStack.push(ReaderState.ADD_IN_SEQUENCE);
					readerStateStack.push(ReaderState.CLOSE_PARENTH);
					readerStateStack.push(ReaderState.NODE_INIT);
				}
				else
					skipLexer = true;

				readerStateStack.push(ReaderState.ADD_IN_SEQUENCE);
				break;

			case OR_ELEM_END:
				if (token == Token.VERTICAL_BAR)
					readerStateStack.push(ReaderState.OR_ELEMENT);
				else
					skipLexer = true;
				readerStateStack.push(ReaderState.ADD_IN_DISJUNCTION);
				break;

			case CLOSE_PARENTH:
				if (token != Token.CLOSE_PARENTH)
					throw new ParseException("Expected ')'", lexer.getOffset());
				break;

			case ADD_IN_NODE:
			{
				assert (elementStack.size() >= 2);
				skipLexer = true;
				PRegexElement element   = simplifyMultipleElement(elementStack.pop());
				PRegexElement container = elementStack.peek();

				assert (container.getType() == Type.NODE);
				int csize = container.getElements().size();
				container.getElements().add(element);

				if (csize > 0)
				{
					if (IPRegexElement.isEmpty(element))
						throw new ParseException(String.format("A node can only have one empty element at the first place, have %s", container), lexer.getOffset());
				}
				break;
			}

			case ADD_IN_SEQUENCE:
			{
				assert (elementStack.size() >= 2);
				skipLexer = true;
				PRegexElement element   = simplifyMultipleElement(elementStack.pop());
				PRegexElement container = elementStack.peek();
				int           csize     = container.getElements().size();

				assert (container.getType() == Type.SEQUENCE);

				if (!container.getElements().isEmpty())
				{
					if (element.hasRootedElement())
					{
						container.getElements().add(element);
						throw new ParseException(String.format("A rooted element must begin a sequence, have %s", container), lexer.getOffset());
					}
					// Check if the last container's element is a tree
					if (container.getElements().get(csize - 1).getType() == Type.NODE)
					{
						container.getElements().add(element);
						throw new ParseException(String.format("A sequence can have a unique terminal node, have %s", container), lexer.getOffset());
					}
					if (element.getType() != Type.EMPTY && container.hasTerminalElement())
					{
						container.getElements().add(element);
						throw new ParseException(String.format("A terminal element must end a sequence, have %s", container), lexer.getOffset());
					}
				}
				container.getElements().add(element);
				break;
			}

			case ADD_IN_DISJUNCTION:
			{
				assert (elementStack.size() >= 2);
				PRegexElement element   = simplifyMultipleElement(elementStack.pop());
				PRegexElement container = elementStack.peek();

				assert (container.getType() == Type.DISJUNCTION);
				container.getElements().add(element);
				skipLexer = true;
				break;
			}

			case END:
			{
				if (token != Token.END)
					throw new ParseException(String.format("Invalid regex: %s", parsed), lexer.getOffset());

				assert (elementStack.size() == 1);
				PRegexElement element = elementStack.pop();
				return simplifyMultipleElement(element);
			}
			default:
				break;
			}
		}

	}

	private PRegexElement simplifyMultipleElement(PRegexElement element)
	{
		if (element instanceof MultipleElement)
		{
			int size = element.getElements().size();

			if (size == 1)
			{
				Quantifier oldQ       = element.getQuantifier();
				boolean    isTerminal = element.isTerminal();
				element = (PRegexElement) element.getElements().get(0);
				element.setQuantifier(Quantifier.mul(oldQ, element.getQuantifier()));

				if (isTerminal)
					element.setTerminal(true);
			}
		}
		return element;
	}
}
