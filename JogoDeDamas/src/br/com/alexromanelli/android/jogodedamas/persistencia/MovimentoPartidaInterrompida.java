package br.com.alexromanelli.android.jogodedamas.persistencia;

import android.content.ContentValues;

public class MovimentoPartidaInterrompida {

    public static final String NOME_TABELA_MOVIMENTO = "movimento"; 
    public static final String KEY_MOVIMENTO_ID = "_id_movimento";
    public static final String KEY_MOVIMENTO_PARTIDA = "id_partida";
    public static final String KEY_MOVIMENTO_ORDEM = "ordem";
    public static final String KEY_MOVIMENTO_DESCRICAO = "descricao";
    
    public static final String SCRIPT_CRIAR_TABELA_MOVIMENTO =
            "create table " + NOME_TABELA_MOVIMENTO + " ( "
            + " " + KEY_MOVIMENTO_ID + " integer primary key autoincrement,"
            + " " + KEY_MOVIMENTO_PARTIDA + " integer not null,"
            + " " + KEY_MOVIMENTO_ORDEM + " integer not null,"
            + " " + KEY_MOVIMENTO_DESCRICAO + " text not null,"
            + " constraint fk_peca_partida foreign key"
            + "   (" + KEY_MOVIMENTO_PARTIDA + ") "
            + "   references partida(" + PartidaInterrompida.KEY_PARTIDA_ID + ")"
            + "   on delete cascade);";

    private long id;
    private long idPartida;
    private int ordem;
    private String descricao;
    
    public MovimentoPartidaInterrompida(long id, long idPartida,
            int ordem, String descricao) {
        super();
        this.id = id;
        this.idPartida = idPartida;
        this.setOrdem(ordem);
        this.descricao = descricao;
    }

    public MovimentoPartidaInterrompida(long idPartida, int ordem, String descricao) {
        super();
        this.idPartida = idPartida;
        this.setOrdem(ordem);
        this.descricao = descricao;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(long idPartida) {
        this.idPartida = idPartida;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ContentValues getContentValues() {
        ContentValues v = new ContentValues();
        v.put(KEY_MOVIMENTO_PARTIDA, getIdPartida());
        v.put(KEY_MOVIMENTO_ORDEM, getOrdem());
        v.put(KEY_MOVIMENTO_DESCRICAO, getDescricao());
        return v;
    }

}
