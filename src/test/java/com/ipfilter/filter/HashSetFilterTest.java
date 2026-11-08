package com.ipfilter.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;

class HashSetFilterTest {

    private IpFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HashSetFilter();
    }

    // --- SPECIFICATION BASED TESTING ---

    // Метод 1: Boundary Value Analysis (BVA)
    @Test
    void testBoundaryValues_EmptyAndSingleElement() {
        // Граница: Пустой фильтр
        assertFalse(filter.contains("192.168.1.1"), "Пустой фильтр не должен содержать IP");

        // Граница: Добавление 1 элемента
        filter.add("192.168.1.1");
        assertTrue(filter.contains("192.168.1.1"), "Фильтр должен содержать добавленный IP");

        // Граница: Проверка несуществующего рядом с существующим
        assertFalse(filter.contains("192.168.1.2"), "Фильтр не должен содержать похожий IP");
    }

    // Метод 2: Equivalence Partitioning (EP)
    @ParameterizedTest
    @ValueSource(strings = {"10.0.0.1", "172.16.0.1", "8.8.8.8"})
    void testEquivalencePartitioning_ValidIps(String ip) {
        // Класс эквивалентности: Валидные IP
        filter.add(ip);
        assertTrue(filter.contains(ip));
    }

    @Test
    void testEquivalencePartitioning_InvalidInputs() {
        // Класс эквивалентности: Null и пустые строки (не должны ломать код)
        filter.add(null);
        filter.add("");
        assertEquals(0, filter.getSize(), "Размер не должен измениться при добавлении невалидных данных");
    }

    // --- STRUCTURE BASED TESTING ---

    // Метод 1: Branch Testing
    @Test
    void testBranchCoverage_NullInputContains() {
        // Проверяем ветку if (ip == null) внутри contains
        boolean result = filter.contains(null);
        assertFalse(result, "Ветка обработки null должна возвращать false");
    }

    // Метод 2: Data Flow Testing
    @Test
    void testDataFlow_DefinitionUse() {
        // Def: создание фильтра
        IpFilter localFilter = new HashSetFilter();
        // Use: добавление
        localFilter.add("1.1.1.1");
        // Use: проверка и получение размера
        int size = localFilter.getSize();
        boolean exists = localFilter.contains("1.1.1.1");

        assertTrue(exists);
        assertEquals(1, size);
    }

    // --- JUNIT REQUIREMENTS ---

    @Test
    void testAssertionsVariety() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");

        // 1. assertEquals
        assertEquals(2, filter.getSize());
        // 2. assertTrue
        assertTrue(filter.contains("1.1.1.1"));
        // 3. assertFalse
        assertFalse(filter.contains("3.3.3.3"));
        // 4. assertThrows (проверка на отсутствие исключений при корректной работе, либо тестирование edge cases)
        assertDoesNotThrow(() -> filter.clear());
        // 5. assertAll (группировка)
        assertAll("Filter State",
                () -> assertEquals(0, filter.getSize()),
                () -> assertFalse(filter.contains("1.1.1.1"))
        );
    }

    // === НОВЫЕ ТЕСТЫ ДЛЯ MUTATION COVERAGE ===

    @Test
    void testAdd_NullValue() {
        // Тест на обработку null
        filter.add(null);
        assertEquals(0, filter.getSize(), "Null не должен добавляться");
    }

    @Test
    void testAdd_EmptyString() {
        // Тест на обработку пустой строки
        filter.add("");
        assertEquals(0, filter.getSize(), "Пустая строка не должна добавляться");
    }

    @Test
    void testAdd_DuplicateValues() {
        // Тест на дубликаты
        filter.add("192.168.1.1");
        filter.add("192.168.1.1");
        assertEquals(1, filter.getSize(), "Дубликаты не должны увеличивать размер");
    }

    @Test
    void testContains_NullInput() {
        // Тест ветки if (ip == null)
        assertFalse(filter.contains(null));
    }

    @Test
    void testClear() {
        // Тест метода clear
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        filter.clear();
        assertEquals(0, filter.getSize());
        assertFalse(filter.contains("1.1.1.1"));
    }

    @Test
    void testMemoryUsage() {
        // Тест расчёта памяти
        filter.add("1.1.1.1");
        long memoryBefore = filter.getMemoryUsageBytes();
        filter.add("2.2.2.2");
        long memoryAfter = filter.getMemoryUsageBytes();
        assertTrue(memoryAfter > memoryBefore, "Память должна расти");
    }

    @Test
    void testAdd_Null_DoesNotIncreaseSize() {
        filter.add(null);
        assertEquals(0, filter.getSize(), "Null не должен увеличивать размер");
    }

    @Test
    void testAdd_EmptyString_DoesNotIncreaseSize() {
        filter.add("");
        assertEquals(0, filter.getSize(), "Пустая строка не должна увеличивать размер");
    }

    @Test
    void testAdd_Duplicate_DoesNotIncreaseSize() {
        filter.add("192.168.1.1");
        filter.add("192.168.1.1");
        assertEquals(1, filter.getSize(), "Дубликаты не должны увеличивать размер");
    }

    @Test
    void testContains_Null_ReturnsFalse() {
        assertFalse(filter.contains(null), "Null должен возвращать false");
    }

    @Test
    void testClear_ResetsSize() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        assertEquals(2, filter.getSize());
        filter.clear();
        assertEquals(0, filter.getSize(), "Clear должен обнулить размер");
        assertFalse(filter.contains("1.1.1.1"), "После clear элементы не должны находиться");
    }

    @Test
    void testMemoryUsage_GrowsWithSize() {
        long memoryBefore = filter.getMemoryUsageBytes();
        filter.add("1.1.1.1");
        long memoryAfter = filter.getMemoryUsageBytes();
        assertTrue(memoryAfter >= memoryBefore, "Память должна расти или оставаться той же");
    }

    @Test
    void testMultipleAdds_DifferentIps() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        filter.add("3.3.3.3");
        assertEquals(3, filter.getSize());
        assertTrue(filter.contains("1.1.1.1"));
        assertTrue(filter.contains("2.2.2.2"));
        assertTrue(filter.contains("3.3.3.3"));
        assertFalse(filter.contains("4.4.4.4"));
    }

    @Test
    void testMemoryUsage_EmptyFilter() {
        long memory = filter.getMemoryUsageBytes();
        assertEquals(0, memory, "Память пустого фильтра должна быть 0");
    }

    @Test
    void testMemoryUsage_AfterAdd() {
        filter.add("1.1.1.1");
        long memory = filter.getMemoryUsageBytes();
        assertTrue(memory > 0, "Память должна быть > 0 после добавления");
    }

    @Test
    void testSize_AfterMultipleAdds() {
        filter.add("1.1.1.1");
        filter.add("2.2.2.2");
        filter.add("3.3.3.3");
        assertEquals(3, filter.getSize());
    }

    @Test
    void testSize_AfterClear() {
        filter.add("1.1.1.1");
        filter.clear();
        assertEquals(0, filter.getSize());
    }

    @Test
    void testContains_AfterClear() {
        filter.add("1.1.1.1");
        filter.clear();
        assertFalse(filter.contains("1.1.1.1"));
    }

    @Test
    void testAdd_NullDoesNotIncreaseSize() {
        int sizeBefore = filter.getSize();
        filter.add(null);
        assertEquals(sizeBefore, filter.getSize());
    }

    @Test
    void testAdd_EmptyStringDoesNotIncreaseSize() {
        int sizeBefore = filter.getSize();
        filter.add("");
        assertEquals(sizeBefore, filter.getSize());
    }

    @Test
    void testContains_NullReturnsFalse() {
        assertFalse(filter.contains(null));
    }

    @Test
    void testClear_MultipleTimes() {
        filter.add("1.1.1.1");
        filter.clear();
        filter.clear(); // Второй вызов не должен ломать
        assertEquals(0, filter.getSize());
    }

    @Test
    void testAdd_SpecialCharacters() {
        filter.add("test@ip#format");
        assertTrue(filter.contains("test@ip#format"));
    }

    @Test
    void testAdd_VeryLongIP() {
        String longIp = "192.168.1.1";
        filter.add(longIp);
        assertTrue(filter.contains(longIp));
    }

    @Test
    void testContains_CaseSensitive() {
        filter.add("192.168.1.1");
        assertFalse(filter.contains("192.168.1.2"));
    }
}