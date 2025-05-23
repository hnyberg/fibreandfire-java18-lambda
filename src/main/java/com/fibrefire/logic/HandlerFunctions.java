package com.fibrefire.logic;

import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HandlerFunctions {

    public static CalculationResult calculateResults(InputData inputData) {
        int birthYear = inputData.birth().year();
        int birthMonth = inputData.birth().month();
        int salary = inputData.income().salary();
        double emergencyNow = inputData.assets().emergencyNow();
        int emergencyGoal = inputData.assets().emergencyGoal();
        double stockSavings = inputData.assets().stockSavings();
        double mortgage = inputData.loans().mortgage();
        int stocksGain = inputData.assets().stocksGain();
        double mortgageRate = inputData.loans().mortgageRate();
        int csnDebt = inputData.loans().csnTotal();
        int csnMonthlyPayoff = inputData.fixedCosts().csnPayoff();
        int mustHaves = inputData.fixedCosts().mustHaves();
        int foodCosts = inputData.spending().foodCosts();
        int travelCosts = inputData.spending().travelCosts();
        int percentForAmortization = inputData.payChoices().percentForAmortization();
        double firePercentage = inputData.payChoices().firePercentage();

        double monthlyStockGainFactor = Math.pow((1 + (double) stocksGain / 100), (double) 1 / 12);

        LocalDate emergencyFilledDate = null;
        LocalDate csnFreeDate = null;
        LocalDate mortgageFreeDate = null;
        LocalDate fireDate = null;
        int fireAge = 0;
        double fireAmount = 0;
        List<CalculationResult.MonthlyStatus> monthlyData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().withDayOfMonth(1);
        int monthCounter = 0;

        while ((emergencyFilledDate == null
                || mortgageFreeDate == null
                || csnFreeDate == null
                || fireDate == null)
                && monthCounter < 600) {

            int age = calculateAge(birthYear, birthMonth, currentDate);

            double mortgageCost = (mortgageRate / 100) * mortgage / 12;
            double monthlyCost = mustHaves + foodCosts + travelCosts + mortgageCost;

            double available = salary - monthlyCost;
            fireAmount = (100 / firePercentage) * monthlyCost * 12;

            //  Portfolio, Fire
            stockSavings *= monthlyStockGainFactor;
            if (fireDate == null && stockSavings >= fireAmount) {
                fireDate = currentDate;
                fireAge = age;
            }

            //  CSN
            if (csnFreeDate == null) {
                int csnPayoff = Math.min(csnMonthlyPayoff, csnDebt);
                csnDebt -= csnPayoff;
                if (csnDebt <= 0) {
                    csnFreeDate = currentDate;
                }
            }

            //  Nödkonto
            if (emergencyNow < emergencyGoal) {
                double emergencyPayoff = Math.min(available, emergencyGoal - emergencyNow);
                emergencyNow += emergencyPayoff;
                available -= emergencyPayoff;
                if (emergencyFilledDate == null && emergencyNow >= emergencyGoal) {
                    emergencyFilledDate = currentDate;
                }
            }

            double saved = 0;
            double payedOff = 0;
            if (available > 0) {

                //  amortera
                if (mortgageFreeDate == null) {
                    payedOff = available * percentForAmortization / 100;
                    mortgage -= payedOff;
                    available -= payedOff;

                    if (mortgage <= 0) {
                        mortgageFreeDate = currentDate;
                    }
                }

                //  börsen
                saved = Math.max(0, available);
                stockSavings += saved;
            }

            monthlyData.add(new CalculationResult.MonthlyStatus(
                    currentDate, age, saved, stockSavings, payedOff, mortgage, csnDebt, fireAmount));

            currentDate = currentDate.plusMonths(1);
            monthCounter++;
        }

        return new CalculationResult(
                emergencyFilledDate, csnFreeDate, mortgageFreeDate, fireDate, fireAge, fireAmount, monthlyData);
    }

    private static int calculateAge(int birthYear, int birthMonth, LocalDate currentDate) {
        return currentDate
                .minusYears(birthYear)
                .minusMonths(birthMonth)
                .getYear();
    }
}
