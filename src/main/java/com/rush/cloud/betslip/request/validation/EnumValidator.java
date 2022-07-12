package com.rush.cloud.betslip.request.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Set<String> enumNames;

    @Override
    public void initialize(ValidEnum validEnum) {
        Class<? extends Enum<?>> enumSelected = validEnum.enumClass();
        this.enumNames = getNamesSet(enumSelected);
    }

    public static Set<String> getNamesSet(Class<? extends Enum<?>> e) {
        Enum<?>[] enums = e.getEnumConstants();
        String[] names = new String[enums.length];
        for (int i = 0; i < enums.length; i++) {
            names[i] = enums[i].name();
        }
        return new HashSet<>(Arrays.asList(names));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else {
            return this.enumNames.contains(value);
        }
    }
}
