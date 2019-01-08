package interceptors;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import util.JWTUtil;

@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

	@Autowired
	JWTUtil jwtUtil;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		logger.info("Auth Interceptor Start");
		if (request.getMethod().equals("GET"))
			try {
				throw new Exception("Get method not allowed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		else {
			String authorization = request.getHeader("Authorization");
			if (authorization == null) {
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
				// JWT Verification.
				try {
					jwtUtil.verfiyToke(authorization);
					
				} catch (URISyntaxException | IOException e) {
					e.printStackTrace();
				}
			}

		}
		return true;

	}

}
