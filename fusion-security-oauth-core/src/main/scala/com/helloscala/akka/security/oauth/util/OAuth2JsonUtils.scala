/*
 * Copyright 2019-2021 helloscala.com
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

package com.helloscala.akka.security.oauth.util

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper, SerializationFeature }
import com.helloscala.akka.security.oauth.jacksons.OAuth2JacksonModule
import fusion.json.jackson.JsonUtils

import java.util.TimeZone

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-09-19 10:19:16
 */
object OAuth2JsonUtils {
  val objectMapper: ObjectMapper = JsonUtils.getCopy
    .registerModule(OAuth2JacksonModule)
    .setTimeZone(TimeZone.getTimeZone("Asia/Chongqing"))
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
    .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)

}
