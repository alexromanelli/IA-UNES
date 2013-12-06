package br.com.alexromanelli.android.jogodedamas;

import java.text.NumberFormat;
import java.util.*;

import br.com.alexromanelli.android.jogodedamas.dinamica.ConjuntoJogador;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Relogio;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.OpcaoAdversario;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.OpcaoTempoPartida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.ResultadoPartida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.CorPeca;
import br.com.alexromanelli.android.jogodedamas.dinamica.ia.JogadorIA;
import br.com.alexromanelli.android.jogodedamas.persistencia.DatabaseHelper;
import br.com.alexromanelli.android.jogodedamas.persistencia.PartidaInterrompida;
import br.com.alexromanelli.android.jogodedamas.persistencia.SnapshotPartida;
import br.com.alexromanelli.android.jogodedamas.view.AtividadeRetomarJogo;
import br.com.alexromanelli.android.jogodedamas.view.TabuleiroDamaView;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private static final int REQUEST_RETOMAR_PARTIDA = 1;
	
    private TabuleiroDamaView tabuleiro;
	private TextView textJogadorAtual;
	private TextView textTempoBrancas;
	private TextView textTempoPretas;
    private ListView listHistoricoMovimentos;
    private LinearLayout layoutAtividade;
    private LinearLayout layoutHistorico;
    private TextView textHistorico;
    private Button buttonExibirHistorico;
	
    private static MainActivity INSTANCE;
    private boolean encerrandoAtividade;

    public static MainActivity getInstance() {
	    return INSTANCE;
	}
	
	private ArrayList<String> historicoMovimentos;
	private ArrayAdapter<String> aaHistoricoMovimentos;

    private JogadorIA jogadorIA;

	@Override
    protected void onRestart() {
        super.onRestart();
        INSTANCE = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        INSTANCE = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        INSTANCE = this;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		INSTANCE = this;
		setContentView(R.layout.activity_main);

        encerrandoAtividade = false;
		
		// obtém referências para os elementos da UI da atividade
		textJogadorAtual = (TextView)findViewById(R.id.textJogadorAtual);
		textTempoBrancas = (TextView)findViewById(R.id.textRelogioBranco);
		textTempoPretas = (TextView)findViewById(R.id.textRelogioPreto);
		listHistoricoMovimentos = (ListView)findViewById(R.id.listviewHistorico);
        layoutAtividade = (LinearLayout)findViewById(R.id.layoutAtividade);
        layoutHistorico = (LinearLayout)findViewById(R.id.layoutHistorico);
        textHistorico = (TextView)findViewById(R.id.textHistorico);
        buttonExibirHistorico = (Button)findViewById(R.id.buttonExibirHistorico);

        // configura lista de histórico de movimentos
		historicoMovimentos = new ArrayList<String>();
		aaHistoricoMovimentos = new ArrayAdapter<String>(this, R.layout.item_historico, historicoMovimentos);
		listHistoricoMovimentos.setAdapter(aaHistoricoMovimentos);
		
		// configura listener de clique no botão para exibir histórico
		buttonExibirHistorico.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                executaExibicaoDeHistorico();
            }
        });

        // inclui vista de tabuleiro na atividade
		tabuleiro = new TabuleiroDamaView(this);
		ViewGroup vgLayout = (ViewGroup)findViewById(R.id.layoutTabuleiro);
		vgLayout.addView(tabuleiro);
		
		ajustaOrientacao();
		
		// configura atualizador de cronômetro na tela
		new Timer().schedule(timerTaskRelogio, 0, 100);
		
		// tenta recuperar dados de configuração salvos. // TODO atualizar para setRetainInstance.
		ArrayList<String> listaRecuperada = (ArrayList<String>)getLastNonConfigurationInstance();
		if (listaRecuperada != null) {
		    historicoMovimentos.addAll(listaRecuperada);
		    aaHistoricoMovimentos.notifyDataSetChanged();
		}
	}
	
    @SuppressLint("NewApi")
	private void ajustaOrientacao() {
        Point tam = new Point();
        getWindowManager().getDefaultDisplay().getSize(tam);
        if (tam.x > tam.y) {
            layoutAtividade.setOrientation(LinearLayout.HORIZONTAL);
            layoutHistorico.getLayoutParams().height = LayoutParams.MATCH_PARENT;
            buttonExibirHistorico.setVisibility(View.GONE);
            textHistorico.setVisibility(View.VISIBLE);
            listHistoricoMovimentos.setVisibility(View.VISIBLE);
        }
        else {
            layoutAtividade.setOrientation(LinearLayout.VERTICAL);
            layoutHistorico.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            buttonExibirHistorico.setVisibility(View.VISIBLE);
            textHistorico.setVisibility(View.GONE);
            listHistoricoMovimentos.setVisibility(View.GONE);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return historicoMovimentos;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.iniciarPartida:
            executaIniciarPartida();
            return true;
            
        case R.id.salvarPartida:
            executaInterromperPartida();
            return true;
            
        case R.id.reiniciarPartida:
            executaReiniciarPartida();
            return true;
            
        case R.id.retomarPartida:
            executaRetomarPartida();
            return true;
            
        case R.id.relatarErro:
            executaRelatarErro();
            return true;
            
        case R.id.sobre:
            executaExibirSobre();
            return true;
            
        case R.id.sair:
            executaSair();
            return true;
        }
        return false;
    }
    
    private void executaIniciarPartida() {
        final AlertDialog dialogNovaPartida = new AlertDialog.Builder(MainActivity.this).create();
        dialogNovaPartida.setTitle("Nova partida");
        dialogNovaPartida.setView(getLayoutInflater().inflate(R.layout.opcoes_partida, null));
        dialogNovaPartida.setButton(AlertDialog.BUTTON_POSITIVE, "Iniciar", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                iniciaPartidaComOpcoes(dialogNovaPartida);
            }
        });
        dialogNovaPartida.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogNovaPartida.dismiss();
            }
        });

        dialogNovaPartida.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Spinner spinnerAdversario = (Spinner)dialogNovaPartida.findViewById(R.id.spinnerOpcaoAdversario);
                final TableRow rowOpcaoDificuldade = (TableRow)dialogNovaPartida.findViewById(R.id.rowOpcaoDificuldade);
                spinnerAdversario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (((CharSequence)adapterView.getItemAtPosition(i)).toString().equals("IA")) {
                            rowOpcaoDificuldade.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        });

        dialogNovaPartida.show();
    }

    protected void iniciaPartidaComOpcoes(AlertDialog dialogNovaPartida) {
        // obtém elementos da UI
        Spinner spinnerAdversario = (Spinner)dialogNovaPartida.findViewById(R.id.spinnerOpcaoAdversario);
        TableRow rowOpcaoDificuldade = (TableRow)dialogNovaPartida.findViewById(R.id.rowOpcaoDificuldade);
        Spinner spinnerDificuldade = (Spinner)dialogNovaPartida.findViewById(R.id.spinnerOpcaoDificuldade);
        Spinner spinnerTempo = (Spinner)dialogNovaPartida.findViewById(R.id.spinnerOpcaoTempo);
        EditText editDesfazer = (EditText)dialogNovaPartida.findViewById(R.id.editNumDesfazer);
        
        // obtém opções
        int intOpcaoAdversario = spinnerAdversario.getSelectedItemPosition();
        int intOpcaoDificuldade = spinnerDificuldade.getSelectedItemPosition();
        int intOpcaoTempo = spinnerTempo.getSelectedItemPosition();
        int numDesfazer = Integer.parseInt(editDesfazer.getText().toString());
        
        // usa tipos corretos
        OpcaoAdversario opcaoAdversario = intOpcaoAdversario == 0 ? OpcaoAdversario.HumanoLocal :
            intOpcaoAdversario == 1 ? OpcaoAdversario.Computador : 
            OpcaoAdversario.HumanoRemoto;
        OpcaoTempoPartida opcaoTempo = intOpcaoTempo == 0 ? OpcaoTempoPartida.Relampago5x5 :
            intOpcaoTempo == 1 ? OpcaoTempoPartida.Curto15x15 :
            intOpcaoTempo == 2 ? OpcaoTempoPartida.Medio30x30 :
            intOpcaoTempo == 3 ? OpcaoTempoPartida.Normal45x45 :
            OpcaoTempoPartida.Ilimitado;

        JogadorIA.NivelJogadorIA opcaoNivelIA;
        
        // TODO corrigir, quando estiver completo
        // ------------------ por enquanto, só aceita adversário humano local
        // --------------------> CORREÇÃO 1: (02/12/2013) aceita computador local
        if (opcaoAdversario != OpcaoAdversario.HumanoLocal && opcaoAdversario != OpcaoAdversario.Computador) {
            opcaoAdversario = OpcaoAdversario.HumanoLocal;
            Toast.makeText(MainActivity.this, "Por enquanto, só é possível jogar contra IA ou com outro humano local.", Toast.LENGTH_LONG).show();
        }
        // ------------------ por enquanto, não implementa operação desfazer
        if (numDesfazer > 0) {
            numDesfazer = 0;
            Toast.makeText(MainActivity.this, "Por enquanto, não é possível usar a operação desfazer.", Toast.LENGTH_LONG).show();
        }
        // ------------------------------------------------------------------
        
        // configura partida
        Partida.OPCAO_ADVERSARIO = opcaoAdversario;
        Partida.OPCAO_TEMPO_JOGO = opcaoTempo;
        
        // inicia partida
        limparHistoricoMovimentos();
        iniciarPartida();
        tabuleiro.invalidate();

        // configura adversário computador
        if (opcaoAdversario == OpcaoAdversario.Computador) {
            // obtém opção de dificuldade
            opcaoNivelIA = intOpcaoDificuldade == 0 ? JogadorIA.NivelJogadorIA.Facil :
                    intOpcaoDificuldade == 1 ? JogadorIA.NivelJogadorIA.Medio : JogadorIA.NivelJogadorIA.Dificil;
            jogadorIA = new JogadorIA(opcaoNivelIA, Partida.getInstancia().getPecasPretas(), this, new Handler());
        }

        // exibe mensagem para anunciar o inicio da partida
        Toast.makeText(MainActivity.this, "Partida iniciada!", Toast.LENGTH_SHORT).show();
    }
    
    private void iniciarPartida() {
        configuraAssociacaoPartidaAtividade();
        Partida.getInstancia().iniciarPartida();
    }

    private void configuraAssociacaoPartidaAtividade() {
        Partida.getInstancia().setAnuncioFimDePartida(new RunAnuncioFimDePartida());
        Partida.getInstancia().setHandlerAtividadePrincipal(new Handler());
        Partida.getInstancia().setRegistradorDeHistorico(new RunRegistradorDeHistorico());
        Partida.getInstancia().setIndicadorRedesenharTabuleiro(new RunRedesenharTabuleiro());
    }

    private void executaInterromperPartida() {
        final AlertDialog dialogInterromperPartida = new AlertDialog.Builder(MainActivity.this).create();
        dialogInterromperPartida.setTitle("Salvar partida");
        dialogInterromperPartida.setView(getLayoutInflater().inflate(R.layout.salvar_partida, null));
        dialogInterromperPartida.setButton(AlertDialog.BUTTON_POSITIVE, "Salvar", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editIniciaisBrancas = (EditText)dialogInterromperPartida.findViewById(R.id.editIniciaisBrancas);
                EditText editIniciaisPretas = (EditText)dialogInterromperPartida.findViewById(R.id.editIniciaisPretas);
                String apelidoBrancas = editIniciaisBrancas.getText().toString();
                String apelidoPretas = editIniciaisPretas.getText().toString();
                salvarPartidaComOpcoes(apelidoBrancas, apelidoPretas);
                
                Toast.makeText(MainActivity.this, "Partida interrompida está salva.", Toast.LENGTH_LONG).show();
                Partida.getInstancia().encerrarPartida();
                historicoMovimentos.clear();
                aaHistoricoMovimentos.notifyDataSetChanged();
            }
        });
        dialogInterromperPartida.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogInterromperPartida.dismiss();
            }
        });
        
        dialogInterromperPartida.show();
    }

    protected void salvarPartidaComOpcoes(String apelidoBrancas,
            String apelidoPretas) {
        Date dataHoraInterrupcao = new Date(System.currentTimeMillis());
        ArrayList<Peca> pecas = new ArrayList<Peca>();
        Collections.addAll(pecas, Partida.getInstancia().getPecasBrancas().getConjuntoPecas());
        Collections.addAll(pecas, Partida.getInstancia().getPecasPretas().getConjuntoPecas());
        
        DatabaseHelper dh = new DatabaseHelper(this);
        dh.registraPartidaInterrompida(dataHoraInterrupcao, 
                apelidoBrancas, apelidoPretas, 
                Partida.getInstancia().getJogadorAtual().getCorPecasJogador(), 
                Partida.OPCAO_ADVERSARIO, 
                Partida.OPCAO_TEMPO_JOGO, 
                Relogio.getInstancia().getTempoRestanteBranco(), 
                Relogio.getInstancia().getTempoRestantePreto(), 
                pecas, 
                historicoMovimentos);
    }

    private void executaReiniciarPartida() {
        final AlertDialog dialogReiniciar = new AlertDialog.Builder(MainActivity.this).create();
        dialogReiniciar.setTitle("Confirmação");
        dialogReiniciar.setMessage("Reiniciar a partida?");
        dialogReiniciar.setButton(AlertDialog.BUTTON_POSITIVE, "Sim", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                limparHistoricoMovimentos();
                configuraAssociacaoPartidaAtividade();
                Partida.getInstancia().reiniciarPartida();
                tabuleiro.invalidate();
                dialogReiniciar.dismiss();
                Toast.makeText(MainActivity.this, "Partida reiniciada!", Toast.LENGTH_LONG).show();
            }
        });
        dialogReiniciar.setButton(AlertDialog.BUTTON_NEGATIVE, "Não", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogReiniciar.dismiss();
            }
        });
        
        dialogReiniciar.show();
    }
    
    @SuppressLint("NewApi")
	private void executaRetomarPartida() {
        Intent i = new Intent(MainActivity.this, AtividadeRetomarJogo.class);
        startActivityForResult(i, REQUEST_RETOMAR_PARTIDA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_RETOMAR_PARTIDA:
            switch (resultCode) {
            case AtividadeRetomarJogo.RESULTADO_CANCELAR:
                break;
            case AtividadeRetomarJogo.RESULTADO_RETOMAR:
                // em caso de retomada de partida, o id da partida salva deve estar no intent de resposta
                boolean oops = false;
                if (data != null && data.getExtras() != null) {
                    long idPartida = data.getLongExtra(PartidaInterrompida.KEY_PARTIDA_ID, -1);
                    if (idPartida != -1)
                        retomarPartidaInterrompida(idPartida);
                    else
                        oops = true;
                }
                else
                    oops = true;
                
                if (oops)
                    Toast.makeText(this, "Oops... Esqueci qual era a partida a ser retomada!", Toast.LENGTH_LONG).show();
                
                break;
            }
        }
    }

    private void retomarPartidaInterrompida(long idPartida) {
        // se uma partida estiver em curso, apresenta o diálogo de interrupção de partida
        if (Partida.getInstancia().isPartidaEmCurso())
            executaInterromperPartida();
        
        // obtém partida salva no banco de dados
        DatabaseHelper dh = new DatabaseHelper(this);
        SnapshotPartida sp = dh.leRegistroDePartidaInterrompida(idPartida);
        
        // executa a retomada da partida interrompida
        configuraAssociacaoPartidaAtividade();
        Partida.retomarPartidaInterrompida(sp);
        historicoMovimentos.clear();
        historicoMovimentos.addAll(sp.getHistoricoDeMovimentos());
        aaHistoricoMovimentos.notifyDataSetChanged();

        // recria jogadorIA, se for necessário
        if (Partida.OPCAO_ADVERSARIO == OpcaoAdversario.Computador)
            // TODO corrigir a configuração do nível do jogador IA. ~> implica em mudança no banco
            jogadorIA = new JogadorIA(JogadorIA.NivelJogadorIA.Facil,
                    Partida.getInstancia().getPecasPretas(), this, new Handler());
    }

    private void executaRelatarErro() {
        // TODO implementar o método para fazer o relato de erro.
        Toast.makeText(this, "Desculpe-me. Ainda não sei fazer isto.", Toast.LENGTH_LONG).show();
    }

    private void executaExibirSobre() {
        // TODO implementar a exibição de tela de informação sobre o aplicativo
        
    }

    private void executaSair() {
        final AlertDialog dialogSair = new AlertDialog.Builder(MainActivity.this).create();
        dialogSair.setTitle("Confirmação");
        dialogSair.setMessage("Sair do aplicativo?");
        dialogSair.setButton(AlertDialog.BUTTON_POSITIVE, "Sim", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogSair.dismiss();
                limparHistoricoMovimentos();
                encerrandoAtividade = true;
                Partida.getInstancia().encerrarPartida();
                finish();
            }
        });
        dialogSair.setButton(AlertDialog.BUTTON_NEGATIVE, "Não", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogSair.dismiss();
            }
        });
        
        dialogSair.show();
    }
    
    @SuppressLint("InlinedApi")
	protected void executaExibicaoDeHistorico() {
        final AlertDialog dialogHistorico = new AlertDialog.Builder(MainActivity.this).create();
        dialogHistorico.setTitle("Histórico de movimentos");
        
        // cria exibição de histórico
        ListView listHistoricoDialog = new ListView(this);
        //listHistoricoDialog.getLayoutParams().height = LayoutParams.MATCH_PARENT;
        //listHistoricoDialog.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        listHistoricoDialog.setAdapter(aaHistoricoMovimentos);
        
        dialogHistorico.setView(listHistoricoDialog);
        dialogHistorico.setButton(AlertDialog.BUTTON_NEUTRAL, "Fechar", 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogHistorico.dismiss();
            }
        });
        
        dialogHistorico.show();
    }

    protected void limparHistoricoMovimentos() {
        historicoMovimentos.clear();
        aaHistoricoMovimentos.notifyDataSetChanged();
    }

    public void setJogadorAtual(CorPeca corJogadorAtual) {
        switch (corJogadorAtual) {
        case Branca:
            textJogadorAtual.setText("Branco");
            break;
        case Preta:
            textJogadorAtual.setText("Preto");
            if (Partida.getInstancia().getJogadorAtual().getTipoJogador() == ConjuntoJogador.TipoJogador.IA)
                if (jogadorIA != null)
                    jogadorIA.executaJogada();
            break;
        }
    }

    public TabuleiroDamaView getTabuleiroDamaView() {
        return this.tabuleiro;
    }

    public void atualizaProgresso(int progresso) {
        // TODO implementar exibição de progresso do processamento de IA
    }

    public boolean isEncerrandoAtividade() {
        return encerrandoAtividade;
    }

    public class RunAnuncioFimDePartida implements Runnable {
	    private ResultadoPartida resultado;
	    private ConjuntoJogador vencedor;
	    
	    public void setResultado(ResultadoPartida resultado) {
	        this.resultado = resultado;
	    }
	    
	    public void setVencedor(ConjuntoJogador vencedor) {
	        this.vencedor = vencedor;
	    }
	    
        @Override
        public void run() {
            StringBuilder mensagemAnuncio = new StringBuilder();
            switch (resultado) {
            case VitoriaIndividual:
                mensagemAnuncio.append("Vitória do jogador das peças ");
                if (vencedor.getCorPecasJogador() == CorPeca.Branca)
                    mensagemAnuncio.append("brancas.");
                else
                    mensagemAnuncio.append("pretas.");
                break;
            case Empate:
                mensagemAnuncio.append("Empate.");
                break;
            }
            
            final AlertDialog dialogAnuncio = new AlertDialog.Builder(MainActivity.this).create();
            dialogAnuncio.setTitle("Fim de jogo!");
            dialogAnuncio.setMessage(mensagemAnuncio);
            dialogAnuncio.setButton(AlertDialog.BUTTON_POSITIVE, "OK", 
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogAnuncio.dismiss();
                }
            });
            
            dialogAnuncio.show();
        }
    }
    
    public class RunRegistradorDeHistorico implements Runnable {

        private CorPeca cor;
        private int origemLinha;
        private int origemColuna;
        private int destinoLinha;
        private int destinoColuna;
        private boolean houveDominacao;
        private NumberFormat nf;
        
        public void setMovimento(CorPeca cor, int origemLinha, int origemColuna, int destinoLinha, int destinoColuna, boolean houveDominacao) {
            this.cor = cor;
            this.origemLinha = origemLinha;
            this.origemColuna = origemColuna;
            this.destinoLinha = destinoLinha;
            this.destinoColuna = destinoColuna;
            this.houveDominacao = houveDominacao;
            nf = NumberFormat.getIntegerInstance();
            nf.setMinimumIntegerDigits(3);
        }

        @Override
        public void run() {
            StringBuilder movimento = new StringBuilder();

            movimento.append(nf.format(historicoMovimentos.size() + 1));
            movimento.append(cor == CorPeca.Branca ?
                    " [b]  " :
                    " [p]  ");
            movimento.append((char)(((int)'a') + origemColuna));
            movimento.append(origemLinha + 1);
            movimento.append(houveDominacao ? ":" : "-");
            movimento.append((char)(((int)'a') + destinoColuna));
            movimento.append(destinoLinha + 1);

            historicoMovimentos.add(0, movimento.toString());
            aaHistoricoMovimentos.notifyDataSetChanged();
        }
        
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Relogio.getInstancia().encerraContagem();
    }

    private Handler handlerCronometro = new Handler();
    private TimerTask timerTaskRelogio = new TimerTask() {
        @Override
        public void run() {
            long tempoBrancas = Relogio.getInstancia().getTempoRestanteBranco();
            long tempoPretas = Relogio.getInstancia().getTempoRestantePreto();

            final StringBuilder sTempoBrancas = new StringBuilder(DateFormat.format("mm:ss", tempoBrancas));
            final StringBuilder sTempoPretas = new StringBuilder(DateFormat.format("mm:ss", tempoPretas));

            if (Partida.OPCAO_TEMPO_JOGO == OpcaoTempoPartida.Ilimitado) {
                sTempoBrancas.setCharAt(0, '-'); sTempoPretas.setCharAt(0, '-');
                sTempoBrancas.setCharAt(1, '-'); sTempoPretas.setCharAt(1, '-');
                sTempoBrancas.setCharAt(3, '-'); sTempoPretas.setCharAt(3, '-');
                sTempoBrancas.setCharAt(4, '-'); sTempoPretas.setCharAt(4, '-');
            }
            
            handlerCronometro.post(new Runnable() {
                @Override
                public void run() {
                    textTempoBrancas.setText(sTempoBrancas);
                    textTempoPretas.setText(sTempoPretas);
                }
            });
        }
    };

    public class RunRedesenharTabuleiro implements Runnable {

        @Override
        public void run() {
            indicaNecessidadeRedesenharTabuleiro();
        }
        
    }
    
    public void indicaNecessidadeRedesenharTabuleiro() {
        tabuleiro.invalidate();
    }
}
