package com.neec.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	@Value("${rabbitmq.topic.exchange.name}")
	private String topicExchangeName;

	@Value("${rabbitmq.email.queue.name}")
	private String emailQueueName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	@Bean
	public TopicExchange topicExchange() {
		return new TopicExchange(topicExchangeName);
	}

	@Bean
	public Queue verificationEmailQueue() {
		return new Queue(emailQueueName);
	}

	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(verificationEmailQueue()).to(topicExchange()).with(routingKey);
	}

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
