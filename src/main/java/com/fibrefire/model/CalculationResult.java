package com.fibrefire.model;

import java.time.LocalDate;
import java.util.List;

public record CalculationResult(
        int emergencyFilledAge,
        int csnFreeAge,
        int mortgageFreeAge,
        int fireAge,
        double fireAmount,
        List<MonthlyData> monthlyData
) {
    public record MonthlyData(
            LocalDate date,
            double age,
            double invested,
            double stockSavings,
            double payedOff,
            double mortgage,
            double csnDebt,
            double fireAmount
    ) {}
}