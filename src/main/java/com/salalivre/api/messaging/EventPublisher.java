package com.salalivre.api.messaging;

import com.salalivre.api.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarReservaCriada(ReservaCriadaEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE,
                    RabbitMqConfig.ROUTING_KEY_RESERVA_CRIADA,
                    event
            );
            log.info("Evento RESERVA_CRIADA publicado: reservaId={}", event.getReservaId());
        } catch (Exception e) {
            log.error("Falha ao publicar evento RESERVA_CRIADA: {}", e.getMessage(), e);
        }
    }

    public void publicarBuscaSalasProximas(BuscaSalasProximasEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE,
                    RabbitMqConfig.ROUTING_KEY_BUSCA_LOCALIZACAO,
                    event
            );
            log.info("Evento BUSCA_SALAS_PROXIMAS publicado: cepOrigem={}, resultados={}",
                    event.getCepOrigem(), event.getQuantidadeResultados());
        } catch (Exception e) {
            log.error("Falha ao publicar evento BUSCA_SALAS_PROXIMAS: {}", e.getMessage(), e);
        }
    }
}
