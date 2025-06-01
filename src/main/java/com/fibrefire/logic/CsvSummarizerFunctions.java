package com.fibrefire.logic;

import com.fibrefire.model.CostCategory;
import com.fibrefire.model.CsvSummary;

import java.util.*;

public class CsvSummarizerFunctions {
    public static CsvSummary summarize(String eventBody) {

        //  collect costs per category
        Map<String, Map<CostCategory, Float>> specificsMap = new HashMap<>();
        String[] lines = eventBody.split("\\r?\\n");
        for (String line : lines) {
            try {
                String[] parts = line.split(";");
                float cost = Float.parseFloat(parts[4]
                        .replace(",", ".")
                        .replace(" ", "")
                        .replaceAll("\"", "")) * -1;
                if (cost < 0) {
                    continue;
                }

                String description = parts[3].toLowerCase();
                CostCategory costCategory = CostCategorizer.getCostCategory(description);

                String date = parts[1].substring(0, 8);
                if (!specificsMap.containsKey(date)) {
                    specificsMap.put(date, new HashMap<>());
                }
                Map<CostCategory, Float> monthSpecifics = specificsMap.get(date);
                if (!monthSpecifics.containsKey(costCategory)) {
                    monthSpecifics.put(costCategory, 0f);
                }
                monthSpecifics.put(costCategory, monthSpecifics.get(costCategory) + cost);

            } catch (Exception ignored) {}
        }

        //  calculate average
        List<CsvSummary.MonthSpecifics> monthSpecificsList = new ArrayList<>();
        for (Map.Entry<String, Map<CostCategory, Float>> entry : specificsMap.entrySet()) {
            String date = entry.getKey();
            Map<CostCategory, Float> specificsForThisMonth = entry.getValue();
            CsvSummary.MonthSpecifics monthSpecifics = new CsvSummary.MonthSpecifics(date, specificsForThisMonth);
            monthSpecificsList.add(monthSpecifics);
        }
        monthSpecificsList.sort(Comparator.comparing(CsvSummary.MonthSpecifics::month));

        return new CsvSummary(monthSpecificsList);
    }
}
