package com.innovaccer.sae.engine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.*;

@Controller
@RequestMapping("/errors")
public class ErrorController {
	@RequestMapping(value = "{number}", method = RequestMethod.GET)
	public @ResponseBody String getShopInJSON(@PathVariable String number) {
		if(number.equals("404"))
			return (new Gson()).toJson("HTTP Error 404");
		return null;

	}
}
