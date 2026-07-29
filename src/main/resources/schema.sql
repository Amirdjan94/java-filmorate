CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    genre_name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS rating_mpa (
  ratingMpaId INTEGER AUTO_INCREMENT PRIMARY KEY,
  ratingMPAname VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS film (
  film_id INTEGER AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR,
  description VARCHAR(200),
  releaseDate DATE,
  duration INTEGER,
  ratingMpaId INTEGER REFERENCES rating_mpa(ratingMpaId)
);

CREATE TABLE IF NOT EXISTS film_genres (
  film_id INTEGER,
  genre_id INTEGER,
  CONSTRAINT unique_film_genres UNIQUE (film_id, genre_id),
  FOREIGN KEY (film_id) REFERENCES film(film_id) ON DELETE CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
  user_id INTEGER AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR NOT NULL UNIQUE,
  login VARCHAR,
  name VARCHAR,
  birthday DATE
);

CREATE TABLE IF NOT EXISTS film_likes (
  film_id INTEGER,
  user_id INTEGER,
  CONSTRAINT unique_film_likes UNIQUE (film_id, user_id),
  FOREIGN KEY (film_id) REFERENCES film(film_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS follows (
  following_user_id INTEGER,
  followed_user_id INTEGER,
  confirmation BOOLEAN DEFAULT FALSE,
  CONSTRAINT unique_follows UNIQUE (following_user_id, followed_user_id),
  FOREIGN KEY (following_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  FOREIGN KEY (followed_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);