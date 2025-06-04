package com.fibrefire.model;

import java.util.List;
import java.util.Map;

public record CsvSummary(List<WeeklySpecifics> weeklySpecifics) {
    public record WeeklySpecifics(String week, Map<CostCategory, Float> costsPerCategory, float fixedCosts, float foodCosts, float totalCosts) {}
}
