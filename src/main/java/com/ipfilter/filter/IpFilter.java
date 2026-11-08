package com.ipfilter.filter;

public interface IpFilter {
    void add(String ip);
    boolean contains(String ip);
    long getMemoryUsageBytes();
    void clear();
    int getSize();
}
