package com.app.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.app.models.CatalogItems;
import com.app.models.Movie;
import com.app.models.Rating;
import com.app.models.UserRating;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/catalog")
@AllArgsConstructor
public class MovieCatalogResource {
	
	private final RestClient restClient;
	
	@GetMapping("/{userId}")
	public List<CatalogItems> getCatalogForUserId(@PathVariable String userId){
		
		UserRating userRating = null;
		try {
			userRating = restClient.get()
		        .uri("http://localhost:8082/ratingsdata/users/" + userId)
		        .retrieve()
		        .body(UserRating.class);
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		

		    List<Rating> ratings = userRating.getUserRatings();
		 
		 return ratings.stream().map(rating -> {
			 //for each movie id call movie info service and get details
			 Movie movie = restClient.get()
					 .uri("http://localhost:8081/movies/"+ rating.getMovieId())
					 .retrieve()
					 .body(Movie.class);
			 //put all of them together
			 return new CatalogItems(movie.getName(), "jnhkjbd", rating.getRating());
		 	}).collect(Collectors.toList());		
	}
}
