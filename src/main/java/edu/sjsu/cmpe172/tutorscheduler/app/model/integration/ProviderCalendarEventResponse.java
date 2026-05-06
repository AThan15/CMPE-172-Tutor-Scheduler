package edu.sjsu.cmpe172.tutorscheduler.app.model.integration;

public class ProviderCalendarEventResponse {

    private String externalEventId;
    private String status;
    private String message;
    private String syncedAt;

    public String getExternalEventId() {
        return externalEventId;
    }

    public void setExternalEventId(String externalEventId) {
        this.externalEventId = externalEventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(String syncedAt) {
        this.syncedAt = syncedAt;
    }
}
