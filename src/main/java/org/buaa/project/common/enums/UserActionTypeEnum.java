package org.buaa.project.common.enums;

public enum UserActionTypeEnum {

    COLLECT("collect"),

    COMMENT( "comment"),

    LIKE( "like");

    private final String type;

    UserActionTypeEnum(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}
