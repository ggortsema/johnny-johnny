package com.grant.chatbot.rules.facts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundMessageFact {
    private String senderType;
    private String text;
    private boolean containsSensitiveTopic;
    private boolean containsConflictSignals;
    private int hourOfDay;
    
    private static final Logger logger = LoggerFactory.getLogger(InboundMessageFact.class);


    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
    	logger.info("set senderType being called with: " + senderType);
        this.senderType = senderType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        
    	logger.info("setText being called with: " + text);
    	this.text = text;
    }

    public boolean isContainsSensitiveTopic() {
        return containsSensitiveTopic;
    }

    public void setContainsSensitiveTopic(boolean containsSensitiveTopic) {
        this.containsSensitiveTopic = containsSensitiveTopic;
    }

    public boolean isContainsConflictSignals() {
        return containsConflictSignals;
    }

    public void setContainsConflictSignals(boolean containsConflictSignals) {
        this.containsConflictSignals = containsConflictSignals;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }
}
