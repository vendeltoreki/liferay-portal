/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.reader;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.liferay.batch.engine.action.ItemReaderPostAction;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.Serializable;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ivica Cardic
 */
public class BatchEngineImportTaskItemReaderUtil {

	public static <T> T convertValue(
			BatchEngineImportTask batchEngineImportTask, Class<T> itemClass,
			Map<String, Object> fieldNameValueMap,
			List<ItemReaderPostAction> itemReaderPostActions)
		throws ReflectiveOperationException {

		Map<String, Serializable> extendedProperties = new HashMap<>();
		T item = itemClass.newInstance();

		for (Map.Entry<String, Object> entry : fieldNameValueMap.entrySet()) {
			String name = entry.getKey();

			Field field = null;

			for (Field declaredField : itemClass.getDeclaredFields()) {
				if (name.equals(declaredField.getName()) ||
					Objects.equals(
						StringPool.UNDERLINE + name, declaredField.getName())) {

					field = declaredField;

					break;
				}
			}

			if (field != null) {
				field.setAccessible(true);

				ObjectMapper objectMapper = _getObjectMapper(field);

				field.set(
					item,
					_processCreatorInfo(objectMapper.convertValue(
						entry.getValue(), field.getType()), name, itemClass, entry.getValue()));

				continue;
			}

			for (Field declaredField : itemClass.getDeclaredFields()) {
				JsonAnySetter[] jsonAnySetters =
					declaredField.getAnnotationsByType(JsonAnySetter.class);

				if (jsonAnySetters.length > 0) {
					field = declaredField;

					break;
				}
			}

			if (field == null) {
				extendedProperties.put(
					entry.getKey(), (Serializable)entry.getValue());
			}
			else {
				field.setAccessible(true);

				Map<String, Object> map = (Map)field.get(item);

				map.put(entry.getKey(), entry.getValue());
			}
		}

		for (ItemReaderPostAction itemReaderPostAction :
				itemReaderPostActions) {

			itemReaderPostAction.run(
				batchEngineImportTask, extendedProperties, item);
		}

		return item;
	}

	private static <T> Object _processCreatorInfo(Object o, String name, Class<T> itemClass, Object value)
		throws IllegalAccessException {

		if (!name.equals("creator")) {
			return o;
		}

		if (!itemClass.getName().equals("com.liferay.object.rest.dto.v1_0.ObjectEntry")) {
			return o;
		}

		if (!(value instanceof Map)) {
			return o;
		}

		Map<String, Object> values = (Map<String, Object>)value;

		String creatorErc = MapUtil.getString(values, "externalReferenceCode");

		if (Validator.isNotNull(creatorErc)) {
			_setFieldValue(o, "externalReferenceCode", creatorErc);
		}

		Integer creatorId = MapUtil.getInteger(values, "id");

		if (Validator.isNotNull(creatorId)) {
			_setFieldValue(o, "id", creatorId);
		}

		return o;
	}

	private static void _setFieldValue(Object o, String fieldName, Object value)
		throws IllegalAccessException {
		Field field = _getFieldByName(o, fieldName);

		if (field != null) {
			field.setAccessible(true);

			field.set(o, value);
		}
	}

	private static Field _getFieldByName(Object o, String fieldName) {
		Field field = null;

		for (Field declaredField : o.getClass().getDeclaredFields()) {
			if (fieldName.equals(declaredField.getName()) ||
				Objects.equals(
					StringPool.UNDERLINE + fieldName, declaredField.getName())) {

				field = declaredField;

				break;
			}
		}
		return field;
	}


	public static Map<String, Object> mapFieldNames(
		Map<String, ? extends Serializable> fieldNameMappingMap,
		Map<String, Object> fieldNameValueMap) {

		if ((fieldNameMappingMap == null) || fieldNameMappingMap.isEmpty()) {
			return fieldNameValueMap;
		}

		Map<String, Object> targetFieldNameValueMap = new HashMap<>();

		for (Map.Entry<String, Object> entry : fieldNameValueMap.entrySet()) {
			String targetFieldName = (String)fieldNameMappingMap.get(
				entry.getKey());

			if (Validator.isNotNull(targetFieldName)) {
				Object object = targetFieldNameValueMap.get(targetFieldName);

				if ((object != null) && (object instanceof Map)) {
					Map<?, ?> map = (Map)object;

					map.putAll((Map)entry.getValue());
				}
				else {
					targetFieldNameValueMap.put(
						targetFieldName, entry.getValue());
				}

				continue;
			}

			String[] fieldNameParts = StringUtil.split(
				entry.getKey(), StringPool.PERIOD);

			targetFieldName = (String)fieldNameMappingMap.get(
				fieldNameParts[0]);

			if (Validator.isNull(targetFieldName)) {
				continue;
			}

			Matcher multiselectPicklistMatcher =
				_multiselectPicklistPattern.matcher(fieldNameParts[1]);

			if (multiselectPicklistMatcher.matches()) {
				if (fieldNameParts[1].startsWith("name_")) {
					continue;
				}

				List<Object> list =
					(List<Object>)targetFieldNameValueMap.computeIfAbsent(
						targetFieldName, key -> new ArrayList<>());

				list.add(entry.getValue());

				continue;
			}

			Map<String, Object> map =
				(Map<String, Object>)targetFieldNameValueMap.computeIfAbsent(
					targetFieldName, key -> new HashMap<>());

			for (int i = 1; i < fieldNameParts.length; i++) {
				if ((fieldNameParts.length - 1) > i) {
					map = (Map<String, Object>)map.computeIfAbsent(
						fieldNameParts[i], key -> new HashMap<>());

					continue;
				}

				map.put(fieldNameParts[i], entry.getValue());
			}
		}

		return targetFieldNameValueMap;
	}

	private static ObjectMapper _getObjectMapper(Field field)
		throws IllegalAccessException, InstantiationException {

		JsonDeserialize[] jsonDeserializes = field.getAnnotationsByType(
			JsonDeserialize.class);

		if (ArrayUtil.isEmpty(jsonDeserializes)) {
			return _objectMapper;
		}

		JsonDeserialize jsonDeserialize = jsonDeserializes[0];

		return new ObjectMapper() {
			{
				SimpleModule simpleModule = new SimpleModule();

				simpleModule.addDeserializer(
					field.getType(),
					jsonDeserialize.using(
					).newInstance());

				registerModule(simpleModule);
			}
		};
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineImportTaskItemReaderUtil.class);

	private static final Pattern _multiselectPicklistPattern = Pattern.compile(
		"key_\\d+|name_\\d+");

	private static final ObjectMapper _objectMapper = new ObjectMapper() {
		{
			SimpleModule simpleModule = new SimpleModule();

			simpleModule.addDeserializer(Map.class, new MapStdDeserializer());

			registerModule(simpleModule);
		}
	};

	private static class MapStdDeserializer
		extends StdDeserializer<Map<String, Object>> {

		public MapStdDeserializer() {
			this(Map.class);
		}

		@Override
		public Map<String, Object> deserialize(
				JsonParser jsonParser,
				DeserializationContext deserializationContext)
			throws IOException {

			try {
				return deserializationContext.readValue(
					jsonParser, LinkedHashMap.class);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				Map<String, Object> map = new LinkedHashMap<>();

				String string = jsonParser.getValueAsString();

				for (String line : string.split(StringPool.RETURN_NEW_LINE)) {
					String[] lineParts = line.split(StringPool.COLON);

					map.put(lineParts[0], lineParts[1]);
				}

				return map;
			}
		}

		protected MapStdDeserializer(Class<?> clazz) {
			super(clazz);
		}

	}

}