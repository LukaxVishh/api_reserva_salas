package com.salalivre.api.service;

import com.salalivre.api.exception.RecursoNaoEncontradoException;
import com.salalivre.api.exception.ReservaConflitanteException;
import com.salalivre.api.model.Reserva;
import com.salalivre.api.model.Sala;
import com.salalivre.api.model.StatusReserva;
import com.salalivre.api.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaService {

    private static final int DURACAO_MAXIMA_HORAS = 4;
    private static final LocalTime HORARIO_FUNCIONAMENTO_INICIO = LocalTime.of(7, 0);
    private static final LocalTime HORARIO_FUNCIONAMENTO_FIM = LocalTime.of(23, 0);

    private final ReservaRepository reservaRepository;
    private final SalaService salaService;

    public ReservaService(ReservaRepository reservaRepository, SalaService salaService) {
        this.reservaRepository = reservaRepository;
        this.salaService = salaService;
    }

    public List<Reserva> listarTodas() {
        List<Reserva> reservas = reservaRepository.listarTodas();
        reservas.forEach(this::atualizarStatus);
        return reservas;
    }

    public Reserva buscarPorId(Integer id) {
        Reserva reserva = reservaRepository.buscarPorId(id);

        if (reserva == null) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada.");
        }

        atualizarStatus(reserva);
        return reserva;
    }

    public List<Reserva> listarPorSala(Integer salaId) {
        salaService.buscarPorId(salaId);
        List<Reserva> reservas = reservaRepository.listarPorSala(salaId);
        reservas.forEach(this::atualizarStatus);
        return reservas;
    }

    public Reserva salvar(Reserva reserva) {
        validarReserva(reserva);
        validarSalaAtiva(reserva.getSalaId());
        validarConflito(reserva, null);

        reserva.setStatus(calcularStatus(reserva));
        return reservaRepository.salvar(reserva);
    }

    public Reserva atualizar(Integer id, Reserva reserva) {
        buscarPorId(id);
        validarReserva(reserva);
        validarSalaAtiva(reserva.getSalaId());
        validarConflito(reserva, id);

        reserva.setStatus(calcularStatus(reserva));
        return reservaRepository.atualizar(id, reserva);
    }

    public void deletar(Integer id) {
        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() == StatusReserva.EM_ANDAMENTO) {
            throw new IllegalArgumentException("Não é possível excluir uma reserva em andamento.");
        }

        reservaRepository.deletar(id);
    }

    private void validarReserva(Reserva reserva) {
        if (reserva.getNomeResponsavel() == null || reserva.getNomeResponsavel().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do responsável é obrigatório.");
        }

        if (reserva.getDescricao() == null || reserva.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da reserva é obrigatória.");
        }

        if (reserva.getData() == null) {
            throw new IllegalArgumentException("A data da reserva é obrigatória.");
        }

        if (reserva.getHoraInicio() == null || reserva.getHoraFim() == null) {
            throw new IllegalArgumentException("Hora de início e fim são obrigatórias.");
        }

        if (reserva.getSalaId() == null) {
            throw new IllegalArgumentException("A sala da reserva é obrigatória.");
        }

        if (reserva.getData().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Não é possível agendar reserva em data passada.");
        }

        if (!reserva.getHoraInicio().isBefore(reserva.getHoraFim())) {
            throw new IllegalArgumentException("A hora de início deve ser anterior à hora de fim.");
        }

        if (reserva.getHoraInicio().isBefore(HORARIO_FUNCIONAMENTO_INICIO)
                || reserva.getHoraFim().isAfter(HORARIO_FUNCIONAMENTO_FIM)) {
            throw new IllegalArgumentException(
                    "A reserva deve estar dentro do horário de funcionamento (07:00 às 23:00).");
        }

        long minutos = Duration.between(reserva.getHoraInicio(), reserva.getHoraFim()).toMinutes();
        if (minutos > DURACAO_MAXIMA_HORAS * 60L) {
            throw new IllegalArgumentException(
                    "A duração máxima de uma reserva é de " + DURACAO_MAXIMA_HORAS + " horas.");
        }

        if (reserva.getData().isEqual(LocalDate.now())
                && reserva.getHoraInicio().isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar reserva em horário já passado.");
        }
    }

    private void validarSalaAtiva(Integer salaId) {
        Sala sala = salaService.buscarPorId(salaId);
        if (Boolean.FALSE.equals(sala.getAtiva())) {
            throw new IllegalArgumentException("Não é possível reservar uma sala inativa.");
        }
    }

    private void validarConflito(Reserva reserva, Integer idIgnorar) {
        List<Reserva> conflitos = reservaRepository.buscarConflitos(
                reserva.getSalaId(),
                reserva.getData(),
                reserva.getHoraInicio(),
                reserva.getHoraFim(),
                idIgnorar
        );

        if (!conflitos.isEmpty()) {
            throw new ReservaConflitanteException(
                    "Já existe uma reserva para esta sala neste horário.");
        }
    }

    private StatusReserva calcularStatus(Reserva reserva) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = LocalDateTime.of(reserva.getData(), reserva.getHoraInicio());
        LocalDateTime fim = LocalDateTime.of(reserva.getData(), reserva.getHoraFim());

        if (agora.isBefore(inicio)) {
            return StatusReserva.AGENDADA;
        }
        if (agora.isAfter(fim)) {
            return StatusReserva.FINALIZADA;
        }
        return StatusReserva.EM_ANDAMENTO;
    }

    private void atualizarStatus(Reserva reserva) {
        reserva.setStatus(calcularStatus(reserva));
    }
}
