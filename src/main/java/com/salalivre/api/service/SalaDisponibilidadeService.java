// java
package com.salalivre.api.service;

import com.salalivre.api.messaging.BuscaSalasProximasEvent;
import com.salalivre.api.messaging.EventPublisher;
import com.salalivre.api.model.Sala;
import com.salalivre.api.model.Reserva;
import com.salalivre.api.model.SalaDisponivelProximaResponse;
import com.salalivre.api.model.EnderecoViaCep;
import com.salalivre.api.repository.SalaRepository;
import com.salalivre.api.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class SalaDisponibilidadeService {

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private LocalizacaoService localizacaoService;

    @Autowired
    private DistanciaService distanciaService;

    @Autowired
    private EventPublisher eventPublisher;

    public List<SalaDisponivelProximaResponse> buscarSalasProximasDisponiveis(
            String cepOrigem,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
        Objects.requireNonNull(cepOrigem, "cep is required");
        Objects.requireNonNull(data, "data is required");
        Objects.requireNonNull(horaInicio, "horaInicio is required");
        Objects.requireNonNull(horaFim, "horaFim is required");
        if (!horaFim.isAfter(horaInicio)) return List.of();

        EnderecoViaCep origem = localizacaoService.buscarEnderecoPorCep(cepOrigem);

        List<Sala> todas = salaRepository.listarTodas();
        List<SalaDisponivelProximaResponse> result = new ArrayList<>();

        for (Sala sala : todas) {
            if (sala == null) continue;
            Boolean ativa = sala.getAtiva();
            String salaCep = sala.getCep();
            if (ativa == null || !ativa) continue;
            if (salaCep == null || salaCep.trim().isEmpty()) continue;

            boolean conflito;
            try {
                List<Reserva> conflitos = reservaRepository.buscarConflitos(
                        sala.getId(), data, horaInicio, horaFim, null
                );
                conflito = conflitos != null && !conflitos.isEmpty();
            } catch (NoSuchMethodError | AbstractMethodError e) {
                conflito = true;
            }

            if (conflito) continue;

            double distancia = distanciaService.calcularDistanciaAproximadaKm(origem, sala);
            String classificacao = distanciaService.classificarProximidade(origem, sala);

            SalaDisponivelProximaResponse resp = new SalaDisponivelProximaResponse(
                    sala.getId(),
                    sala.getNome(),
                    sala.getBloco(),
                    sala.getCapacidade(),
                    sala.getTemProjetor(),
                    sala.getCep(),
                    sala.getLogradouro(),
                    sala.getBairro(),
                    sala.getCidade(),
                    sala.getUf(),
                    distancia,
                    classificacao
            );
            result.add(resp);
        }

        result.sort(Comparator.comparingDouble(SalaDisponivelProximaResponse::getDistanciaAproximadaKm));

        eventPublisher.publicarBuscaSalasProximas(new BuscaSalasProximasEvent(
                cepOrigem, data, horaInicio, horaFim, result.size()
        ));

        return result;
    }
}