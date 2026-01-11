package com.hotel.common;

import java.io.Serializable;

public class Request implements Serializable {
    private RequestType type;
    private Object data;

    public Request(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() { return type; }
    public Object getData() { return data; }
}