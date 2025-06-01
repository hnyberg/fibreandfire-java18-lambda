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
import com.fibrefire.lambda.CsvSummarizer;
import com.fibrefire.lambda.FinancialPlanner;
import com.fibrefire.model.CalculationResult;
import com.fibrefire.model.InputData;
import jakarta.annotation.PostConstruct;
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

    private final LambdaLogger lambdaLogger = Mockito.mock(LambdaLogger.class);
    private final Context lambdaContext = Mockito.mock(Context.class);

    private final FinancialPlanner lambdaFinancialPlanner = new FinancialPlanner();
    private final CsvSummarizer csvSummarizer = new CsvSummarizer();

    @PostConstruct
    private void postConstruct() {
        Mockito.doNothing().when(lambdaLogger).log(Mockito.anyString());
        Mockito.doReturn(lambdaLogger).when(lambdaContext).getLogger();
    }

    @PostMapping("/finance")
    public ResponseEntity<String> calculateFinance(@RequestBody String json) {

        try {
            InputData inputData = mapper.readValue(json, InputData.class);
            APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                    .withBody(mapper.writeValueAsString(inputData));

            APIGatewayProxyResponseEvent response = lambdaFinancialPlanner.handleRequest(event, lambdaContext);
            CalculationResult result = mapper.readValue(response.getBody(), CalculationResult.class);

            return ResponseEntity.ok(mapper.writeValueAsString(result));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/csv")
    public ResponseEntity<String> summarizeCsv(@RequestBody String csvString) {

        try {
            APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent().withBody(csvString);

            APIGatewayProxyResponseEvent response = csvSummarizer.handleRequest(event, lambdaContext);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
