package com.salalivre.api.controller;

import com.salalivre.api.model.EnderecoViaCep;
import com.salalivre.api.service.LocalizacaoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/localizacao")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    public LocalizacaoController(LocalizacaoService localizacaoService) {
        this.localizacaoService = localizacaoService;
    }

    @GetMapping("/cep/{cep}")
    public EnderecoViaCep buscarEnderecoPorCep(@PathVariable String cep) {
        return localizacaoService.buscarEnderecoPorCep(cep);
    }
}