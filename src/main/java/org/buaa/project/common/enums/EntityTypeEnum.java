package org.buaa.project.common.enums;

/**
 * 实体类型枚举
 */
public enum EntityTypeEnum {

    QUESTION("question"),

    ANSWER("answer"),

    USER("user");

    private final String type;

    EntityTypeEnum(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}
