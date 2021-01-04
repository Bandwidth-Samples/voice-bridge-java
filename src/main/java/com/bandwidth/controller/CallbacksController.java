package com.bandwidth.controller;

import com.bandwidth.BandwidthClient;
import com.bandwidth.Environment;
import com.bandwidth.Model.VoiceCallback;
import com.bandwidth.exceptions.ApiException;
import com.bandwidth.voice.bxml.verbs.Bridge;
import com.bandwidth.voice.bxml.verbs.Response;
import com.bandwidth.voice.bxml.verbs.Ring;
import com.bandwidth.voice.bxml.verbs.SpeakSentence;
import com.bandwidth.voice.controllers.APIController;
import com.bandwidth.voice.models.ApiCreateCallRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    private final String username = System.getenv("BANDWIDTH_USERNAME");
    private final String password = System.getenv("BANDWIDTH_PASSWORD");
    private final String accountId = System.getenv("BANDWIDTH_ACCOUNT_ID");
    private final String applicationId = System.getenv("BANDWIDTH_VOICE_APPLICATION_ID");
    private final String baseUrl = System.getenv("BASE_URL");
    private final String bwPhoneNumber = System.getenv("BANDWIDTH_PHONE_NUMBER");
    private final String maskedPhoneNumber = System.getenv("MASKED_PHONE_NUMBER");

    private final BandwidthClient client = new BandwidthClient.Builder()
            .voiceBasicAuthCredentials(username, password)
            .environment(Environment.PRODUCTION)
            .build();

    private final APIController controller = client.getVoiceClient().getAPIController();



    @RequestMapping("/inbound")
    public String inboundCall(@RequestBody VoiceCallback callback) throws IOException {

        Response response = new Response();

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());

        if("initiate".equalsIgnoreCase(callback.getEventType())) {
            try {
                controller.createCall(accountId, new ApiCreateCallRequest.Builder()
                    .tag(callback.getCallId()) // call Id will be used to bridge
                    .to(maskedPhoneNumber)
                    .from(bwPhoneNumber)
                    .answerUrl(baseUrl + "/callbacks/outbound")
                    .applicationId(applicationId)
                    .build());
            } catch (ApiException e) {
                logger.info(e.getMessage());
                return response.add(
                    SpeakSentence.builder().text( "An error occured.  Ending call").build()
                ).toBXML();
            }

            SpeakSentence ss = SpeakSentence.builder()
                .text("Hold while we connect you.")
                .build();

            Ring ring = Ring.builder().duration(30.0).build();

            response.addAll(ss, ring);
        }
        return response.toBXML();

    }

    @RequestMapping("/outbound")
    public String outboundCall(@RequestBody VoiceCallback callback) {

        Response response = new Response();

        logger.info(callback.getEventType());
        logger.info(callback.getCallId());

        if("answer".equalsIgnoreCase(callback.getEventType())) {
            SpeakSentence ss = SpeakSentence.builder()
                .text("Hold while we connect you. Bridging now.")
                .build();

            Bridge bridge = Bridge.builder()
                .callId(callback.getTag()).build();

            response.addAll(ss, bridge);
        }
        return response.toBXML();
    }

}
