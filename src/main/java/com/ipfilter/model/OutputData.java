package com.ipfilter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OutputData {
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("results")
    private List<FilterResult> results;
    @JsonProperty("statistics")
    private FilterStatistics statistics;
    @JsonProperty("recommendation")
    private String recommendation;

    // Getters and Setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public List<FilterResult> getResults() { return results; }
    public void setResults(List<FilterResult> results) { this.results = results; }
    public FilterStatistics getStatistics() { return statistics; }
    public void setStatistics(FilterStatistics statistics) { this.statistics = statistics; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}