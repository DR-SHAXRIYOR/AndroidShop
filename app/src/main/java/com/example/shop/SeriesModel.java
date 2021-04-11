package com.example.shop;

import java.io.Serializable;

public class SeriesModel implements Serializable {
    private Integer id;
    private Integer client_id;
    private Integer userId;
    private String seriya;

    public SeriesModel() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClient_id() {
        return client_id;
    }

    public void setClient_id(Integer client_id) {
        this.client_id = client_id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSeriya() {
        return seriya;
    }

    public void setSeriya(String seriya) {
        this.seriya = seriya;
    }
}