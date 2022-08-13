package com.cap.moviecatalogservice.resources;

import com.cap.moviecatalogservice.models.CatalogItem;
import com.cap.moviecatalogservice.models.Movie;
import com.cap.moviecatalogservice.models.Rating;
import com.cap.moviecatalogservice.models.UserRating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public MovieCatalogResource() {
    }

    @RequestMapping("/{userId}")
    //@HystrixCommand(fallbackMethod = "getFallbackCatalog")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){

        UserRating ratings= restTemplate.getForObject("http://localhost:rating-data-service/ratingdata/users/"+ userId, UserRating.class);

        return ratings.getRatings().stream().map(rating -> {
            //For each movieID, call movie info service and get details
            Movie movie=restTemplate.getForObject("http://localhost:movie-info-service/movies/"+rating.getMovieId(), Movie.class, UserRating.class);
            //put them all together
            return new CatalogItem(movie.getName(), movie.getDescription(), rating.getRating());
        })
                .collect(Collectors.toList());
    }

//    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId)
//    {
//        return Arrays.asList(new CatalogItem("No movie","",0));
//    }
}

/*
            Movie movie= webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/movies/"+rating.getMovieId())
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
 */