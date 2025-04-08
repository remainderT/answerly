package org.buaa.project.common.enums;

/**
 * 用户类别枚举
 */
public enum UserTypeEnum {

    STUDENT("collect"),

    VOLUNTEER( "comment"),

    ADMIN( "useful");

    UserTypeEnum(String type) {
        this.type = type;
    }

    private final String type;

    public String type() {
        return type;
    }

}
