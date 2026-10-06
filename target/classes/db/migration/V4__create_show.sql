CREATE TABLE shows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id UUID NOT NULL REFERENCES movies(id),
    screen_id UUID NOT NULL REFERENCES screens(id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    base_price DECIMAL(10, 2) NOT NULL,
    language VARCHAR(50),
    is_3d BOOLEAN NOT NULL DEFAULT FALSE,
    is_atmos BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_shows_movie ON shows(movie_id);
CREATE INDEX idx_shows_screen ON shows(screen_id);
CREATE INDEX idx_shows_start_time ON shows(start_time);
CREATE INDEX idx_shows_status ON shows(status);
CREATE INDEX idx_shows_movie_screen_time ON shows(movie_id, screen_id, start_time);