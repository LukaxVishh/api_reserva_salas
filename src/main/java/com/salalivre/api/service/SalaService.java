package com.salalivre.api.service;

import com.salalivre.api.exception.RecursoNaoEncontradoException;
import com.salalivre.api.model.Sala;
import com.salalivre.api.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public List listarTodas() {
        return salaRepository.listarTodas();
    }

    public Sala buscarPorId(Integer id) {
        Sala sala = salaRepository.buscarPorId(id);

        if (sala == null) {
            throw new RecursoNaoEncontradoException("Sala não encontrada.");
        }

        return sala;
    }

    public Sala salvar(Sala sala) {
        validarSala(sala);
        return salaRepository.salvar(sala);
    }

    public Sala atualizar(Integer id, Sala sala) {
        buscarPorId(id);
        validarSala(sala);
        return salaRepository.atualizar(id, sala);
    }

    public void deletar(Integer id) {
        buscarPorId(id);
        salaRepository.deletar(id);
    }

    private void validarSala(Sala sala) {
        if (sala.getNome() == null || sala.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da sala é obrigatório.");
        }

        if (sala.getBloco() == null || sala.getBloco().trim().isEmpty()) {
            throw new IllegalArgumentException("O bloco da sala é obrigatório.");
        }

        if (sala.getCapacidade() == null || sala.getCapacidade() <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        }

        if (sala.getTemProjetor() == null) {
            throw new IllegalArgumentException("Informe se a sala possui projetor.");
        }

        if (sala.getAtiva() == null) {
            throw new IllegalArgumentException("Informe se a sala está ativa.");
        }
    }
}