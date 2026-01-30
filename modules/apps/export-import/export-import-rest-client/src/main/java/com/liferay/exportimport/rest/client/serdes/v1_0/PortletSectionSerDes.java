/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.client.serdes.v1_0;

import com.liferay.exportimport.rest.client.dto.v1_0.PortletEntry;
import com.liferay.exportimport.rest.client.dto.v1_0.PortletSection;
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
public class PortletSectionSerDes {

	public static PortletSection toDTO(String json) {
		PortletSectionJSONParser portletSectionJSONParser =
			new PortletSectionJSONParser();

		return portletSectionJSONParser.parseToDTO(json);
	}

	public static PortletSection[] toDTOs(String json) {
		PortletSectionJSONParser portletSectionJSONParser =
			new PortletSectionJSONParser();

		return portletSectionJSONParser.parseToDTOs(json);
	}

	public static String toJSON(PortletSection portletSection) {
		if (portletSection == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (portletSection.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(portletSection.getName()));

			sb.append("\"");
		}

		if (portletSection.getPortletEntries() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletEntries\": ");

			sb.append("[");

			for (int i = 0; i < portletSection.getPortletEntries().length;
				 i++) {

				sb.append(
					String.valueOf(portletSection.getPortletEntries()[i]));

				if ((i + 1) < portletSection.getPortletEntries().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		PortletSectionJSONParser portletSectionJSONParser =
			new PortletSectionJSONParser();

		return portletSectionJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(PortletSection portletSection) {
		if (portletSection == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (portletSection.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(portletSection.getName()));
		}

		if (portletSection.getPortletEntries() == null) {
			map.put("portletEntries", null);
		}
		else {
			map.put(
				"portletEntries",
				String.valueOf(portletSection.getPortletEntries()));
		}

		return map;
	}

	public static class PortletSectionJSONParser
		extends BaseJSONParser<PortletSection> {

		@Override
		protected PortletSection createDTO() {
			return new PortletSection();
		}

		@Override
		protected PortletSection[] createDTOArray(int size) {
			return new PortletSection[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "name")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "portletEntries")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			PortletSection portletSection, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					portletSection.setName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "portletEntries")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					PortletEntry[] portletEntriesArray =
						new PortletEntry[jsonParserFieldValues.length];

					for (int i = 0; i < portletEntriesArray.length; i++) {
						portletEntriesArray[i] = PortletEntrySerDes.toDTO(
							(String)jsonParserFieldValues[i]);
					}

					portletSection.setPortletEntries(portletEntriesArray);
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