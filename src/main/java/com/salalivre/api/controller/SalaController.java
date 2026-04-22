package com.salalivre.api.controller;

import com.salalivre.api.model.Sala;
import com.salalivre.api.service.SalaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public List listarTodas() {
        return salaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Sala buscarPorId(@PathVariable Integer id) {
        return salaService.buscarPorId(id);
    }

    @PostMapping
    public Sala salvar(@RequestBody Sala sala) {
        return salaService.salvar(sala);
    }

    @PutMapping("/{id}")
    public Sala atualizar(@PathVariable Integer id, @RequestBody Sala sala) {
        return salaService.atualizar(id, sala);
    }

    @DeleteMapping("/{id}")
    public String deletar(@PathVariable Integer id) {
        salaService.deletar(id);
        return "Sala deletada com sucesso.";
    }
}