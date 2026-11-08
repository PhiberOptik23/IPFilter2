package com.ipfilter.filter;

import java.util.BitSet;
import java.util.zip.CRC32;

public class BloomFilter implements IpFilter {
    private final BitSet bitSet;
    private final int size;
    private final int hashFunctionsCount;
    private final long seed;
    private int elementsCount = 0;

    // Конструктор: размер битового массива, кол-во хэш-функций, сид
    public BloomFilter(int size, int hashFunctionsCount, long seed) {
        this.size = size;
        this.hashFunctionsCount = hashFunctionsCount;
        this.seed = seed;
        this.bitSet = new BitSet(size);
    }

    @Override
    public void add(String ip) {
        if (ip == null || ip.isEmpty()) return;
        for (int i = 0; i < hashFunctionsCount; i++) {
            int index = getHash(ip, i) % size;
            bitSet.set(index);
        }
        elementsCount++;
    }

    @Override
    public boolean contains(String ip) {
        if (ip == null) return false;
        for (int i = 0; i < hashFunctionsCount; i++) {
            int index = getHash(ip, i) % size;
            if (!bitSet.get(index)) {
                return false; // Точно нет
            }
        }
        return true; // Возможно есть (False Positive possible)
    }

    // Детерминированная хэш-функция на основе CRC32 и сида
    private int getHash(String ip, int iteration) {
        CRC32 crc = new CRC32();
        String input = ip + "_" + iteration + "_" + seed;
        crc.update(input.getBytes());
        return Math.abs((int) crc.getValue()); // Всегда положительное!
    }

    @Override
    public long getMemoryUsageBytes() {
        return size / 8L + 32L; // Биты в байты + оверхед объекта
    }

    @Override
    public void clear() {
        bitSet.clear();
        elementsCount = 0;
    }

    @Override
    public int getSize() {
        return elementsCount;
    }

    // Расчет вероятности False Positive: (1 - e^(-kn/m))^k
    public double getFalsePositiveProbability() {
        if (size == 0 || elementsCount == 0) return 0.0;
        double m = size;
        double k = hashFunctionsCount;
        double n = elementsCount;
        return Math.pow((1 - Math.exp(-k * n / m)), k);
    }
}
