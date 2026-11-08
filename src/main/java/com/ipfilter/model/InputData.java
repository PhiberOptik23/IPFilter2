package com.ipfilter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class InputData {
    @JsonProperty("blacklist")
    private List<String> blacklist;
    @JsonProperty("checkIps")
    private List<String> checkIps;

    public List<String> getBlacklist() { return blacklist; }
    public void setBlacklist(List<String> blacklist) { this.blacklist = blacklist; }
    public List<String> getCheckIps() { return checkIps; }
    public void setCheckIps(List<String> checkIps) { this.checkIps = checkIps; }
}