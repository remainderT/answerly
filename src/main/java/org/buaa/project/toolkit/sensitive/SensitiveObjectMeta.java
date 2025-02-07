package org.buaa.project.toolkit.sensitive;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 敏感词相关配置
 * 利用反射，拿数据库实体对象的所有成员，然后判断是否有自定义注解SensitiveField，有则表示需要将这个信息记录下来，在后面就需要进行替换了
 */
@Data
public class SensitiveObjectMeta {

    private static final String JAVA_LANG_OBJECT = "java.lang.object";
    /**
     * 是否启用脱敏
     */
    private Boolean enabledSensitiveReplace;

    /**
     * 类名
     */
    private String className;

    /**
     * 标注 SensitiveField 的成员
     */
    private List<SensitiveFieldMeta> sensitiveFieldMetaList;

    public static Optional<SensitiveObjectMeta> buildSensitiveObjectMeta(Object param) {
        if (isNull(param)) {
            return Optional.empty();
        }

        Class<?> clazz = param.getClass();
        SensitiveObjectMeta sensitiveObjectMeta = new SensitiveObjectMeta();
        sensitiveObjectMeta.setClassName(clazz.getName());

        List<SensitiveFieldMeta> sensitiveFieldMetaList = newArrayList();
        sensitiveObjectMeta.setSensitiveFieldMetaList(sensitiveFieldMetaList);
        boolean sensitiveField = parseAllSensitiveFields(clazz, sensitiveFieldMetaList);
        sensitiveObjectMeta.setEnabledSensitiveReplace(sensitiveField);
        return Optional.of(sensitiveObjectMeta);
    }

    private static boolean parseAllSensitiveFields(Class<?> clazz, List<SensitiveFieldMeta> sensitiveFieldMetaList) {
        Class<?> tempClazz = clazz;
        boolean hasSensitiveField = false;
        while (nonNull(tempClazz) && !JAVA_LANG_OBJECT.equalsIgnoreCase(tempClazz.getName())) {
            for (Field field : tempClazz.getDeclaredFields()) {
                SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
                if (nonNull(sensitiveField)) {
                    SensitiveFieldMeta sensitiveFieldMeta = new SensitiveFieldMeta();
                    sensitiveFieldMeta.setName(field.getName());
                    sensitiveFieldMetaList.add(sensitiveFieldMeta);
                    hasSensitiveField = true;
                }
            }
            tempClazz = tempClazz.getSuperclass();
        }
        return hasSensitiveField;
    }

    @Data
    public static class SensitiveFieldMeta {
        /**
         * 默认根据字段名，找db中同名的字段
         */
        private String name;

    }
}