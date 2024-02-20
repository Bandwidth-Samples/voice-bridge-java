package com.bandwidth.controller;

import com.bandwidth.Main;
import com.bandwidth.sdk.model.InitiateCallback;
import com.bandwidth.sdk.model.AnswerCallback;
import com.bandwidth.sdk.api.*;
import com.bandwidth.sdk.api.CallsApi;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.ApiResponse;
import com.bandwidth.sdk.ApiException;
import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.auth.HttpBasicAuth;
import com.bandwidth.sdk.Configuration;
import com.bandwidth.sdk.model.*;
import com.bandwidth.sdk.model.bxml.*;
import com.bandwidth.sdk.model.bxml.Response;
import com.bandwidth.sdk.model.bxml.Ring;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    private final String username = System.getenv("BW_USERNAME");
    private final String password = System.getenv("BW_PASSWORD");
    private final String accountId = System.getenv("BW_ACCOUNT_ID");
    private final String applicationId = System.getenv("BW_VOICE_APPLICATION_ID");
    private final String baseUrl = System.getenv("BASE_CALLBACK_URL");
    private final String bwPhoneNumber = System.getenv("BW_NUMBER");
    private final String maskedPhoneNumber = System.getenv("USER_NUMBER");

    public ApiClient defaultClient = Configuration.getDefaultApiClient();
    public HttpBasicAuth Basic = (HttpBasicAuth) defaultClient.getAuthentication("Basic");
    public final CallsApi api = new CallsApi(defaultClient);
    private static CreateCall createCallBody = new CreateCall();
    

    @RequestMapping("/inbound")
    public String inboundCall(@RequestBody InitiateCallback callback) throws JAXBException, URISyntaxException {

        Response response = new Response();
	JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        String eventType = callback.getEventType();

        logger.info(eventType);
        logger.info(callback.getCallId());

        Basic.setUsername(username);
        Basic.setPassword(password);
        createCallBody.setTo(maskedPhoneNumber);
        createCallBody.setFrom(bwPhoneNumber);
        createCallBody.setApplicationId(applicationId);
        createCallBody.setAnswerUrl(new URI(baseUrl + "/callbacks/outbound"));


        if("initiate".equalsIgnoreCase(callback.getEventType())) {
            try {
                ApiResponse<CreateCallResponse> call = api.createCallWithHttpInfo(accountId, createCallBody);
            } catch (ApiException e) {
                logger.info(e.getMessage());
                return response.with(
				     new SpeakSentence( "An error occured.  Ending call")).toBxml(jaxbContext);
            }

            SpeakSentence ss = new SpeakSentence("Hold while we connect you.");

            Ring ring = new Ring(30d, true);

            response.withVerbs(ss, ring);
        }
        return response.toBxml(jaxbContext);

    }

    @RequestMapping("/outbound")
    public String outboundCall(@RequestBody AnswerCallback callback) throws JAXBException {

        Response response = new Response();
	JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());

        if("answer".equalsIgnoreCase(callback.getEventType())) {
            SpeakSentence ss = new SpeakSentence("Hold while we connect you. Bridging now.");

            Bridge bridge = new Bridge().builder().targetCallId(callback.getTag()).build();

            response.withVerbs(ss, bridge);
        }
        return response.toBxml(jaxbContext);
    }

}
