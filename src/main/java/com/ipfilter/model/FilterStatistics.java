package com.ipfilter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilterStatistics {
    @JsonProperty("hashSetMemoryBytes")
    private long hashSetMemoryBytes;
    @JsonProperty("bloomFilterMemoryBytes")
    private long bloomFilterMemoryBytes;
    @JsonProperty("hashSetTimeMs")
    private long hashSetTimeMs;
    @JsonProperty("bloomFilterTimeMs")
    private long bloomFilterTimeMs;
    @JsonProperty("falsePositiveProbability")
    private double falsePositiveProbability;

    // Getters and Setters
    public long getHashSetMemoryBytes() { return hashSetMemoryBytes; }
    public void setHashSetMemoryBytes(long hashSetMemoryBytes) { this.hashSetMemoryBytes = hashSetMemoryBytes; }
    public long getBloomFilterMemoryBytes() { return bloomFilterMemoryBytes; }
    public void setBloomFilterMemoryBytes(long bloomFilterMemoryBytes) { this.bloomFilterMemoryBytes = bloomFilterMemoryBytes; }
    public long getHashSetTimeMs() { return hashSetTimeMs; }
    public void setHashSetTimeMs(long hashSetTimeMs) { this.hashSetTimeMs = hashSetTimeMs; }
    public long getBloomFilterTimeMs() { return bloomFilterTimeMs; }
    public void setBloomFilterTimeMs(long bloomFilterTimeMs) { this.bloomFilterTimeMs = bloomFilterTimeMs; }
    public double getFalsePositiveProbability() { return falsePositiveProbability; }
    public void setFalsePositiveProbability(double falsePositiveProbability) { this.falsePositiveProbability = falsePositiveProbability; }
}