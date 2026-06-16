package com.example.gerenciadordepagamentos.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class Moeda {

    private static final DecimalFormat FORMATO;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        FORMATO = new DecimalFormat("#,##0.00", simbolos);
    }

    private Moeda() {}

    /** Formata um valor no padrão brasileiro, ex: 3300.0 -> "R$ 3.300,00" */
    public static String formatar(double valor) {
        return "R$ " + FORMATO.format(valor);
    }
}
