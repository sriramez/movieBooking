package com.movie.controller;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movie.model.User;
import com.movie.services.UserService;

@RestController
public class UserController {

	@Autowired
	UserService user;
	
	
	@Autowired
	MongoTemplate mongo;
	
	
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
	public String payment(@RequestParam String userid,@RequestParam String uuid,@RequestParam String card,@RequestParam String ccv)
	{
		String paymentStatus = mongo.findOne(Query.query(Criteria.where("uuid").is(Long.valueOf(uuid))), String.class,"payment");
		JSONObject paymentStatusObj = new JSONObject(paymentStatus);
		
		paymentStatusObj.put("payment", 1);
		
		Document paymentDoc = Document.parse(paymentStatusObj.toString());
		
		mongo.findAndReplace(Query.query(Criteria.where("uuid").is(Long.valueOf(uuid))), paymentDoc,"payment");
		
		return paymentStatusObj.toString();
		
		
	}
}
