package impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import model.User;
import service.LoginServices;

@Service
public class LoginImpl implements LoginServices{

	@Override
	public Object userLogin(User user) {
		String username = user.getUsername();
		String password = user.getPassword();
		Map<String, String> map = new HashMap<>();
		
		if(("Ahmed".equalsIgnoreCase(username) || ("Mohammed".equalsIgnoreCase(username)) && "123".equalsIgnoreCase(password)))
		{
			map.put("result", "true");
			return map;
		}
		map.put("result", "false");
		return map;
	}

}
