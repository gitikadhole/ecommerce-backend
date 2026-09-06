package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class CacheConfig {
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
		return RedisCacheManager.builder(factory).build();
	}
}
