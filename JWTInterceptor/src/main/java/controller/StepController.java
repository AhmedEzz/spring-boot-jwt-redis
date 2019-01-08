package controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import service.StepServices;
import util.JWTUtil;

@Controller
public class StepController {

	@Autowired
	StepServices stepServices;
	
	@Autowired
	JWTUtil jwtUtil;
	
	@RequestMapping(value = "/listSteps", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object listSteps(HttpServletRequest request)
	{
		String authorization = request.getHeader("Authorization");
		try {
			jwtUtil.verfiyToke(authorization);
			
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return stepServices.listAllSteps(request);
	}
}
