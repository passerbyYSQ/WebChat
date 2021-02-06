package net.ysq.webchat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @Description: 自定义响应结构, 转换类
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = SpringUtils.getBean(ObjectMapper.class);
//        MAPPER = new ObjectMapper();
    }

    /**
     * 将对象转换成json字符串
     * @param obj
     * @return
     */
    public static String objectToJson(Object obj) {
    	try {
            return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	return null;
    }

    /**
     * 将json结果集转化为对象
     *
     * @param json json数据
     * @param clazz 对象中的object类型
     * @return
     */
    public static <T> T jsonToObj(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json数据转换成pojo对象list
     * @param json
     * @param clazz
     * @return
     */
    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
    	JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
