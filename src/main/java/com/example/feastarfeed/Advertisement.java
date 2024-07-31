package com.example.feastarfeed;

public class Advertisement {
    private final String ADUrl;
    private final Long Aid;


    public Advertisement(String ADUrl, long Aid) {
        if (ADUrl == null) {
            throw new IllegalArgumentException("VideoUrl cannot be null");
        }

        this.ADUrl = ADUrl;
        this.Aid = Aid;
    }

    public String getADUrl() {return ADUrl;}

    public Long getAId() { return Aid; }
}
