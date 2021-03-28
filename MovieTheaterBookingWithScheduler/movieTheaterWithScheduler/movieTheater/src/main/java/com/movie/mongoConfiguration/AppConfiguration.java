package com.movie.mongoConfiguration;

import java.time.Duration;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.aggregation.DateOperators.Minute;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class AppConfiguration {

	 @Bean
	    public Docket api() { 
	        return new Docket(DocumentationType.SWAGGER_2)  
	          .select()                                  
	          .apis(RequestHandlerSelectors.any())              
	          .paths(PathSelectors.any())                          
	          .build();                                           
	    }
	 
	 @Bean
	 @Qualifier("userAuth")
	 Cache<String,Integer> userCache()
	 {
		 CacheManager cacheManager = CacheManagerBuilder
		          .newCacheManagerBuilder().build();
		        cacheManager.init();

		        Cache<String,Integer> cache = cacheManager
		          .createCache("users", CacheConfigurationBuilder
		            .newCacheConfigurationBuilder(
		              String.class, Integer.class,
		              ResourcePoolsBuilder.heap(10)).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(600))));
		        return cache;
	 }
	 
	 
	 @Bean
	 CacheManager manager()
	 {
		 CacheManager cacheManager = CacheManagerBuilder
		          .newCacheManagerBuilder().build();
		        cacheManager.init();
		        return cacheManager;
	 }
	 
	 
	 
	 
	 @Bean
	 @Qualifier("timings")
	 Cache<Integer,Cache> ticketBlocking(CacheManager manager)
	 {
		        ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(1));
		        
		        Cache<Integer,Cache> cache = manager()
		          .createCache("timings", CacheConfigurationBuilder
		            .newCacheConfigurationBuilder(
		              Integer.class, Cache.class,
		              ResourcePoolsBuilder.heap(10)));
		        
		        return cache;
	 }
}
