package com.salalivre.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "sala-livre.exchange";

    public static final String QUEUE_RESERVAS_CRIADAS = "sala-livre.reservas.criadas";
    public static final String QUEUE_BUSCAS_LOCALIZACAO = "sala-livre.buscas.localizacao";

    public static final String ROUTING_KEY_RESERVA_CRIADA = "reserva.criada";
    public static final String ROUTING_KEY_BUSCA_LOCALIZACAO = "busca.localizacao";

    @Bean
    public TopicExchange salaLivreExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue reservasCriadasQueue() {
        return new Queue(QUEUE_RESERVAS_CRIADAS, true);
    }

    @Bean
    public Queue buscasLocalizacaoQueue() {
        return new Queue(QUEUE_BUSCAS_LOCALIZACAO, true);
    }

    @Bean
    public Binding bindingReservasCriadas(Queue reservasCriadasQueue, TopicExchange salaLivreExchange) {
        return BindingBuilder.bind(reservasCriadasQueue).to(salaLivreExchange).with(ROUTING_KEY_RESERVA_CRIADA);
    }

    @Bean
    public Binding bindingBuscasLocalizacao(Queue buscasLocalizacaoQueue, TopicExchange salaLivreExchange) {
        return BindingBuilder.bind(buscasLocalizacaoQueue).to(salaLivreExchange).with(ROUTING_KEY_BUSCA_LOCALIZACAO);
    }

    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper rabbitObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setExchange(EXCHANGE);
        return template;
    }
}
