package com.github.artemdvn.core;

import com.github.artemdvn.exception.SerializationException;
import com.github.artemdvn.util.ClassUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.artemdvn.core.Separators.COLLECTION_SEPARATOR;
import static com.github.artemdvn.core.Separators.FIELD_SEPARATOR;
import static com.github.artemdvn.core.Separators.KEY_VALUE_SEPARATOR;
import static com.github.artemdvn.core.Separators.OBJECT_SEPARATOR;

public class Serializer {

    public void serialize(OutputStream outputStream, Object obj) {
        Map<String, Object> fieldsMap = getObjectFields(obj);
        String serialized = mergeFields(fieldsMap, FIELD_SEPARATOR);
        try {
            outputStream.write(serialized.getBytes());
        } catch (IOException e) {
            throw new SerializationException("Serialization exception: error writing to output stream");
        }
    }

    private Map<String, Object> getObjectFields(Object obj) {
        Map<String, Object> fieldsMap = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Class<?> type = field.getType();
                if (ClassUtils.isPrimitive(type) || ClassUtils.isWrapper(type) || ClassUtils.isString(type)
                        || ClassUtils.isEnum(type) || ClassUtils.isMap(type)) {
                    //just put field name and value to map
                    if (field.get(obj) != null) {
                        fieldsMap.put(field.getName(), field.get(obj));
                    }
                } else if (ClassUtils.isCollection(type)) {
                    //get actual type of objects in collection
                    Class collectionElementType =
                            (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    Collection collectionFieldValue = (Collection) field.get(obj);
                    if (collectionFieldValue == null) {
                        continue;
                    }
                    if (ClassUtils.isPrimitive(collectionElementType) || ClassUtils.isWrapper(collectionElementType)) {
                        //save list of primitive values to map
                        List<Object> listOfPrimitives = new ArrayList<>();
                        listOfPrimitives.addAll(collectionFieldValue);
                        fieldsMap.put(field.getName(), listOfPrimitives);
                    } else {
                        //serialize each non-primitive value from collection and save list of them to map
                        List<Map<String, Object>> listOfNonPrimitives = new ArrayList<>();
                        for (Object element : collectionFieldValue) {
                            if (element != null) {
                                listOfNonPrimitives.add(getObjectFields(element));
                            }
                        }
                        fieldsMap.put(field.getName(), listOfNonPrimitives);
                    }
                } else {
                    //serialize non-primitive value and save it to map
                    fieldsMap.put(field.getName(), getObjectFields(field.get(obj)));
                }
            } catch (IllegalAccessException e) {
                throw new SerializationException("Serialization exception: error while reading object fields");
            }
        }
        return fieldsMap;
    }

    private String mergeFields(Map<String, Object> fieldsMap, final int fieldSeparator) {
        List<String> fieldsList = new ArrayList<>();
        for (Map.Entry<String, Object> field : fieldsMap.entrySet()) {
            StringBuilder builder = new StringBuilder();
            Object fieldValue = field.getValue();
            if (fieldValue == null) {
                builder.append(field.getKey())
                        .append(KEY_VALUE_SEPARATOR)
                        .append("null");
            } else if (ClassUtils.isCollection(fieldValue.getClass())) {
                builder.append(field.getKey())
                        .append((char) COLLECTION_SEPARATOR);
                List<String> collectionValues = new ArrayList<>();
                for (Object element : (List) fieldValue) {
                    if (ClassUtils.isWrapper(element.getClass())) {
                        collectionValues.add(element.toString());
                    } else {
                        collectionValues.add(mergeFields((Map) element, fieldSeparator + 1));
                    }
                }
                builder.append(String.join(Character.toString((char) OBJECT_SEPARATOR), collectionValues));
            } else if (ClassUtils.isMap(fieldValue.getClass())) {
                builder.append(field.getKey())
                        .append((char) OBJECT_SEPARATOR)
                        .append(mergeFields((Map) fieldValue, fieldSeparator + 1));
            } else {
                builder.append(field.getKey())
                        .append(KEY_VALUE_SEPARATOR)
                        .append(fieldValue);
            }
            fieldsList.add(builder.toString());
        }
        return String.join(Character.toString((char) fieldSeparator), fieldsList);
    }
}
