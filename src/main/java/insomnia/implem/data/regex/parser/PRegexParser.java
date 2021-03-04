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
 * A parser able to read path regular expressions.
 * <p>
 * A valid regex is a sequence of elements delimited by 'point' characters: E<sub>1</sub>.E<sub>2</sub>. ... .E<sub>n</sub>.
 * An element E of a regex can contain a label, a value and a quantifier like: <code>"person=zuri{1,2}"</code>; each of its items can be absent which can result in an empty element.
 * The disjunction
 * <q>{@code |}</q> create a new element that will refer to the classic disjunction case, its priority is lower than the point
 * <q>{@code .}</q>.
 * Parenthesis can be used to define recursively an element containing more elements, and permits to change operators priority and to apply a quantifier.
 * </p>
 * <p>
 * A value or a label may be delimited by <em>delimiters</em>, which are pair of characters (begin/end) that can enclose them.
 * Delimiters can be set at the creation of the parser (see. {@link #PRegexParser(Map)}, {@link #PRegexParser(String)}).
 * When no delimiters are presents, the {@link PLexer} consider that a char belongs to a word if it verify {@code (c == '_' || Character.isLetterOrDigit(c) || Character.isSpaceChar(c))}.
 * </p>
 * <p>
 * If a regex contains only one element without label, then the represented path has only one node.
 * To specify a null label the parser must set the <em>nullLabels</em> informations ({@link #setNullLabels(Collection)}).
 * It's a collection of {@link String} that stores labels that are considered to be null.
 * By default the parser consider the label
 * <q>_</q> to a null label, so the regex:
 * <q>{@code _=1}</q> represents an edge labeled by a null followed by a node with the value of
 * <q>1</q>.
 * </p>
 * <h3>Some examples:</h3>
 * <ul>
 * <li>person.(name="zuri"|name="noodle"){1,3}</li>
 * <li>repeat=more{100}</li>
 * </ul>
 * </p>
 * <h2>Rooted/terminal</h2>
 * <p>
 * A path may be prefixed by {@code ^} to refer to a true data root.
 * In this case it is considered as an element and must be follow by a point if the root has to store its own value.
 * If {@code ^} has no relevant information to store than the point may be omitted.
 * An element may also be suffixed by {@code $} to refer to a true data leaf; it is called a terminal path.
 * <h2>Some examples:</h2>
 * <ul>
 * <li>^person</li>
 * <li>^=rootValue.person</li>
 * <li>name$</li>
 * <li>^.person.name=zuri$</li>
 * <li>^=150$</li>
 * <li>^$</li>
 * </ul>
 * 
 * @author zuri
 * @author noodle
 */
public final class PRegexParser
{
	private static final String EMSG_INVALID_REGEX = "Invalid regex";

	private enum ReaderState
	{
		END, //
		OR_ELEMENT, ELEMENT, //
		ADD_IN_SEQUENCE, ADD_IN_DISJUNCTION, //
		ROOT_CHECK_WORD, //
		TERMINAL, //
		VALUE, VALUE2, QUANTIFIER, QUANT_INF, QUANT_SUP, QUANT_SUP2, //
		POINT, VERTICAL_BAR, //
		COMMA, CLOSE_BRACE, CLOSE_PARENTH;
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
		PLexer lexer = new PLexer(regexStream, allDelimiters);

		Stack<ReaderState>   readerStateStack = new Stack<>();
		Stack<PRegexElement> elementStack     = new Stack<>();

		elementStack.push(PRegexElements.createDisjunction());
		readerStateStack.push(ReaderState.END);
		readerStateStack.push(ReaderState.OR_ELEMENT);

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
			}
			switch (state)
			{
			case OR_ELEMENT:
				readerStateStack.push(ReaderState.VERTICAL_BAR);
				readerStateStack.push(ReaderState.ELEMENT);
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

					readerStateStack.push(ReaderState.POINT);
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
					readerStateStack.push(ReaderState.POINT);
					readerStateStack.push(ReaderState.TERMINAL);
					readerStateStack.push(ReaderState.VALUE);
					readerStateStack.push(ReaderState.ROOT_CHECK_WORD);
					break;
				case OPEN_PARENTH:
					elementStack.push(PRegexElements.createDisjunction());
					readerStateStack.push(ReaderState.POINT);
					readerStateStack.push(ReaderState.QUANTIFIER);
					readerStateStack.push(ReaderState.CLOSE_PARENTH);
					readerStateStack.push(ReaderState.OR_ELEMENT);
					break;
				default:
					elementStack.push(PRegexElements.createEmpty());
					readerStateStack.push(ReaderState.POINT);
					readerStateStack.push(ReaderState.TERMINAL);
					readerStateStack.push(ReaderState.QUANTIFIER);
					readerStateStack.push(ReaderState.VALUE);
					skipLexer = true;
				}
				break;

			case ROOT_CHECK_WORD:
				if (token == Token.WORD)
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
					readerStateStack.push(ReaderState.CLOSE_BRACE);
				}
				else if (token == Token.COMMA)
				{
					readerStateStack.push(ReaderState.CLOSE_BRACE);
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

			case CLOSE_BRACE:
				if (token != Token.CLOSE_BRACE)
					throw new ParseException("Expected '}'", lexer.getOffset());
				elementStack.peek().setQuantifier(Quantifier.from(quant_inf, quant_sup));
				break;

			case POINT:
				if (token == Token.POINT)
				{
					if (elementStack.peek().isTerminal())
						throw new ParseException("A terminal element must end a sequence", lexer.getOffset());

					readerStateStack.push(ReaderState.ELEMENT);
				}
				else
					skipLexer = true;
				readerStateStack.push(ReaderState.ADD_IN_SEQUENCE);
				break;

			case VERTICAL_BAR:
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

			case ADD_IN_SEQUENCE:
			{
				skipLexer = true;
				PRegexElement element   = simplifyMultipleElement(elementStack.pop());
				PRegexElement container = elementStack.peek();

				if (container.getType() != Type.SEQUENCE)
				{
					container = PRegexElements.createSequence();
					elementStack.push(container);
				}
				if (!container.getElements().isEmpty())
				{
					if (element.isRooted())
						throw new ParseException("A rooted element must begin a sequence", lexer.getOffset());

					// transform the first element from 'empty' to 'key'
					if (container.getElements().size() == 1)
					{
						IPRegexElement first = container.getElements().get(0);
						if (first.getType() == Type.EMPTY && !first.isRooted())
							container.getElements().set(0, PRegexElements.createKey(first));
					}

					if (element.getType() == Type.EMPTY)
					{
						PRegexElement newElement = PRegexElements.createKey(element);
						newElement.setValue(element.getValue());
						element = newElement;
					}
				}
				container.getElements().add(element);

				if (element.isRooted())
					container.setRooted(true);
				if (element.isTerminal())
					container.setTerminal(true);
				break;
			}

			case ADD_IN_DISJUNCTION:
			{
				PRegexElement element = simplifyMultipleElement(elementStack.pop());

				if (elementStack.isEmpty())
					throw new ParseException(EMSG_INVALID_REGEX, lexer.getOffset());

				PRegexElement container = elementStack.peek();

				if (container.getType() != Type.DISJUNCTION)
					throw new ParseException(EMSG_INVALID_REGEX, lexer.getOffset());

				container.getElements().add(element);

				if (element.isTerminal())
					container.setTerminal(true);

				skipLexer = true;
				break;
			}

			case END:
			{
				if (token != Token.END)
					throw new ParseException("Invalid regex", lexer.getOffset());

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
				Quantifier oldQ = element.getQuantifier();
				element = (PRegexElement) element.getElements().get(0);
				element.setQuantifier(Quantifier.mul(oldQ, element.getQuantifier()));
			}
		}
		return element;
	}
}
