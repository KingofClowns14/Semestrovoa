package com.semestr;

/*  Этот класс отвечает за чтение и запись файла players.json. 
Он использует регулярные выражения (Regex - шаблон поиска), чтобы "понять" JSON без специальных библиотек. */
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatsManager {
    private static final String FILE_NAME = "players.json";
    private Map<String, PlayerStats> statsMap = new HashMap<>();

    public StatsManager() {
        loadStats();
    }

    // Обновление статистики после игры
    public synchronized void updateStats(String winner, String loser) {
        // Получаем или создаем статистику для победителя
        PlayerStats wStats = statsMap.getOrDefault(winner, new PlayerStats(winner, 0, 0));
        wStats.wins++;
        statsMap.put(winner, wStats);
        // Получаем или создаем статистику для проигравшего
        PlayerStats lStats = statsMap.getOrDefault(loser, new PlayerStats(loser, 0, 0));
        lStats.losses++;
        statsMap.put(loser, lStats);
        saveStats();
    }

    public String getStatsString(String nickname) {
        if (statsMap.containsKey(nickname)) {
            PlayerStats ps = statsMap.get(nickname);
            return String.format("[Побед: %d | Поражений: %d]", ps.wins, ps.losses);
        }
        return "[Новичок]";
    }

    // Ручное сохранение в JSON
    private void saveStats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println("{");
            int count = 0;
            for (PlayerStats p : statsMap.values()) {
                // Форматирование в строку: "Nick": {"wins": 1, "losses": 2}
                String jsonLine = String.format("  \"%s\": {\"wins\": %d, \"losses\": %d}", p.nickname, p.wins,
                        p.losses);
                if (count < statsMap.size() - 1) {
                    jsonLine += ",";
                }
                writer.println(jsonLine);
                count++;
            }
            writer.println("}");
        } catch (IOException e) {
            System.err.println("ОШИБКА СОХРАНЕНИЯ JSON" + e.getMessage());
        }
    }

    // Загрузка из JSON
    private void loadStats() {
        File file = new File(FILE_NAME);
        if (!file.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            /*
             * Регулярное выражение ищет шаблон: "Nick": {"wins": 10, "losses": 5}
             * Группа 1: Ник, Группа 2: Победы, Группа 3: Поражения
             * \" -> ищет просто кавычку
             * (.*?) -> Группа 1
             * "." -> любой символ
             * "*" -> сколько угодно раз
             * "?" -> взять как можно меньше(до следующей кавычки)
             * \": \{ -> ищет символы ": {
             * \"wins\": -> ищет текст "wins:"
             * (\\d+) -> Группа 2
             * \d -> цифра
             * + -> одна или больше
             * , \"losses\" -> ищет текст ,"losses"
             * (\d+) -> Группа 3 Аналогично Группе 2
             */
            Pattern pattern = Pattern.compile("\"(.*?)\": \\{\"wins\": (\\d+), \"losses\": (\\d+)\\}");
            while ((line = br.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    String nick = m.group(1);
                    int wins = Integer.parseInt(m.group(2));
                    int losses = Integer.parseInt(m.group(3));
                    statsMap.put(nick, new PlayerStats(nick, wins, losses));
                }
            }
        } catch (IOException e) {
            System.err.println("ОШИБКА ЗАГРУЗКИ В JSON" + e.getMessage());
        }
    }
}
