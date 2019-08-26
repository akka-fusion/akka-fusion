/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.json.jackson.http;

import akka.Done;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.json.jackson.Jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JacksonHelper {
    public static final Map<String, String> empty = new HashMap<>();

    public static <T> Marshaller<T, RequestEntity> marshaller() {
        return marshaller(Jackson.defaultObjectMapper());
    }

    public static <T> Marshaller<T, RequestEntity> marshaller(ObjectMapper mapper) {
        return Marshaller.wrapEntity(
                u -> toJSON(mapper, u),
                Marshaller.stringToEntity(),
                MediaTypes.APPLICATION_JSON
        );
    }

    public static <T> Unmarshaller<ByteString, T> byteStringUnmarshaller(Class<T> expectedType) {
        return byteStringUnmarshaller(Jackson.defaultObjectMapper(), expectedType);
    }

    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(Class<T> expectedType) {
        return unmarshaller(Jackson.defaultObjectMapper(), expectedType);
    }

    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(ObjectMapper mapper, Class<T> expectedType) {
        return Unmarshaller.forMediaType(MediaTypes.APPLICATION_JSON, Unmarshaller.entityToString())
                .thenApply(s -> fromJSON(mapper, s, expectedType));
    }

    public static <T> Unmarshaller<ByteString, T> byteStringUnmarshaller(ObjectMapper mapper, Class<T> expectedType) {
        return Unmarshaller.sync(s -> fromJSON(mapper, s.utf8String(), expectedType));
    }

    private static String toJSON(ObjectMapper mapper, Object object) {
        try {
            if (object instanceof Done) {
                return mapper.writeValueAsString(empty);
            }
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshal to JSON: " + object, e);
        }
    }

    private static <T> T fromJSON(ObjectMapper mapper, String json, Class<T> expectedType) {
        try {
            return mapper.readerFor(expectedType).readValue(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot unmarshal JSON as " + expectedType.getSimpleName(), e);
        }
    }
}

