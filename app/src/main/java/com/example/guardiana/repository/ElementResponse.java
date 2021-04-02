package com.example.guardiana.repository;

import com.example.guardiana.model.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ElementResponse {
    private List<Element> elementList;
    private int statusCode;
    private String message;
    private int flag;

    public ElementResponse() {
        elementList = new ArrayList<>();
    }

    public ElementResponse(int statusCode) {
        this();
        this.statusCode = statusCode;
    }

    public ElementResponse(List<Element> elementList, int statusCode, String message, int flag) {
        this.elementList = elementList;
        this.statusCode = statusCode;
        this.message = message;
        this.flag = flag;
    }

    public List<Element> getElementList() {
        return elementList;
    }

    public void setElementList(List<Element> elementList) {
        this.elementList = elementList;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "ElementResponse{" +
                "elementList=" + elementList +
                ", statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", flag=" + flag +
                '}';
    }
}
