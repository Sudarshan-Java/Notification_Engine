package com.example.notificationbatch.dto;

public class StatusUpdateDTO {
    private String status;
    private Integer retryIncrement;

    public StatusUpdateDTO() {}

    public StatusUpdateDTO(String status, Integer retryIncrement) {
        this.status = status;
        this.retryIncrement = retryIncrement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryIncrement() {
        return retryIncrement;
    }

    public void setRetryIncrement(Integer retryIncrement) {
        this.retryIncrement = retryIncrement;
    }
}