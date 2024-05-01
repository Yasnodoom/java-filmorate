package ru.yandex.practicum.filmorate.util;

import java.util.Map;

public class Utils {
    private Utils() {
    }

    public static long getNextId(final Map<Long, ?> map) {
        long currentMaxId = map.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
