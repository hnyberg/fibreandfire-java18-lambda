package com.fibrefire.logic;

import com.fibrefire.model.CostCategory;
import com.fibrefire.model.CsvSummary;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

import static com.fibrefire.model.CostCategory.*;

public class CsvSummarizerFunctions {

    private static final String OVERFORING = "överföring";

    public static CsvSummary summarize(String eventBody) {
        Map<String, Map<CostCategory, Float>> specificsMap = collectCosts(eventBody);
        List<CsvSummary.WeeklySpecifics> weeklySpecificsList = calculateAverage(specificsMap);
        return new CsvSummary(weeklySpecificsList);
    }

    private static Map<String, Map<CostCategory, Float>> collectCosts(String input) {
        Map<String, Map<CostCategory, Float>> specificsMap = new HashMap<>();
        String[] lines = input.split("\\r?\\n");
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

                String type = parts[2].toLowerCase().replaceAll("\"", "");
                if (OVERFORING.equals(type)) {
                    continue;
                }
                String description = parts[3].toLowerCase().replaceAll("\"", "");

                String[] dateParts = parts[1].replaceAll("\"", "").split("-");
                LocalDate date = LocalDate.of(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
                int week = date.get(WeekFields.of(Locale.GERMAN).weekOfYear());
                String dateKey = dateParts[0] + "-v" + String.format("%02d", week);
                if (!specificsMap.containsKey(dateKey)) {
                    specificsMap.put(dateKey, new HashMap<>());
                }

                CostCategory costCategory = CostCategorizer.getCostCategory(description, type, dateKey, cost);
                if (OTHER.equals(costCategory) && cost > 0) {
                    System.out.println(costCategory + ", " + type + ", " + description + ", : " + cost);
                }
                Map<CostCategory, Float> monthSpecifics = specificsMap.get(dateKey);
                if (!monthSpecifics.containsKey(costCategory)) {
                    monthSpecifics.put(costCategory, 0f);
                }
                monthSpecifics.put(costCategory, monthSpecifics.get(costCategory) + cost);

            } catch (Exception ignored) {
            }
        }
        return specificsMap;
    }

    private static List<CsvSummary.WeeklySpecifics> calculateAverage(Map<String, Map<CostCategory, Float>> specificsMap) {
        List<CsvSummary.WeeklySpecifics> weeklySpecificsList = new ArrayList<>();
        for (Map.Entry<String, Map<CostCategory, Float>> entry : specificsMap.entrySet()) {
            String date = entry.getKey();
            Map<CostCategory, Float> specificsForThisMonth = entry.getValue();
            float fixedCosts = summarizeFixedCosts(specificsForThisMonth);
            float foodCosts = summarizeFoodCosts(specificsForThisMonth);
            float allCosts = summarizeAllCosts(specificsForThisMonth);
            CsvSummary.WeeklySpecifics weeklySpecifics = new CsvSummary.WeeklySpecifics(
                    date, specificsForThisMonth, fixedCosts, foodCosts, allCosts);
            weeklySpecificsList.add(weeklySpecifics);
        }
        weeklySpecificsList.sort(Comparator.comparing(CsvSummary.WeeklySpecifics::week));

        return weeklySpecificsList;
    }

    private static float summarizeFixedCosts(Map<CostCategory, Float> input) {
        return input.entrySet().stream()
                .filter(entry -> FIXED.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .reduce(Float::sum)
                .orElse(0f);
    }

    private static float summarizeFoodCosts(Map<CostCategory, Float> input) {
        return input.entrySet().stream()
                .filter(entry -> FOODS.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .reduce(Float::sum)
                .orElse(0f);
    }

    private static float summarizeAllCosts(Map<CostCategory, Float> input) {
        return input.values().stream()
                .reduce(Float::sum)
                .orElse(0f);
    }
}
