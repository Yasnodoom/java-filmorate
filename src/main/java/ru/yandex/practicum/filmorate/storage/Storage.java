package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.StorageData;

import java.util.Collection;
import java.util.Optional;

public interface Storage<T extends StorageData> {
    void create(T data);

    void update(T data);

    Collection<T> getAll();

    Optional<T> getElement(Long id);
}
