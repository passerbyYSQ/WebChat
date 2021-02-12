package net.ysq.webchat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
     *
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
     * 将json结果集转化为对象（不包含泛型）
     *
     * @param json  json数据
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
     * 目标对象只包含一个泛型，且没有嵌套
     *
     * @param json
     * @param valueTypeRef
     * @param <T>
     * @return
     */
    public static <T> T jsonToObj(String json, TypeReference<T> valueTypeRef) {
        try {
            return MAPPER.readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 目标对象可包含任意个泛型，且可嵌套
     *
     * @param json
     * @param clazz     目标对象的class类型
     * @param <T>
     * @return
     */
    public static <T> T jsonToUniversalObj(String json, Class<T> clazz) {
        JavaType javaType = parseJavaType(clazz);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归解析所有泛型类型
     *
     * @param type
     * @return
     */
    private static List<Class<?>> parseGenericType(Type type) {
        List<Class<?>> rootList = new ArrayList<>();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            rootList.add((Class<?>) pType.getRawType());
            for (Type at : pType.getActualTypeArguments()) {
                List<Class<?>> childList = parseGenericType(at); // 递归调用
                rootList.addAll(childList);
            }
        } else {
            rootList.add((Class<?>) type);
        }
        return rootList;
    }

    private static JavaType parseJavaType(Type genericParameterType) {
        List<Class<?>> list = parseGenericType(genericParameterType);
        if (list.size() == 1) {
            return MAPPER.getTypeFactory().constructType(genericParameterType);
        }
        Class<?>[] classes = list.toArray(new Class[0]);
        Class<?>[] paramClasses = new Class[classes.length - 1];
        System.arraycopy(classes, 1, paramClasses, 0, paramClasses.length);
        //JavaType javaType = MAPPER.getTypeFactory().constructParametrizedType(classes[0], classes[0], paramClasses);
        return MAPPER.getTypeFactory().constructParametricType(classes[0], paramClasses);
    }

    /**
     * 将json数据转换成pojo对象list
     *
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
