package com.fibrefire;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HandlerUnitTest {

    Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Handler();
    }

    @Test
    void handleRequest() throws JsonProcessingException {
        InputData inputData = new InputData(
                new InputData.Birth(1986, 3),
                new InputData.Income(35000),
                new InputData.Assets(0, 70000, 50000, 5),
                new InputData.Loans(1000000, 3, 100000),
                new InputData.FixedCosts(5000, 700),
                new InputData.Spending(3000, 1000),
                new InputData.PayChoices(40)
        );

        final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withBody(mapper.writeValueAsString(inputData));

        LambdaLogger logger = Mockito.mock(LambdaLogger.class);
        Mockito.doNothing().when(logger).log(Mockito.anyString());
        Context lambdaContext = Mockito.mock(Context.class);
        Mockito.doReturn(logger).when(lambdaContext).getLogger();

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, lambdaContext);
        CalculationResult result = mapper.readValue(response.getBody(), CalculationResult.class);
        System.out.println("emergency: " + result.emergencyFilledDate());
        System.out.println("csn: " + result.csnFreeDate());
        System.out.println("mortgage: " + result.mortgageFreeDate());
        System.out.println("fire: " + result.fireDate());
    }
}