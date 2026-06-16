package com.example.gerenciadordepagamentos;

import android.app.AlertDialog;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gerenciadordepagamentos.database.AppDatabase;
import com.example.gerenciadordepagamentos.database.Conta;
import com.example.gerenciadordepagamentos.util.Moeda;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView txtSaldo, txtReceitas, txtDespesas;
    private RecyclerView recyclerContas;
    private ContaAdapter adaptador;
    private AppDatabase banco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarComponentes();
        configurarBanco();
        configurarLista();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (banco != null) {
            recarregarLista();
            atualizarResumo();
        }
    }

    private void iniciarComponentes() {
        txtSaldo = findViewById(R.id.txtSaldo);
        txtReceitas = findViewById(R.id.txtReceitas);
        txtDespesas = findViewById(R.id.txtDespesas);
        recyclerContas = findViewById(R.id.recyclerContas);

        findViewById(R.id.btnAdicionar).setOnClickListener(v -> abrirDialogoCadastro(null));

        findViewById(R.id.btnRelatorio).setOnClickListener(v -> {
            Intent intent = new Intent(this, RelatorioActivity.class);
            startActivity(intent);
        });
    }

    private void configurarBanco() {
        banco = AppDatabase.getInstancia(this);
    }

    private void configurarLista() {
        adaptador = new ContaAdapter(new ArrayList<>(), new ContaAdapter.AoClicar() {
            @Override
            public void aoClicarLongo(Conta conta) {}

            @Override
            public void aoClicarCurto(Conta conta) {
                abrirDialogoCadastro(conta);
            }
        });
        recyclerContas.setLayoutManager(new LinearLayoutManager(this));
        recyclerContas.setAdapter(adaptador);
    }

    private List<String> carregarCategorias() {
        SharedPreferences prefs = getSharedPreferences("dados_app", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("categorias", null);
        if (set == null || set.isEmpty()) {
            List<String> padrao = new ArrayList<>();
            padrao.add("Geral");
            padrao.add("Alimentação");
            padrao.add("Transporte");
            return padrao;
        }
        return new ArrayList<>(set);
    }

    private void salvarCategorias(List<String> categorias) {
        SharedPreferences prefs = getSharedPreferences("dados_app", MODE_PRIVATE);
        prefs.edit().putStringSet("categorias", new HashSet<>(categorias)).apply();
    }

    private void abrirDialogoCadastro(Conta contaExistente) {
        AlertDialog.Builder construtor = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_cadastro, null);
        construtor.setView(view);
        AlertDialog dialogo = construtor.create();

        EditText editDescricao = view.findViewById(R.id.editDescricao);
        EditText editValor = view.findViewById(R.id.editValor);
        TextView txtDataCadastro = view.findViewById(R.id.txtDataCadastro);
        Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        RadioGroup grupoTipo = view.findViewById(R.id.grupoTipo);
        RadioGroup grupoPagamento = view.findViewById(R.id.grupoPagamento);
        Button btnSalvar = view.findViewById(R.id.btnSalvar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);
        Button btnExcluir = view.findViewById(R.id.btnExcluir);
        ImageButton btnAddCat = view.findViewById(R.id.btnAddCategoria);
        ImageButton btnEditCat = view.findViewById(R.id.btnEditCategoria);

        TextView txtLabelPagamento = view.findViewById(R.id.txtLabelPagamento);
        android.widget.RadioButton radioCartao = view.findViewById(R.id.radioCartao);

        List<String> categorias = carregarCategorias();
        ArrayAdapter<String> adapterCat = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.WHITE);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.WHITE);
                tv.setBackgroundColor(android.graphics.Color.parseColor("#2A2A2A"));
                return tv;
            }
        };
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCat);

        Calendar calCadastro = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (contaExistente != null && contaExistente.data != null) {
            txtDataCadastro.setText(contaExistente.data);
        } else {
            txtDataCadastro.setText(sdf.format(calCadastro.getTime()));
        }

        txtDataCadastro.setOnClickListener(v -> {
            new DatePickerDialog(this, (view1, ano, mes, dia) -> {
                calCadastro.set(ano, mes, dia);
                txtDataCadastro.setText(sdf.format(calCadastro.getTime()));
            }, calCadastro.get(Calendar.YEAR), calCadastro.get(Calendar.MONTH), calCadastro.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnAddCat.setOnClickListener(v -> {
            EditText input = new EditText(this);
            new AlertDialog.Builder(this).setTitle("Nova Categoria").setView(input)
                    .setPositiveButton("Adicionar", (d, w) -> {
                        categorias.add(input.getText().toString());
                        salvarCategorias(categorias);
                        adapterCat.notifyDataSetChanged();
                        spinnerCategoria.setSelection(categorias.size() - 1);
                    }).show();
        });

        btnEditCat.setOnClickListener(v -> {
            if (spinnerCategoria.getSelectedItem() == null) return;
            String catAtual = spinnerCategoria.getSelectedItem().toString();
            EditText input = new EditText(this);
            input.setText(catAtual);
            new AlertDialog.Builder(this).setTitle("Editar/Excluir Categoria").setView(input)
                    .setPositiveButton("Salvar", (d, w) -> {
                        categorias.remove(catAtual);
                        categorias.add(input.getText().toString());
                        salvarCategorias(categorias);
                        adapterCat.notifyDataSetChanged();
                    })
                    .setNeutralButton("Excluir", (d, w) -> {
                        categorias.remove(catAtual);
                        if (categorias.isEmpty()) categorias.add("Geral");
                        salvarCategorias(categorias);
                        adapterCat.notifyDataSetChanged();
                    }).show();
        });

        btnCancelar.setOnClickListener(v -> dialogo.dismiss());

        if (contaExistente != null) {
            editDescricao.setText(contaExistente.descricao);
            editValor.setText(String.valueOf(contaExistente.valor));

            if (contaExistente.tipo.equals("Receita")) grupoTipo.check(R.id.radioReceita);
            else grupoTipo.check(R.id.radioDespesa);

            if (contaExistente.formaPagamento != null) {
                if (contaExistente.formaPagamento.equals("Cartão")) grupoPagamento.check(R.id.radioCartao);
                else if (contaExistente.formaPagamento.equals("Dinheiro")) grupoPagamento.check(R.id.radioDinheiro);
                else grupoPagamento.check(R.id.radioPix);
            }

            btnExcluir.setVisibility(View.VISIBLE);
            btnExcluir.setOnClickListener(v -> {
                banco.contaDAO().deletar(contaExistente);
                dialogo.dismiss();
                recarregarLista();
                atualizarResumo();
            });
        }

        grupoTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioReceita) {
                txtLabelPagamento.setText("Forma de Recebimento:");
                radioCartao.setVisibility(View.GONE);
                if (grupoPagamento.getCheckedRadioButtonId() == R.id.radioCartao) {
                    grupoPagamento.check(R.id.radioPix);
                }
            } else {
                txtLabelPagamento.setText("Forma de Pagamento:");
                radioCartao.setVisibility(View.VISIBLE);
            }
        });

        if (grupoTipo.getCheckedRadioButtonId() == R.id.radioReceita) {
            txtLabelPagamento.setText("Forma de Recebimento:");
            radioCartao.setVisibility(View.GONE);
        }

        btnSalvar.setOnClickListener(v -> {
            if (editDescricao.getText().toString().isEmpty() || editValor.getText().toString().isEmpty()) {
                Toast.makeText(this, "Preencha a descrição e o valor!", Toast.LENGTH_SHORT).show();
                return;
            }

            Conta c = (contaExistente == null) ? new Conta() : contaExistente;
            c.descricao = editDescricao.getText().toString();
            c.valor = Double.parseDouble(editValor.getText().toString());
            c.categoria = spinnerCategoria.getSelectedItem() != null ? spinnerCategoria.getSelectedItem().toString() : "Geral";
            c.tipo = grupoTipo.getCheckedRadioButtonId() == R.id.radioReceita ? "Receita" : "Despesa";
            c.data = txtDataCadastro.getText().toString();

            int idPagamento = grupoPagamento.getCheckedRadioButtonId();
            if (idPagamento == R.id.radioCartao) c.formaPagamento = "Cartão";
            else if (idPagamento == R.id.radioDinheiro) c.formaPagamento = "Dinheiro";
            else c.formaPagamento = "Pix";

            if (contaExistente == null) {
                banco.contaDAO().inserir(c);
                Toast.makeText(this, c.tipo + " adicionada com sucesso!", Toast.LENGTH_SHORT).show();

                editDescricao.setText("");
                editValor.setText("");
                editDescricao.requestFocus();
            } else {
                banco.contaDAO().atualizar(c);
                Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                dialogo.dismiss();
            }

            recarregarLista();
            atualizarResumo();
        });
        dialogo.show();
    }

    private void recarregarLista() {
        adaptador.atualizarLista(banco.contaDAO().listarTodas());
    }

    private void atualizarResumo() {
        double rec = banco.contaDAO().buscarTotalReceitas();
        double des = banco.contaDAO().buscarTotalDespesas();
        txtReceitas.setText(Moeda.formatar(rec));
        txtDespesas.setText(Moeda.formatar(des));
        txtSaldo.setText(Moeda.formatar(rec - des));
    }
}