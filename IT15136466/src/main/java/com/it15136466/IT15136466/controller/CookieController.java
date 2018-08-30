package com.it15136466.IT15136466.controller;

import java.util.HashMap;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.it15136466.IT15136466.model.Account;
import com.it15136466.IT15136466.model.User;

@Controller
public class CookieController {

	
	//store cookies
		HashMap<String, String> cookieStore = new HashMap<>();

		//load login page
		@RequestMapping(value = "/", method = RequestMethod.GET)
		public String index() {
			return "index";
		}

		//process login
		@RequestMapping(value = "/login", method = RequestMethod.POST)
		public String submit(@Valid @ModelAttribute("User") User user, BindingResult result, Model model,
				HttpServletResponse response, HttpServletRequest request) {

			//check login credentials
			if (user.getUsername().equals("admin") && user.getPassword().equals("admin")) {
				if (result.hasErrors()) {
					return "error"; 
				}

				//generate random value for session cookie
				String ssid_value = generateRamdomValue();

				//store session cookie on browser
				Cookie c1 = new Cookie("ssid", ssid_value);
				c1.setMaxAge(600 * 600); // set expire time for 1 hour
				c1.setHttpOnly(true); //session cookie doesn't read by javascript
				c1.setSecure(false); // this example is not using https
				response.addCookie(c1);
				cookieStore.put("ssid", ssid_value);

				//generate random seed as csrf token and store it
				cookieStore.put("random_seed", generateRamdomValue());

				//load user view page
				return "UserView";
			} else {
				//load error page for invalid logins
				return "error"; 
			}

		}

		//send csrf token with ajax request
		@RequestMapping(value = "/cookie", method = RequestMethod.POST)
		public ResponseEntity<String> cookie(HttpServletResponse response, HttpServletRequest request) {

			//get all cookies from request
			Cookie[] cookies = request.getCookies();

			if (cookies != null) {
				for (Cookie cookie : cookies) {

					//send csrf token for valid session cookie
					if (cookie.getValue().equals(cookieStore.get("ssid"))) {
						return ResponseEntity.status(HttpStatus.OK).body(cookieStore.get("random_seed"));
					}
				}
			}

			return ResponseEntity.status(HttpStatus.OK).body("error");

		}

		//process transfer money
		@RequestMapping(value = "/money", method = RequestMethod.POST)
		public String transferMoney(@Valid @ModelAttribute("Account") Account account, BindingResult result, Model model,
				HttpServletResponse response, HttpServletRequest request) {

			//get all cookies from request
			Cookie[] cookies = request.getCookies();

			if (cookies != null) {
				for (Cookie cookie : cookies) {

					//check session cookie and csrf token
					if (cookie.getValue().equals(cookieStore.get("ssid")) && account.getCsrf().equals(cookieStore.get("random_seed"))) {
						System.out.println(cookieStore.get("random_seed"));
						model.addAttribute("msg", "your money transfer is successfull");
						return "UserView";
					}
				}
			}

			return "error";

		}
		

		//generate random string value for cookies
		protected String generateRamdomValue() {
	        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	        StringBuilder salt = new StringBuilder();
	        Random rnd = new Random();
	        while (salt.length() < 18) { // length of the random string.
	            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
	            salt.append(SALTCHARS.charAt(index));
	        }
	        String saltStr = salt.toString();
	        return saltStr;

	    }

}
