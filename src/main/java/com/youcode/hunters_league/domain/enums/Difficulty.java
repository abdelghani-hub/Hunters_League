package com.youcode.hunters_league.domain.enums;

public enum Difficulty {
    COMMON(1), RARE(2), EPIC(3), LEGENDARY(5);

    private final int value;

    Difficulty(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
