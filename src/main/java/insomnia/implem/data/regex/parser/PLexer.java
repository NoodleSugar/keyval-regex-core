package insomnia.implem.data.regex.parser;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

class PLexer
{
	/**
	 * Represents the type of element that the Lexer will send to the Parser
	 */
	enum Token
	{
		END, WORD, //
		POINT, VERTICAL_BAR, //
		OPEN_PARENTH, CLOSE_PARENTH, //
		OPEN_BRACE, COMMA, CLOSE_BRACE, //
		ROOTED, TERMINAL, STAR, PLUS, QUESTION_MARK, EQUALS;
	}

	/**
	 * Contains the type and the data of the Lexer elements
	 */
	class LexerValue
	{
		private Token  token;
		private String data;
		private String delimiters;

		public LexerValue(Token token, String data, String delimiters)
		{
			this.token      = token;
			this.data       = data;
			this.delimiters = delimiters;
		}

		public LexerValue(Token token, String data)
		{
			this.token      = token;
			this.data       = data;
			this.delimiters = "";
		}

		public LexerValue(Token token)
		{
			this.token      = token;
			this.data       = null;
			this.delimiters = null;
		}

		public Token getToken()
		{
			return token;
		}

		public String getData()
		{
			return data;
		}

		public String getDelimiters()
		{
			return delimiters;
		}
	}

	private StringBuilder sb;
	private InputStream   regexStream;
	private char          lastChar;
	private int           offset;

	private Map<String, String> delimiters;

	public PLexer(InputStream input, Map<String, String> delimiters) throws IOException
	{
		this.regexStream = input;
		this.delimiters  = delimiters;
		lastChar         = 0;
		offset           = 0;
		sb               = new StringBuilder();
	}

	public void resetInput(InputStream input)
	{
		regexStream = input;
		offset      = 0;
	}

	public int getOffset()
	{
		return offset;
	}

	private void escapeChar() throws IOException
	{
		int c = regexStream.read();
		offset++;

		if (c == -1)
			return;

		sb.append(c);
	}

	private void consumeDelimitedWord(char endDelimiter) throws IOException, ParseException
	{
		char c;
		for (;;)
		{
			int i = regexStream.read();
			offset++;
			if (i == -1)
				throw new ParseException("Expected a " + endDelimiter + " end delimiter", offset);
			c = (char) i;
			if (c == '\\')
				escapeChar();
			else if (c == endDelimiter)
				return;
			else
				sb.append(c);
		}
	}

	private boolean isWordChar(char c)
	{
		return c == '_' || Character.isLetterOrDigit(c) || Character.isSpaceChar(c);
	}

	private void consumeWord() throws IOException
	{
		char c;
		for (;;)
		{
			int i = regexStream.read();
			offset++;
			if (i == -1)
				return;
			c = (char) i;
			if (c == '\\')
				escapeChar();
			else if (isWordChar(c))
				sb.append(c);
			else
				break;
		}
		lastChar = c;
		return;
	}

	public LexerValue nextToken() throws IOException, ParseException
	{
		sb.setLength(0);
		String currentDelimiters;
		int    i;
		char   c;

		for (;;)
		{
			if (0 != lastChar)
			{
				i        = lastChar;
				lastChar = 0;
			}
			else
			{
				i = regexStream.read();
				offset++;
			}
			if (i == -1)
				return new LexerValue(Token.END);
			c = (char) i;
			if (c == '^')
				return new LexerValue(Token.ROOTED);
			if (c == '$')
				return new LexerValue(Token.TERMINAL);
			if (c == '(')
				return new LexerValue(Token.OPEN_PARENTH);
			if (c == ')')
				return new LexerValue(Token.CLOSE_PARENTH);
			if (c == '+')
				return new LexerValue(Token.PLUS);
			if (c == '*')
				return new LexerValue(Token.STAR);
			if (c == '?')
				return new LexerValue(Token.QUESTION_MARK);
			if (c == '|')
				return new LexerValue(Token.VERTICAL_BAR);
			if (c == '.')
				return new LexerValue(Token.POINT);
			if (c == '{')
				return new LexerValue(Token.OPEN_BRACE);
			if (c == '}')
				return new LexerValue(Token.CLOSE_BRACE);
			if (c == ',')
				return new LexerValue(Token.COMMA);
			if (c == '=')
				return new LexerValue(Token.EQUALS);

			String endDelimiter   = "\0";
			String startDelimiter = String.valueOf(c);
			if (null != (endDelimiter = delimiters.get(startDelimiter)))
			{
				currentDelimiters = startDelimiter + endDelimiter;
				consumeDelimitedWord(endDelimiter.charAt(0));
			}
			else if (c == '\\')
			{
				currentDelimiters = "";
				escapeChar();
				consumeWord();
			}
			else if (isWordChar(c))
			{
				currentDelimiters = "";
				sb.append(c);
				consumeWord();
			}
			else
				throw new ParseException("(Lexer) Unexpected char: " + c, offset);

			return new LexerValue(Token.WORD, sb.toString(), currentDelimiters);
		}
	}
}
