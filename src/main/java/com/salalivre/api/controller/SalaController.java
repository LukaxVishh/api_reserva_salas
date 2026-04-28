package com.salalivre.api.controller;

import com.salalivre.api.model.Sala;
import com.salalivre.api.service.SalaService;
import org.springframework.http.HttpStatus;
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
    public List<Sala> listarTodas() {
        return salaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Sala buscarPorId(@PathVariable Integer id) {
        return salaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sala salvar(@RequestBody Sala sala) {
        return salaService.salvar(sala);
    }

    @PutMapping("/{id}")
    public Sala atualizar(@PathVariable Integer id, @RequestBody Sala sala) {
        return salaService.atualizar(id, sala);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Integer id) {
        salaService.deletar(id);
    }
}