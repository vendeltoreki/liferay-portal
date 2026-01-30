/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.client.serdes.v1_0;

import com.liferay.exportimport.rest.client.dto.v1_0.PortletEntry;
import com.liferay.exportimport.rest.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Petteri Karttunen
 * @generated
 */
@Generated("")
public class PortletEntrySerDes {

	public static PortletEntry toDTO(String json) {
		PortletEntryJSONParser portletEntryJSONParser =
			new PortletEntryJSONParser();

		return portletEntryJSONParser.parseToDTO(json);
	}

	public static PortletEntry[] toDTOs(String json) {
		PortletEntryJSONParser portletEntryJSONParser =
			new PortletEntryJSONParser();

		return portletEntryJSONParser.parseToDTOs(json);
	}

	public static String toJSON(PortletEntry portletEntry) {
		if (portletEntry == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (portletEntry.getCount() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"count\": ");

			sb.append(portletEntry.getCount());
		}

		if (portletEntry.getPortletDescription() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletDescription\": ");

			sb.append("\"");

			sb.append(_escape(portletEntry.getPortletDescription()));

			sb.append("\"");
		}

		if (portletEntry.getPortletId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletId\": ");

			sb.append("\"");

			sb.append(_escape(portletEntry.getPortletId()));

			sb.append("\"");
		}

		if (portletEntry.getPortletTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletTitle\": ");

			sb.append("\"");

			sb.append(_escape(portletEntry.getPortletTitle()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		PortletEntryJSONParser portletEntryJSONParser =
			new PortletEntryJSONParser();

		return portletEntryJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(PortletEntry portletEntry) {
		if (portletEntry == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (portletEntry.getCount() == null) {
			map.put("count", null);
		}
		else {
			map.put("count", String.valueOf(portletEntry.getCount()));
		}

		if (portletEntry.getPortletDescription() == null) {
			map.put("portletDescription", null);
		}
		else {
			map.put(
				"portletDescription",
				String.valueOf(portletEntry.getPortletDescription()));
		}

		if (portletEntry.getPortletId() == null) {
			map.put("portletId", null);
		}
		else {
			map.put("portletId", String.valueOf(portletEntry.getPortletId()));
		}

		if (portletEntry.getPortletTitle() == null) {
			map.put("portletTitle", null);
		}
		else {
			map.put(
				"portletTitle", String.valueOf(portletEntry.getPortletTitle()));
		}

		return map;
	}

	public static class PortletEntryJSONParser
		extends BaseJSONParser<PortletEntry> {

		@Override
		protected PortletEntry createDTO() {
			return new PortletEntry();
		}

		@Override
		protected PortletEntry[] createDTOArray(int size) {
			return new PortletEntry[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "count")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "portletDescription")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "portletId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "portletTitle")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			PortletEntry portletEntry, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "count")) {
				if (jsonParserFieldValue != null) {
					portletEntry.setCount(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "portletDescription")) {

				if (jsonParserFieldValue != null) {
					portletEntry.setPortletDescription(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "portletId")) {
				if (jsonParserFieldValue != null) {
					portletEntry.setPortletId((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "portletTitle")) {
				if (jsonParserFieldValue != null) {
					portletEntry.setPortletTitle((String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}