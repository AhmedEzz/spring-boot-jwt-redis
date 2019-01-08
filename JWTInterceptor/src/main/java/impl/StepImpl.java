package impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import service.StepServices;

@Service
public class StepImpl implements StepServices{

	@Override
	public Object listAllSteps(HttpServletRequest request) {
		
		Map<String, Object> listSteps = new HashMap<>();
		listSteps.put("Steps", "List of Steps");
		return listSteps;
	}

}
