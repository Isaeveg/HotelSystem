package com.hotel.common;

import java.io.Serializable;

/**
 * Represents a network response sent from server to client.
 */
public class Response implements Serializable {
    private boolean success;
    private String message;
    private Object data;

    /**
     * Constructs a new Response.
     *
     * @param success indicates if the request was processed successfully
     * @param message a descriptive message about the result
     * @param data    any data returned by the server
     */
    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}