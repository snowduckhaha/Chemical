package com.qidian.site.dto;

public class ParameterItemDto {

    private String name;
    private String value;

    public ParameterItemDto() {
    }

    public ParameterItemDto(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
