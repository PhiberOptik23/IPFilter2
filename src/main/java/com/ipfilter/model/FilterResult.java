package com.ipfilter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilterResult {
    @JsonProperty("ip")
    private String ip;
    @JsonProperty("hashSetResult")
    private boolean hashSetResult;
    @JsonProperty("bloomFilterResult")
    private boolean bloomFilterResult;
    @JsonProperty("match")
    private boolean match; // Совпадают ли результаты

    public FilterResult() {}

    public FilterResult(String ip, boolean hashSetResult, boolean bloomFilterResult) {
        this.ip = ip;
        this.hashSetResult = hashSetResult;
        this.bloomFilterResult = bloomFilterResult;
        this.match = (hashSetResult == bloomFilterResult);
    }

    // Getters and Setters
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public boolean isHashSetResult() { return hashSetResult; }
    public void setHashSetResult(boolean hashSetResult) { this.hashSetResult = hashSetResult; }
    public boolean isBloomFilterResult() { return bloomFilterResult; }
    public void setBloomFilterResult(boolean bloomFilterResult) { this.bloomFilterResult = bloomFilterResult; }
    public boolean isMatch() { return match; }
    public void setMatch(boolean match) { this.match = match; }
}