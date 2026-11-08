package com.ipfilter.service;

import com.ipfilter.model.OutputData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterServiceBoundaryTest {

    private final FilterService service = new FilterService();

    @Test
    void testBoundary_ZeroIps() {
        OutputData result = service.processAndCompare(List.of());
        assertEquals(0, result.getResults().size());
    }

    @Test
    void testBoundary_OneIp() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        assertEquals(1, result.getResults().size());
    }

    @Test
    void testBoundary_VeryLongIp() {
        String longIp = "192.168.1.1";
        OutputData result = service.processAndCompare(List.of(longIp));
        assertNotNull(result);
    }

    @Test
    void testBoundary_SpecialCharacters() {
        OutputData result = service.processAndCompare(List.of("invalid.ip"));
        assertNotNull(result);
    }
}
