package com.salalivre.api.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salalivre.api.config.RabbitMqConfig;
import com.salalivre.api.model.EventoSistema;
import com.salalivre.api.repository.EventoSistemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final EventoSistemaRepository eventoRepository;
    private final ObjectMapper objectMapper;

    public EventConsumer(EventoSistemaRepository eventoRepository, ObjectMapper rabbitObjectMapper) {
        this.eventoRepository = eventoRepository;
        this.objectMapper = rabbitObjectMapper;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_RESERVAS_CRIADAS)
    public void consumirReservaCriada(ReservaCriadaEvent event) {
        log.info("Recebido evento RESERVA_CRIADA: reservaId={}", event.getReservaId());
        persistir(event.getTipo(), event);
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_BUSCAS_LOCALIZACAO)
    public void consumirBuscaSalasProximas(BuscaSalasProximasEvent event) {
        log.info("Recebido evento BUSCA_SALAS_PROXIMAS: cepOrigem={}", event.getCepOrigem());
        persistir(event.getTipo(), event);
    }

    private void persistir(String tipo, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            EventoSistema evento = new EventoSistema(null, tipo, json, null);
            eventoRepository.salvar(evento);
            log.info("Evento {} persistido em eventos_sistema.", tipo);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar payload do evento {}: {}", tipo, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Falha ao persistir evento {}: {}", tipo, e.getMessage(), e);
        }
    }
}
