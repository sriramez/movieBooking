package com.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.movie.model.Theater;
import com.movie.services.TheaterService;

@RestController
public class TheaterController {

	@Autowired
	TheaterService service;
	
	@PostMapping("theater")
	public String createTheater(@RequestBody Theater theater)
	{
		return service.createTheater(theater);
	}
}
