package com.fatec.biblioteca;

public enum SituacaoReserva {
    ATIVA, AGUARDANDO, ATENDIDA, CANCELADA, EXPIRADA;

    public boolean isViva() {
        return this == ATIVA || this == AGUARDANDO;
    }
}
