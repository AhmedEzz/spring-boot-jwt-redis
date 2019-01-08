package util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.User;

@Service
public class RedisUtil {

	@Autowired
	private RedisTemplate<String, User> template;
	
	private final String FITNESS_USER = "FITUSER";
	
	ObjectMapper mapper = new ObjectMapper();
	
	public Object getValue(final String key)
	{
		HashOperations<String, String, String> hashOps = template.opsForHash();
		User user = new User();
		try {
			user = mapper.readValue(hashOps.get(FITNESS_USER, key), User.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	public void setValue(final User user)
	{
		HashOperations<String, String, String> hashOps = template.opsForHash();
		final String key = user.getMobileNumber();
		String jsonInString = "";
		try {
			jsonInString = mapper.writeValueAsString(user);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		hashOps.put(FITNESS_USER, key, jsonInString);
	}
}
