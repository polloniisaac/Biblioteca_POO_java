package com.fatec.biblioteca;

/**
 * ATIVA = na fila; AGUARDANDO = cópia separada esperando retirada;
 * ATENDIDA = virou empréstimo; CANCELADA; EXPIRADA = não retirou no prazo.
 */
public enum SituacaoReserva {
    ATIVA, AGUARDANDO, ATENDIDA, CANCELADA, EXPIRADA;

    /** Reserva "viva" ainda ocupa lugar na fila. */
    public boolean isViva() {
        return this == ATIVA || this == AGUARDANDO;
    }
}
