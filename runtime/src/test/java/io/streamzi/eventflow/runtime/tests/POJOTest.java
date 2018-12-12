/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamzi.eventflow.runtime.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhiden
 */
public class POJOTest {
    @Test
    public void test() throws Exception {
        HashMap<String, Object> data = new HashMap<>();
        data.put("K1", "Value1");
        data.put("K2", 45.02);
        data.put("K3", 34);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(data);
        Object result = mapper.readValue(json, Object.class);
        System.out.println(result.getClass().getName());

        MethodTest mt = new MethodTest();
        mt.send(data);
        final Map<String, String> contents = new HashMap<>();
        contents.put("value", Double.toString(Math.random()));

        final CloudEvent<Map<String, String>> simpleKeyValueEvent = new CloudEventBuilder()
                .type("AType")
                .id("12345")
                .source(new URI("/somewhere"))
                .data(contents)
                .build();
        mt.send(simpleKeyValueEvent);
    }
}
