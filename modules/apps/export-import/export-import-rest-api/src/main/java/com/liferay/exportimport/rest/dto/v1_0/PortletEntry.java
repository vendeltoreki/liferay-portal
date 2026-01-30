/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Petteri Karttunen
 * @generated
 */
@Generated("")
@GraphQLName("PortletEntry")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "PortletEntry")
public class PortletEntry implements Serializable {

	public static PortletEntry toDTO(String json) {
		return ObjectMapperUtil.readValue(PortletEntry.class, json);
	}

	public static PortletEntry unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(PortletEntry.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema
	public Long getCount() {
		if (_countSupplier != null) {
			count = _countSupplier.get();

			_countSupplier = null;
		}

		return count;
	}

	public void setCount(Long count) {
		this.count = count;

		_countSupplier = null;
	}

	@JsonIgnore
	public void setCount(UnsafeSupplier<Long, Exception> countUnsafeSupplier) {
		_countSupplier = () -> {
			try {
				return countUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Long count;

	@JsonIgnore
	private Supplier<Long> _countSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getPortletDescription() {
		if (_portletDescriptionSupplier != null) {
			portletDescription = _portletDescriptionSupplier.get();

			_portletDescriptionSupplier = null;
		}

		return portletDescription;
	}

	public void setPortletDescription(String portletDescription) {
		this.portletDescription = portletDescription;

		_portletDescriptionSupplier = null;
	}

	@JsonIgnore
	public void setPortletDescription(
		UnsafeSupplier<String, Exception> portletDescriptionUnsafeSupplier) {

		_portletDescriptionSupplier = () -> {
			try {
				return portletDescriptionUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String portletDescription;

	@JsonIgnore
	private Supplier<String> _portletDescriptionSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getPortletId() {
		if (_portletIdSupplier != null) {
			portletId = _portletIdSupplier.get();

			_portletIdSupplier = null;
		}

		return portletId;
	}

	public void setPortletId(String portletId) {
		this.portletId = portletId;

		_portletIdSupplier = null;
	}

	@JsonIgnore
	public void setPortletId(
		UnsafeSupplier<String, Exception> portletIdUnsafeSupplier) {

		_portletIdSupplier = () -> {
			try {
				return portletIdUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String portletId;

	@JsonIgnore
	private Supplier<String> _portletIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getPortletTitle() {
		if (_portletTitleSupplier != null) {
			portletTitle = _portletTitleSupplier.get();

			_portletTitleSupplier = null;
		}

		return portletTitle;
	}

	public void setPortletTitle(String portletTitle) {
		this.portletTitle = portletTitle;

		_portletTitleSupplier = null;
	}

	@JsonIgnore
	public void setPortletTitle(
		UnsafeSupplier<String, Exception> portletTitleUnsafeSupplier) {

		_portletTitleSupplier = () -> {
			try {
				return portletTitleUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String portletTitle;

	@JsonIgnore
	private Supplier<String> _portletTitleSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PortletEntry)) {
			return false;
		}

		PortletEntry portletEntry = (PortletEntry)object;

		return Objects.equals(toString(), portletEntry.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		Long count = getCount();

		if (count != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"count\": ");

			sb.append(count);
		}

		String portletDescription = getPortletDescription();

		if (portletDescription != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletDescription\": ");

			sb.append("\"");

			sb.append(_escape(portletDescription));

			sb.append("\"");
		}

		String portletId = getPortletId();

		if (portletId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletId\": ");

			sb.append("\"");

			sb.append(_escape(portletId));

			sb.append("\"");
		}

		String portletTitle = getPortletTitle();

		if (portletTitle != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portletTitle\": ");

			sb.append("\"");

			sb.append(_escape(portletTitle));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.exportimport.rest.dto.v1_0.PortletEntry",
		name = "x-class-name"
	)
	public String xClassName;

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
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
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}