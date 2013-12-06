package br.com.alexromanelli.android.jogodedamas.persistencia;

import java.util.ArrayList;

public class SnapshotPartida {
    private PartidaInterrompida registroPartida;
    private ArrayList<PecaPartidaInterrompida> listaDePecas;
    private ArrayList<String> historicoDeMovimentos;
    
    public SnapshotPartida(PartidaInterrompida registroPartida,
            ArrayList<PecaPartidaInterrompida> listaDePecas,
            ArrayList<String> historicoDeMovimentos) {
        super();
        this.registroPartida = registroPartida;
        this.listaDePecas = listaDePecas;
        this.historicoDeMovimentos = historicoDeMovimentos;
    }

    public PartidaInterrompida getRegistroPartida() {
        return registroPartida;
    }

    public void setRegistroPartida(PartidaInterrompida registroPartida) {
        this.registroPartida = registroPartida;
    }

    public ArrayList<PecaPartidaInterrompida> getListaDePecas() {
        return listaDePecas;
    }

    public void setListaDePecas(ArrayList<PecaPartidaInterrompida> listaDePecas) {
        this.listaDePecas = listaDePecas;
    }

    public ArrayList<String> getHistoricoDeMovimentos() {
        return historicoDeMovimentos;
    }

    public void setHistoricoDeMovimentos(ArrayList<String> historicoDeMovimentos) {
        this.historicoDeMovimentos = historicoDeMovimentos;
    }
    
    
}
