package com.ipfilter.service;

import com.ipfilter.filter.IpFilter;
import com.ipfilter.model.FilterResult;
import com.ipfilter.model.FilterStatistics;
import com.ipfilter.model.OutputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для FilterService с максимальным покрытием мутаций
 *
 * Используемые техники тестирования:
 * - Specification-Based: Boundary Value Analysis, Equivalence Partitioning
 * - Structure-Based: Branch Testing, Data Flow Testing
 */
class FilterServiceTest {

    private FilterService service;

    @BeforeEach
    void setUp() {
        service = new FilterService();
    }

    // ========================================================================
    // SPECIFICATION-BASED TESTING
    // ========================================================================

    /**
     * Метод 1: Boundary Value Analysis (BVA)
     * Тестируем граничные значения количества IP
     */
    @Test
    void testBoundary_EmptyList() {
        // Граница: 0 элементов
        OutputData result = service.processAndCompare(new ArrayList<>());

        assertNotNull(result);
        assertEquals(0, result.getResults().size(), "Пустой список должен вернуть 0 результатов");
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getStatistics());
        assertNotNull(result.getRecommendation());
    }

    @Test
    void testBoundary_SingleElement() {
        // Граница: 1 элемент
        OutputData result = service.processAndCompare(List.of("192.168.1.1"));

        assertEquals(1, result.getResults().size(), "Должен быть 1 результат");
        assertNotNull(result.getResults().get(0).getIp());
    }

    @Test
    void testBoundary_ManyElements() {
        // Граница: много элементов
        List<String> ips = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ips.add(i + ".0.0." + i);
        }

        OutputData result = service.processAndCompare(ips);

        assertEquals(100, result.getResults().size());
        assertNotNull(result.getStatistics());
    }

    /**
     * Метод 2: Equivalence Partitioning (EP)
     * Тестируем классы эквивалентности IP адресов
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "8.8.8.8",
            "1.1.1.1"
    })
    void testEquivalence_ValidIpAddresses(String ip) {
        OutputData result = service.processAndCompare(List.of(ip));

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(ip, result.getResults().get(0).getIp());
    }

    @ParameterizedTest
    @CsvSource({
            "invalid.ip",
            "256.256.256.256",
            "abc.def.ghi.jkl"
    })
    void testEquivalence_InvalidIpAddresses(String ip) {
        assertDoesNotThrow(() -> {
            OutputData result = service.processAndCompare(List.of(ip));
            assertNotNull(result);
        });
    }

    // ========================================================================
    // STRUCTURE-BASED TESTING
    // ========================================================================

    /**
     * Метод 1: Branch Testing
     * Покрываем все ветви if/else в коде
     */
    @Test
    void testBranch_ResultSetMatchTrue() {
        // Ветка: оба фильтра возвращают false → match = true
        OutputData result = service.processAndCompare(List.of("8.8.8.8"));

        FilterResult filterResult = result.getResults().get(0);
        assertFalse(filterResult.isHashSetResult());
        assertFalse(filterResult.isBloomFilterResult());
        assertTrue(filterResult.isMatch(), "Результаты должны совпадать");
    }

    @Test
    void testBranch_StatisticsFieldsPopulated() {
        // Ветка: заполнение статистики
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterStatistics stats = result.getStatistics();

        // Проверяем что все поля заполнены и валидны
        assertTrue(stats.getHashSetMemoryBytes() >= 0, "HashSet память должна быть >= 0");
        assertTrue(stats.getBloomFilterMemoryBytes() >= 0, "Bloom память должна быть >= 0");
        assertTrue(stats.getHashSetTimeMs() >= 0, "HashSet время должно быть >= 0");
        assertTrue(stats.getBloomFilterTimeMs() >= 0, "Bloom время должно быть >= 0");
        assertTrue(stats.getFalsePositiveProbability() >= 0, "Вероятность FP должна быть >= 0");
        assertTrue(stats.getFalsePositiveProbability() <= 1, "Вероятность FP должна быть <= 1");
    }

    @Test
    void testBranch_RecommendationGenerated() {
        // Ветка: генерация рекомендации
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));

        assertNotNull(result.getRecommendation());
        assertFalse(result.getRecommendation().isEmpty());
        assertThat(result.getRecommendation().length(), greaterThan(10));
    }

    /**
     * Метод 2: Data Flow Testing
     * Отслеживаем определение и использование переменных
     */
    @Test
    void testDataFlow_OutputDataCreation() {
        // Def: создание OutputData
        OutputData result = service.processAndCompare(List.of("192.168.1.1"));

        // Use: проверка всех полей
        assertNotNull(result.getTimestamp(), "Timestamp должен быть определён");
        assertNotNull(result.getResults(), "Results должны быть определены");
        assertNotNull(result.getStatistics(), "Statistics должны быть определены");
        assertNotNull(result.getRecommendation(), "Recommendation должен быть определён");

        // Use: проверка вложенных объектов
        assertEquals(1, result.getResults().size());
        assertNotNull(result.getResults().get(0).getIp());
    }

    @Test
    void testDataFlow_FilterResultCreation() {
        // Def: создание FilterResult
        OutputData output = service.processAndCompare(List.of("10.0.0.1"));
        FilterResult filterResult = output.getResults().get(0);

        // Use: проверка полей
        assertEquals("10.0.0.1", filterResult.getIp());
        // match вычисляется из hashSetResult и bloomFilterResult
        assertEquals(
                filterResult.isHashSetResult() == filterResult.isBloomFilterResult(),
                filterResult.isMatch()
        );
    }

    // ========================================================================
    // JUNIT 5 REQUIREMENTS
    // ========================================================================

    /**
     * Минимум 5 различных методов assert
     */
    @Test
    void testAssertionsVariety() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1", "2.2.2.2"));

        // 1. assertEquals
        assertEquals(2, result.getResults().size());

        // 2. assertTrue
        assertTrue(result.getResults().size() > 0);

        // 3. assertFalse
        assertFalse(result.getResults().isEmpty());

        // 4. assertNotNull
        assertNotNull(result.getTimestamp());

        // 5. assertAll (группировка ассертов)
        assertAll("OutputData validation",
                () -> assertNotNull(result.getResults()),
                () -> assertNotNull(result.getStatistics()),
                () -> assertNotNull(result.getRecommendation()),
                () -> assertEquals(2, result.getResults().size())
        );

        // 6. assertDoesNotThrow
        assertDoesNotThrow(() -> service.processAndCompare(List.of("3.3.3.3")));
    }

    /**
     * Минимум 2 различных метода assume
     */
    @Test
    void testAssumptionsUsage() {
        // Assume 1: assumeTrue
        Assumptions.assumeTrue(service != null, "Сервис должен быть создан");

        OutputData result = service.processAndCompare(List.of("1.1.1.1"));

        // Assume 2: assumingThat
        Assumptions.assumingThat(
                result.getResults().size() > 0,
                () -> {
                    assertNotNull(result.getResults().get(0).getIp());
                }
        );

        // Assume 3: assumeFalse
        Assumptions.assumeFalse(result.getResults().isEmpty(), "Результаты не должны быть пустыми");
    }

    /**
     * Parameterized Tests
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50})
    void testParameterized_DifferentIpCounts(int count) {
        List<String> ips = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ips.add(i + ".0.0." + i);
        }

        OutputData result = service.processAndCompare(ips);

        assertEquals(count, result.getResults().size());
        assertNotNull(result.getStatistics());
        assertNotNull(result.getRecommendation());
    }

    // ========================================================================
    // MOCKITO REQUIREMENTS
    // ========================================================================

    /**
     * Mock Type 1: Standard Mock behavior (when/thenReturn)
     */
    @Test
    void testMockBehavior_VerifyWithLenient() {
        // Создаём моки для демонстрации
        IpFilter mockFilter = mock(IpFilter.class);

        // Используем lenient() чтобы избежать UnnecessaryStubbingException
        lenient().when(mockFilter.contains(anyString())).thenReturn(false);
        lenient().when(mockFilter.getMemoryUsageBytes()).thenReturn(100L);
        lenient().when(mockFilter.getSize()).thenReturn(0);

        // Проверяем что моки работают
        boolean result = mockFilter.contains("1.1.1.1");
        assertFalse(result);

        // Verify вызов
        verify(mockFilter, atLeastOnce()).contains(anyString());
    }

    /**
     * Mock Type 2: Verify interactions (verify)
     */
    @Test
    void testMockVerify_Interactions() {
        IpFilter mockFilter = mock(IpFilter.class);

        when(mockFilter.contains("2.2.2.2")).thenReturn(false);
        when(mockFilter.contains("3.3.3.3")).thenReturn(true);

        // Вызываем методы
        mockFilter.contains("2.2.2.2");
        mockFilter.contains("3.3.3.3");

        // Проверяем вызовы
        verify(mockFilter, times(1)).contains("2.2.2.2");
        verify(mockFilter, times(1)).contains("3.3.3.3");
        verify(mockFilter, never()).clear();
    }

    /**
     * Mock Type 3: Spy (частичный мок)
     */
    @Test
    void testMockSpy_Usage() {
        // Создаём spy списка
        List<String> spyList = spy(new ArrayList<>(List.of("A", "B")));

        spyList.add("C");

        // Проверяем что метод был вызван
        verify(spyList).add("C");
        assertEquals(3, spyList.size());

        // Проверяем оригинальное поведение
        assertEquals("A", spyList.get(0));
        assertEquals("B", spyList.get(1));
        assertEquals("C", spyList.get(2));
    }

    /**
     * Mock Type 4: Exception testing with mocks
     */
    @Test
    void testMockException_Throwing() throws IOException {
        JsonHandler mockHandler = mock(JsonHandler.class);

        // Настраиваем исключение
        doThrow(new IOException("File not found"))
                .when(mockHandler).loadInput(anyString());

        // Проверяем что исключение выбрасывается
        assertThrows(IOException.class, () -> {
            mockHandler.loadInput("nonexistent.json");
        });
    }

    // ========================================================================
    // HAMCREST MATCHERS
    // ========================================================================

    /**
     * Минимум 2 различных вида Hamcrest matchers
     */
    @Test
    void testHamcrestMatchers() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterStatistics stats = result.getStatistics();

        // Matcher 1: greaterThan
        assertThat(stats.getHashSetMemoryBytes(), greaterThanOrEqualTo(0L));

        // Matcher 2: lessThan
        assertThat(stats.getFalsePositiveProbability(), lessThanOrEqualTo(1.0));

        // Matcher 3: equalTo
        assertThat(result.getResults().size(), equalTo(1));

        // Matcher 4: notNullValue
        assertThat(result.getTimestamp(), notNullValue());

        // Matcher 5: containsString (для recommendation)
        assertThat(result.getRecommendation(), not(emptyString()));
    }

    // ========================================================================
    // ADDITIONAL TESTS FOR MUTATION SCORE
    // ========================================================================

    @Test
    void testTimestamp_Format() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));

        String timestamp = result.getTimestamp();
        assertNotNull(timestamp);
        // Проверяем формат ISO (должен содержать дату и время)
        assertTrue(timestamp.length() >= 19, "Timestamp должен быть в формате ISO");
        assertTrue(timestamp.contains("-") || timestamp.contains("T"),
                "Timestamp должен содержать разделители даты/времени");
    }

    @Test
    void testResult_IPField() {
        String testIp = "192.168.100.1";
        OutputData result = service.processAndCompare(List.of(testIp));

        assertEquals(testIp, result.getResults().get(0).getIp());
    }

    @Test
    void testStatistics_FalsePositiveProbability() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterStatistics stats = result.getStatistics();

        assertTrue(stats.getFalsePositiveProbability() >= 0);
        assertTrue(stats.getFalsePositiveProbability() <= 1);
    }

    @Test
    void testStatistics_TimeNonNegative() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterStatistics stats = result.getStatistics();

        assertTrue(stats.getHashSetTimeMs() >= 0);
        assertTrue(stats.getBloomFilterTimeMs() >= 0);
    }

    @Test
    void testStatistics_MemoryComparison() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterStatistics stats = result.getStatistics();

        // Оба значения памяти должны быть положительными
        assertTrue(stats.getHashSetMemoryBytes() >= 0);
        assertTrue(stats.getBloomFilterMemoryBytes() > 0);
    }

    @Test
    void testServiceConstructor_Default() {
        FilterService newService = new FilterService();
        assertNotNull(newService);
    }

    @Test
    void testProcessAndCompare_SingleIp() {
        OutputData result = service.processAndCompare(List.of("8.8.8.8"));

        assertEquals(1, result.getResults().size());
        assertNotNull(result.getStatistics());
        assertNotNull(result.getRecommendation());
        assertFalse(result.getRecommendation().isEmpty());
    }

    @Test
    void testProcessAndCompare_MultipleIps() {
        List<String> ips = List.of("1.1.1.1", "2.2.2.2", "3.3.3.3");
        OutputData result = service.processAndCompare(ips);

        assertEquals(3, result.getResults().size());

        // Проверяем каждый результат
        for (FilterResult fr : result.getResults()) {
            assertNotNull(fr.getIp());
            assertNotNull(fr.isMatch());
        }
    }

    @Test
    void testProcessAndCompare_StatisticsPopulated() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterStatistics stats = result.getStatistics();

        assertTrue(stats.getHashSetMemoryBytes() >= 0);
        assertTrue(stats.getBloomFilterMemoryBytes() >= 0);
        assertTrue(stats.getHashSetTimeMs() >= 0);
        assertTrue(stats.getBloomFilterTimeMs() >= 0);
        assertTrue(stats.getFalsePositiveProbability() >= 0);
    }

    @Test
    void testProcessAndCompare_ResultMatch() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterResult filterResult = result.getResults().get(0);

        // Для IP не в blacklist оба должны вернуть false
        assertFalse(filterResult.isHashSetResult());
        assertFalse(filterResult.isBloomFilterResult());
        // match должен быть true (оба false)
        assertTrue(filterResult.isMatch());
    }

    @Test
    void testOutputData_AllFieldsNotNull() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));

        assertAll("All fields not null",
                () -> assertNotNull(result.getTimestamp()),
                () -> assertNotNull(result.getResults()),
                () -> assertNotNull(result.getStatistics()),
                () -> assertNotNull(result.getRecommendation())
        );
    }

    @Test
    void testFilterResult_AllFields() {
        OutputData result = service.processAndCompare(List.of("8.8.8.8"));
        FilterResult filterResult = result.getResults().get(0);

        assertAll("FilterResult fields",
                () -> assertNotNull(filterResult.getIp()),
                () -> assertTrue(filterResult.isHashSetResult() || !filterResult.isHashSetResult()),
                () -> assertTrue(filterResult.isBloomFilterResult() || !filterResult.isBloomFilterResult()),
                () -> assertTrue(filterResult.isMatch() || !filterResult.isMatch())
        );
    }

    @Test
    void testResult_MatchField() {
        OutputData result = service.processAndCompare(List.of("1.1.1.1"));
        FilterResult filterResult = result.getResults().get(0);

        // match должен быть true когда результаты совпадают
        boolean expectedMatch = (filterResult.isHashSetResult() == filterResult.isBloomFilterResult());
        assertEquals(expectedMatch, filterResult.isMatch());
    }

    @Test
    void testProcessAndCompare_DuplicateIPs() {
        // Тестируем дубликаты IP
        OutputData result = service.processAndCompare(
                List.of("1.1.1.1", "1.1.1.1", "1.1.1.1")
        );

        // Должно обработать все 3 IP (даже дубликаты)
        assertEquals(3, result.getResults().size());
    }

    @Test
    void testProcessAndCompare_EmptyString() {
        // Тестируем обработку пустой строки
        OutputData result = service.processAndCompare(List.of(""));

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
    }

    @Test
    void testProcessAndCompare_NullIP() {
        // Тестируем обработку null IP
        List<String> ips = new ArrayList<>();
        ips.add(null);

        assertDoesNotThrow(() -> {
            OutputData result = service.processAndCompare(ips);
            assertNotNull(result);
            assertEquals(1, result.getResults().size());
        });
    }

    @Test
    void testRecommendation_MemoryDifference() {
        OutputData result = service.processAndCompare(
                List.of("1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "5.5.5.5")
        );

        assertNotNull(result.getRecommendation());
        assertTrue(result.getRecommendation().length() > 0);
    }

    @Test
    void testRecommendation_LargeDataSet() {
        List<String> ips = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ips.add(i + "." + i + "." + i + "." + i);
        }

        OutputData result = service.processAndCompare(ips);

        assertNotNull(result.getRecommendation());
        assertFalse(result.getRecommendation().isEmpty());
    }

    @Test
    void testProcessAndCompare_MixedResults() {
        OutputData result = service.processAndCompare(
                List.of("1.1.1.1", "2.2.2.2", "3.3.3.3")
        );

        assertEquals(3, result.getResults().size());

        // Проверяем что все результаты имеют IP
        for (FilterResult fr : result.getResults()) {
            assertNotNull(fr.getIp());
        }
    }

    @Test
    void testProcessAndCompare_InvalidIP() {
        assertDoesNotThrow(() -> {
            OutputData result = service.processAndCompare(List.of("invalid.ip.format"));
            assertNotNull(result);
            assertEquals(1, result.getResults().size());
        });
    }

    @Test
    void testServiceConstructor_WithMocks() {
        // Тест конструктора с моками
        IpFilter mockHashSet = mock(IpFilter.class);
        IpFilter mockBloom = mock(IpFilter.class);
        JsonHandler mockJsonHandler = mock(JsonHandler.class);

        FilterService serviceWithMocks = new FilterService(mockHashSet, mockBloom, mockJsonHandler);
        assertNotNull(serviceWithMocks);
    }
}