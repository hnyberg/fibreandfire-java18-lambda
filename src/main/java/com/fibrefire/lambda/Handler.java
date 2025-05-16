package com.fibrefire.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent inputEvent, Context context) {

        String eventBody = inputEvent.getBody();
        context.getLogger().log("Raw body: " + eventBody);

        InputData inputData;

        try {
            inputData = mapper.readValue(eventBody, InputData.class);

            context.getLogger().log("Parsed input: " + inputData);

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

            CalculationResult calculationResult = new CalculationResult(
                    emergencyFilledDate, csnFreeDate, mortgageFreeDate, fireDate, fireAmount, monthlyData);

            String resultAsString = mapper.writeValueAsString(calculationResult);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(resultAsString)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*"
                    ));

        } catch (JsonProcessingException e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Internal server error: " + e.getMessage());
        }
    }

    private int calculateAge(int birthYear, int birthMonth, LocalDate currentDate) {
        return currentDate
                .minusYears(birthYear)
                .minusMonths(birthMonth)
                .getYear();
    }
}