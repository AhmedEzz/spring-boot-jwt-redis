package util;

import static java.time.ZoneOffset.UTC;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import model.User;

@Component
public class JWTUtil {

	private static final String ISSUER = "fitapp.jwt";
	private SecretKeyProvider secretKeyProvider;

	@Autowired
	RedisUtil redisUtil;
	
	@Autowired
    public JWTUtil(SecretKeyProvider secretKeyProvider) {
        this.secretKeyProvider = secretKeyProvider;
    }
	
	/**
	 * Main method to generate the token.
	 * 
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public String tokenFor(User user) throws IOException, URISyntaxException {
        byte[] secretKey = secretKeyProvider.getKey();
        Date expiration = Date.from(LocalDateTime.now().plusMinutes(2).toInstant(UTC));
        return Jwts.builder()
                .setSubject(user.getMobileNumber())
                .setExpiration(expiration)
                .setIssuer(ISSUER)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
	
	/**
	 * Use this method to generate the refreshment token.
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public String refreshTokenFor(String username) throws IOException, URISyntaxException {
        byte[] secretKey = secretKeyProvider.getKey();
        Date expiration = Date.from(LocalDateTime.now().plusMinutes(2).toInstant(UTC));
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(expiration)
                .setIssuer(ISSUER)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
	
	/**
	 * After generate the refresh token, we can pass it back in new header.
	 * 
	 * @param token
	 * @return
	 */
	public String refreshToken(String token)
	{
        Jws<Claims> claims = returnTokenClaims(token);
        Date expDate = claims.getBody().getExpiration();
        Date now = new Date();
        if(expDate.after(now))
        {
        	try {
        		token = refreshTokenFor(claims.getBody().getSubject());
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
        }
		return token;
	}
	
	/**
	 * Parse Token information.
	 * 
	 * @param token
	 * @return
	 */
	public Jws<Claims> returnTokenClaims(String token)
	{
		byte[] secretKey;
		Jws<Claims> claims = null;
		try {
			secretKey = secretKeyProvider.getKey();
			claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		} catch (URISyntaxException | IOException e) {
			// Incase of exception that mean this token not valid
			e.printStackTrace();
		}

        return claims;
	}
	
	public void verfiyToke(String token) throws URISyntaxException, IOException
	{
		byte[] secretKey  = secretKeyProvider.getKey();
		Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		
	}
	
	public boolean isTokenExpired(String token)
	{
		Jws<Claims> claims = returnTokenClaims(token);
        Date expDate = claims.getBody().getExpiration();
        Date now = Date.from(LocalDateTime.now().toInstant(UTC));
        return expDate.before(now);
	}
	
	public boolean isTokenRelatedToSession(String token, String SID)
	{
		Jws<Claims> claims = returnTokenClaims(token);
		User user = (User) redisUtil.getValue(claims.getBody().getSubject());
		boolean flag = SID.equals(user.getSID());
		return flag;
	}
	
	public String generateRefreshToken(){
		String uniqueID = UUID.randomUUID().toString();
		uniqueID = uniqueID.replaceAll("-", "");
		return uniqueID;
	}
}
