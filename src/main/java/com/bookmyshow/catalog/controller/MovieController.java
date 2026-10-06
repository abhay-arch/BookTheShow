package com.bookmyshow.catalog.controller;

import com.bookmyshow.catalog.dto.MovieDtos;
import com.bookmyshow.catalog.service.MovieService;
import com.bookmyshow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
    
    private final MovieService movieService;
    
    @GetMapping("/now-playing")
    public ResponseEntity<ApiResponse<Page<MovieDtos.MovieListResponse>>> getNowPlayingMovies(
            @RequestParam(required = false) UUID cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<MovieDtos.MovieListResponse> movies = movieService.getNowPlayingMovies(cityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(movies));
    }
    
    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse<Page<MovieDtos.MovieListResponse>>> getComingSoonMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<MovieDtos.MovieListResponse> movies = movieService.getComingSoonMovies(pageable);
        return ResponseEntity.ok(ApiResponse.success(movies));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MovieDtos.MovieListResponse>>> searchMovies(
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<MovieDtos.MovieListResponse> movies = movieService.searchMovies(cityId, query, language, pageable);
        return ResponseEntity.ok(ApiResponse.success(movies));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDtos.MovieResponse>> getMovieById(@PathVariable UUID id) {
        MovieDtos.MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }
    
    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<MovieDtos.GenreResponse>>> getAllGenres() {
        List<MovieDtos.GenreResponse> genres = movieService.getAllGenres();
        return ResponseEntity.ok(ApiResponse.success(genres));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieDtos.MovieResponse>> createMovie(
            @Valid @RequestBody MovieDtos.CreateMovieRequest request) {
        MovieDtos.MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieDtos.MovieResponse>> updateMovie(
            @PathVariable UUID id,
            @Valid @RequestBody MovieDtos.UpdateMovieRequest request) {
        MovieDtos.MovieResponse movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/genres")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieDtos.GenreResponse>> createGenre(
            @Valid @RequestBody MovieDtos.CreateGenreRequest request) {
        MovieDtos.GenreResponse genre = movieService.createGenre(request);
        return ResponseEntity.ok(ApiResponse.success(genre));
    }
}