package controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import model.User;
import service.LoginServices;
import util.JWTUtil;
import util.RedisUtil;

@Controller
public class LoginController {

	@Autowired
	LoginServices loginServices;
	
	@Autowired
	JWTUtil jwtUtil;
	
	@Autowired
	RedisUtil redisUtil;
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public Object userLogin(@RequestBody User user, BindingResult result, HttpServletResponse response, HttpServletRequest request)
	{
		@SuppressWarnings("unchecked")
		Map<String, String> serviceResponse = (Map<String, String>) loginServices.userLogin(user);
		if(serviceResponse.get("result").equalsIgnoreCase("true"))
		{
			try {
				String refreshToke = jwtUtil.generateRefreshToken();
				response.setHeader("access_token", jwtUtil.tokenFor(user));
				response.setHeader("refresh_toke", refreshToke);
				user.setRefreshToken(refreshToke);
				
				// Add user to Redis DB
				user.setSID(request.getSession().getId());
				redisUtil.setValue(user);
				
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		serviceResponse.put("SID",request.getSession().getId());
		return serviceResponse;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/viewRT", method = RequestMethod.POST)
	@ResponseBody
	public Object viewRefreshToken(@RequestBody User user, BindingResult result, HttpServletResponse response, HttpServletRequest request)
	{
		
		String accessToken = request.getHeader("access_token");
		if (accessToken == null) {
			try {
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.sendError(HttpURLConnection.HTTP_FORBIDDEN,"ILLEGAL ACCESS - No token found in the request");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		} else {
			// JWT Verification - verify the signature .
			try {
				jwtUtil.verfiyToke(accessToken);
				
			} catch (URISyntaxException | IOException e) {
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				try {
					response.sendError(HttpURLConnection.HTTP_FORBIDDEN,"ILLEGAL ACCESS - Device Provide invalid token..");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			
			// JWT Verfication - verify Exp date
			if(jwtUtil.isTokenExpired(accessToken))
			{
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				try {
					response.sendError(HttpURLConnection.HTTP_FORBIDDEN,"ILLEGAL ACCESS - The provided token expired..");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			// JWT Verfication - verify Exp date
			if(!jwtUtil.isTokenRelatedToSession(accessToken, request.getSession().getId()))
			{
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				try {
					response.sendError(HttpURLConnection.HTTP_FORBIDDEN,"ILLEGAL ACCESS - The provided token related to another session...");
					try {
						throw new Exception("ILLEGAL ACCESS - The provided token related to another session...");
					} catch (Exception e) {
						
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		
		return redisUtil.getValue(user.getMobileNumber());
	}
	
	@RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
	@ResponseBody
	public Object refreshToken(@RequestBody User user, BindingResult result, HttpServletResponse response, HttpServletRequest request)
	{
		
		String accessToken = request.getHeader("access_token");
		String refreshToken = request.getHeader("refresh_token");
		
		if (accessToken == null || refreshToken == null) {
			try {
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.sendError(HttpURLConnection.HTTP_FORBIDDEN,"ILLEGAL ACCESS - The old token not found / refresg token");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		} else {
			// JWT get user information from the old token to verify
			try {
				Jws<Claims> userInformation = jwtUtil.returnTokenClaims(accessToken);
				String mobileNumber = userInformation.getBody().getSubject();
				User redisUser = (User) redisUtil.getValue(mobileNumber);
				if(!refreshToken.equals(redisUser.getRefreshToken()))
				{
					response.setStatus(403);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.sendError(HttpURLConnection.HTTP_FORBIDDEN,"ILLEGAL ACCESS - Invalid Refresh Token..");
				}else
				{
					String refreshToke = jwtUtil.generateRefreshToken();
					response.setHeader("access_token", jwtUtil.tokenFor(user));
					response.setHeader("refresh_toke", refreshToke);
					user.setRefreshToken(refreshToke);
					
					// Add user to Redis DB
					redisUtil.setValue(user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		Map<String, String> serviceResponse = new HashMap<>();
		serviceResponse.put("Redis Refresh Key", (String) redisUtil.getValue(user.getMobileNumber()));
		return serviceResponse;
	}
	
	@RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
	@ResponseBody
	public Object refreshTokenGET(HttpServletResponse response, HttpServletRequest request)
	{
		
		return "ok";
	}
}
