package com.fibrefire;

import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HandlerUnitTest {

    Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Handler();
    }

    @Test
    void handleRequest() {
        InputData inputData = new InputData(
                new InputData.Birth(1986, 3),
                new InputData.Income(35000),
                new InputData.Assets(0, 70000, 50000, 5),
                new InputData.Loans(1000000, 3, 100000),
                new InputData.FixedCosts(5000, 700),
                new InputData.Spending(3000, 1000),
                new InputData.PayChoices(40)
        );
        CalculationResult result = handler.handleRequest(inputData, null);
        System.out.println("emergency: " + result.emergencyFilledDate());
        System.out.println("csn: " + result.csnFreeDate());
        System.out.println("mortgage: " + result.mortgageFreeDate());
        System.out.println("fire: " + result.fireDate());
    }
}