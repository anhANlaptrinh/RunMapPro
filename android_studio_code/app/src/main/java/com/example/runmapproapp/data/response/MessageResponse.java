package com.example.runmapproapp.data.response;

/**
 * Standard API response for admin operations
 */
public class MessageResponse {
    private String status;
    private String message;
    
    public MessageResponse() {
    }
    
    public MessageResponse(String status, String message) {
        this.status = status;
        this.message = message;
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
    
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
