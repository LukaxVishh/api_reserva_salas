package com.salalivre.api.service;

import com.salalivre.api.exception.RecursoNaoEncontradoException;
import com.salalivre.api.model.EnderecoViaCep;
import com.salalivre.api.model.Sala;
import com.salalivre.api.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;
    private final LocalizacaoService localizacaoService;

    public SalaService(SalaRepository salaRepository, LocalizacaoService localizacaoService) {
        this.salaRepository = salaRepository;
        this.localizacaoService = localizacaoService;
    }

    public List<Sala> listarTodas() {
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
        normalizarDadosBasicos(sala);
        preencherEnderecoPorCepQuandoInformado(sala);
        validarSala(sala);
        return salaRepository.salvar(sala);
    }

    public Sala atualizar(Integer id, Sala sala) {
        buscarPorId(id);
        normalizarDadosBasicos(sala);
        preencherEnderecoPorCepQuandoInformado(sala);
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

        validarUfQuandoInformada(sala.getUf());
    }

    private void preencherEnderecoPorCepQuandoInformado(Sala sala) {
        if (sala.getCep() == null || sala.getCep().trim().isEmpty()) {
            return;
        }

        EnderecoViaCep endereco = localizacaoService.buscarEnderecoPorCep(sala.getCep());

        sala.setCep(endereco.getCep());
        sala.setLogradouro(endereco.getLogradouro());
        sala.setBairro(endereco.getBairro());
        sala.setCidade(endereco.getCidade());
        sala.setUf(endereco.getUf());

        sala.setNumero(normalizarTexto(sala.getNumero()));
        sala.setComplemento(normalizarTexto(sala.getComplemento()));
    }

    private void validarUfQuandoInformada(String uf) {
        if (uf == null || uf.trim().isEmpty()) {
            return;
        }

        if (!uf.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("A UF deve conter exatamente 2 letras maiúsculas.");
        }
    }

    private void normalizarDadosBasicos(Sala sala) {
        sala.setNome(normalizarTexto(sala.getNome()));
        sala.setBloco(normalizarTexto(sala.getBloco()));
        sala.setNumero(normalizarTexto(sala.getNumero()));
        sala.setComplemento(normalizarTexto(sala.getComplemento()));
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }
}