package com.bookmyshow.catalog.service;

import com.bookmyshow.catalog.dto.MovieDtos;
import com.bookmyshow.catalog.entity.Movie;
import com.bookmyshow.catalog.entity.Genre;
import com.bookmyshow.catalog.entity.MovieGenre;
import com.bookmyshow.catalog.repository.MovieRepository;
import com.bookmyshow.catalog.repository.GenreRepository;
import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {
    
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    
    @Cacheable(value = "movies", key = "'playing:' + #cityId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize", unless = "#result.isEmpty()")
    public Page<MovieDtos.MovieListResponse> getNowPlayingMovies(UUID cityId, Pageable pageable) {
        Instant now = Instant.now();
        Page<Movie> movies = movieRepository.searchMovies(cityId, null, null, now, pageable);
        return movies.map(this::mapToListResponse);
    }
    
    @Cacheable(value = "movies", key = "'coming:' + #pageable.pageNumber + ':' + #pageable.pageSize", unless = "#result.isEmpty()")
    public Page<MovieDtos.MovieListResponse> getComingSoonMovies(Pageable pageable) {
        Instant now = Instant.now();
        return movieRepository.findByIsComingSoonTrueAndIsActiveTrue(pageable).map(this::mapToListResponse);
    }
    
    @Cacheable(value = "movies", key = "'search:' + #cityId + ':' + #query + ':' + #language + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<MovieDtos.MovieListResponse> searchMovies(
            UUID cityId, String query, String language, Pageable pageable) {
        Instant now = Instant.now();
        Page<Movie> movies = movieRepository.searchMovies(cityId, query, language, now, pageable);
        return movies.map(this::mapToListResponse);
    }
    
    public MovieDtos.MovieResponse getMovieById(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        
        List<MovieDtos.GenreResponse> genres = getGenresForMovie(movieId);
        return MovieDtos.MovieResponse.from(movie, genres);
    }
    
    public List<MovieDtos.GenreResponse> getAllGenres() {
        return genreRepository.findAllByOrderByNameAsc().stream()
                .map(genre -> MovieDtos.GenreResponse.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .description(genre.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<MovieDtos.GenreResponse> getGenresForMovie(UUID movieId) {
        // This would typically use a join query or separate repository
        // For now, returning empty list - would need MovieGenreRepository
        return List.of();
    }
    
    private MovieDtos.MovieListResponse mapToListResponse(Movie movie) {
        return MovieDtos.MovieListResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .durationMinutes(movie.getDurationMinutes())
                .language(movie.getLanguage())
                .certification(movie.getCertification())
                .releaseDate(movie.getReleaseDate())
                .posterUrl(movie.getPosterUrl())
                .rating(movie.getRating())
                .isComingSoon(movie.getIsComingSoon())
                .genreNames(List.of()) // Would be populated from genres
                .build();
    }
    
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public MovieDtos.MovieResponse createMovie(MovieDtos.CreateMovieRequest request) {
        if (movieRepository.findByTitleIgnoreCase(request.getTitle()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "Movie title already exists");
        }
        
        Movie movie = Movie.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .language(request.getLanguage())
                .certification(request.getCertification())
                .releaseDate(request.getReleaseDate())
                .endDate(request.getEndDate())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .director(request.getDirector())
                .castMembers(request.getCastMembers())
                .isActive(true)
                .isComingSoon(request.getReleaseDate().isAfter(Instant.now()))
                .build();
        
        movie = movieRepository.save(movie);
        
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            assignGenres(movie.getId(), request.getGenreIds());
        }
        
        log.info("Created movie: {}", movie.getTitle());
        return getMovieById(movie.getId());
    }
    
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public MovieDtos.MovieResponse updateMovie(UUID movieId, MovieDtos.UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        
        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) movie.setDurationMinutes(request.getDurationMinutes());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getCertification() != null) movie.setCertification(request.getCertification());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getEndDate() != null) movie.setEndDate(request.getEndDate());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getRating() != null) movie.setRating(request.getRating());
        if (request.getIsActive() != null) movie.setIsActive(request.getIsActive());
        if (request.getIsComingSoon() != null) movie.setIsComingSoon(request.getIsComingSoon());
        if (request.getDirector() != null) movie.setDirector(request.getDirector());
        if (request.getCastMembers() != null) movie.setCastMembers(request.getCastMembers());
        
        movie = movieRepository.save(movie);
        
        if (request.getGenreIds() != null) {
            assignGenres(movieId, request.getGenreIds());
        }
        
        log.info("Updated movie: {}", movie.getTitle());
        return getMovieById(movie.getId());
    }
    
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        
        movie.setIsActive(false);
        movieRepository.save(movie);
        log.info("Deactivated movie: {}", movie.getTitle());
    }
    
    @Transactional
    public MovieDtos.GenreResponse createGenre(MovieDtos.CreateGenreRequest request) {
        if (genreRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "Genre already exists");
        }
        
        Genre genre = Genre.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        genre = genreRepository.save(genre);
        log.info("Created genre: {}", genre.getName());
        return MovieDtos.GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();
    }
    
    private void assignGenres(UUID movieId, List<UUID> genreIds) {
        // This would use MovieGenreRepository to create associations
        // Implementation depends on having that repository
    }
}