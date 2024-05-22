package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class Film extends StorageData {
    @NonNull
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Duration duration;
    private Set<Long> likes = new HashSet<>();

    @JsonProperty("duration")
    public long getDurationTimeSeconds() {
        return duration.getSeconds();
    }
}
