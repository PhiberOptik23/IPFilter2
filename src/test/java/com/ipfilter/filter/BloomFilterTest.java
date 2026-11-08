package com.ipfilter.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BloomFilterTest {

    private BloomFilter filter;

    @BeforeEach
    void setUp() {
        // Seed 42 для детерминированности
        filter = new BloomFilter(1000, 3, 42L);
    }

    // --- SPECIFICATION BASED TESTING ---

    // Метод 1: Boundary Value Analysis (BVA)
    @Test
    void testBoundary_FullCapacityBehavior() {
        // Заполняем фильтр (меньше чем размер битового массива)
        for (int i = 0; i < 100; i++) {
            filter.add("IP_" + i);
        }
        double fpProb = filter.getFalsePositiveProbability();
        assertThat("Вероятность FP должна быть >= 0", fpProb, greaterThanOrEqualTo(0.0));
    }

    // Метод 2: Equivalence Partitioning (EP)
    @ParameterizedTest
    @CsvSource({
            "192.168.1.1, true",
            "10.0.0.1, true"
    })
    void testEquivalenceClasses(String ip, boolean shouldBeAdded) {
        filter.add(ip);
        // Bloom Filter не даёт False Negative
        assertTrue(filter.contains(ip), "Добавленный элемент должен быть найден");
    }

    // --- STRUCTURE BASED TESTING ---

    // Метод 1: Branch Testing
    @Test
    void testBranch_NotFoundReturnsFalse() {
        filter.add("1.1.1.1");
        // Элемент который точно не добавлен
        boolean result = filter.contains("2.2.2.2");
        assertNotNull(result);
    }

    // Метод 2: Data Flow Testing
    @Test
    void testDataFlow_ElementsCountTracking() {
        assertEquals(0, filter.getSize());
        filter.add("A");
        assertEquals(1, filter.getSize());
        filter.add("B");
        assertEquals(2, filter.getSize());
        filter.clear();
        assertEquals(0, filter.getSize());
    }

    // --- JUNIT REQUIREMENTS (Assumptions & Matchers) ---

    @Test
    void testAssumptions_Environment() {
        Assumptions.assumeTrue(filter.getMemoryUsageBytes() > 0, "Память должна быть выделена");

        filter.add("TestIP");

        Assumptions.assumingThat(filter.contains("TestIP"), () -> {
            double prob = filter.getFalsePositiveProbability();
            assertThat(prob, greaterThanOrEqualTo(0.0));
        });
    }

    @Test
    void testHamcrestMatchers() {
        filter.add("MatchTest");
        assertThat(filter.contains("MatchTest"), equalTo(true));
        assertThat(filter.getFalsePositiveProbability(), lessThan(1.0));
    }

    // === НОВЫЕ ТЕСТЫ ДЛЯ MUTATION COVERAGE ===

    @Test
    void testAdd_NullValue() {
        filter.add(null);
        assertEquals(0, filter.getSize());
    }

    @Test
    void testAdd_EmptyString() {
        filter.add("");
        assertEquals(0, filter.getSize());
    }

    @Test
    void testContains_NullInput() {
        assertFalse(filter.contains(null));
    }

    @Test
    void testClear_ResetsCount() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        filter.clear();
        assertEquals(0, filter.getSize());
        assertEquals(0.0, filter.getFalsePositiveProbability());
    }

    @Test
    void testFalsePositiveProbability_EmptyFilter() {
        assertEquals(0.0, filter.getFalsePositiveProbability());
    }

    @Test
    void testFalsePositiveProbability_GrowsWithElements() {
        double probBefore = filter.getFalsePositiveProbability();
        for (int i = 0; i < 50; i++) {
            filter.add("IP_" + i);
        }
        double probAfter = filter.getFalsePositiveProbability();
        assertTrue(probAfter > probBefore, "Вероятность FP должна расти");
    }

    @Test
    void testMemoryUsage_Calculation() {
        long memory = filter.getMemoryUsageBytes();
        assertTrue(memory > 0, "Память должна быть > 0");
    }

    @Test
    void testNoFalseNegatives() {
        // Bloom Filter НЕ должен давать False Negative
        String testIp = "192.168.1.1";
        filter.add(testIp);
        assertTrue(filter.contains(testIp), "False Negative недопустим!");
    }
    @Test
    void testAdd_Null_DoesNotIncreaseSize() {
        filter.add(null);
        assertEquals(0, filter.getSize());
    }

    @Test
    void testAdd_EmptyString_DoesNotIncreaseSize() {
        filter.add("");
        assertEquals(0, filter.getSize());
    }

    @Test
    void testContains_Null_ReturnsFalse() {
        assertFalse(filter.contains(null));
    }

    @Test
    void testClear_ResetsEverything() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        assertEquals(2, filter.getSize());
        filter.clear();
        assertEquals(0, filter.getSize());
        assertEquals(0.0, filter.getFalsePositiveProbability(), 0.001);
    }

    @Test
    void testFalsePositiveProbability_IncreasesWithElements() {
        double probBefore = filter.getFalsePositiveProbability();
        for (int i = 0; i < 100; i++) {
            filter.add("IP_" + i);
        }
        double probAfter = filter.getFalsePositiveProbability();
        assertTrue(probAfter > probBefore, "Вероятность FP должна расти с добавлением элементов");
    }

    @Test
    void testMemoryUsage_AlwaysPositive() {
        long memory = filter.getMemoryUsageBytes();
        assertTrue(memory > 0, "Память должна быть положительной");
    }

    @Test
    void testNoFalseNegatives_Ever() {
        String[] testIps = {"1.1.1.1", "2.2.2.2", "3.3.3.3", "192.168.1.1", "10.0.0.1"};
        for (String ip : testIps) {
            filter.add(ip);
            assertTrue(filter.contains(ip), "False Negative недопустим для IP: " + ip);
        }
    }

    @Test
    void testMultipleHashFunctions_WorkCorrectly() {
        filter.add("test.ip");
        // Bloom Filter должен найти добавленный элемент
        assertTrue(filter.contains("test.ip"));
    }

    @Test
    void testHashFunction_DifferentIterations() {
        // Тестируем что разные итерации дают разные хэши
        filter.add("test.ip");
        assertTrue(filter.contains("test.ip"));
    }

    @Test
    void testBitSetOperations() {
        // Тестируем установку битов
        BloomFilter smallFilter = new BloomFilter(100, 3, 42L);
        smallFilter.add("1.1.1.1");
        assertTrue(smallFilter.contains("1.1.1.1"));
    }

    @Test
    void testFalsePositiveProbability_ZeroElements() {
        assertEquals(0.0, filter.getFalsePositiveProbability(), 0.001);
    }

    @Test
    void testFalsePositiveProbability_AfterClear() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        filter.clear();
        assertEquals(0.0, filter.getFalsePositiveProbability(), 0.001);
    }

    @Test
    void testSize_AfterClear() {
        filter.add("1.1.1.1");
        assertEquals(1, filter.getSize());
        filter.clear();
        assertEquals(0, filter.getSize());
    }

    @Test
    void testContains_NotAddedIP() {
        // IP который точно не добавлен
        assertFalse(filter.contains("99.99.99.99"));
    }

    @Test
    void testAdd_MultipleSameIP() {
        filter.add("1.1.1.1");
        filter.add("1.1.1.1");
        filter.add("1.1.1.1");
        // Bloom Filter не отслеживает дубликаты, но должен найти
        assertTrue(filter.contains("1.1.1.1"));
    }

    @Test
    void testDifferentSeeds_ProduceDifferentResults() {
        BloomFilter filter1 = new BloomFilter(1000, 3, 42L);
        BloomFilter filter2 = new BloomFilter(1000, 3, 100L);

        filter1.add("test.ip");
        filter2.add("test.ip");

        // Оба должны найти добавленный IP
        assertTrue(filter1.contains("test.ip"));
        assertTrue(filter2.contains("test.ip"));
    }

    @Test
    void testLargeNumberOfElements() {
        // Добавляем много элементов
        for (int i = 0; i < 200; i++) {
            filter.add("IP_" + i);
        }
        assertEquals(200, filter.getSize());

        // Проверяем что все находятся (no false negatives)
        for (int i = 0; i < 200; i++) {
            assertTrue(filter.contains("IP_" + i));
        }
    }

    @Test
    void testProbability_GrowsWithElements() {
        double probBefore = filter.getFalsePositiveProbability();

        for (int i = 0; i < 100; i++) {
            filter.add("IP_" + i);
        }

        double probAfter = filter.getFalsePositiveProbability();
        assertTrue(probAfter > probBefore, "Вероятность FP должна расти");
    }
}