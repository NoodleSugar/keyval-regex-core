package insomnia.implem.kv.regex;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayDeque;

import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.regex.element.IElement;
import insomnia.implem.kv.regex.element.Key;
import insomnia.implem.kv.regex.element.MultipleElement;
import insomnia.implem.kv.regex.element.OrElement;
import insomnia.implem.kv.regex.element.Quantifier;
import insomnia.implem.kv.regex.element.Regex;
import insomnia.implem.kv.regex.element.Value;

public class RegexParser
{
	/**
	 * Represents the type of element that the Lexer will send to the Parser
	 */
	private enum Token
	{
		END, WORD, REGEX, //
		POINT, VERTICAL_BAR, //
		OPEN_PARENTH, CLOSE_PARENTH, //
		OPEN_BRACE, COMMA, CLOSE_BRACE, NUMBER, //
		STAR, PLUS, QUESTION_MARK;
	}

	/**
	 * Contains the type and the data of the Lexer elements
	 */
	private class LexerValue
	{
		private Token  token;
		private String data;

		public LexerValue(Token token, String data)
		{
			this.token = token;
			this.data  = data;
		}

		public Token getToken()
		{
			return token;
		}

		public String getData()
		{
			return data;
		}
	}

	/**
	 * The different states of the Lexer automaton
	 */
	private enum LexerState
	{
		START, WORD, DELIMITED_WORD, REGEX, NUMBER, BACKSLASH;
	}

	private class Lexer
	{
		private InputStream regexStream;
		private boolean     readLastChar;
		private int         lastChar;
		private int         offset;

		public Lexer(InputStream regexStream) throws IOException
		{
			this.regexStream = regexStream;
			readLastChar     = false;
			lastChar         = ' ';
			offset           = 0;
		}

		public LexerValue nextToken() throws IOException, ParseException
		{
			LexerState lexerState = LexerState.START;
			LexerState wordState  = null;
			String     data       = "";

			char c;
			int  d;
			while (true)
			{
				if (readLastChar)
				{
					d            = lastChar;
					readLastChar = false;
				}
				else
				{
					d = regexStream.read();
					offset++;
				}
				c = (char) d;

				switch (lexerState)
				{
				case START:
					if (d == -1)
						return new LexerValue(Token.END, null);
					if (c == '(')
						return new LexerValue(Token.OPEN_PARENTH, null);
					if (c == ')')
						return new LexerValue(Token.CLOSE_PARENTH, null);
					if (c == '+')
						return new LexerValue(Token.PLUS, null);
					if (c == '*')
						return new LexerValue(Token.STAR, null);
					if (c == '?')
						return new LexerValue(Token.QUESTION_MARK, null);
					if (c == '|')
						return new LexerValue(Token.VERTICAL_BAR, null);
					if (c == '.')
						return new LexerValue(Token.POINT, null);
					if (c == '{')
						return new LexerValue(Token.OPEN_BRACE, null);
					if (c == '}')
						return new LexerValue(Token.CLOSE_BRACE, null);
					if (c == ',')
						return new LexerValue(Token.COMMA, null);
					if (c == '"')
					{
						lexerState = LexerState.DELIMITED_WORD;
						wordState  = LexerState.DELIMITED_WORD;
						break;
					}
					if (c == '~')
					{
						lexerState = LexerState.REGEX;
						wordState  = LexerState.REGEX;
						break;
					}
					if (c == '\\')
					{
						lexerState = LexerState.BACKSLASH;
						wordState  = LexerState.WORD;
						break;
					}
					if (Character.isLetter(c))
					{
						lexerState  = LexerState.WORD;
						wordState   = LexerState.WORD;
						data       += c;
						break;
					}
					if (Character.isDigit(c) || c == '-')
					{
						lexerState  = LexerState.NUMBER;
						data       += c;
						break;
					}
					throw new ParseException("Invalid character '" + c + "'", offset);

				case NUMBER:
					if (Character.isDigit(c))
					{
						data += c;
						break;
					}
					readLastChar = true;
					lastChar = d;
					return new LexerValue(Token.NUMBER, data);

				case WORD:
					if (c == '\\')
					{
						lexerState = LexerState.BACKSLASH;
						break;
					}
					if (Character.isLetter(c) || c == '_' || c == '-' || c == '\\' || c == '"' || c == '\'' || c == '/')
					{
						data += c;
						break;
					}
					readLastChar = true;
					lastChar = d;
					return new LexerValue(Token.WORD, data);

				case DELIMITED_WORD:
					if (d == -1)
						throw new ParseException("EOF while reading word", offset);
					if (c == '\\')
					{
						lexerState = LexerState.BACKSLASH;
						break;
					}
					if (c != '"')
					{
						data += c;
						break;
					}
					return new LexerValue(Token.WORD, data);
				case REGEX:
					if (c == '\\')
					{
						lexerState = LexerState.BACKSLASH;
						break;
					}
					if (c != '~')
					{
						data += c;
						break;
					}
					return new LexerValue(Token.REGEX, data);

				case BACKSLASH:
					if (d == -1)
						throw new ParseException("EOF while reading String value", offset);
					lexerState = wordState;
					data += c;
					break;

				default:
					throw new ParseException("An unknown error occured", offset);
				}
			}

		}

	}

	private enum ReaderState
	{
		END, STORE, //
		OR_ELEMENT, MULTI_ELEMENT, ELEMENT, //
		SYMBOL, QUANTIFIER, QUANT_INF, QUANT_SUP, QUANT_SUP2, //
		POINT, VERTICAL_BAR, //
		COMMA, CLOSE_BRACE, CLOSE_PARENTH;
	};

	public IElement readRegexStream(InputStream regexStream, KVValue value) throws IOException, ParseException
	{
		IElement        elts = readRegexStream(regexStream);
		MultipleElement e    = new MultipleElement();
		e.add(elts);
		e.add(new Value(value));
		return e;
	}

	public IElement readRegexStream(InputStream regexStream) throws IOException, ParseException
	{
		Lexer lexer = new Lexer(regexStream);

		ArrayDeque<ReaderState> readerStateStack = new ArrayDeque<ReaderState>();
		ArrayDeque<IElement>    elementStack     = new ArrayDeque<IElement>();

		readerStateStack.push(ReaderState.END);
		readerStateStack.push(ReaderState.OR_ELEMENT);

		OrElement or = new OrElement();
		or.setQuantifier(Quantifier.from(1, 1));
		elementStack.push(or);

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
				readerStateStack.push(ReaderState.MULTI_ELEMENT);
				skipLexer = true;
				break;

			case MULTI_ELEMENT:
				if (elementStack.peek() instanceof OrElement)
				{
					MultipleElement m = new MultipleElement();
					m.setQuantifier(Quantifier.from(1, 1));

					elementStack.push(m);
					readerStateStack.push(ReaderState.STORE);
				}
				readerStateStack.push(ReaderState.POINT);
				readerStateStack.push(ReaderState.ELEMENT);
				skipLexer = true;
				break;

			case ELEMENT:
				if (token == Token.WORD || token == Token.REGEX)
				{
					readerStateStack.push(ReaderState.STORE);
					readerStateStack.push(ReaderState.QUANTIFIER);
					readerStateStack.push(ReaderState.SYMBOL);
					skipLexer = true;
					break;
				}
				else if (token == Token.OPEN_PARENTH)
				{
					OrElement o = new OrElement();
					o.setQuantifier(Quantifier.from(1, 1));

					elementStack.push(o);
					readerStateStack.push(ReaderState.CLOSE_PARENTH);
					readerStateStack.push(ReaderState.OR_ELEMENT);
					break;
				}
				throw new ParseException("Expected element", lexer.offset);

			case SYMBOL:
				if (token == Token.WORD)
				{
					elementStack.push(new Key(data));
					break;
				}
				else if (token == Token.REGEX)
				{
					elementStack.push(new Regex(data));
					break;
				}
				throw new ParseException("Expected symbol", lexer.offset);

			case QUANTIFIER:
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
					q         = Quantifier.from(1, 1);
					skipLexer = true;
				}
				elementStack.peek().setQuantifier(q);

				break;

			case QUANT_INF:
				if (token != Token.NUMBER)
					throw new ParseException("Expected number", lexer.offset);
				quant_inf = Integer.parseInt(data);
				break;

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
					throw new ParseException("Expected number or '}'", lexer.offset);
				break;

			case QUANT_SUP2:
				if (token != Token.NUMBER)
					throw new ParseException("Expected number", lexer.offset);

				quant_sup = Integer.parseInt(data);
				break;

			case COMMA:
				if (token != Token.COMMA)
					throw new ParseException("Expected ','", lexer.offset);
				break;

			case CLOSE_BRACE:
				if (token != Token.CLOSE_BRACE)
					throw new ParseException("Expected '}'", lexer.offset);
				elementStack.peek().setQuantifier(Quantifier.from(quant_inf, quant_sup));
				break;

			case POINT:
				if (token == Token.POINT)
					readerStateStack.push(ReaderState.MULTI_ELEMENT);
				else
					skipLexer = true;
				break;

			case VERTICAL_BAR:
				if (token == Token.VERTICAL_BAR)
					readerStateStack.push(ReaderState.OR_ELEMENT);
				else
					skipLexer = true;
				break;

			case CLOSE_PARENTH:
				if (token != Token.CLOSE_PARENTH)
					throw new ParseException("Expected ')'", lexer.offset);
				readerStateStack.push(ReaderState.STORE);
				readerStateStack.push(ReaderState.QUANTIFIER);
				break;

			case STORE:
				IElement element = elementStack.pop();
				MultipleElement container = (MultipleElement) elementStack.peek();
				boolean addElt = true;

				// Si le conteneur n'a qu'un élément
				if (element instanceof MultipleElement)
				{
					MultipleElement top  = (MultipleElement) element;
					Quantifier      oldQ = top.getQuantifier();
					if (top.size() == 1)
					{
						element = top.toArray(new IElement[] {})[0];
						element.setQuantifier(Quantifier.multiplication(oldQ, element.getQuantifier()));
					}
				}

				if (element.getQuantifier() == Quantifier.from(1, 1) && element instanceof MultipleElement)
				{
					MultipleElement top = (MultipleElement) element;
					if (element.getClass() == container.getClass())
					{
						addElt = false;
						for (IElement e : top)
							container.add(e);
					}
				}
				if (addElt)
					container.add(element);

				skipLexer = true;
				break;

			case END:
				if (token != Token.END)
					throw new ParseException("Invalid regex", lexer.offset);

				IElement elt = elementStack.pop();
				if (elt instanceof MultipleElement && ((MultipleElement) elt).size() == 1)
					return ((MultipleElement) elt).toArray(new IElement[] {})[0];

				return elt;

			default:
				break;
			}
		}
	}
}
