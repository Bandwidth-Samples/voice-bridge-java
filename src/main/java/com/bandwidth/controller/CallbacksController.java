package com.bandwidth.controller;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.api.CallsApi;
import com.bandwidth.sdk.model.AnswerCallback;
import com.bandwidth.sdk.model.CreateCall;
import com.bandwidth.sdk.model.InitiateCallback;
import com.bandwidth.sdk.model.bxml.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    private final String accountId = System.getenv("BW_ACCOUNT_ID");
    private final String clientId = System.getenv("BW_CLIENT_ID");
    private final String clientSecret = System.getenv("BW_CLIENT_SECRET");
    private final String applicationId = System.getenv("BW_VOICE_APPLICATION_ID");
    private final String number = System.getenv("BW_NUMBER");
    private final String userNumber = System.getenv("USER_NUMBER");
    private final String baseCallbackUrl = System.getenv("BASE_CALLBACK_URL");

    private final CallsApi callsApi = new CallsApi(new ApiClient(clientId, clientSecret, null));

    @PostMapping(value = "/inbound/voice", produces = "application/xml")
    public ResponseEntity<String> inboundCallback(@RequestBody InitiateCallback callback) throws JAXBException {
        if (!"initiate".equals(callback.getEventType())) {
            return ResponseEntity.badRequest().build();
        }

        CreateCall createCall = new CreateCall()
                .applicationId(applicationId)
                .to(userNumber)
                .from(number)
                .answerUrl(URI.create(baseCallbackUrl + "/callbacks/outbound/voice"))
                .tag(callback.getCallId());

        Response response = new Response();
        try {
            callsApi.createCall(accountId, createCall);
            response.withVerbs(
                    new SpeakSentence("Hold while we connect you."),
                    Ring.builder().duration(30d).build()
            );
        } catch (Exception e) {
            logger.error("Failed to create call: " + e.getMessage());
            response.withVerbs(
                    new SpeakSentence("An error occurred. Ending call."),
                    new Hangup()
            );
        }

        return ResponseEntity.ok(response.toBxml(JAXBContext.newInstance(Response.class)));
    }

    @PostMapping(value = "/outbound/voice", produces = "application/xml")
    public ResponseEntity<String> outboundCallback(@RequestBody AnswerCallback callback) throws JAXBException {
        Response response = new Response();

        String eventType = String.valueOf(callback.getEventType());
        switch (eventType) {
            case "answer":
                response.withVerbs(
                        new SpeakSentence("Hold while we connect you. We will begin to bridge you now."),
                        Bridge.builder().targetCallId(callback.getTag()).build()
                );
                break;
            case "disconnect":
                logger.info("Call " + callback.getCallId() + " has disconnected");
                break;
            default:
                logger.info("Unexpected event type " + eventType + " received");
                break;
        }

        return ResponseEntity.ok(response.toBxml(JAXBContext.newInstance(Response.class)));
    }

}
