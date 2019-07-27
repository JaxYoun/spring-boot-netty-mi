package com.yang.springbootnetty.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author: Yang
 * @date: 2019/7/25 22:29
 * @description:
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T jsonToBean(String json, Class<T> beanClass) {
        try {
            return OBJECT_MAPPER.readValue(json, beanClass);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
