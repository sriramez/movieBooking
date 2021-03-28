package com.movie.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoId;
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
	@Qualifier("timings")
	Cache<Integer, Cache> show;
	
	@Autowired
	CacheManager manager;
	
	@Autowired
	MongoTemplate mongo;
	
	@GetMapping("movie")
	public String getMovieDetails(@RequestParam String movieName,@RequestParam int timings)
	{
		return movieService.getMovieSeats(movieName, timings);
	}
	
	@PostMapping("movie")
	public String bookMovieTickets(@RequestParam String movie,@RequestParam int timings,@RequestParam int userid,@RequestBody ArrayList<String> ticketIds,@RequestParam String username,@RequestParam String uuid)
	{
		if(!userCache.containsKey(username))
		{
			return "FAILURE:please login and try again";
		}
		return movieService.bookMovieSeats(movie, timings, userid, ticketIds,uuid);
	}
	
	
	@PostMapping("blocktickets")
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
				}
			}
		}
		
		if(show.containsKey(timings))
		{
			
			Cache<String,JSONObject> cache= show.get(timings);
			
			if(cache.containsKey(movie+":"+timings))
			{
				JSONObject obj = cache.get(movie+":"+timings);
				List<String> unableToBook = new ArrayList<String>();
				for(String seat : booked)
				{
					for(String key : obj.getJSONObject("seats").keySet())
					{
						if (seat.contains(key)) {
							unableToBook.add(seat);
						}
					}	
				}
				if(unableToBook.size()>0)
				{
					return "FAILURE:"+unableToBook.toString()+"cannot be booked";
				}
				
			}
			JSONObject values = new JSONObject();
			JSONObject seatStatus = new JSONObject();
			JSONObject userStatus = new JSONObject();
			for(String seatIds : booked)
			{
				if(!seatStatus.has(seatIds))
				{
					seatStatus.put(seatIds, userid);	
				}
				
			}
			userStatus.put(String.valueOf(userid), booked.toString());
			values.put("seats", seatStatus);
			values.put("users", userStatus);
			
			cache.put(movie+":"+timings, values);
			
			
			show.put(timings, cache);
			
			JSONObject status = new JSONObject();
			int price = booked.size()*200;
			status.put("price", price);
			status.put("payment", 0);
			status.put("booked", booked.toString());
			status.put("userid", userid);
			long currentTimeMillis = System.currentTimeMillis();
			status.put("uuid", currentTimeMillis);
			
			org.bson.Document paymentDoc = org.bson.Document.parse(status.toString());
			mongo.insert(paymentDoc,"payment");
			
			return status.toString();
		}
		else
		{
			Cache<String, JSONObject> showone = manager
			          .createCache("seats", CacheConfigurationBuilder
					            .newCacheConfigurationBuilder(
					              String.class, JSONObject.class,
					              ResourcePoolsBuilder.heap(10)).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2))));
			
			JSONObject values = new JSONObject();
			JSONObject seatStatus = new JSONObject();
			JSONObject userStatus = new JSONObject();
			for(String seatIds : booked)
			{
				if(!seatStatus.has(seatIds))
				{
					seatStatus.put(seatIds, userid);	
				}
				
			}
			userStatus.put(String.valueOf(userid), booked.toString());
			values.put("seats", seatStatus);
			values.put("users", userStatus);
			
			showone.put(movie+":"+timings, values);
			
			
			show.put(timings, showone);
			
			JSONObject status = new JSONObject();
			int price = booked.size()*200;
			status.put("price", price);
			status.put("payment", 0);
			status.put("booked", booked.toString());
			status.put("uuid", System.currentTimeMillis());
			status.put("userid", userid);
			
			org.bson.Document paymentDoc = org.bson.Document.parse(status.toString());
			mongo.insert(paymentDoc,"payment");
			
			return status.toString();
			
		}
	}
	
	
	
}
