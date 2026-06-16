package com.example.gerenciadordepagamentos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gerenciadordepagamentos.database.Conta;
import java.util.List;

public class ContaAdapter extends RecyclerView.Adapter<ContaAdapter.ContaDetentor> {

    private List<Conta> listaDeContas;
    private AoClicar escutador;

    public interface AoClicar {
        void aoClicarLongo(Conta conta);
        void aoClicarCurto(Conta conta);
    }

    public ContaAdapter(List<Conta> lista, AoClicar escutador) {
        this.listaDeContas = lista;
        this.escutador = escutador;
    }

    @NonNull
    @Override
    public ContaDetentor onCreateViewHolder(@NonNull ViewGroup pai, int tipoVisao) {
        View itemVisual = LayoutInflater.from(pai.getContext()).inflate(R.layout.item_conta, pai, false);
        return new ContaDetentor(itemVisual);
    }

    @Override
    public void onBindViewHolder(@NonNull ContaDetentor detentor, int posicao) {
        Conta contaAtual = listaDeContas.get(posicao);

        detentor.txtCategoria.setText(contaAtual.categoria);
        detentor.txtDescricao.setText(contaAtual.descricao);
        detentor.txtData.setText(contaAtual.data);
        detentor.txtValor.setText(String.format("R$ %.2f", contaAtual.valor));

        // Trata contas antigas para não crashar caso a forma de pagamento seja nula
        String formaPag = (contaAtual.formaPagamento != null && !contaAtual.formaPagamento.isEmpty()) ? " via " + contaAtual.formaPagamento : "";

        if ("Receita".equals(contaAtual.tipo)) {
            detentor.txtValor.setTextColor(Color.parseColor("#4CAF50"));
            detentor.txtStatus.setTextColor(Color.parseColor("#4CAF50"));
            detentor.txtStatus.setText("● Recebido" + formaPag);
            detentor.txtIcone.setText("💰");
        } else {
            detentor.txtValor.setTextColor(Color.parseColor("#F44336"));
            detentor.txtStatus.setTextColor(Color.parseColor("#F44336"));
            detentor.txtStatus.setText("● Pago" + formaPag);
            detentor.txtIcone.setText("💸");
        }

        detentor.itemView.setOnClickListener(v -> escutador.aoClicarCurto(contaAtual));

        detentor.itemView.setOnLongClickListener(v -> {
            escutador.aoClicarLongo(contaAtual);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaDeContas != null ? listaDeContas.size() : 0;
    }

    public void atualizarLista(List<Conta> novasContas) {
        this.listaDeContas = novasContas;
        notifyDataSetChanged();
    }

    static class ContaDetentor extends RecyclerView.ViewHolder {
        TextView txtCategoria, txtDescricao, txtData, txtValor, txtStatus, txtIcone;

        public ContaDetentor(@NonNull View itemDaLista) {
            super(itemDaLista);
            txtCategoria = itemDaLista.findViewById(R.id.txtCategoria);
            txtDescricao = itemDaLista.findViewById(R.id.txtDescricao);
            txtData = itemDaLista.findViewById(R.id.txtData);
            txtValor = itemDaLista.findViewById(R.id.txtValor);
            txtStatus = itemDaLista.findViewById(R.id.txtStatus);
            txtIcone = itemDaLista.findViewById(R.id.txtIcone);
        }
    }
}