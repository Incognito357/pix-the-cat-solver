package com.incognito.models.serialization;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.awt.Point;

public class PointKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String s, DeserializationContext deserializationContext) throws NumberFormatException {
        String[] split = s.split("\\|");
        return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
}
