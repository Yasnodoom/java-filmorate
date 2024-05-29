package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public Collection<Mpa> getAll() {
        return mpaDbStorage.getAll();
    }

    public Mpa getMpa(long id) {
        return mpaDbStorage.getMpa(id)
                .orElseThrow(() -> new NotFoundException("не найден mpa с id: " + id));
    }
}
