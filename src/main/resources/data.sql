DELETE FROM FILMS;
DELETE FROM rating;
DELETE FROM genre;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE genre ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE rating ALTER COLUMN rating_id RESTART WITH 1;

INSERT INTO genre (name) VALUES ('Комедия');
INSERT INTO genre (name) VALUES ('Драма');
INSERT INTO genre (name) VALUES ('Мультфильм');
INSERT INTO genre (name) VALUES ('Триллер');
INSERT INTO genre (name) VALUES ('Документальный');
INSERT INTO genre (name) VALUES ('Боевик');

INSERT INTO rating (name, description) VALUES ('G', 'у фильма нет возрастных ограничений');
INSERT INTO rating (name, description) VALUES ('PG', 'детям рекомендуется смотреть фильм с родителями');
INSERT INTO rating (name, description) VALUES ('PG-13', 'детям до 13 лет просмотр не желателен');
INSERT INTO rating (name, description) VALUES ('R', 'лицам до 17 лет просматривать фильм можно только в присутствии взрослого');
INSERT INTO rating (name, description) VALUES ('NC-17', 'лицам до 18 лет просмотр запрещён');
