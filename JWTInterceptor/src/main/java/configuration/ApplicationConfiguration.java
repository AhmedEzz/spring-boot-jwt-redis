package configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import model.User;

@Configuration
public class ApplicationConfiguration extends WebMvcConfigurerAdapter{

	private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

//	@Autowired
//	AuthenticationInterceptor authenticationInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		logger.info("Inside Application configuration to add Auth Interceptor");
		//registry.addInterceptor(authenticationInterceptor).addPathPatterns("/").excludePathPatterns("/login");
	}

	@Bean
	JedisConnectionFactory jedisConnectionFactory(){
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName("localhost");
		factory.setPort(6379);
		factory.setUsePool(true);
		return factory;
	}
	
	@Bean
	RedisTemplate<String, User> redisTemplate(){
		final RedisTemplate<String, User> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericToStringSerializer<>(Object.class));
		template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		return template;
	}
	
	@Bean
	MessageListenerAdapter messageListener() {
	 return new MessageListenerAdapter( new RedisMessageListener() );
	}
	
	@Bean
	RedisMessageListenerContainer redisContainer() {
	 final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

	 container.setConnectionFactory( jedisConnectionFactory() );
	 container.addMessageListener( messageListener(), new ChannelTopic( "fitApp-queue" ) );

	 return container;
	}
}
