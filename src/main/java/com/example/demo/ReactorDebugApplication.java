package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.blockhound.BlockHound;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;

@SpringBootApplication
public class ReactorDebugApplication {
	static {
		final Logger blockingLog = LoggerFactory.getLogger("blocking.call");
		BlockHound.install(
				builder -> builder.blockingMethodCallback(blockingMethod -> {
					blockingLog.error("blocking call inside method: {}", blockingMethod, new Error());
				})
		);

		ReactorDebugAgent.init();

		Mono.delay(Duration.ofMillis(1))
				.doOnNext(it -> {
					try {
						Thread.sleep(1);
					}
					catch (InterruptedException e) {
						throw Exceptions.propagate(e);
					}
				})
				.subscribe(); // test log

	}

	public static void main(String[] args) {

		SpringApplication.run(ReactorDebugApplication.class, args);
	}

}

@RequestMapping("/test")
@RestController()
class Ctrl {

	@GetMapping
	public Mono<?> getObj() {
		return Mono.delay(Duration.ofMillis(2))
				.doOnNext(aLong -> {
					try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						throw Exceptions.propagate(e);
					}
					throw new RuntimeException("boom");
				});
	}

}
