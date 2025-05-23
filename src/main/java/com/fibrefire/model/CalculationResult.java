package com.fibrefire.model;

import java.time.LocalDate;
import java.util.List;

public record CalculationResult(
        LocalDate emergencyFilledDate,
        LocalDate csnFreeDate,
        LocalDate mortgageFreeDate,
        LocalDate fireDate,
        int fireAge,
        double fireAmount,
        List<MonthlyStatus> monthlyData
) {
    public record MonthlyStatus(
            LocalDate date,
            int age,
            double saved,
            double stockSavings,
            double payedOff,
            double mortgageLeft,
            double csnLeft,
            double fireAmount
    ) {}
}