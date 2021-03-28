package com.movie.mongoConfiguration;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

	@Autowired
	MongoTemplate mongo;

	@Scheduled(cron = "* * * * * *")
	public void cleanUpUnbookedTickets() {
		List<String> payments = mongo.findAll(String.class, "payment");
		for (String payment : payments) {
			JSONObject paymentObj = new JSONObject(payment);
			if (paymentObj.getInt("payment") == 0) {
				long currentTimeMillis = System.currentTimeMillis();
				long uuid = paymentObj.getLong("uuid");
				long time = currentTimeMillis - uuid;
				if (time > 120000) {
					List<String> ticketIds = new ArrayList<>();
					String booked = paymentObj.getString("booked");
					JSONArray array = new JSONArray(booked);
					for (Object key : array) {
						ticketIds.add(String.valueOf(key));
					}
					String movie = paymentObj.getString("movie");
					int timings = paymentObj.getInt("timings");
					String movieDetails = mongo.findOne(Query.query(Criteria.where("moviename").is(movie)),
							String.class, "moviebooking");
					JSONObject movieDetailsObject = new JSONObject(movieDetails);
					JSONArray showtimes = (JSONArray) movieDetailsObject.get("showtimes");
					JSONObject showtime = showtimes.getJSONObject(timings);
					JSONObject layout = showtime.getJSONObject("layout");

					for (String ticketId : ticketIds) {
						if (layout.has(ticketId)) {
							JSONObject seat = layout.getJSONObject(ticketId);
							seat.put("isBooked", 0);
							seat.put("bookedBy", 0);
							seat.put("isConfirmed", 0);
							layout.put(ticketId, seat);
						}
					}
					showtime.put("layout", layout);
					showtimes.put(timings, showtime);
					movieDetailsObject.put("showtimes", showtimes);
					mongo.findAndReplace(Query.query(Criteria.where("moviename").is(movie)),
							movieDetailsObject.toString(), "moviebooking");
					mongo.findAndRemove(Query.query(Criteria.where("uuid").is(uuid)),
							String.class, "payment");
				}
			}

		}
	}
}
