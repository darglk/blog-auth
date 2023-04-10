package com.darglk.blogauth.config;

import com.darglk.blogcommons.events.Subjects;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(rabbitHost);
    }

    @Bean
    public RabbitAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue userCreatedQueue() {
       return new Queue(Subjects.USER_CREATED_QUEUE);
    }
}