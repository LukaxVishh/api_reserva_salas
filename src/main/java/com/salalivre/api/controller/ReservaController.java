package com.salalivre.api.controller;

import com.salalivre.api.model.Reserva;
import com.salalivre.api.service.ReservaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<Reserva> listarTodas() {
        return reservaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable Integer id) {
        return reservaService.buscarPorId(id);
    }

    @GetMapping("/sala/{salaId}")
    public List<Reserva> listarPorSala(@PathVariable Integer salaId) {
        return reservaService.listarPorSala(salaId);
    }

    @PostMapping
    public Reserva salvar(@RequestBody Reserva reserva) {
        return reservaService.salvar(reserva);
    }

    @PutMapping("/{id}")
    public Reserva atualizar(@PathVariable Integer id, @RequestBody Reserva reserva) {
        return reservaService.atualizar(id, reserva);
    }

    @DeleteMapping("/{id}")
    public String deletar(@PathVariable Integer id) {
        reservaService.deletar(id);
        return "Reserva deletada com sucesso.";
    }
}
