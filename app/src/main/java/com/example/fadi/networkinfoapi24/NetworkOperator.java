package com.example.fadi.networkinfoapi24;

import com.android.internal.telephony.OperatorInfo;

/**
 * Represents a mobile network operator.
 * Currently supports belgian operators, can be expanded to support others.
 */
public enum NetworkOperator {
    NOT_APPLICABLE("N/A"),
    BASE("BASE"),
    PROXIMUS("Proximus"),
    ORANGE("Orange B");

    private final String name;

    NetworkOperator(String name) {
        this.name = name;
    }

    /**
     * @return the name of the operator as describes by {@link OperatorInfo#getOperatorAlphaLong()}}
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean correspondsTo(OperatorInfo operator) {
        return operator.getOperatorAlphaLong().equals(name);
    }
}
