package com.movie.controller;

import java.util.ArrayList;
import java.util.List;

import org.ehcache.Cache;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movie.services.MovieService;

@RestController
public class MovieController {

	@Autowired
	MovieService movieService;
	
	@Autowired
	@Qualifier("userAuth")
	Cache<String,Integer> userCache;
	
		
	@Autowired
	MongoTemplate mongo;
	
	@GetMapping("/movie")
	public String getMovieDetails(@RequestParam String movieName,@RequestParam int timings)
	{
		return movieService.getMovieSeats(movieName, timings);
	}
	
	
	@PostMapping("/ticket")
	public String blockTickets(@RequestParam String movie,@RequestParam int timings,@RequestParam int userid,@RequestBody ArrayList<String> ticketIds,@RequestParam String username)
	{
		if(!userCache.containsKey(username))
		{
			return "FAILURE:please login and try again";
		}
		List<String> booked = new ArrayList<String>();
		
		String movieDetails = mongo.findOne(Query.query(Criteria.where("moviename").is(movie)),String.class,"moviebooking");
		JSONObject movieDetailsObject = new JSONObject(movieDetails);
		JSONArray showtimes = (JSONArray)movieDetailsObject.get("showtimes");
		JSONObject showtime = showtimes.getJSONObject(timings);
		JSONObject layout = showtime.getJSONObject("layout");
		for(String ticketId : ticketIds)
		{
			if(layout.has(ticketId))
			{
				JSONObject seat = layout.getJSONObject(ticketId);
				if(seat.getInt("isBooked")==0&&seat.getInt("isConfirmed")==0)
				{
					booked.add(ticketId);
					seat.put("isBooked", 1);
					seat.put("bookedBy", userid);
					layout.put(ticketId, seat);
				}
			}
		}
		showtime.put("layout",layout);
		showtimes.put(timings, showtime);
		movieDetailsObject.put("showtimes", showtimes);
		mongo.findAndReplace(Query.query(Criteria.where("moviename").is(movie)), movieDetailsObject.toString(), "moviebooking");
		
			JSONObject status = new JSONObject();
			int price = booked.size()*200;
			status.put("price", price);
			status.put("payment", 0);
			status.put("booked", booked.toString());
			status.put("uuid", System.currentTimeMillis());
			status.put("userid", userid);
			status.put("movie", movie);
			status.put("timings", timings);
			
			org.bson.Document paymentDoc = org.bson.Document.parse(status.toString());
			mongo.insert(paymentDoc,"payment");
			
			return status.toString();
			
	}
	
	
	
}
