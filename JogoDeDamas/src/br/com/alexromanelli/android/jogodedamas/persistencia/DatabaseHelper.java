package br.com.alexromanelli.android.jogodedamas.persistencia;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import br.com.alexromanelli.android.jogodedamas.dinamica.Partida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.OpcaoTempoPartida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.OpcaoAdversario;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.CorPeca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.EstadoPeca;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "damas";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL(PartidaInterrompida.SCRIPT_CRIAR_TABELA_PARTIDA);
        db.execSQL(PecaPartidaInterrompida.SCRIPT_CRIAR_TABELA_PECA);
        db.execSQL(MovimentoPartidaInterrompida.SCRIPT_CRIAR_TABELA_MOVIMENTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long registraPartidaInterrompida(Date dataInterrupcao,
            String apelidoJogadorPecasBrancas,
            String apelidoJogadorPecasPretas,
            Peca.CorPeca corPecasJogadorAtual,
            Partida.OpcaoAdversario opcaoAdversario,
            Partida.OpcaoTempoPartida opcaoTempo,
            long tempoRestanteBrancas,
            long tempoRestantePretas,
            ArrayList<Peca> pecas,
            ArrayList<String> historicoMovimentos) {

        SQLiteDatabase db = this.getWritableDatabase();
        
        // cria registro de partida
        long idPartida = db.insert(PartidaInterrompida.NOME_TABELA_PARTIDA, null, 
                new PartidaInterrompida(
                        apelidoJogadorPecasBrancas, 
                        apelidoJogadorPecasPretas, 
                        dataInterrupcao, 
                        corPecasJogadorAtual.toChar(), 
                        opcaoAdversario, 
                        opcaoTempo, 
                        tempoRestanteBrancas, 
                        tempoRestantePretas).getContentValues());
        
        // cria registros de peças associadas à partida
        for (Peca p : pecas)
            db.insert(PecaPartidaInterrompida.NOME_TABELA_PECA, null, 
                    new PecaPartidaInterrompida(
                            idPartida, 
                            p.getCor(), 
                            p.getEstado(), 
                            p.getEstado() != EstadoPeca.Perdida ? p.getLocalizacao().getLinha() : -1, 
                            p.getEstado() != EstadoPeca.Perdida ? p.getLocalizacao().getColuna() : -1)
            .getContentValues());
        
        // cria registros de movimentos associados à partida
        int ordem = historicoMovimentos.size();
        for (String mov : historicoMovimentos)
            db.insert(MovimentoPartidaInterrompida.NOME_TABELA_MOVIMENTO, null, 
                    new MovimentoPartidaInterrompida(idPartida, ordem--, mov).getContentValues());
        
        return idPartida;
    }
    
    public ArrayList<PartidaInterrompida> leCabecalhosDePartidasInterrompidas() {
        ArrayList<PartidaInterrompida> salvas = new ArrayList<PartidaInterrompida>();
        
        String[] colunas = new String[] {
                PartidaInterrompida.KEY_PARTIDA_ID,
                PartidaInterrompida.KEY_PARTIDA_DATA_HORA,
                PartidaInterrompida.KEY_PARTIDA_APELIDO_BRANCAS,
                PartidaInterrompida.KEY_PARTIDA_APELIDO_PRETAS
        };
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                PartidaInterrompida.NOME_TABELA_PARTIDA, 
                colunas, 
                null, null, null, null, 
                PartidaInterrompida.KEY_PARTIDA_DATA_HORA + " desc");
        
        if (cursor.getCount() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            cursor.moveToFirst();
            do {
                long partidaId = cursor.getLong(cursor.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_ID));

                String datahora = cursor.getString(cursor.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_DATA_HORA));
                Date partidaDataHora;
                try {
                    partidaDataHora = dateFormat.parse(datahora);
                } catch (ParseException e) {
                    partidaDataHora = new GregorianCalendar(1500,4,22).getTime();
                }
                String partidaApelidoBrancas = cursor.getString(cursor.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_APELIDO_BRANCAS));
                String partidaApelidoPretas = cursor.getString(cursor.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_APELIDO_PRETAS));
                
                PartidaInterrompida partida = new PartidaInterrompida(partidaId, partidaApelidoBrancas, partidaApelidoPretas, partidaDataHora);
                salvas.add(partida);
            } while (cursor.moveToNext());
        }
        
        return salvas;
    }
    
    public SnapshotPartida leRegistroDePartidaInterrompida(long id) {
        PartidaInterrompida registroPartida = null;
        ArrayList<PecaPartidaInterrompida> listaDePecas;
        ArrayList<String> historicoDeMovimentos;
        
        // obtém o registro de partida com id indicado
        String[] colunasPartida = new String[] {
                PartidaInterrompida.KEY_PARTIDA_ID,
                PartidaInterrompida.KEY_PARTIDA_DATA_HORA,
                PartidaInterrompida.KEY_PARTIDA_APELIDO_BRANCAS,
                PartidaInterrompida.KEY_PARTIDA_APELIDO_PRETAS,
                PartidaInterrompida.KEY_PARTIDA_JOGADOR_ATUAL,
                PartidaInterrompida.KEY_PARTIDA_OPCAO_ADVERSARIO,
                PartidaInterrompida.KEY_PARTIDA_OPCAO_TEMPO,
                PartidaInterrompida.KEY_PARTIDA_TEMPO_RESTANTE_BRANCAS,
                PartidaInterrompida.KEY_PARTIDA_TEMPO_RESTANTE_PRETAS
        };
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cPartida = db.query(
                PartidaInterrompida.NOME_TABELA_PARTIDA, 
                colunasPartida, 
                PartidaInterrompida.KEY_PARTIDA_ID + " = ?", 
                new String[] { Long.toString(id) }, 
                null, null, null);
        
        if (cPartida.getCount() > 0) {
            cPartida.moveToFirst();

            long idPartida = cPartida.getLong(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_ID));
            Date dataInterrupcao = new Date(cPartida.getLong(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_DATA_HORA)));
            String apelidoBrancas = cPartida.getString(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_APELIDO_BRANCAS));
            String apelidoPretas = cPartida.getString(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_APELIDO_PRETAS));
            char jogadorAtual = cPartida.getString(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_JOGADOR_ATUAL)).charAt(0);
            int opAdv = cPartida.getInt(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_OPCAO_ADVERSARIO));
            OpcaoAdversario opcaoAdversario = opAdv == 0 ? OpcaoAdversario.HumanoLocal :
                opAdv == 1 ? OpcaoAdversario.Computador : OpcaoAdversario.HumanoRemoto;
            int opTempo = cPartida.getInt(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_OPCAO_TEMPO));
            OpcaoTempoPartida opcaoTempo = 
                opTempo == 0 ? OpcaoTempoPartida.Relampago5x5 :
                opTempo == 1 ? OpcaoTempoPartida.Curto15x15 :
                opTempo == 2 ? OpcaoTempoPartida.Medio30x30 :
                opTempo == 3 ? OpcaoTempoPartida.Normal45x45 :
                OpcaoTempoPartida.Ilimitado;
            long tempoRestanteBrancas = cPartida.getLong(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_TEMPO_RESTANTE_BRANCAS));
            long tempoRestantePretas = cPartida.getLong(cPartida.getColumnIndexOrThrow(PartidaInterrompida.KEY_PARTIDA_TEMPO_RESTANTE_PRETAS));
                
            registroPartida = new PartidaInterrompida(idPartida, 
                    apelidoBrancas, 
                    apelidoPretas, 
                    dataInterrupcao, 
                    jogadorAtual, 
                    opcaoAdversario, 
                    opcaoTempo, 
                    tempoRestanteBrancas, 
                    tempoRestantePretas);
        }
        
        if (registroPartida != null) {
            
            // obtém registros de peças relacionadas à partida selecionada
            listaDePecas = new ArrayList<PecaPartidaInterrompida>();
            
            String[] colunasPecas = new String[] {
                    PecaPartidaInterrompida.KEY_PECA_ID,
                    PecaPartidaInterrompida.KEY_PECA_COR,
                    PecaPartidaInterrompida.KEY_PECA_ESTADO,
                    PecaPartidaInterrompida.KEY_PECA_LINHA,
                    PecaPartidaInterrompida.KEY_PECA_COLUNA
            };
            
            Cursor cPeca = db.query(
                    PecaPartidaInterrompida.NOME_TABELA_PECA, 
                    colunasPecas, 
                    PecaPartidaInterrompida.KEY_PECA_PARTIDA + " = ?", 
                    new String[] { Long.toString(id) }, 
                    null, null, null);
            
            if (cPeca.getCount() > 0) {
                cPeca.moveToFirst();
                do {
                    long idPeca = cPeca.getLong(cPeca.getColumnIndexOrThrow(PecaPartidaInterrompida.KEY_PECA_ID));
                    char corChar = cPeca.getString(cPeca.getColumnIndexOrThrow(PecaPartidaInterrompida.KEY_PECA_COR)).charAt(0);
                    CorPeca cor = corChar == 'b' ? CorPeca.Branca : CorPeca.Preta;
                    int estadoInt = cPeca.getInt(cPeca.getColumnIndexOrThrow(PecaPartidaInterrompida.KEY_PECA_ESTADO));
                    EstadoPeca estado = estadoInt == 0 ? EstadoPeca.Pedra : estadoInt == 1 ? EstadoPeca.Dama : EstadoPeca.Perdida;
                    int linha = cPeca.getInt(cPeca.getColumnIndexOrThrow(PecaPartidaInterrompida.KEY_PECA_LINHA));
                    int coluna = cPeca.getInt(cPeca.getColumnIndexOrThrow(PecaPartidaInterrompida.KEY_PECA_COLUNA));
                    
                    PecaPartidaInterrompida peca = new PecaPartidaInterrompida(idPeca, id, cor, estado, linha, coluna);
                    listaDePecas.add(peca);
                } while (cPeca.moveToNext());
            }
            
            // obtém registros de movimentos relacionados à partida selecionada
            historicoDeMovimentos = new ArrayList<String>();
            
            String[] colunasMovimentos = new String[] {
                    MovimentoPartidaInterrompida.KEY_MOVIMENTO_ID,
                    MovimentoPartidaInterrompida.KEY_MOVIMENTO_ORDEM,
                    MovimentoPartidaInterrompida.KEY_MOVIMENTO_DESCRICAO
            };
            
            Cursor cMovimento = db.query(
                    MovimentoPartidaInterrompida.NOME_TABELA_MOVIMENTO, 
                    colunasMovimentos, 
                    MovimentoPartidaInterrompida.KEY_MOVIMENTO_PARTIDA + " = ?", 
                    new String[] { Long.toString(id) }, 
                    null, null, 
                    MovimentoPartidaInterrompida.KEY_MOVIMENTO_ORDEM + " desc");
            
            if (cMovimento.getCount() > 0) {
                cMovimento.moveToFirst();
                do {
                    String descricao = cMovimento.getString(cMovimento.getColumnIndexOrThrow(MovimentoPartidaInterrompida.KEY_MOVIMENTO_DESCRICAO));
                    historicoDeMovimentos.add(descricao);
                } while (cMovimento.moveToNext());
            }
            
            return new SnapshotPartida(registroPartida, listaDePecas, historicoDeMovimentos);
        }
        
        return null;
    }
    
    public boolean excluiPartidaInterrompida(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int n = db.delete(PartidaInterrompida.NOME_TABELA_PARTIDA, 
                PartidaInterrompida.KEY_PARTIDA_ID + " = ?", 
                new String[] { Long.toString(id) });
        return n > 0;
    }
}
