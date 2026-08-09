MERGE INTO genres (genre_name) KEY (genre_name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

MERGE INTO rating_mpa (ratingMPAname) KEY (ratingMPAname) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');