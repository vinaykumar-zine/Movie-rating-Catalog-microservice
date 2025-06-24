package com.app.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.app.models.Movie;
import com.app.models.MovieSummary;

@RestController
@RequestMapping("/movies")

public class MovieResource {
	
	@Value("${api_key}")
	private String apiKey; 
	
	@Autowired
	private RestClient restClient;

	@GetMapping("/{movieId}")
	public Movie getMovieinfo(@PathVariable String movieId) {
		MovieSummary movieSummary = restClient.get()
				.uri("https://api.themoviedb.org/3/movie/"+movieId+"?api_key="+apiKey)
				.retrieve()
				.body(MovieSummary.class);
		return new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());
	}
}
