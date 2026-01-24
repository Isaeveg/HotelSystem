package com.hotel.common;

import java.io.Serializable;

/**
 * Represents a network request sent from client to server.
 */
public class Request implements Serializable {
    private RequestType type;
    private Object data;

    /**
     * Constructs a new Request.
     *
     * @param type the type of request
     * @param data the payload data associated with the request
     */
    public Request(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}