package br.com.alexromanelli.android.jogodedamas.view;

import java.util.ArrayList;
import br.com.alexromanelli.android.jogodedamas.R;
import br.com.alexromanelli.android.jogodedamas.persistencia.DatabaseHelper;
import br.com.alexromanelli.android.jogodedamas.persistencia.PartidaInterrompida;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AtividadeRetomarJogo extends ListActivity {
    
    public static final int RESULTADO_CANCELAR = 0;
    public static final int RESULTADO_RETOMAR = 1;

    private ArrayList<PartidaInterrompida> listPartidas;
    private AdapterPartidaInterrompida adapterPartidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retomar_partida);
        
        DatabaseHelper dh = new DatabaseHelper(this);
        listPartidas = dh.leCabecalhosDePartidasInterrompidas();
        dh.close();
        adapterPartidas = new AdapterPartidaInterrompida(this, listPartidas);
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(adapterPartidas);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return listPartidas;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_retomar_partida, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.cancelaRetomarPartida:
            cancelaRetomarPartida();
            return true;
        case R.id.excluiPartidaSalva:
            excluiPartidaSalva();
            return true;
        case R.id.confirmaRetomarPartida:
            retomarPartida();
            return true;
        }
        return false;
    }

    private void cancelaRetomarPartida() {
        this.setResult(RESULTADO_CANCELAR);
        finish();
    }

    private void excluiPartidaSalva() {
        final AlertDialog dialogExcluir = new AlertDialog.Builder(this).create();
        dialogExcluir.setTitle("Confirmação");
        dialogExcluir.setMessage("Deseja realmente excluir as partidas selecionadas?");
        dialogExcluir.setButton(AlertDialog.BUTTON_POSITIVE, "Sim", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                excluiPartidasSelecionadas();
                dialogExcluir.dismiss();
            }
        });
        dialogExcluir.setButton(AlertDialog.BUTTON_NEGATIVE, "Não", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogExcluir.dismiss();
            }
        });
        
        dialogExcluir.show();

    }

    protected void excluiPartidasSelecionadas() {
        ArrayList<Long> selecionados = adapterPartidas.getIdSelecionadas();
        if (selecionados.size() > 0) {
            DatabaseHelper dh = new DatabaseHelper(this);
            for (Long id : selecionados) {
                dh.excluiPartidaInterrompida(id);
            }
        }
        else {
            Toast.makeText(this, "Antes, é preciso selecionar partidas.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("NewApi")
	private void retomarPartida() {
        ArrayList<Long> selecionados = adapterPartidas.getIdSelecionadas();
        if (selecionados.size() == 1) {
            long idPartida = selecionados.get(0).longValue();
            Intent res = new Intent();
            res.putExtra(PartidaInterrompida.KEY_PARTIDA_ID, idPartida);
            this.setResult(RESULTADO_RETOMAR, res);
            this.finish();
        }
        else {
            Toast.makeText(this, "Por favor, selecione uma partida.", Toast.LENGTH_LONG).show();
        }
    }

}

class AdapterPartidaInterrompida extends BaseAdapter {
    
    private ArrayList<PartidaInterrompida> lista;
    private AtividadeRetomarJogo ctx;
    private java.text.DateFormat df;
    private java.text.DateFormat tf;
    private boolean[] selecao;
    
    public AdapterPartidaInterrompida(AtividadeRetomarJogo ctx, ArrayList<PartidaInterrompida> lista) {
        super();
        this.ctx = ctx;
        this.lista = lista;
        this.selecao = new boolean[lista.size()];
        
        this.df = DateFormat.getDateFormat(ctx);
        this.tf = DateFormat.getTimeFormat(ctx);
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lista.get(position).getId();
    }
    
    public ArrayList<Long> getIdSelecionadas() {
        ArrayList<Long> selecionadas = new ArrayList<Long>();

        for (int i = 0; i < selecao.length; i++)
            if (selecao[i])
                selecionadas.add(lista.get(i).getId());
        
        return selecionadas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PartidaInterrompida partida = lista.get(position);
        
        String textoAdversarios = partida.getApelidoBrancas() + " vs " + 
                partida.getApelidoPretas();
        
        String textoData = df.format(partida.getDataInterrupcao()) + " " +
                tf.format(partida.getDataInterrupcao()); 
        
        LinearLayout llPartida = new LinearLayout(ctx);
        ctx.getLayoutInflater().inflate(R.layout.item_partida_salva, llPartida);

        TextView adversarios = (TextView)llPartida.findViewById(R.id.textAdversarios);
        TextView data = (TextView)llPartida.findViewById(R.id.textItemData);
        final CheckBox checkPartida = (CheckBox)llPartida.findViewById(R.id.checkPartida);
        
        final int pos = position;
        checkPartida.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selecao[pos] = isChecked;
            }
        });

        View.OnClickListener listenerSeleciona = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPartida.setChecked(!checkPartida.isChecked());
            }
        };

        llPartida.setOnClickListener(listenerSeleciona);
        adversarios.setOnClickListener(listenerSeleciona);
        data.setOnClickListener(listenerSeleciona);
        
        adversarios.setText(textoAdversarios);
        data.setText(textoData);
        
        return llPartida;
    }

    @Override
    public void notifyDataSetChanged() {
        this.selecao = new boolean[this.lista.size()];
        super.notifyDataSetChanged();
    }
    
}







