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
import com.fibrefire.logic.HandlerFunctions;
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

            CalculationResult calculationResult = HandlerFunctions.calculateResults(inputData);

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


}