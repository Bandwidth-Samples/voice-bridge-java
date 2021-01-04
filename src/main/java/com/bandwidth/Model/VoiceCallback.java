package com.bandwidth.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoiceCallback {

    private String eventType;
    private String accountId;
    private String applicationId;
    private String from;
    private String to;
    private String direction;
    private String callId;
    private String callUrl;
    private String startTime;
    private String endTime;
    private String digits;
    private String tag;

}
