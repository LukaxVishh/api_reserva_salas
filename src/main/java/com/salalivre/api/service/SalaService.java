package com.salalivre.api.service;

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

    public List<Sala> listarTodas() {
        return salaRepository.listarTodas();
    }
}