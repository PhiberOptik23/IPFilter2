package com.ipfilter.filter;

import java.util.HashSet;
import java.util.Set;

public class HashSetFilter implements IpFilter {
    private final Set<String> blacklist;

    public HashSetFilter() {
        this.blacklist = new HashSet<>();
    }

    @Override
    public void add(String ip) {
        if (ip != null && !ip.isEmpty()) {
            blacklist.add(ip);
        }
    }

    @Override
    public boolean contains(String ip) {
        // Branch Testing point: null check
        if (ip == null) {
            return false;
        }
        return blacklist.contains(ip);
    }

    @Override
    public long getMemoryUsageBytes() {
        // Approximation: Object overhead + String size + HashSet overhead
        // Roughly 64 bytes per entry + string content
        return blacklist.size() * 100L;
    }

    @Override
    public void clear() {
        blacklist.clear();
    }

    @Override
    public int getSize() {
        return blacklist.size();
    }
}
