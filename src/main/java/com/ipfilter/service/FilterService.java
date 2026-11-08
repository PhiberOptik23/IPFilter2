package com.ipfilter.service;

import com.ipfilter.filter.BloomFilter;
import com.ipfilter.filter.HashSetFilter;
import com.ipfilter.filter.IpFilter;
import com.ipfilter.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FilterService {
    private final IpFilter hashSetFilter;
    private final IpFilter bloomFilter;
    private final JsonHandler jsonHandler;

    public FilterService() {
        this.hashSetFilter = new HashSetFilter();
        this.bloomFilter = new BloomFilter(10000, 3, 42L);
        this.jsonHandler = new JsonHandler();
    }

    // Конструктор для тестов с моками
    public FilterService(IpFilter hashSetFilter, IpFilter bloomFilter, JsonHandler jsonHandler) {
        this.hashSetFilter = hashSetFilter;
        this.bloomFilter = bloomFilter;
        this.jsonHandler = jsonHandler;
    }


    public void loadDataFromFile(String filePath) throws Exception {
        InputData data = jsonHandler.loadInput(filePath);
        if (data.getBlacklist() != null) {
            for (String ip : data.getBlacklist()) {
                hashSetFilter.add(ip);
                bloomFilter.add(ip);
            }
        }
    }

    public OutputData processAndCompare(List<String> ipsToCheck) {
        List<FilterResult> results = new ArrayList<>();

        // Обработка null списка
        if (ipsToCheck == null) {
            ipsToCheck = List.of();
        }

        long startHash = System.nanoTime();
        long startBloom = System.nanoTime();

        for (String ip : ipsToCheck) {
            // Обработка null IP
            if (ip == null) {
                results.add(new FilterResult(null, false, false));
                continue;
            }

            boolean hashRes = hashSetFilter.contains(ip);
            boolean bloomRes = bloomFilter.contains(ip);
            results.add(new FilterResult(ip, hashRes, bloomRes));
        }

        long endHash = System.nanoTime();
        long endBloom = System.nanoTime();

        FilterStatistics stats = new FilterStatistics();
        stats.setHashSetMemoryBytes(hashSetFilter.getMemoryUsageBytes());
        stats.setBloomFilterMemoryBytes(bloomFilter.getMemoryUsageBytes());
        stats.setHashSetTimeMs((endHash - startHash) / 1_000_000);
        stats.setBloomFilterTimeMs((endBloom - startBloom) / 1_000_000);

        if (bloomFilter instanceof BloomFilter) {
            stats.setFalsePositiveProbability(((BloomFilter) bloomFilter).getFalsePositiveProbability());
        }

        String recommendation = generateRecommendation(stats);

        OutputData output = new OutputData();
        output.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        output.setResults(results);
        output.setStatistics(stats);
        output.setRecommendation(recommendation);

        return output;
    }

    private String generateRecommendation(FilterStatistics stats) {
        long memoryDiff = stats.getHashSetMemoryBytes() - stats.getBloomFilterMemoryBytes();
        if (memoryDiff > 0) {
            double percent = (double) memoryDiff / stats.getHashSetMemoryBytes() * 100;
            return String.format("Рекомендуется Bloom Filter. Экономия памяти: %.1f%%. Вероятность ошибки: %.4f", percent, stats.getFalsePositiveProbability());
        } else {
            return "Рекомендуется HashSet. Объем данных мал, точность приоритетнее экономии памяти.";
        }
    }

    public void saveResults(OutputData data, String path) throws Exception {
        jsonHandler.saveOutput(path, data);
    }
}