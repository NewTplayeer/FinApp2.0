package com.example.gerenciadordepagamentos.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tabela_contas")
public class Conta {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String tipo;
    public String categoria;
    public String descricao;
    public double valor;
    public String data;
    public String formaPagamento;

    public Conta() {}
}