package ru.gosuslugi.pgu.fs.component.child;

public enum ChildGenderFilters {
    MALE("male"),
    FEMALE("female"),
    BOTH("both");

    ChildGenderFilters(String name) {
        this.name = name;
    }

    private final String name;

    public String getName() {
        return name;
    }

    public static ChildGenderFilters formString(String string) {
        for (ChildGenderFilters gender : values()) {
            if (gender.name.equals(string)) {
                return gender;
            }
        }
        return null;
    }
}
