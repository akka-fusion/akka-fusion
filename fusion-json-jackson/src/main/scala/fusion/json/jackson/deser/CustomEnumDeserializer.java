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

package fusion.json.jackson.deser;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.util.EnumResolver;
import helloscala.common.util.StringUtils$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CustomEnumDeserializer extends StdScalarDeserializer<Enum<?>> implements ContextualDeserializer {
    private final static Logger logger = LoggerFactory.getLogger(CustomEnumDeserializer.class);
    private final Enum<?>[] enums;
    private final EnumResolver enumResolver;

    public CustomEnumDeserializer(EnumResolver byNameResolver) {
        super(byNameResolver.getEnumClass());
        this.enumResolver = byNameResolver;
        this.enums = enumResolver.getRawEnums();
    }

    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Class<Enum<?>> t = enumResolver.getEnumClass();
        Optional<Method> maybe = Arrays.stream(t.getMethods())
                .filter(method -> method.getAnnotation(JsonValue.class) != null && method.getReturnType() == Integer.class)
                .findFirst();
        if (!maybe.isPresent()) {
            Field field = dealEnumType(t).orElseThrow(() -> new JsonParseException(p, String.format("Could not find @JsonValue annotation in Class: %s.", t.getName())));
            try {
                maybe = Optional.ofNullable(t.getDeclaredMethod(getMethodCapitalize(field, field.getName())));
            } catch (NoSuchMethodException e) {
                throw new JsonParseException(p, "The @JsonValue field must define get or is method.");
            }
        }

        for (Enum<?> en : enums) {
            Enum<?> anEnum;
            if (maybe.isPresent()) {
                anEnum = findEnum(p, maybe.get(), en);
            } else {
                anEnum = en.toString().equals(p.getValueAsString()) ? en : null;
            }
            if (Objects.nonNull(anEnum))
                return anEnum;
        }

        throw new JsonParseException(p, "枚举值解析失败");
    }

    private Enum<?> findEnum(JsonParser p, Method method, Enum<?> en) {
        try {
            Class<?> rt = method.getReturnType();
            if (Integer.class.isAssignableFrom(rt)) {
                Integer value = (Integer) method.invoke(en);
                if (value == p.getIntValue()) {
                    return en;
                }
            } else {
                String value = p.getValueAsString();
                if (en.toString().equals(value)) {
                    return en;
                }
            }
            return null;
        } catch (IllegalAccessException | InvocationTargetException | IOException e) {
            logger.error("findEnum error {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return this;
    }

    public static Optional<Field> dealEnumType(Class<?> clazz) {
        return clazz.isEnum() ? Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(JsonValue.class)).findFirst() : Optional.empty();
    }

    public static String getMethodCapitalize(Field field, final String str) {
        Class<?> fieldType = field.getType();
        return (boolean.class.equals(fieldType) ? "is" : "get") + StringUtils$.MODULE$.capitalize(str);
    }
}
