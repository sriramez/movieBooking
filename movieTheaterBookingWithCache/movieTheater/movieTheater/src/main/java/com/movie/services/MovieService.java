package com.movie.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class MovieService {
	
	@Autowired
	MongoTemplate mongo;
	
	public String getMovieSeats(String movie,int timings)
	{
		JSONArray showtimes = getShowtime(movie);
		return showtimes.getJSONObject(timings).toString();
	}


	private JSONArray getShowtime(String movie) {
		String movieDetails = mongo.findOne(Query.query(Criteria.where("moviename").is(movie)),String.class,"moviebooking");
		JSONObject movieDetailsObject = new JSONObject(movieDetails);
		JSONArray showtimes = (JSONArray)movieDetailsObject.get("showtimes");
		return showtimes;
	}
	
	
	public String bookMovieSeats(String movie,int timings,int userid,List<String> ticketIds,String uuid)
	{
		
		String paymentStatus = mongo.findOne(Query.query(Criteria.where("uuid").is(Long.valueOf(uuid))), String.class,"payment");
		JSONObject paymentStatusObj = new JSONObject(paymentStatus);
		
		if(paymentStatusObj.getInt("payment")==0)
		{
			return "FAILURE:PAYMENT NOT DONE YET";
		}
		List<String> invalidIds = new ArrayList<String>();
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
					seat.put("isBooked", 1);
					seat.put("isConfirmed", 1);
					seat.put("bookedBy", userid);
					layout.put(ticketId, seat);
					showtime.put("layout",layout);
					showtimes.put(timings, showtime);
					movieDetailsObject.put("showtimes", showtimes);
				}
			}else
			{
				invalidIds.add(ticketId);
			}
		}
		if(invalidIds.size()>0)
		{
			return "FALIURE:received invalid seat numbers";	
		}
		Document doc = Document.parse(movieDetailsObject.toString());
		mongo.findAndReplace(Query.query(Criteria.where("moviename").is(movie)),doc,"moviebooking");
		
		String userDetails = mongo.findOne(
				  Query.query(Criteria.where("userid").is(userid)), String.class,"users");
	
		JSONObject userObj = new JSONObject(userDetails);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
		   LocalDateTime now = LocalDateTime.now();  
		   JSONObject bookings = new JSONObject();
		   bookings.put("date", dtf.format(now));
		   bookings.put("showtime",timings);
		   bookings.put("tickets", ticketIds);
		   JSONArray array = new JSONArray();
		   array.put(bookings);
		   userObj.put("bookings", array);
		
		Document user = Document.parse(userObj.toString());
		mongo.findAndReplace(Query.query(Criteria.where("userid").is(userid)),user,"users");
		return "SUCCESS:Ticket booked successfully";
		
	}

}
