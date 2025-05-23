package org.buaa.project.common.enums;

/**
 * 消息类型枚举
 */
public enum MessageTypeEnum {

    SYSTEM("system"),

    LIKE("like"),

    COMMENT("comment"),

    COLLECT("collect"),

    USEFUL("useful"),

    DELETE("delete"),

    INSERT("insert"),

    UPDATE("update");

    private final String type;

    MessageTypeEnum(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}
