package com.ipfilter.service;

import com.ipfilter.model.OutputData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterServiceEdgeCaseTest {

    private final FilterService service = new FilterService();

    @Test
    void testSingleElement() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        assertEquals(1, result.getResults().size());
    }

    @Test
    void testTwoElements() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1", "2.2.2.2"));
        assertEquals(2, result.getResults().size());
    }

    @Test
    void testHundredElements() {
        java.util.List<String> ips = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ips.add(i + "." + i + "." + i + "." + i);
        }
        OutputData result = service.processAndCompare(ips);
        assertEquals(100, result.getResults().size());
    }

    @Test
    void testRecommendation_NotEmpty() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        assertFalse(result.getRecommendation().isEmpty());
        assertTrue(result.getRecommendation().length() >= 10);
    }

    @Test
    void testStatistics_AllValuesValid() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));

        assertTrue(result.getStatistics().getHashSetMemoryBytes() >= 0);
        assertTrue(result.getStatistics().getBloomFilterMemoryBytes() >= 0);
        assertTrue(result.getStatistics().getHashSetTimeMs() >= 0);
        assertTrue(result.getStatistics().getBloomFilterTimeMs() >= 0);
        assertTrue(result.getStatistics().getFalsePositiveProbability() >= 0);
        assertTrue(result.getStatistics().getFalsePositiveProbability() <= 1);
    }
}