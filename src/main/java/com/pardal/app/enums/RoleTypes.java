package com.pardal.app.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleTypes {
    ADM("Admin"),
    MANAGER("Manager"),
    OPERATOR("Operator");

    private final String value;

}
