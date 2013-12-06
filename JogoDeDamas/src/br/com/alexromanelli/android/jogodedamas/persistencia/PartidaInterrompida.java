package br.com.alexromanelli.android.jogodedamas.persistencia;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentValues;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.OpcaoAdversario;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.OpcaoTempoPartida;

@SuppressWarnings("serial")
public class PartidaInterrompida extends HashMap<String,String> {

    @Override
    public String get(Object key) {
        String chave = (String)key;
        if (chave == KEY_PARTIDA_ID)
            return Long.toString(id);
        if (chave == KEY_PARTIDA_DATA_HORA) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(getDataInterrupcao());
        }
        if (chave == KEY_PARTIDA_APELIDO_BRANCAS)
            return apelidoBrancas;
        if (chave == KEY_PARTIDA_APELIDO_PRETAS)
            return apelidoPretas;
        return null;
    }

    public static final String NOME_TABELA_PARTIDA = "partida"; 
    public static final String KEY_PARTIDA_ID = "_id_partida";
    public static final String KEY_PARTIDA_APELIDO_BRANCAS = "apelido_brancas";
    public static final String KEY_PARTIDA_APELIDO_PRETAS = "apelido_pretas";
    public static final String KEY_PARTIDA_DATA_HORA = "data_interrupcao";
    public static final String KEY_PARTIDA_JOGADOR_ATUAL = "jogador_atual";
    public static final String KEY_PARTIDA_OPCAO_ADVERSARIO = "opcao_adversario";
    public static final String KEY_PARTIDA_OPCAO_TEMPO = "opcao_tempo";
    public static final String KEY_PARTIDA_TEMPO_RESTANTE_BRANCAS = "tempo_restante_brancas";
    public static final String KEY_PARTIDA_TEMPO_RESTANTE_PRETAS = "tempo_restante_pretas";
    
    public static final String SCRIPT_CRIAR_TABELA_PARTIDA = 
            "create table " + NOME_TABELA_PARTIDA + " ( "
            + " " + KEY_PARTIDA_ID + " integer primary key autoincrement,"
            + " " + KEY_PARTIDA_APELIDO_BRANCAS + " char(3) not null,"
            + " " + KEY_PARTIDA_APELIDO_PRETAS + " char(3) not null,"
            + " " + KEY_PARTIDA_DATA_HORA + " datetime not null,"
            + " " + KEY_PARTIDA_JOGADOR_ATUAL + " char(1) not null,"
            + " " + KEY_PARTIDA_OPCAO_ADVERSARIO + " integer not null,"
            + " " + KEY_PARTIDA_OPCAO_TEMPO + " integer not null,"
            + " " + KEY_PARTIDA_TEMPO_RESTANTE_BRANCAS + " integer,"
            + " " + KEY_PARTIDA_TEMPO_RESTANTE_PRETAS + " integer);";
    
    private long id;
    private String apelidoBrancas;
    private String apelidoPretas;
    private Date dataInterrupcao;
    private char jogadorAtual;
    private Partida.OpcaoAdversario opcaoAdversario;
    private Partida.OpcaoTempoPartida opcaoTempo;
    private long tempoRestanteBrancas;
    private long tempoRestantePretas;
    
    public PartidaInterrompida(long id, String apelidoBrancas,
            String apelidoPretas, Date dataInterrupcao) {
        super();
        this.id = id;
        this.apelidoBrancas = apelidoBrancas;
        this.apelidoPretas = apelidoPretas;
        this.dataInterrupcao = dataInterrupcao;
    }

    public PartidaInterrompida(long id, String apelidoBrancas,
            String apelidoPretas, Date dataInterrupcao, char jogadorAtual,
            OpcaoAdversario opcaoAdversario, OpcaoTempoPartida opcaoTempo,
            long tempoRestanteBrancas, long tempoRestantePretas) {
        super();
        this.id = id;
        this.apelidoBrancas = apelidoBrancas;
        this.apelidoPretas = apelidoPretas;
        this.dataInterrupcao = dataInterrupcao;
        this.jogadorAtual = jogadorAtual;
        this.opcaoAdversario = opcaoAdversario;
        this.opcaoTempo = opcaoTempo;
        this.tempoRestanteBrancas = tempoRestanteBrancas;
        this.tempoRestantePretas = tempoRestantePretas;
    }

    public PartidaInterrompida(String apelidoBrancas, String apelidoPretas,
            Date dataInterrupcao, char jogadorAtual,
            OpcaoAdversario opcaoAdversario, OpcaoTempoPartida opcaoTempo,
            long tempoRestanteBrancas, long tempoRestantePretas) {
        super();
        this.apelidoBrancas = apelidoBrancas;
        this.apelidoPretas = apelidoPretas;
        this.dataInterrupcao = dataInterrupcao;
        this.jogadorAtual = jogadorAtual;
        this.opcaoAdversario = opcaoAdversario;
        this.opcaoTempo = opcaoTempo;
        this.tempoRestanteBrancas = tempoRestanteBrancas;
        this.tempoRestantePretas = tempoRestantePretas;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApelidoBrancas() {
        return apelidoBrancas;
    }

    public void setApelidoBrancas(String apelidoBrancas) {
        this.apelidoBrancas = apelidoBrancas;
    }

    public String getApelidoPretas() {
        return apelidoPretas;
    }

    public void setApelidoPretas(String apelidoPretas) {
        this.apelidoPretas = apelidoPretas;
    }

    public Date getDataInterrupcao() {
        return dataInterrupcao;
    }

    public void setDataInterrupcao(Date dataInterrupcao) {
        this.dataInterrupcao = dataInterrupcao;
    }

    public char getJogadorAtual() {
        return jogadorAtual;
    }

    public void setJogadorAtual(char jogadorAtual) {
        this.jogadorAtual = jogadorAtual;
    }

    public Partida.OpcaoAdversario getOpcaoAdversario() {
        return opcaoAdversario;
    }

    public void setOpcaoAdversario(Partida.OpcaoAdversario opcaoAdversario) {
        this.opcaoAdversario = opcaoAdversario;
    }

    public Partida.OpcaoTempoPartida getOpcaoTempo() {
        return opcaoTempo;
    }

    public void setOpcaoTempo(Partida.OpcaoTempoPartida opcaoTempo) {
        this.opcaoTempo = opcaoTempo;
    }

    public long getTempoRestanteBrancas() {
        return tempoRestanteBrancas;
    }

    public void setTempoRestanteBrancas(long tempoRestanteBrancas) {
        this.tempoRestanteBrancas = tempoRestanteBrancas;
    }

    public long getTempoRestantePretas() {
        return tempoRestantePretas;
    }

    public void setTempoRestantePretas(long tempoRestantePretas) {
        this.tempoRestantePretas = tempoRestantePretas;
    }

    public ContentValues getContentValues() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        
        ContentValues v = new ContentValues();
        v.put(KEY_PARTIDA_APELIDO_BRANCAS, getApelidoBrancas());
        v.put(KEY_PARTIDA_APELIDO_PRETAS, getApelidoPretas());
        v.put(KEY_PARTIDA_DATA_HORA, dateFormat.format(getDataInterrupcao()));
        v.put(KEY_PARTIDA_JOGADOR_ATUAL, Character.toString(getJogadorAtual()));
        v.put(KEY_PARTIDA_OPCAO_ADVERSARIO, converteParaInteiro(getOpcaoAdversario()));
        v.put(KEY_PARTIDA_OPCAO_TEMPO, converteParaInteiro(getOpcaoTempo()));
        if (getOpcaoTempo() != OpcaoTempoPartida.Ilimitado) {
            v.put(KEY_PARTIDA_TEMPO_RESTANTE_BRANCAS, getTempoRestanteBrancas());
            v.put(KEY_PARTIDA_TEMPO_RESTANTE_PRETAS, getTempoRestantePretas());
        }
        return v;
    }

    private int converteParaInteiro(OpcaoTempoPartida opcao) {
        switch (opcao) {
        case Relampago5x5:
            return 0;
        case Curto15x15:
            return 1;
        case Medio30x30:
            return 2;
        case Normal45x45:
            return 3;
        case Ilimitado:
            return 4;
        }
        return -1;
    }

    private int converteParaInteiro(OpcaoAdversario opcao) {
        switch (opcao) {
        case HumanoLocal:
            return 0;
        case Computador:
            return 1;
        case HumanoRemoto:
            return 2;
        }
        return -1;
    }

}

