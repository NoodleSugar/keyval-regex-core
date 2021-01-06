package insomnia.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import insomnia.json.JsonParser;
import insomnia.json.JsonValueExtractor;
import insomnia.json.JsonWriter;

class TestJson
{
	JsonParser parser = new JsonParser();
	String file;

	@Test
	void errorValue()
	{
		file = "error_value.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Value expected");
	}

	@Test
	void errorKey()
	{
		file = "error_key.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Invalid key");
	}

	@Test
	void errorColon()
	{
		file = "error_colon.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Missing ':' after the key 'key'");
	}

	@Test
	void errorCloseObject()
	{
		file = "error_close_object.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Expected ',' or '}'");
	}

	@Test
	void errorCloseArray()
	{
		file = "error_close_array.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Expected ',' or ']'");
	}

	@Test
	void errorKeyExists()
	{
		file = "error_key_exists.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Key 'key' already exists");
	}

	@Test
	void errorInvalidJson()
	{
		file = "error_invalid_json.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Invalid Json stream");
	}

	@Test
	void errorInvalidCharacter()
	{
		file = "error_invalid_character.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Invalid character '\\'");
	}

	@Test
	void errorEOF()
	{
		file = "error_EOF.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"EOF while reading String value");
	}

	@Test
	void errorNumber()
	{
		file = "error_number.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Invalid number syntax '20e'");
	}

	@Test
	void errorLiterral()
	{
		file = "error_literral.json";
		assertThrows(ParseException.class, () -> parser.readJsonStream(this.getClass().getResourceAsStream(file)),
				"Unknown literal 'TRUE'");
	}

	@Test
	void valid() throws ParseException, IOException
	{
		String jsonData = "{\"key\":\"value\",\"key2\":[true,false,20.0],\"key3\":{\"object\":\"blabla\"}}";
		ByteArrayInputStream jsonStream = new ByteArrayInputStream(jsonData.getBytes());
		Object data = parser.readJsonStream(jsonStream);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonWriter.writeJson(out, data, true);
		assertEquals(jsonData, out.toString());
	}

	@Test
	void values() throws ParseException, IOException
	{
		file = "values.json";
		Object data = parser.readJsonStream(this.getClass().getResourceAsStream(file));
		ArrayList<String> paths = new ArrayList<>();
		paths.add("object.array.key");
		ArrayList<Object> values = JsonValueExtractor.getValues(data, paths);
		assertEquals("Pouet", values.get(0));
		assertEquals(true, values.get(1));
	}
}
