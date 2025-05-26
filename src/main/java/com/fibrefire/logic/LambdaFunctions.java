package com.fibrefire.logic;

import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LambdaFunctions {

    public static CalculationResult calculateResults(InputData inputData) {
        int birthYear = inputData.age().birthYear();
        int birthMonth = inputData.age().birthMonth();
        int expectedLifespan = inputData.age().expectedLifespan();
        int retirementAge = inputData.age().retirementAge();
        int netSalary = inputData.income().netSalary();
        int retirementPay = inputData.income().retirementPay();
        double emergencyNow = inputData.assets().emergencyNow();
        int emergencyGoal = inputData.assets().emergencyGoal();
        double stockSavings = inputData.assets().stockSavings();
        double mortgage = inputData.loans().mortgage();
        double stocksGain = inputData.assets().stocksGain();
        double mortgageRate = inputData.loans().mortgageRate();
        int csnDebt = inputData.loans().csnTotal();
        int csnMonthlyPayoff = inputData.fixedCosts().csnPayoff();
        int mustHaves = inputData.fixedCosts().mustHaves();
        int foodCosts = inputData.spending().foodCosts();
        int travelCosts = inputData.spending().travelCosts();
        int percentForAmortization = inputData.payChoices().percentForAmortization();
        double firePercentage = inputData.payChoices().firePercentage();

        double monthlyStockGainFactor = Math.pow((1 + stocksGain / 100), (double) 1 / 12);

        int emergencyFilledAge = 0;
        int csnFreeAge = 0;
        int mortgageFreeAge = 0;
        int fireAge = 0;

        double fireAmount = 0;
        List<CalculationResult.MonthlyData> monthlyData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().withDayOfMonth(1);
        int age = 0;
        int loopLimit = 1000;
        int loopCount = 0;

        while (age < expectedLifespan && loopCount < loopLimit) {

            loopCount++;
            age = calculateAge(birthYear, birthMonth, currentDate);
            boolean retired = age >= retirementAge;
            double mortgageCost = (mortgageRate / 100) * mortgage / 12;
            double monthlyCost = mustHaves + foodCosts + travelCosts + mortgageCost;
            fireAmount = 25 * monthlyCost * 12;

            double income = retired ? retirementPay : netSalary;
            double available = income - monthlyCost;
            if (available < 0) {
                stockSavings += available;
                available = 0;
            }

            //  Portfolio, Fire
            stockSavings *= monthlyStockGainFactor;
            if (fireAge == 0 && stockSavings >= fireAmount) {
                fireAge = age;
            }

            //  CSN
            if (csnFreeAge == 0) {
                int csnPayoff = Math.min(csnMonthlyPayoff, csnDebt);
                csnDebt -= csnPayoff;
                if (csnDebt <= 0) {
                    csnFreeAge = age;
                }
            }

            //  Nödkonto
            if (emergencyNow < emergencyGoal) {
                double emergencyPayoff = Math.min(available, emergencyGoal - emergencyNow);
                emergencyNow += emergencyPayoff;
                available -= emergencyPayoff;
                if (emergencyFilledAge == 0 && emergencyNow >= emergencyGoal) {
                    emergencyFilledAge = age;
                }
            }

            double saved = 0;
            double payedOff = 0;
            if (available > 0) {

                //  amortera
                if (mortgageFreeAge == 0) {
                    payedOff = available * percentForAmortization / 100;
                    mortgage -= payedOff;
                    available -= payedOff;

                    if (mortgage <= 0) {
                        mortgageFreeAge = age;
                    }
                }

                //  börsen
                saved = Math.max(0, available);
                stockSavings += saved;
            }

            monthlyData.add(new CalculationResult.MonthlyData(
                    currentDate, age, saved, stockSavings, payedOff, mortgage, csnDebt, fireAmount));

            currentDate = currentDate.plusMonths(1);
        }

        return new CalculationResult(
                emergencyFilledAge, csnFreeAge, mortgageFreeAge, fireAge, fireAmount, monthlyData);
    }

    private static int calculateAge(int birthYear, int birthMonth, LocalDate currentDate) {
        return currentDate
                .minusYears(birthYear)
                .minusMonths(birthMonth)
                .getYear();
    }
}
