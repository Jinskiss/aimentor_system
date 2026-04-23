package com.jins.aimentor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Jackson 配置 - 将 Long 类型序列化为字符串，避免前端 JavaScript 精度丢失
 */
@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ObjectMapper objectMapper = builder.build();

        // 自定义序列化：将 Long 类型转为字符串
        com.fasterxml.jackson.databind.ser.std.ToStringSerializer toStringSerializer =
                com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance;

        objectMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                .addSerializer(Long.class, toStringSerializer)
                .addSerializer(Long.TYPE, toStringSerializer)
                .addSerializer(java.math.BigInteger.class, toStringSerializer));

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
