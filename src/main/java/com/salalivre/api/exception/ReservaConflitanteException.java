package com.salalivre.api.exception;

public class ReservaConflitanteException extends RuntimeException {

    public ReservaConflitanteException(String mensagem) {
        super(mensagem);
    }
}
