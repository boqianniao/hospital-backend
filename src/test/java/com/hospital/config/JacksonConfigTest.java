package com.hospital.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigTest {

    @Test
    void serializesLongIdsAsStringsWithoutChangingPrimitiveCounters() throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().longIdToStringCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();

        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", 2070714321230184455L);
        value.put("total", 2);

        assertEquals("{\"id\":\"2070714321230184455\",\"total\":2}", mapper.writeValueAsString(value));
    }
}
