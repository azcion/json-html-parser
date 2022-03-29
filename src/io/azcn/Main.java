package io.azcn;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class Main {

	private static final String _indentation = "\t";
	private static StringBuilder _res;

	public static void main(String[] args) {
		_res = new StringBuilder();
		String inFile = "helloWorld";

		try {
			Path jsonPath = Path.of(inFile + ".json");
			String json = Files.readString(jsonPath);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);

			parse(rootNode);
			String result = _res.toString();

			System.out.println(result);
			//Path htmlPath = Path.of(inFile + ".html");
			//Files.writeString(htmlPath, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parse(JsonNode node) {
		if (!node.has("language")) {
			_res.append("<html>\n");
		}

		traverse(node, 1);
		_res.append("</html>");
	}

	private static void traverse(JsonNode node, int depth) {
		if (!node.isObject()) {
			return;
		}

		Iterator<String> fieldNames = node.getFieldNames();

		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();

			if (fieldName.equals("attributes")) {
				// Already parsed
				continue;
			}

			String indent = _indentation.repeat(depth);
			JsonNode fieldValue = node.get(fieldName);

			switch (fieldName) {
				case "doctype" -> parseDoctype(fieldValue);
				case "language" -> _res.append(String.format("<html lang=%1$s>\n", fieldValue));
				case "meta" -> parseMeta(fieldValue, indent);
				case "link" -> parseLinkArray(node, indent);
				default -> parseGeneric(fieldValue, fieldName, depth, indent);
			}
		}
	}

	private static void parseGeneric(JsonNode node, String name, int depth, String indent) {
		String attributes = getAttributes(node);

		if (!node.isObject() && !node.isArray()) {
			String text = node.toString().replace("\"", "");
			//Format: indent<name attributes>text</name>
			final String format = "%1$s<%2$s%4$s>%3$s</%2$s>\n";
			String line = String.format(format, indent, name, text, attributes);
			_res.append(line);
		} else if (node.isObject()) {
			//Format: indent<name attributes>
			final String format = "%1$s<%2$s%3$s>\n";
			_res.append(String.format(format, indent, name, attributes));
			traverse(node, depth + 1);
			//Format: indent</name>
			final String formatEnd = "%1$s</%2$s>\n";
			_res.append(String.format(formatEnd, indent, name));
		}
	}

	private static void parseDoctype(JsonNode node) {
		String text = node.toString().replace("\"", "");
		String line = String.format("<!DOCTYPE %1$s>\n", text);
		_res.insert(0, line);
	}

	private static String getAttributes(JsonNode node) {
		if (!node.has("attributes")) {
			return "";
		}

		JsonNode attributes = node.get("attributes");
		Iterator<String> attrFieldNames = attributes.getFieldNames();
		StringBuilder line = new StringBuilder();

		while (attrFieldNames.hasNext()) {
			String attrFieldName = attrFieldNames.next();

			if (attrFieldName.equals("style")) {
				String style = getStyle(attributes.get("style"));
				line.append(String.format(" style=\"%1$s\"", style));
			} else {
				String attrNode = attributes.get(attrFieldName).toString();
				line.append(String.format(" %1$s=%2$s", attrFieldName, attrNode));
			}
		}

		return line.toString();
	}

	private static String getStyle(JsonNode node) {
		String text = node.toString();
		text = text.replaceAll("[{}\"]", "");
		text = text.replace(",", ";");

		return text;
	}

	private static void parseMeta(JsonNode node, String indent) {
		Iterator<String> metaFieldNames = node.getFieldNames();
		StringBuilder line = new StringBuilder();

		while (metaFieldNames.hasNext()) {
			String metaName = metaFieldNames.next();

			if (metaName.equals("charset")) {
				String format = "%1$s<meta charset=%2$s>\n";
				line.append(String.format(format, indent, node.get("charset")));
				continue;
			}

			if (metaName.equals("viewport")) {
				//Format: indent<meta viewport="...">
				final String format = "%1$s<meta viewport=\"%2$s\">\n";
				String viewport = getViewport(node.get("viewport"));
				line.append(String.format(format, indent, viewport));
				continue;
			}

			//Format: indent<meta name="..." content="...">
			final String format = "%1$s<meta name=\"%2$s\" content=%3$s>\n";
			line.append(String.format(format, indent, metaName, node.get(metaName)));
		}

		_res.append(line);
	}

	private static String getViewport(JsonNode node) {
		String text = node.toString();
		text = text.replaceAll("[{}\"]", "");
		text = text.replace(":", "=");
		text = text.replace(",", ", ");

		return text;
	}

	private static void parseLinkArray(JsonNode node, String indent) {
		ArrayNode arrayNode = (ArrayNode) node.get("link");
		StringBuilder line = new StringBuilder();

		for (int i = 0; i < arrayNode.size(); ++i) {
			JsonNode arrayElement = arrayNode.get(i);
			String params = "";

			if (arrayElement.has("href")) {
				params += " href=" + arrayElement.get("href");
			}

			if (arrayElement.has("rel")) {
				params += " rel=" + arrayElement.get("rel");
			}

			if (arrayElement.has("type")) {
				params += " type=" + arrayElement.get("type");
			}

			//Format: indent<link params>
			final String format = "%1$s<link%2$s>\n";
			line.append(String.format(format, indent, params));
		}

		_res.append(line);
	}

}
