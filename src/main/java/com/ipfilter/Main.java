package com.ipfilter;

import com.ipfilter.model.OutputData;
import com.ipfilter.service.FilterService;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FilterService service = new FilterService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        boolean fileIsLoaded = false;
        String path = "";
        List<String> checkIps = null; // Список IP для проверки из файла

        System.out.println("=== IP Blacklist Filter ===");

        while (running) {
            printMenu();
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        System.out.print("Путь к JSON файлу (например, input_example1.json): ");
                        path = scanner.nextLine();
                        service.loadDataFromFile(path);
                        System.out.println("Данные загружены успешно!");
                        fileIsLoaded = true;
                        // Загружаем checkIps из того же файла для пункта 3
                        checkIps = loadCheckIpsFromFile(path);
                        break;

                    case "2":
                        System.out.print("Введите IP для проверки: ");
                        String ip = scanner.nextLine();
                        OutputData singleRes = service.processAndCompare(List.of(ip));
                        System.out.println("HashSet: " + singleRes.getResults().get(0).isHashSetResult());
                        System.out.println("Bloom: " + singleRes.getResults().get(0).isBloomFilterResult());
                        break;

                    case "3":
                        List<String> testIps;

                        if (fileIsLoaded && checkIps != null && !checkIps.isEmpty()) {
                            // Используем IP из загруженного файла
                            System.out.println("Используем IP из загруженного файла: " + path);
                            testIps = checkIps;
                        } else {
                            // Используем фиксированный список
                            System.out.println("Файл не загружен. Используем тестовый список IP.");
                            testIps = Arrays.asList("192.168.1.1", "8.8.8.8", "10.0.0.1");
                        }

                        OutputData res = service.processAndCompare(testIps);
                        System.out.println("\nСтатистика:");
                        System.out.println("Память HS: " + res.getStatistics().getHashSetMemoryBytes() + " b");
                        System.out.println("Память BF: " + res.getStatistics().getBloomFilterMemoryBytes() + " b");
                        System.out.println("Время HS: " + res.getStatistics().getHashSetTimeMs() + " ms");
                        System.out.println("Время BF: " + res.getStatistics().getBloomFilterTimeMs() + " ms");
                        System.out.println("Вероятность FP: " + res.getStatistics().getFalsePositiveProbability());
                        System.out.println("Рекомендация: " + res.getRecommendation());

                        System.out.print("\nСохранить в output_example.json? (y/n): ");
                        if (scanner.nextLine().equalsIgnoreCase("y")) {
                            service.saveResults(res, "output_example.json");
                            System.out.println("Сохранено.");
                        }
                        break;

                    case "4":
                        System.out.println("Справка: Программа сравнивает точность HashSet и скорость/память Bloom Filter.");
                        break;

                    case "5":
                        running = false;
                        System.out.println("Выход.");
                        break;

                    default:
                        System.out.println("Неверный выбор.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n1. Загрузить blacklist из JSON");
        System.out.println("2. Проверить IP адрес");
        System.out.println("3. Сравнить алгоритмы (Demo)");
        System.out.println("4. Справка");
        System.out.println("5. Выход");
    }

    // Вспомогательный метод для загрузки checkIps из JSON файла
    private static List<String> loadCheckIpsFromFile(String filePath) {
        try {
            com.ipfilter.service.JsonHandler jsonHandler = new com.ipfilter.service.JsonHandler();
            com.ipfilter.model.InputData inputData = jsonHandler.loadInput(filePath);
            return inputData.getCheckIps();
        } catch (Exception e) {
            System.out.println("Предупреждение: Не удалось загрузить checkIps из файла: " + e.getMessage());
            return null;
        }
    }
}