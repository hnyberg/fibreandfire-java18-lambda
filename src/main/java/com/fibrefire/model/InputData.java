package com.fibrefire.model;

public record InputData(
        Birth birth,
        Income income,
        Assets assets,
        Loans loans,
        FixedCosts fixedCosts,
        Spending spending,
        PayChoices payChoices
) {
    public record Birth(int year, int month) {}
    public record Income(int salary) {}
    public record Assets(int emergencyNow, int emergencyGoal, int stockSavings, int stocksGain) {}
    public record Loans(int mortgage, double mortgageRate, int csnTotal) {}
    public record FixedCosts(int mustHaves, int csnPayoff) {}
    public record Spending(int foodCosts, int travelCosts) {}
    public record PayChoices(int percentForAmortization, double firePercentage) {}
}
