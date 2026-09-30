package tetris.stats;

import java.time.Instant;

public record MatchRecord(
        int score,
        int boardWidth,
        int boardHeight,
        Instant playedAt
) {
    public String toJson() {
        return "{"
                + "\"score\":" + score + ","
                + "\"boardWidth\":" + boardWidth + ","
                + "\"boardHeight\":" + boardHeight + ","
                + "\"playedAt\":" + playedAt + "\""
                + "}";
    }

    public static MatchRecord fromJson(String json) {
        return new MatchRecord(
                intField(json, "score"),
                intField(json, "boardWidth"),
                intField(json, "boardHeight"),
                Instant.parse(stringField(json, "playedAt"))
        );
    }

    private static int intField(String json, String field) {
        String marker = "\"" + field + "\":";
        int start = json.indexOf(marker) + marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return Integer.parseInt(json.substring(start, end));
    }

    private static String stringField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker) + marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start,end);
    }
}
