package com.github.artemdvn;

import com.github.artemdvn.core.Deserializer;
import com.github.artemdvn.core.Serializer;

import java.io.InputStream;
import java.io.OutputStream;

public class CustomSerializer {

    private Serializer serializer = new Serializer();
    private Deserializer deserializer = new Deserializer();

    public void serialize(OutputStream outputStream, Object obj) {
        serializer.serialize(outputStream, obj);
    }

    public <T> T deserialize(InputStream inputStream, Class<T> clazz) {
        return deserializer.deserialize(inputStream, clazz);
    }
}
