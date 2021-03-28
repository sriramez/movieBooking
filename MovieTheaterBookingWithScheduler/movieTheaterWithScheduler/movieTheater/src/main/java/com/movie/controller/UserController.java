package com.movie.controller;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.ehcache.Cache;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movie.model.User;
import com.movie.services.MovieService;
import com.movie.services.UserService;

@RestController
public class UserController {

	@Autowired
	UserService user;
	
	
	@Autowired
	MongoTemplate mongo;
	
	
	@Autowired
	MovieService movieService;
	
	
	@Autowired
	@Qualifier("userAuth")
	Cache<String,Integer> userCache;
	
	@PostMapping
	public String createUser(@RequestBody User user)
	{
		return this.user.createUser(user);
	}
	
	@PostMapping("login")
	public String login(@RequestBody User user)
	{
		return this.user.login(user);
	}
	
	
	@PostMapping("payment")
	public String payment(@RequestParam int userid,@RequestParam String uuid,@RequestParam String card,@RequestParam String ccv,@RequestParam String username)
	{
		if(!userCache.containsKey(username))
		{
			return "FAILURE:please login and try again";
		}
		String paymentStatus = mongo.findOne(Query.query(Criteria.where("uuid").is(Long.valueOf(uuid))), String.class,"payment");
		JSONObject paymentStatusObj = new JSONObject(paymentStatus);
		
		paymentStatusObj.put("payment", 1);
		String movie=paymentStatusObj.getString("movie");
		int timings=paymentStatusObj.getInt("timings");
		List<String> ticketIds = new ArrayList<>();
		String booked = paymentStatusObj.getString("booked");
		JSONArray array = new JSONArray(booked);
		for(Object key : array)
		{
			ticketIds.add(String.valueOf(key));
		}
		Document paymentDoc = Document.parse(paymentStatusObj.toString());
		
		mongo.findAndReplace(Query.query(Criteria.where("uuid").is(Long.valueOf(uuid))), paymentDoc,"payment");
		
		movieService.bookMovieSeats(movie, timings, userid, ticketIds, uuid);
		
		return paymentStatusObj.toString();
		
		
	}
}
