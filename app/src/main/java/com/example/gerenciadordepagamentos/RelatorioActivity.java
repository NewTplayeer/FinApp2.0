package com.example.gerenciadordepagamentos;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gerenciadordepagamentos.database.AppDatabase;
import com.example.gerenciadordepagamentos.database.Conta;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RelatorioActivity extends AppCompatActivity {

    private TextView txtDataInicio, txtDataFim, txtRelReceitas, txtRelDespesas, txtRelSaldo, txtQtdRegistros;
    private AppDatabase banco;
    private Calendar calInicio, calFim;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        banco = AppDatabase.getInstancia(this);
        sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calInicio = Calendar.getInstance();
        calFim = Calendar.getInstance();

        txtDataInicio = findViewById(R.id.txtDataInicio);
        txtDataFim = findViewById(R.id.txtDataFim);
        txtRelReceitas = findViewById(R.id.txtRelReceitas);
        txtRelDespesas = findViewById(R.id.txtRelDespesas);
        txtRelSaldo = findViewById(R.id.txtRelSaldo);
        txtQtdRegistros = findViewById(R.id.txtQtdRegistros);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        txtDataInicio.setOnClickListener(v -> mostrarCalendario(txtDataInicio, calInicio));
        txtDataFim.setOnClickListener(v -> mostrarCalendario(txtDataFim, calFim));

        findViewById(R.id.btnLimpar).setOnClickListener(v -> {
            txtDataInicio.setText("");
            txtDataFim.setText("");
            calcularResumo();
        });

        findViewById(R.id.btnMesAtual).setOnClickListener(v -> {
            Calendar hoje = Calendar.getInstance();

            calInicio.set(Calendar.YEAR, hoje.get(Calendar.YEAR));
            calInicio.set(Calendar.MONTH, hoje.get(Calendar.MONTH));
            calInicio.set(Calendar.DAY_OF_MONTH, 1);

            calFim.set(Calendar.YEAR, hoje.get(Calendar.YEAR));
            calFim.set(Calendar.MONTH, hoje.get(Calendar.MONTH));
            calFim.set(Calendar.DAY_OF_MONTH, hoje.getActualMaximum(Calendar.DAY_OF_MONTH));

            txtDataInicio.setText(sdf.format(calInicio.getTime()));
            txtDataFim.setText(sdf.format(calFim.getTime()));
            calcularResumo();
        });

        calcularResumo();
    }

    private void mostrarCalendario(TextView textView, Calendar calendarioSelecionado) {
        new DatePickerDialog(this, (view, ano, mes, dia) -> {
            calendarioSelecionado.set(ano, mes, dia);
            textView.setText(sdf.format(calendarioSelecionado.getTime()));
            calcularResumo();
        }, calendarioSelecionado.get(Calendar.YEAR), calendarioSelecionado.get(Calendar.MONTH), calendarioSelecionado.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void calcularResumo() {
        List<Conta> todas = banco.contaDAO().listarTodas();
        double rec = 0, des = 0;
        int contador = 0;

        String inicioStr = txtDataInicio.getText().toString();
        String fimStr = txtDataFim.getText().toString();

        try {
            Date dataIn = inicioStr.isEmpty() ? null : sdf.parse(inicioStr);
            Date dataF = fimStr.isEmpty() ? null : sdf.parse(fimStr);

            for (Conta c : todas) {
                Date dataConta = sdf.parse(c.data);
                boolean dentro = true;

                if (dataIn != null && dataConta.before(dataIn)) dentro = false;
                if (dataF != null && dataConta.after(dataF)) dentro = false;

                if (dentro) {
                    contador++;
                    if ("Receita".equals(c.tipo)) rec += c.valor;
                    else des += c.valor;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        txtRelReceitas.setText(String.format("Receitas: R$ %.2f", rec));
        txtRelDespesas.setText(String.format("Despesas: R$ %.2f", des));

        double saldo = rec - des;
        txtRelSaldo.setText(String.format("Saldo: R$ %.2f", saldo));
        if (saldo < 0) {
            txtRelSaldo.setTextColor(Color.parseColor("#F44336"));
        } else {
            txtRelSaldo.setTextColor(Color.parseColor("#4CAF50"));
        }

        txtQtdRegistros.setText("Registros (" + contador + ")");
    }
}