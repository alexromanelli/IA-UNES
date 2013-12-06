package br.com.alexromanelli.android.jogodedamas.dinamica;

import java.util.ArrayList;

import android.os.Handler;
import br.com.alexromanelli.android.jogodedamas.MainActivity;
import br.com.alexromanelli.android.jogodedamas.dinamica.ConjuntoJogador.BaseJogador;
import br.com.alexromanelli.android.jogodedamas.dinamica.ConjuntoJogador.TipoJogador;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.CorPeca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.EstadoPeca;
import br.com.alexromanelli.android.jogodedamas.persistencia.PecaPartidaInterrompida;
import br.com.alexromanelli.android.jogodedamas.persistencia.SnapshotPartida;

public class Partida {

	public enum OpcaoTempoPartida {
		Relampago5x5,
		Curto15x15,
		Medio30x30,
		Normal45x45,
		Ilimitado
	}
	
	public enum OpcaoAdversario {
		Computador,
		HumanoLocal,
		HumanoRemoto
	}
	
	public enum ResultadoPartida {
		Empate,
		VitoriaIndividual
	}
	
	public static OpcaoTempoPartida OPCAO_TEMPO_JOGO = OpcaoTempoPartida.Medio30x30;
	public static OpcaoAdversario OPCAO_ADVERSARIO = OpcaoAdversario.HumanoLocal;
	public static int NUMERO_DESFAZER_POR_JOGADOR = 0;

	public static void setOpcaoTempoJogo(OpcaoTempoPartida opcao) throws Exception {
		if (INSTANCIA == null)
			OPCAO_TEMPO_JOGO = opcao;
		else {
			Exception e = new Exception("ERRO: tentativa de mudança do tempo do jogo com o mesmo em andamento.");
			throw(e);
		}
	}

	private static Partida INSTANCIA;
	
	public static Partida getInstancia() {
		if (INSTANCIA == null) {
			INSTANCIA = new Partida(new RegraClassica());
		}
		return INSTANCIA;
	}

	private boolean partidaEmCurso;
	private Tabuleiro tabuleiro;
	private ConjuntoJogador pecasBrancas;
	private ConjuntoJogador pecasPretas;
	private ConjuntoJogador jogadorAtual;
	
	private Peca pecaSelecionada;
	
	private RegraDoJogo regra;
    private boolean emMovimento;
    
    private Handler handlerAtividadePrincipal;
    private Runnable anuncioFimDePartida;
    private Runnable registradorDeHistorico;
    private Runnable indicadorRedesenharTabuleiro;
	
	public void setIndicadorRedesenharTabuleiro(
            Runnable indicadorRedesenharTabuleiro) {
        this.indicadorRedesenharTabuleiro = indicadorRedesenharTabuleiro;
    }

    public Partida(RegraDoJogo regra) {
		tabuleiro = new Tabuleiro();
		setPartidaEmCurso(false);
		
		this.regra = regra;
	}

	private void anunciaJogadorAtual() {
	    if (jogadorAtual != null)
            handlerAtividadePrincipal.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.getInstance().setJogadorAtual(jogadorAtual.getCorPecasJogador());
                }
            });
	}
	
	public void iniciarPartida() {
	    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> para testes
	    //ajustaParaTestes();
	    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	    emMovimento = false;

	    pecasBrancas = new ConjuntoJogador(TipoJogador.Humano, Peca.CorPeca.Branca, BaseJogador.Baixo, tabuleiro);
        TipoJogador adversario = OPCAO_ADVERSARIO == OpcaoAdversario.HumanoLocal ? TipoJogador.Humano :
                OPCAO_ADVERSARIO == OpcaoAdversario.Computador ? TipoJogador.IA : TipoJogador.DispositivoRemoto;
        pecasPretas = new ConjuntoJogador(adversario, Peca.CorPeca.Preta, BaseJogador.Alto, tabuleiro);
        jogadorAtual = pecasBrancas;
        
        System.gc();

		encontraMovimentosValidos(jogadorAtual);
		Relogio.getInstancia().iniciaContagem();
		setPartidaEmCurso(true);
		anunciaJogadorAtual();
	}
	
	public void iniciarPartida(ConjuntoJogador brancas, ConjuntoJogador pretas, ConjuntoJogador atual) {
	    emMovimento = false;
	    
	    pecasBrancas = brancas;
	    pecasPretas = pretas;
	    jogadorAtual = atual;
	    
	    System.gc();
	    
	    encontraMovimentosValidos(jogadorAtual);
	    Relogio.getInstancia().iniciaContagem();
	    setPartidaEmCurso(true);
	    anunciaJogadorAtual();
	}
	
	public void reiniciarPartida() {
        tabuleiro = new Tabuleiro();
        
        Relogio.getInstancia().reiniciaContagem();
        iniciarPartida();
	}
	
	private void encontraMovimentosValidos(ConjuntoJogador jogador) {
	    int pecasLivres = 0;
	    int maxDominadas = 0;
	    // percorre as peças do conjunto do jogador para identificar jogadas válidas
		for (Peca peca : jogador.getConjuntoPecas()) {
		    if (peca.getEstado() != EstadoPeca.Perdida) {
		        ArrayList<Jogada> jogadasValidas = new ArrayList<Jogada>();
                pecasLivres += regra.identificaJogadasValidas(peca, jogadasValidas) ? 1 : 0;
                peca.setJogadasValidas(jogadasValidas);
                System.gc();
                
                if (jogadasValidas.size() > 0) {
                    int pecasDominadas = jogadasValidas.get(0).getDominadasNoPercurso().size();
                    if (pecasDominadas > maxDominadas)
                        maxDominadas = pecasDominadas;
                }
		    }
		}
		
		// percorre novamente as peças, mas para eliminar as que tenham movimentos com menos dominação (se houver algum com dominação)
		for (Peca peca : jogador.getConjuntoPecas()) {
		    if (peca.getEstado() != EstadoPeca.Perdida &&
		            peca.getJogadasValidas() != null &&
		            peca.getJogadasValidas().size() > 0 &&
		            peca.getJogadasValidas().get(0).getDominadasNoPercurso().size() < maxDominadas) {
		        peca.getJogadasValidas().clear();
		        System.gc();
		    }
		}
		
		if (pecasLivres == 0)
		    informaFimDePartida();
	}

	public Tabuleiro getTabuleiro() {
		return tabuleiro;
	}

	public void selecionaPeca(Peca peca) {
	    // se estiver com peça em movimento, não pode mais selecionar peça :)
	    if (emMovimento)
	        return;
	    
		ConjuntoJogador conjunto = peca.getCor() == Peca.CorPeca.Branca ? pecasBrancas : pecasPretas;
		for (Peca p : conjunto.getConjuntoPecas())
			p.setSelecionada(false);
		peca.setSelecionada(true);
		pecaSelecionada = peca;
		
		marcaMovimentosValidos(peca);
	}
	
	public void setPecaSelecionada(Peca pecaSelecionada) {
	    this.pecaSelecionada = pecaSelecionada;
	}
	
	public Peca getPecaSelecionada() {
	    return pecaSelecionada;
	}
	
	public ConjuntoJogador getPecasBrancas() {
		return pecasBrancas;
	}

	public ConjuntoJogador getPecasPretas() {
		return pecasPretas;
	}

	private void marcaMovimentosValidos(Peca peca) {
		for (int i = 0; i < 8; i++)
		    for (int j = 0; j < 8; j++) {
		        Casa casa = tabuleiro.getCasa(i, j);
		        if (casa != null) {
		            if (parteDeJogadaValida(casa, peca))
		                casa.setParteDeJogadaValida(true);
		            else
		                casa.setParteDeJogadaValida(false);
		        }
		    }
	}

	private boolean parteDeJogadaValida(Casa casa, Peca peca) {
        for (Jogada jogada : peca.getJogadasValidas())
            for (Casa c : jogada.getPercurso())
                if (c == casa)
                    return true;
        return false;
    }

    public ConjuntoJogador getJogadorAtual() {
		return jogadorAtual;
	}

	public void informaFimDePartida() {
        if (jogadorAtual != null)
            // se o jogador atual não é nulo, ainda, então é garantido que
            // esta seja a primeira chamada deste método por identificação
            // de fim de partida, nesta partida. sendo assim, torna-se
            // necessário identificar as jogadas válidas para o adversário
            // para verificar se ele possui peças livres e, portanto, venceu.
            encontraMovimentosValidos(jogadorAtual == pecasBrancas ? pecasPretas : pecasBrancas);

	    jogadorAtual = null;
	    
	    ArrayList<ConjuntoJogador> vencedor = new ArrayList<ConjuntoJogador>();
		ResultadoPartida resultado = regra.determinaResultadoPartida(getPecasBrancas(), getPecasPretas(), vencedor);
		
        ((MainActivity.RunAnuncioFimDePartida)anuncioFimDePartida).setResultado(resultado);
        
        if (resultado == ResultadoPartida.VitoriaIndividual)
		    ((MainActivity.RunAnuncioFimDePartida)anuncioFimDePartida).setVencedor(vencedor.get(0));

        handlerAtividadePrincipal.post(anuncioFimDePartida);
        getInstancia().encerrarPartida();
	}

	public void setJogadorAtual(ConjuntoJogador jogadorAtual) {
		this.jogadorAtual = jogadorAtual;
	}

    public void movePecaSelecionada(int linha, int coluna) {
        int origemLinha = pecaSelecionada.getLocalizacao().getLinha();
        int origemColuna = pecaSelecionada.getLocalizacao().getColuna();
        
        Casa destino = tabuleiro.getCasa(linha, coluna);
        
        // verifica se este destino é o próximo passo de alguma jogada válida
        if (!isProximoPassoJogadaValida(destino, pecaSelecionada))
            // se não é, então aborta movimentação
            return;
        
        // bloqueia seleção de peça nesta jogada
        emMovimento = true;
        
        boolean haDominacao = haDominacaoEmMovimento(destino);
        
        ((MainActivity.RunRegistradorDeHistorico)registradorDeHistorico).setMovimento(pecaSelecionada.getCor(), origemLinha, origemColuna, linha, coluna, haDominacao);
        handlerAtividadePrincipal.post(registradorDeHistorico);
        
        // move peça selecionada
        boolean haSequencia = pecaSelecionada.movePeca(destino);
        
        if (!haSequencia) {
            informaConclusaoDeJogada(haDominacao);
        }
        else {
            // atualiza marcação de tabuleiro
            marcaMovimentosValidos(pecaSelecionada);
        }
    }

    private boolean haDominacaoEmMovimento(Casa destino) {
        for (Jogada jogada : pecaSelecionada.getJogadasValidas())
            if (jogada.getPercurso().get(0) == destino)
                return jogada.getDominadasNoPercurso().size() > 0;
        return false;
    }

    private boolean isProximoPassoJogadaValida(Casa destino, Peca peca) {
        for (Jogada jogada : peca.getJogadasValidas())
            if (jogada.getPercurso().get(0) == destino)
                return true;
        return false;
    }

    private void informaConclusaoDeJogada(boolean houveDominacao) {
        // verifica se a peça tornou-se uma "dama"
        if (RegraClassica.pecaTornouSeDama(pecaSelecionada))
            pecaSelecionada.setEstado(EstadoPeca.Dama); // parabéns! :)

        boolean empate = regra.avaliaPossivelEmpate(pecaSelecionada, houveDominacao, pecasBrancas, pecasPretas);
        if (empate) {
            informaFimDePartida();
            return;
        }
        
        limpaMarcacoesMovimentosValidos();
        pecaSelecionada.setSelecionada(false);
        pecaSelecionada = null;
        jogadorAtual = (jogadorAtual == pecasBrancas) ? pecasPretas : pecasBrancas;
        emMovimento = false;
        encontraMovimentosValidos(jogadorAtual);
        
        anunciaJogadorAtual();
    }

    private void limpaMarcacoesMovimentosValidos() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (tabuleiro.getCasa(i, j) != null)
                    tabuleiro.getCasa(i, j).setParteDeJogadaValida(false);
    }

    public void setHandlerAtividadePrincipal(Handler handlerAtividadePrincipal) {
        this.handlerAtividadePrincipal = handlerAtividadePrincipal;
    }

    public void setAnuncioFimDePartida(Runnable anuncioFimDePartida) {
        this.anuncioFimDePartida = anuncioFimDePartida;
    }
    
    public void setRegistradorDeHistorico(Runnable registradorDeHistorico) {
        this.registradorDeHistorico = registradorDeHistorico;
    }
    
    /**
     * Método usado para executar testes no jogo.
     */
    @SuppressWarnings("unused")
    private void ajustaParaTestes() {
        // limpa tabuleiro
        for (Peca peca : pecasBrancas.getConjuntoPecas()) {
            peca.setEstado(EstadoPeca.Perdida);
            peca.setLocalizacao(null);
        }
        for (Peca peca : pecasPretas.getConjuntoPecas()) {
            peca.setEstado(EstadoPeca.Perdida);
            peca.setLocalizacao(null);
        }
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                Casa casa = tabuleiro.getCasa(i, j);
                if (casa != null)
                    casa.setOcupante(null);
            }
        
        // posiciona uma dama branca no canto superior direito
       Peca damaBranca = pecasBrancas.getConjuntoPecas()[0];
       damaBranca.setEstado(EstadoPeca.Dama);
       damaBranca.setLocalizacao(tabuleiro.getCasa(0, 5));
       
       // posiciona uma peça preta a duas casas de distância da dama branca
       Peca pecaPreta = pecasPretas.getConjuntoPecas()[0];
       pecaPreta.setEstado(EstadoPeca.Pedra);
       pecaPreta.setLocalizacao(tabuleiro.getCasa(2, 3));
       
       // posiciona outra peça preta na penúltima linha de baixo, na antepenúltima coluna
       pecaPreta = pecasPretas.getConjuntoPecas()[1];
       pecaPreta.setEstado(EstadoPeca.Pedra);
       pecaPreta.setLocalizacao(tabuleiro.getCasa(6, 3));
       
       pecaPreta = pecasPretas.getConjuntoPecas()[2];
       pecaPreta.setEstado(EstadoPeca.Pedra);
       pecaPreta.setLocalizacao(tabuleiro.getCasa(5, 6));
       
       pecaPreta = pecasPretas.getConjuntoPecas()[3];
       pecaPreta.setEstado(EstadoPeca.Pedra);
       pecaPreta.setLocalizacao(tabuleiro.getCasa(2, 5));
       
       pecaPreta = pecasPretas.getConjuntoPecas()[4];
       pecaPreta.setEstado(EstadoPeca.Pedra);
       pecaPreta.setLocalizacao(tabuleiro.getCasa(3, 6));
    }

    public boolean isPartidaEmCurso() {
        return partidaEmCurso;
    }

    public void setPartidaEmCurso(boolean partidaEmCurso) {
        this.partidaEmCurso = partidaEmCurso;
    }

    public void encerrarPartida() {
        setPartidaEmCurso(false);
        Relogio.getInstancia().encerraContagem();
        INSTANCIA = null;
        if (!MainActivity.getInstance().isEncerrandoAtividade())
            atualizaDesenhoTabuleiro();
    }

    private void atualizaDesenhoTabuleiro() {
        handlerAtividadePrincipal.post(indicadorRedesenharTabuleiro);
    }

    public static void retomarPartidaInterrompida(SnapshotPartida sp) {
        OPCAO_ADVERSARIO = sp.getRegistroPartida().getOpcaoAdversario();
        OPCAO_TEMPO_JOGO = sp.getRegistroPartida().getOpcaoTempo();

        ConjuntoJogador brancas = new ConjuntoJogador(TipoJogador.Humano, CorPeca.Branca, BaseJogador.Baixo, getInstancia().tabuleiro);

        TipoJogador adversario = OPCAO_ADVERSARIO == OpcaoAdversario.HumanoLocal ? TipoJogador.Humano :
            OPCAO_ADVERSARIO == OpcaoAdversario.Computador ? TipoJogador.IA :
            TipoJogador.DispositivoRemoto;
        ConjuntoJogador pretas = new ConjuntoJogador(adversario, CorPeca.Preta, BaseJogador.Alto, getInstancia().tabuleiro);

        retiraPecasDoTabuleiro(brancas);
        retiraPecasDoTabuleiro(pretas);

        carregaPecas(brancas, sp.getListaDePecas());
        carregaPecas(pretas, sp.getListaDePecas());
        
        ConjuntoJogador atual = sp.getRegistroPartida().getJogadorAtual() == 'b' ? brancas : pretas;
        
        getInstancia().iniciarPartida(brancas, pretas, atual);
        if (OPCAO_TEMPO_JOGO != OpcaoTempoPartida.Ilimitado) {
            Relogio.getInstancia().setTempoRestanteBranco(sp.getRegistroPartida().getTempoRestanteBrancas());
            Relogio.getInstancia().setTempoRestantePreto(sp.getRegistroPartida().getTempoRestantePretas());
        }
        else {
            Relogio.getInstancia().setTempoRestanteBranco(Long.MAX_VALUE);
            Relogio.getInstancia().setTempoRestantePreto(Long.MAX_VALUE);
        }
        getInstancia().atualizaDesenhoTabuleiro();
    }

    private static void retiraPecasDoTabuleiro(ConjuntoJogador conj) {
        for (Peca p : conj.getConjuntoPecas())
            p.setLocalizacao(null);
    }

    private static void carregaPecas(ConjuntoJogador conj,
            ArrayList<PecaPartidaInterrompida> listaDePecas) {
        CorPeca cor = conj.getCorPecasJogador();
        
        Peca[] pecas = conj.getConjuntoPecas();

        int ind = 0;
        for (PecaPartidaInterrompida p : listaDePecas) {
            if (p.getCor() == cor) {
                EstadoPeca estado = p.getEstado();
                pecas[ind].setEstado(estado);
                pecas[ind].getJogadasValidas().clear();
                
                if (estado != EstadoPeca.Perdida) {
                    int linha = p.getLinha();
                    int coluna = p.getColuna();
                    Casa localizacao = getInstancia().getTabuleiro().getCasa(linha, coluna);
                    pecas[ind].setLocalizacao(localizacao);
                }
                else
                    pecas[ind].setLocalizacao(null);
                
                ind++;
            }
        }
    }

    public RegraDoJogo getRegra() {
        return this.regra;
    }
}

