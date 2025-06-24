package com.app.resources;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.models.Rating;
import com.app.models.UserRating;

@RestController
@RequestMapping("/ratingsdata")
public class RatingResource {
//	List<Rating> ratings = Arrays.asList(
//			 new Rating("1234", 4),
//			 new Rating("5678", 3)
//			 );
	@GetMapping("/{movieId}")
	public Rating getRating(@PathVariable String movieId) {
		return new Rating(movieId, 4);
	}
	
	@GetMapping("/users/{userId}")
	public UserRating getUserRating(@PathVariable String userId){
		List<Rating> ratings = Arrays.asList(
				 new Rating("12", 4),
				 new Rating("11", 3)
				 );
		
		
		return new UserRating(ratings);
	}
}
