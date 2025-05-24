package com.fibrefire.model;

public record InputData(
        Age age,
        Income income,
        Assets assets,
        Loans loans,
        FixedCosts fixedCosts,
        Spending spending,
        PayChoices payChoices
) {
    public record Age(int birthYear, int birthMonth, int expectedLifespan, int retirementAge) {}
    public record Income(int netSalary, int retirementPay) {}
    public record Assets(int emergencyNow, int emergencyGoal, int stockSavings, double stocksGain) {}
    public record Loans(int mortgage, double mortgageRate, int csnTotal) {}
    public record FixedCosts(int mustHaves, int csnPayoff) {}
    public record Spending(int foodCosts, int travelCosts) {}
    public record PayChoices(int percentForAmortization, double firePercentage) {}
}
