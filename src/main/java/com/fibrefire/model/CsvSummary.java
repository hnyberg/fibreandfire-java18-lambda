package com.fibrefire.model;

import java.util.List;
import java.util.Map;

public record CsvSummary(List<MonthSpecifics> monthSummaries) {
    public record MonthSpecifics(String month, Map<CostCategory, Float> costsPerCategory) {}
}
