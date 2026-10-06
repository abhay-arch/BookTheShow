CREATE TABLE genres (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE movies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(300) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    language VARCHAR(50) NOT NULL,
    certification VARCHAR(10) NOT NULL,
    release_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    rating DECIMAL(3, 1),
    director VARCHAR(200),
    cast_members TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_coming_soon BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE movie_genres (
    movie_id UUID NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_movies_language ON movies(language);
CREATE INDEX idx_movies_certification ON movies(certification);
CREATE INDEX idx_movies_release_date ON movies(release_date);
CREATE INDEX idx_movies_active ON movies(is_active);
CREATE INDEX idx_movies_coming_soon ON movies(is_coming_soon);
CREATE INDEX idx_genres_name ON genres(name);