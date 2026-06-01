package com.salalivre.api.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salalivre.api.model.SalaDisponivelProximaResponse;
import com.salalivre.api.service.SalaDisponibilidadeService;

@RestController
@RequestMapping("/salas/disponiveis")
public class SalaDisponibilidadeController {

    @Autowired
    private SalaDisponibilidadeService disponibilidadeService;

    @GetMapping("/proximas")
    public ResponseEntity<List<SalaDisponivelProximaResponse>> buscarProximas(
            @RequestParam String cep,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim
    ) {
        List<SalaDisponivelProximaResponse> lista = disponibilidadeService.buscarSalasProximasDisponiveis(cep, data, horaInicio, horaFim);
        return ResponseEntity.ok(lista);
    }
}