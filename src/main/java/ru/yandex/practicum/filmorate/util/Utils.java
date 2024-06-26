package ru.yandex.practicum.filmorate.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.StorageData;

import java.util.Collection;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static long getNextId(final Collection<? extends StorageData> data) {
        long currentMaxId = data.stream()
                .map(StorageData::getId)
                .max(Long::compareTo)
                .orElse(0L);
        return ++currentMaxId;
    }

    public static boolean hasId(final Collection<? extends StorageData> data, final Long id) {
        return data.stream().anyMatch(obj -> Objects.equals(obj.getId(), id));
    }
}
