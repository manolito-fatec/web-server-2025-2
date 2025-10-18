package com.pardal.app.entity;

import lombok.Data;

import java.util.List;


@Data
public class InsightTheme {

    private String theme;
    private String percentage;
    private List<String> actions;

}