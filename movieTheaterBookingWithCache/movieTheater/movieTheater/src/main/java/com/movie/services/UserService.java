package com.movie.services;


import java.util.Collection;


import org.bson.Document;
import org.ehcache.Cache;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.movie.model.User;

@Service
public class UserService {
	
	
	@Autowired
	MongoTemplate mongo;
	
	@Autowired
	@Qualifier("userAuth")
	Cache<String,Integer> userCache;
	
	public String createUser(User user)
	{
		if(validateUser(user).contains("SUCCESS"))
		{
		JSONObject userObj = new JSONObject();
		userObj.put("username", user.getUsername());
		userObj.put("password", user.getPassword());
		
		MongoCollection<Document> collection = mongo.getCollection("users");
		userObj.put("userid", collection.countDocuments()+1);
		Document document = Document.parse(userObj.toString());
		mongo.insert(document, "users");
		return "SUCCESS:Usercreated successfully";
		}
		else
			return "FAILURE:uservalidation failed";
	}

	private String validateUser(User user) {
		if(user.getUsername()==null || user.getUsername().isEmpty())
			return "FAILURE: username is invalid";
		if(user.getPassword()==null || user.getPassword().isEmpty())
			return "FAILURE: password is invalid";
		return "SUCCESS: validation passed";
	}
	
	public String login(User user)
	{
		if(validateUser(user).contains("SUCCESS"))
		{
			String userDetails = mongo.findOne(
					  Query.query(Criteria.where("username").is(user.getUsername())), String.class,"users");
		
			JSONObject userObj = new JSONObject(userDetails);
			if(userObj.getString("password").equals(user.getPassword()))
			{
				userCache.put(user.getUsername(),userObj.getInt("userid"));
				return "SUCCESS:User logged in successfully:userid::"+userObj.getInt("userid");
			}
			return "FAILURE:invalid password";
		}
		return "FAILURE:uservalidation failed";
	}
}
