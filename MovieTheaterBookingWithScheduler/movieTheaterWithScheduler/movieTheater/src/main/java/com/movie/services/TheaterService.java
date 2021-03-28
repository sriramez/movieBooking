package com.movie.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.movie.model.Theater;
import com.sun.xml.txw2.Document;

@Service
public class TheaterService {

	@Autowired
	MongoTemplate mongo;
	
	public String createTheater(Theater theater)
	{
		
		if(validateTheater(theater).contains("SUCCESS"))
		{
			int theaterRow = 0;
			int theaterColumn = 0;
			if(theater.getRow()>15)
			{
				theaterRow=15;
			}
			else
			{
				theaterRow = theater.getRow();
			}
			if(theater.getColumn()>50)
			{
				theaterColumn=50;
			}
			else
			{
				theaterColumn = theater.getColumn();
			}
			JSONObject theaterObj = new JSONObject();
			theaterObj.put("theatername", theater.getTheaterName());
			theaterObj.put("showtimes", theater.getShowtimes());
			theaterObj.put("movies", theater.getMovies());
			
			JSONObject layout = new JSONObject();
			
			JSONObject defaultStatus = new JSONObject();
			defaultStatus.put("isBooked", 0);
			defaultStatus.put("bookedBy", 0);
			defaultStatus.put("isConfirmed", 0);
			
			char[] alphabets = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'};
			
			for(int i=0;i<theaterRow;i++)
			{
				for(int j=0;j<theaterColumn;j++)
				{
					layout.put(alphabets[i]+String.valueOf(j), defaultStatus);
				}
			}
			
			theaterObj.put("layout", layout);
			
			org.bson.Document theaterLayout = org.bson.Document.parse(theaterObj.toString());
			mongo.insert(theaterLayout,"theater");
			
			for(String movie : theater.getMovies())
			{
				JSONObject movieObj = new JSONObject();
				JSONArray showtimes = new JSONArray();
				movieObj.put("moviename", movie);
				movieObj.put("theatername", theater.getTheaterName());
				for(String showtime : theater.getShowtimes())
				{
					JSONObject show = new JSONObject();
					show.put("showtime", showtime);
					show.put("layout", layout);
					showtimes.put(show);
				}
				movieObj.put("showtimes", showtimes);
				
				org.bson.Document movieBooking = org.bson.Document.parse(movieObj.toString());
				mongo.insert(movieBooking,"moviebooking");
			}
			
			
			
			
		}
		return "SUCCESS: theater created successfully";
	}

	private String validateTheater(Theater theater) {
		if(theater.getTheaterName()==null || theater.getTheaterName().isEmpty())
			return "ERROR:theater name cannot be null or empty";
		if(theater.getShowtimes()==null|| theater.getShowtimes().isEmpty())
			return "ERROR:show times cannot be null or empty";
		if(theater.getMovies()==null||theater.getMovies().isEmpty())
			return "ERROR:movies cannot be null or empty";
		if(theater.getRow()<=0||theater.getColumn()<=0)
			return "ERROR:theater seating size should be greater than 0";
		return "SUCCESS:theater can be created";
	}
}
