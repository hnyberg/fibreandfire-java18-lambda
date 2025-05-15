package com.fibrefire;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Handler implements RequestHandler<InputData, CalculationResult> {

    @Override
    public CalculationResult handleRequest(InputData inputData, Context context) {

        System.out.println("event received: " + inputData.toString());

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

        double monthlyStockGainFactor = Math.pow((1 + (double) stocksGain / 100), (double) 1 / 12);

        LocalDate emergencyFilledDate = null;
        LocalDate csnFreeDate = null;
        LocalDate mortgageFreeDate = null;
        LocalDate fireDate = null;
        double fireAmount = 0;
        List<CalculationResult.MonthlyStatus> monthlyData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().withDayOfMonth(1);
        int monthCounter = 0;

        while ((emergencyFilledDate == null
                || mortgageFreeDate == null
                || csnFreeDate == null
                || fireDate == null)
                && monthCounter < 600) {

            double mortgageCost = (mortgageRate / 100) * mortgage / 12;
            double monthlyCost = mustHaves + foodCosts + travelCosts + mortgageCost;

            double available = salary - monthlyCost;
            fireAmount = 25 * monthlyCost * 12;

            //  Portfolio, Fire
            stockSavings *= monthlyStockGainFactor;
            if (fireDate == null && stockSavings >= fireAmount) {
                fireDate = currentDate;
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

            if (available > 0) {

                //  amortera
                if (mortgageFreeDate == null) {
                    double mortgagePayoff = available * percentForAmortization / 100;
                    mortgage -= mortgagePayoff;
                    available -= mortgagePayoff;

                    if (mortgage <= 0) {
                        mortgageFreeDate = currentDate;
                    }
                }

                //  börsen
                stockSavings += Math.max(0, available);
            }

            int age = calculateAge(birthYear, birthMonth, currentDate);
            monthlyData.add(new CalculationResult.MonthlyStatus(
                    currentDate, age, stockSavings, mortgage, csnDebt, fireAmount));

            currentDate = currentDate.plusMonths(1);
            monthCounter++;
        }

        return new CalculationResult(
                emergencyFilledDate, csnFreeDate, mortgageFreeDate, fireDate, fireAmount, monthlyData);
    }

    private int calculateAge(int birthYear, int birthMonth, LocalDate currentDate) {
        return currentDate
                .minusYears(birthYear)
                .minusMonths(birthMonth)
                .getYear();
    }
}