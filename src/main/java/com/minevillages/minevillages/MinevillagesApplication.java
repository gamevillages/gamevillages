package com.minevillages.minevillages;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.clients.jedis.Jedis;

@SpringBootApplication
public class MinevillagesApplication {
	public static Jedis jedis = new Jedis("localhost",6379);
	public static void main(String[] args) {
		SpringApplication.run(MinevillagesApplication.class, args);

	}

}
