package com.example.gerenciadordepagamentos.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ContaDAO {

    @Insert
    void inserir(Conta conta);

    @Update
    void atualizar(Conta conta);

    @Delete
    void deletar(Conta conta);

    @Query("SELECT * FROM tabela_contas ORDER BY id DESC")
    List<Conta> listarTodas();

    @Query("SELECT IFNULL(SUM(valor), 0) FROM tabela_contas WHERE tipo = 'Receita'")
    double buscarTotalReceitas();

    @Query("SELECT IFNULL(SUM(valor), 0) FROM tabela_contas WHERE tipo = 'Despesa'")
    double buscarTotalDespesas();

    @Query("SELECT DISTINCT categoria FROM tabela_contas")
    List<String> listarCategorias();

    @Query("UPDATE tabela_contas SET categoria = :novaCategoria WHERE categoria = :categoriaAntiga")
    void atualizarNomeCategoria(String categoriaAntiga, String novaCategoria);

    @Query("UPDATE tabela_contas SET categoria = 'Geral' WHERE categoria = :categoriaExcluida")
    void limparCategoriaExcluida(String categoriaExcluida);
}