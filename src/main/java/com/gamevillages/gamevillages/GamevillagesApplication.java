package com.gamevillages.gamevillages;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.clients.jedis.Jedis;

@SpringBootApplication
public class GamevillagesApplication {
	public static Jedis jedis = new Jedis("localhost",6379);
	public static void main(String[] args) {
		SpringApplication.run(GamevillagesApplication.class, args);

	}

}
