package insomnia.json;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

public class JsonParser
{
	/**
	 * Represents the type of element that the Lexer will send to the Parser
	 */
	private enum Token
	{
		END, STRING, NUMBER, LITERAL,//
		COMMA, COLON,//
		OPEN_RBRACKET, CLOSE_RBRACKET,//
		OPEN_BRACE, CLOSE_BRACE;
	}

	/**
	 * Contains the type and the data of the Lexer elements
	 */
	private class LexerValue
	{
		private Token token;
		private String data;

		public LexerValue(Token token, String data)
		{
			this.token = token;
			this.data = data;
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
		START, BACKSLASH, STRING, NUMBER, LITERAL;
	}

	private class Lexer
	{
		private InputStream jsonStream;
		private boolean readLastChar;
		private int lastChar;
		private int offset;

		public Lexer(InputStream jsonStream) throws IOException
		{
			this.jsonStream = jsonStream;
			readLastChar = false;
			lastChar = ' ';
			offset = 0;
		}

		private final Pattern NUMBER = Pattern.compile("\\-?([1-9][0-9]*|0)(.([0-9]*[1-9]|0))?([Ee][+-]?[1-9][0-9]*)?");

		private boolean isNumber(String s)
		{
			return NUMBER.matcher(s).matches();
		}

		private boolean isLiteral(String s)
		{
			return s.equals("true") || s.equals("false") || s.equals("null");
		}

		public LexerValue nextToken() throws IOException, ParseException
		{
			LexerState lexerState = LexerState.START;
			String data = "";

			char c;
			int d;
			while(true)
			{
				if(readLastChar)
				{
					d = lastChar;
					readLastChar = false;
				}
				else
				{
					d = jsonStream.read();
					offset++;
				}
				c = (char) d;

				switch(lexerState)
				{
				case START:
					if(d == -1)
						return new LexerValue(Token.END, null);
					if(c == ':')
						return new LexerValue(Token.COLON, null);
					if(c == ',')
						return new LexerValue(Token.COMMA, null);
					if(c == '[')
						return new LexerValue(Token.OPEN_RBRACKET, null);
					if(c == ']')
						return new LexerValue(Token.CLOSE_RBRACKET, null);
					if(c == '{')
						return new LexerValue(Token.OPEN_BRACE, null);
					if(c == '}')
						return new LexerValue(Token.CLOSE_BRACE, null);
					if(c == '"')
					{
						lexerState = LexerState.STRING;
						break;
					}
					if(Character.isDigit(c) || c == '-')
					{
						lexerState = LexerState.NUMBER;
						data += c;
						break;
					}
					if(Character.isLetter(c))
					{
						lexerState = LexerState.LITERAL;
						data += c;
						break;
					}
					if(c != ' ' && c != '\t' && c != '\n' && c != '\r')
						throw new ParseException("Invalid character '" + c + "'", offset);
					break;

				case BACKSLASH:
					if(d == -1)
						throw new ParseException("EOF while reading String value", offset);
					lexerState = LexerState.STRING;
					data += c;
					break;

				case STRING:
					if(d == -1)
						throw new ParseException("EOF while reading String value", offset);
					if(c == '\\')
					{
						lexerState = LexerState.BACKSLASH;
						break;
					}
					if(c != '"')
					{
						data += c;
						break;
					}
					return new LexerValue(Token.STRING, data);

				case NUMBER:
					if(Character.isDigit(c) || c == 'E' || c == 'e' || c == '.' || c == '-' || c == '+')
					{
						data += c;
						break;
					}
					if(isNumber(data))
					{
						readLastChar = true;
						lastChar = d;
						return new LexerValue(Token.NUMBER, data);
					}
					throw new ParseException("Invalid number syntax '" + data + "'", offset);

				case LITERAL:
					if(Character.isLetter(c))
					{
						data += c;
						break;
					}
					if(isLiteral(data))
					{
						readLastChar = true;
						lastChar = d;
						return new LexerValue(Token.LITERAL, data);
					}
					throw new ParseException("Unknown literal '" + data + "'", offset);

				default:
					throw new ParseException("Lexer Error", offset);
				}
			}
		}
	}

	private Double toNumber(String s)
	{
		return Double.parseDouble(s);
	}

	private Boolean toLiteral(String s) throws ParseException
	{
		if(s.equals("true"))
			return true;
		if(s.equals("false"))
			return false;
		if(s.equals("null"))
			return null;
		throw new ParseException("Invalid literal : " + s, 0);
	}

	/**
	 * The different states of the Parser automaton
	 */
	private enum ReaderState
	{
		END, KEY, VALUE, COLON,//
		OPEN_OBJECT, OPEN_ARRAY,//
		COMMA_OBJECT, COMMA_ARRAY,//
		STORE_IN_OBJECT, STORE_IN_ARRAY;
	};

	private final String ROOT_KEY = "ROOT_KEY";

	/**
	 * 
	 * @param jsonStream
	 * @return json datas that can be HashMap<String, Object> for json object
	 *         ArrayList<Object> String double boolean or null
	 * @throws ParseException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Object readJsonStream(InputStream jsonStream) throws ParseException, IOException
	{
		Lexer lexer = new Lexer(jsonStream);
		LinkedHashMap<String, Object> datas = new LinkedHashMap<String, Object>();

		ArrayDeque<ReaderState> readerStateStack = new ArrayDeque<ReaderState>();
		Stack<Object> dataStack = new Stack<Object>();

		dataStack.push(datas);
		dataStack.push(ROOT_KEY);

		readerStateStack.push(ReaderState.END);
		readerStateStack.push(ReaderState.STORE_IN_OBJECT);
		readerStateStack.push(ReaderState.VALUE);

		ReaderState state;

		Object val;
		String key;

		LexerValue v = null;
		Token token = null;
		String data = null;

		boolean skipLexer = false;
		while(true)
		{
			state = readerStateStack.pop();

			if(skipLexer)
				skipLexer = false;
			else
			{
				v = lexer.nextToken();
				token = v.getToken();
				data = v.getData();
			}

			switch(state)
			{
			case VALUE:
				if(token == Token.OPEN_BRACE)
					readerStateStack.push(ReaderState.OPEN_OBJECT);
				else if(token == Token.OPEN_RBRACKET)
					readerStateStack.push(ReaderState.OPEN_ARRAY);
				else if(token == Token.STRING)
					dataStack.push(data);
				else if(token == Token.NUMBER)
					dataStack.push(toNumber(data));
				else if(token == Token.LITERAL)
					dataStack.push(toLiteral(data));
				else
					throw new ParseException("Value expected", lexer.offset);
				break;

			case KEY:
				if(token != Token.STRING)
					throw new ParseException("Invalid key", lexer.offset);
				dataStack.push(data);
				break;

			case COLON:
				if(token != Token.COLON)
					throw new ParseException("Missing ':' after the key '" + dataStack.peek() + "'", lexer.offset);
				break;

			case OPEN_OBJECT:
				dataStack.push(new LinkedHashMap<String, Object>());
				if(token != Token.CLOSE_BRACE)
				{
					readerStateStack.push(ReaderState.COMMA_OBJECT);
					readerStateStack.push(ReaderState.STORE_IN_OBJECT);
					readerStateStack.push(ReaderState.VALUE);
					readerStateStack.push(ReaderState.COLON);
					readerStateStack.push(ReaderState.KEY);
					skipLexer = true;
				}
				break;

			case OPEN_ARRAY:
				dataStack.push(new ArrayList<Object>());
				if(token != Token.CLOSE_RBRACKET)
				{
					readerStateStack.push(ReaderState.COMMA_ARRAY);
					readerStateStack.push(ReaderState.STORE_IN_ARRAY);
					readerStateStack.push(ReaderState.VALUE);
					skipLexer = true;
				}
				break;

			case COMMA_OBJECT:
				if(token == Token.COMMA)
				{
					readerStateStack.push(ReaderState.COMMA_OBJECT);
					readerStateStack.push(ReaderState.STORE_IN_OBJECT);
					readerStateStack.push(ReaderState.VALUE);
					readerStateStack.push(ReaderState.COLON);
					readerStateStack.push(ReaderState.KEY);
				}
				else if(token != Token.CLOSE_BRACE)
					throw new ParseException("Expected ',' or '}'", lexer.offset);
				break;

			case COMMA_ARRAY:
				if(token == Token.COMMA)
				{
					readerStateStack.push(ReaderState.COMMA_ARRAY);
					readerStateStack.push(ReaderState.STORE_IN_ARRAY);
					readerStateStack.push(ReaderState.VALUE);
				}
				else if(token != Token.CLOSE_RBRACKET)
					throw new ParseException("Expected ',' or ']'", lexer.offset);
				break;

			case STORE_IN_OBJECT:
				val = dataStack.pop();
				key = (String) dataStack.pop();
				Map<String, Object> h = ((Map<String, Object>) dataStack.peek());
				if(h.containsKey(key))
					throw new ParseException("Key '" + key + "' already exists", lexer.offset);
				h.put(key, val);
				skipLexer = true;
				break;

			case STORE_IN_ARRAY:
				val = dataStack.pop();
				((List<Object>) dataStack.peek()).add(val);
				skipLexer = true;
				break;

			case END:
				if(token == Token.END)
					return datas.get(ROOT_KEY);
				throw new ParseException("Invalid Json stream", lexer.offset);

			default:
				throw new ParseException("An unknown error occured...", lexer.offset);
			}
		}
	}
}
