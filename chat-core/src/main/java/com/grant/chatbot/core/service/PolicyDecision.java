package com.grant.chatbot.core.service;

public class PolicyDecision {
    private String personaVariant = "default";
    private boolean humorAllowed = false;
    private boolean autoSendAllowed = false;
    private boolean approvalRequired = true;
    private int maxWords = 60;

    public String getPersonaVariant() {
        return personaVariant;
    }

    public void setPersonaVariant(String personaVariant) {
        this.personaVariant = personaVariant;
    }

    public boolean isHumorAllowed() {
        return humorAllowed;
    }

    public void setHumorAllowed(boolean humorAllowed) {
        this.humorAllowed = humorAllowed;
    }

    public boolean isAutoSendAllowed() {
        return autoSendAllowed;
    }

    public void setAutoSendAllowed(boolean autoSendAllowed) {
        this.autoSendAllowed = autoSendAllowed;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public int getMaxWords() {
        return maxWords;
    }

    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }
}
