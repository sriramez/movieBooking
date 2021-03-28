package com.movie.model;

import java.util.List;

public class Theater {

	String theaterName;
	
	List<String> showtimes;
	
	List<String> movies;
	
	int row;
	
	int column;

	public String getTheaterName() {
		return theaterName;
	}

	public void setTheaterName(String theaterName) {
		this.theaterName = theaterName;
	}

	public List<String> getShowtimes() {
		return showtimes;
	}

	public void setShowtimes(List<String> showtimes) {
		this.showtimes = showtimes;
	}

	public List<String> getMovies() {
		return movies;
	}

	public void setMovies(List<String> movies) {
		this.movies = movies;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
	
	
	
}
