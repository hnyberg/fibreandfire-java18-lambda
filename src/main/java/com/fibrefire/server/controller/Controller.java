package com.fibrefire.server.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fibrefire.lambda.Handler;
import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
public class Controller {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Handler lambdaHandler = new Handler();

    @PostMapping("/finance")
    public ResponseEntity<String> handlePost(@RequestBody String json) {

        try {

            LambdaLogger logger = Mockito.mock(LambdaLogger.class);
            Mockito.doNothing().when(logger).log(Mockito.anyString());
            Context lambdaContext = Mockito.mock(Context.class);
            Mockito.doReturn(logger).when(lambdaContext).getLogger();

            InputData inputData = mapper.readValue(json, InputData.class);
            APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                    .withBody(mapper.writeValueAsString(inputData));

            APIGatewayProxyResponseEvent response = lambdaHandler.handleRequest(event, lambdaContext);
            CalculationResult result = mapper.readValue(response.getBody(), CalculationResult.class);

            return ResponseEntity.ok(mapper.writeValueAsString(result));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
