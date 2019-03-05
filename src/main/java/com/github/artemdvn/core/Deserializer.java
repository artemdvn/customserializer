package com.github.artemdvn.core;

import com.github.artemdvn.exception.DeserializationException;
import com.github.artemdvn.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.github.artemdvn.core.Separators.COLLECTION_SEPARATOR;
import static com.github.artemdvn.core.Separators.FIELD_SEPARATOR;
import static com.github.artemdvn.core.Separators.KEY_VALUE_SEPARATOR;
import static com.github.artemdvn.core.Separators.OBJECT_SEPARATOR;

public class Deserializer {

    private static final String COLLECTION_SEPARATOR_STRING = Character.toString((char) COLLECTION_SEPARATOR);
    private static final String OBJECT_SEPARATOR_STRING = Character.toString((char) OBJECT_SEPARATOR);

    public <T> T deserialize(InputStream inputStream, Class<T> clazz) {
        T result;
        try {
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            result = clazz.getConstructor().newInstance();
            extract(new String(data), result, FIELD_SEPARATOR);
        } catch (IOException e) {
            throw new DeserializationException("Deserialization exception: error reading from input stream");
        } catch (ReflectiveOperationException e) {
            throw new DeserializationException("Deserialization exception: error while object creation");
        }
        return result;
    }

    private static void extract(String initialString, Object result, final int fieldSeparator) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (String fieldAndValue : initialString.split(Character.toString((char) fieldSeparator))) {
            if (fieldAndValue.contains(COLLECTION_SEPARATOR_STRING)) {
                extractCollection(result, fieldSeparator, fieldAndValue);
            } else if (fieldAndValue.contains(OBJECT_SEPARATOR_STRING)) {
                extractObjects(result, fieldSeparator, fieldAndValue);
            } else {
                extractValues(result, fieldAndValue);
            }
        }
    }

    private static void extractCollection(Object result, int fieldSeparator, String fieldAndValue) throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String[] fieldAndValueArray = fieldAndValue.split(COLLECTION_SEPARATOR_STRING);
        Field field = result.getClass().getDeclaredField(fieldAndValueArray[0]);
        field.setAccessible(true);

        Collection fieldObject;
        if (ClassUtils.isSet(field.getType())) {
            fieldObject = new HashSet<>();
        } else {
            fieldObject = new ArrayList<>();
        }

        field.set(result, fieldObject);

        ParameterizedType collectionType = (ParameterizedType) field.getGenericType();
        Class<?> elementType = (Class<?>) collectionType.getActualTypeArguments()[0];
        if (ClassUtils.isPrimitive(elementType)
                || ClassUtils.isWrapper(elementType)) {
            //extract primitive or wrapper collection elements
            for (String element : fieldAndValueArray[1].split(OBJECT_SEPARATOR_STRING)) {
                fieldObject.add(parseValue(element, elementType));
            }
        } else {
            //extract non-primitive collection elements
            for (String element : fieldAndValueArray[1].split(OBJECT_SEPARATOR_STRING)) {
                Object innerCollectionObject = Class.forName(elementType.getName()).newInstance();
                extract(element, innerCollectionObject, fieldSeparator + 1);
                fieldObject.add(innerCollectionObject);
            }
        }
    }

    private static void extractObjects(Object result, int fieldSeparator, String fieldAndValue) throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String[] fieldAndValueArray = fieldAndValue.split(OBJECT_SEPARATOR_STRING);
        Field field = result.getClass().getDeclaredField(fieldAndValueArray[0]);
        field.setAccessible(true);

        if (ClassUtils.isMap(field.getType())) {
            Map mapObject = new HashMap<>();
            field.set(result, mapObject);

            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            Class<?> keyClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            Class<?> valueClass = (Class<?>) stringListType.getActualTypeArguments()[1];
            int nextFieldSeparator = fieldSeparator + 1;
            for (String keyValue : fieldAndValueArray[1].split(Character.toString((char) nextFieldSeparator))) {
                String[] keyAndValueArray = keyValue.split(KEY_VALUE_SEPARATOR);
                Object keyObject = parseValue(keyAndValueArray[0], keyClass);
                Object valueObject;
                if (ClassUtils.isPrimitive(valueClass) || ClassUtils.isWrapper(valueClass)) {
                    valueObject = parseValue(keyAndValueArray[1], valueClass);
                } else {
                    valueObject = Class.forName(keyClass.getName()).newInstance();
                    extract(keyAndValueArray[1], valueObject, ++nextFieldSeparator);
                }
                mapObject.put(keyObject, valueObject);
            }
        } else {
            Object fieldObject = Class.forName(field.getType().getName()).newInstance();
            field.set(result, fieldObject);
            extract(fieldAndValueArray[1], fieldObject, fieldSeparator + 1);
        }
    }

    private static void extractValues(Object result, String fieldAndValue) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        String[] keyAndValueArray = fieldAndValue.split(KEY_VALUE_SEPARATOR);
        Field field = result.getClass().getDeclaredField(keyAndValueArray[0]);
        field.setAccessible(true);

        Class fieldType = parsePrimitiveType(field.getType().getName());
        if (ClassUtils.isEnum(fieldType)) {
            field.set(result, Enum.valueOf((Class<? extends Enum>) Class.forName(fieldType.getName()), keyAndValueArray[1]));
        } else {
            field.set(result, parseValue(keyAndValueArray[1], fieldType));
        }
    }

    private static Object parseValue(String value, Class type) {
        if ("null".equals(value)) {
            return null;
        }
        switch (type.getName()) {
            case "java.lang.Boolean":
                return Boolean.parseBoolean(value);
            case "java.lang.Double":
                return Double.parseDouble(value);
            case "java.lang.Float":
                return Float.parseFloat(value);
            case "java.lang.Integer":
                return Integer.parseInt(value);
            case "java.lang.Long":
                return Long.parseLong(value);
            case "java.lang.Short":
                return Short.parseShort(value);
            default:
                return value;
        }
    }

    private static Class parsePrimitiveType(String type) throws ClassNotFoundException {
        switch (type) {
            case "boolean":
                return Boolean.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "int":
                return Integer.class;
            case "long":
                return Long.class;
            case "short":
                return Short.class;
            default:
                return Class.forName(type);
        }
    }
}
