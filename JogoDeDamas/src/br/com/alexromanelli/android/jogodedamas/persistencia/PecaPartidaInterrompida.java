package br.com.alexromanelli.android.jogodedamas.persistencia;

import android.content.ContentValues;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.CorPeca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.EstadoPeca;

public class PecaPartidaInterrompida {

    public static final String NOME_TABELA_PECA = "peca"; 
    public static final String KEY_PECA_ID = "_id_peca";
    public static final String KEY_PECA_PARTIDA = "id_partida";
    public static final String KEY_PECA_COR = "cor"; 
    public static final String KEY_PECA_ESTADO = "estado"; 
    public static final String KEY_PECA_LINHA = "linha"; 
    public static final String KEY_PECA_COLUNA = "coluna";
    
    public static final String SCRIPT_CRIAR_TABELA_PECA =
            "create table " + NOME_TABELA_PECA + " ( "
            + " " + KEY_PECA_ID + " integer primary key autoincrement,"
            + " " + KEY_PECA_PARTIDA + " integer not null,"
            + " " + KEY_PECA_COR + " char(1) not null,"
            + " " + KEY_PECA_ESTADO + " char(1) not null,"
            + " " + KEY_PECA_LINHA + " integer not null default -1,"
            + " " + KEY_PECA_COLUNA + " integer not null default -1,"
            + " constraint fk_peca_partida foreign key"
            + "   (" + KEY_PECA_PARTIDA + ") "
            + "   references partida(" + PartidaInterrompida.KEY_PARTIDA_ID + ")"
            + "   on delete cascade);";

    private long id;
    private long idPartida;
    private Peca.CorPeca cor;
    private Peca.EstadoPeca estado;
    private int linha;
    private int coluna;
    
    public PecaPartidaInterrompida(long id, long idPartida, CorPeca cor,
            EstadoPeca estado, int linha, int coluna) {
        super();
        this.id = id;
        this.idPartida = idPartida;
        this.cor = cor;
        this.estado = estado;
        this.linha = linha;
        this.coluna = coluna;
    }

    public PecaPartidaInterrompida(long idPartida, CorPeca cor,
            EstadoPeca estado, int linha, int coluna) {
        super();
        this.idPartida = idPartida;
        this.cor = cor;
        this.estado = estado;
        this.linha = linha;
        this.coluna = coluna;
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

    public Peca.CorPeca getCor() {
        return cor;
    }

    public void setCor(Peca.CorPeca cor) {
        this.cor = cor;
    }

    public Peca.EstadoPeca getEstado() {
        return estado;
    }

    public void setEstado(Peca.EstadoPeca estado) {
        this.estado = estado;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    public ContentValues getContentValues() {
        ContentValues v = new ContentValues();
        v.put(KEY_PECA_PARTIDA, getIdPartida());
        v.put(KEY_PECA_COR, Character.toString(getCor().toChar()));
        v.put(KEY_PECA_ESTADO, getEstado().toInt());
        if (getEstado() != EstadoPeca.Perdida) {
            v.put(KEY_PECA_LINHA, getLinha());
            v.put(KEY_PECA_COLUNA, getColuna());
        }
        return v;
    }
    
    
}
