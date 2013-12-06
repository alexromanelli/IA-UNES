package br.com.alexromanelli.android.jogodedamas.dinamica;

import java.util.ArrayList;

import br.com.alexromanelli.android.jogodedamas.dinamica.Casa.Direcao;
import br.com.alexromanelli.android.jogodedamas.dinamica.ConjuntoJogador.BaseJogador;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida.ResultadoPartida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.CorPeca;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca.EstadoPeca;

public class RegraClassica implements RegraDoJogo {

	public RegraClassica() {
	    this.contMovSucDeDamasSemDominacao = 0;
	    this.estadoDeEmpateEm5 = false;
	    this.contMovEmEstadoDeEmpateEm5 = 0;
	}

    public RegraClassica(int contMovSucDeDamasSemDominacao, boolean estadoDeEmpateEm5,
                         int contMovEmEstadoDeEmpateEm5) {
        this.contMovSucDeDamasSemDominacao = contMovSucDeDamasSemDominacao;
        this.estadoDeEmpateEm5 = estadoDeEmpateEm5;
        this.contMovEmEstadoDeEmpateEm5 = contMovEmEstadoDeEmpateEm5;
    }

    @Override
	public boolean identificaJogadasValidas(Peca pecaSelecionada,
			ArrayList<Jogada> jogadas) {
		ArvoreMovimentos arvore = new ArvoreMovimentos(pecaSelecionada);
		arvore.montaRamificacoes();
		System.gc();
		jogadas.addAll(arvore.identificaMaioresJogadas());
		return jogadas.size() > 0;
	}

    public static boolean pecaTornouSeDama(Peca peca) {
        if (peca.getEstado() == EstadoPeca.Pedra &&
            ((peca.getBase() == BaseJogador.Baixo && peca.getLocalizacao().getLinha() == 0) ||
             (peca.getBase() == BaseJogador.Alto && peca.getLocalizacao().getLinha() == 7)))
            return true; // parabéns! :)
        return false;
    }

	@Override
	public ResultadoPartida determinaResultadoPartida(ConjuntoJogador jogador1,
			ConjuntoJogador jogador2, ArrayList<ConjuntoJogador> vencedor) {
	    int contaPecasJogador1 = 0, contaPecasJogador2 = 0;
        int contaPecasLivresJogador1 = 0, contaPecasLivresJogador2 = 0;
	    
		for (Peca peca : jogador1.getConjuntoPecas()) {
		    if (peca.getEstado() != EstadoPeca.Perdida)
		        contaPecasJogador1++;
		    if (peca.getEstado() != EstadoPeca.Perdida &&
		            peca.getJogadasValidas().size() > 0)
		        contaPecasLivresJogador1++;
		}
        
        for (Peca peca : jogador2.getConjuntoPecas()) {
            if (peca.getEstado() != EstadoPeca.Perdida)
                contaPecasJogador2++;
            if (peca.getEstado() != EstadoPeca.Perdida &&
                    peca.getJogadasValidas().size() > 0)
                contaPecasLivresJogador2++;
        }
        
        if (contaPecasJogador1 > 0 && contaPecasLivresJogador1 > 0 &&
                (contaPecasJogador2 == 0 || contaPecasLivresJogador2 == 0)) {
            vencedor.add(jogador1);
            return ResultadoPartida.VitoriaIndividual;
        }
        if (contaPecasJogador2 > 0 && contaPecasLivresJogador2 > 0 &&
                (contaPecasJogador1 == 0 || contaPecasLivresJogador1 == 0)) {
            vencedor.add(jogador2);
            return ResultadoPartida.VitoriaIndividual;
        }

        // TODO verificar o motivo de declarar empate incorretamente. (será que é por causa do "getJogadasValidas()" estar com tamanho zero?)
        vencedor.add(jogador1);
        vencedor.add(jogador2);
		return ResultadoPartida.Empate;
	}

    public int getContMovSucDeDamasSemDominacao() {
        return contMovSucDeDamasSemDominacao;
    }

    public int getContMovEmEstadoDeEmpateEm5() {
        return contMovEmEstadoDeEmpateEm5;
    }

    public boolean isEstadoDeEmpateEm5() {
        return estadoDeEmpateEm5;
    }

    private int contMovSucDeDamasSemDominacao;
	private int contMovEmEstadoDeEmpateEm5;
	private boolean estadoDeEmpateEm5;
	
    @Override
    public boolean avaliaPossivelEmpate(Peca pecaMovida, boolean houveDominacao,
            ConjuntoJogador jogador1, ConjuntoJogador jogador2) {

        ConjuntoJogador brancas = jogador1.getCorPecasJogador() == CorPeca.Branca ? jogador1 : jogador2;
        ConjuntoJogador pretas = jogador1.getCorPecasJogador() == CorPeca.Preta ? jogador1 : jogador2;
        estadoDeEmpateEm5 = entrouNoEstadoDeEmpateEm5(brancas, pretas);

        if (houveDominacao) {
            contMovSucDeDamasSemDominacao = 0;
            contMovEmEstadoDeEmpateEm5 = 0;
            
        }
        else {
            if (pecaMovida.getEstado() == EstadoPeca.Dama) {
                contMovSucDeDamasSemDominacao++;
                
                /**
                 * primeira condição: empata se forem realizados 20 lançes sucessivos
                 * de damas sem dominação ou deslocamento de pedras.
                 */
                if (contMovSucDeDamasSemDominacao == 20)
                    return true;
            }
            else {
                contMovSucDeDamasSemDominacao = 0;
            }
            
            if (estadoDeEmpateEm5) {
                contMovEmEstadoDeEmpateEm5++;
                
                /**
                 * segunda condição: empata se estiver em uma condição de empate em 5
                 * e estes cinco lances tiverem sido feitos.
                 */
                if (contMovEmEstadoDeEmpateEm5 == 5)
                    return true;
            }
        }
        
        return false;
    }

    private boolean entrouNoEstadoDeEmpateEm5(ConjuntoJogador brancas, 
            ConjuntoJogador pretas) {
        int contDamasBrancas = 0;
        int contPedrasBrancas = 0;
        int contDamasPretas = 0;
        int contPedrasPretas = 0;
        
        for (Peca p : brancas.getConjuntoPecas())
            if (p.getEstado() == EstadoPeca.Dama)
                contDamasBrancas++;
            else if (p.getEstado() == EstadoPeca.Pedra)
                contPedrasBrancas++;

        for (Peca p : pretas.getConjuntoPecas())
            if (p.getEstado() == EstadoPeca.Dama)
                contDamasPretas++;
            else if (p.getEstado() == EstadoPeca.Pedra)
                contPedrasPretas++;

        boolean cond1 = contDamasBrancas == 2 && contPedrasBrancas == 0 &&
                        contDamasPretas == 2 && contPedrasPretas == 0;
        
        boolean cond2 = (contDamasBrancas == 2 && contPedrasBrancas == 0 &&
                         contDamasPretas == 1 && contPedrasPretas == 0) ||
                        (contDamasBrancas == 1 && contPedrasBrancas == 0 &&
                         contDamasPretas == 2 && contPedrasPretas == 0);

        boolean cond3 = (contDamasBrancas == 2 && contPedrasBrancas == 0 &&
                         contDamasPretas == 1 && contPedrasPretas == 1) ||
                        (contDamasBrancas == 1 && contPedrasBrancas == 1 &&
                         contDamasPretas == 2 && contPedrasPretas == 0);

        boolean cond4 = contDamasBrancas == 1 && contPedrasBrancas == 0 &&
                        contDamasPretas == 1 && contPedrasPretas == 0;

        boolean cond5 = (contDamasBrancas == 1 && contPedrasBrancas == 0 &&
                         contDamasPretas == 1 && contPedrasPretas == 1) ||
                        (contDamasBrancas == 1 && contPedrasBrancas == 1 &&
                         contDamasPretas == 1 && contPedrasPretas == 0);

        return cond1 || cond2 || cond3 || cond4 || cond5;
    }

}

class MovimentoPeca {
	private Casa origem;
	private Casa destino;
	private Peca pecaDominada;

	public MovimentoPeca(Casa origem, Casa destino, Peca pecaDominada) {
		super();
		this.origem = origem;
		this.destino = destino;
		this.pecaDominada = pecaDominada;
	}

	public Casa getOrigem() {
		return origem;
	}

	public Casa getDestino() {
		return destino;
	}

	public Peca getPecaDominada() {
		return pecaDominada;
	}

}

class NoMovimentoPeca {
	private MovimentoPeca elemento;
	private ArrayList<NoMovimentoPeca> sequenciasPossiveis;
	private NoMovimentoPeca antecessor;

	public NoMovimentoPeca(MovimentoPeca elemento, NoMovimentoPeca antecessor) {
		super();
		this.elemento = elemento;
		this.antecessor = antecessor;
		if (elemento.getPecaDominada() == null)
			this.sequenciasPossiveis = null;
		else
			this.sequenciasPossiveis = new ArrayList<NoMovimentoPeca>();
	}

	public MovimentoPeca getElemento() {
		return elemento;
	}

	public NoMovimentoPeca getAntecessor() {
		return antecessor;
	}

	public ArrayList<NoMovimentoPeca> getSequenciasPossiveis() {
		return sequenciasPossiveis;
	}

	public void addSequencia(NoMovimentoPeca seq) throws Exception {
		if (sequenciasPossiveis != null)
			sequenciasPossiveis.add(seq);
		else
			throw new Exception(
					"ERRO: não é possível encadear movimentos a partir de um simples deslocamento sem dominação.");
	}
}

class ArvoreMovimentos {
	private Peca pecaSelecionada;
	private ArrayList<NoMovimentoPeca> raiz;

	public ArvoreMovimentos(Peca pecaSelecionada) {
		this.pecaSelecionada = pecaSelecionada;
		raiz = new ArrayList<NoMovimentoPeca>();
	}

	public NoMovimentoPeca insereMovimentoPeca(MovimentoPeca movimento,
			NoMovimentoPeca partida) {
		NoMovimentoPeca no = new NoMovimentoPeca(movimento, partida);

		if (partida == null)
			raiz.add(no);
		else
			try {
				partida.addSequencia(no);
			} catch (Exception e) {
				e.printStackTrace();
			}

		return no;
	}

	public ArrayList<Jogada> identificaMaioresJogadas() {
		ArrayList<Jogada> maioresJogadas = new ArrayList<Jogada>();

		ArrayList<Jogada> jogadas = new ArrayList<Jogada>();
		for (NoMovimentoPeca no : raiz) {
			ArrayList<MovimentoPeca> caminho = new ArrayList<MovimentoPeca>();
			caminho.add(no.getElemento());
			listaJogadasSubsequentes(no, caminho, jogadas);
		}

		int maxMovimentos = 0;
		int minDominadas = 0;
		for (Jogada j : jogadas) {
			if (j.getPassosJogada() == maxMovimentos
			        && j.getDominadasNoPercurso().size() == minDominadas)
				maioresJogadas.add(j);
			else if (j.getPassosJogada() > maxMovimentos
			        || j.getDominadasNoPercurso().size() > minDominadas) {
				maioresJogadas.clear();
				maioresJogadas.add(j);
				maxMovimentos = j.getPassosJogada();
				minDominadas = j.getDominadasNoPercurso().size();
			}
		}

		return maioresJogadas;
	}

	public void listaJogadasSubsequentes(NoMovimentoPeca origem,
			ArrayList<MovimentoPeca> caminhoAnterior,
			ArrayList<Jogada> jogadasIdentificadas) {
		if (origem.getSequenciasPossiveis() == null
				|| origem.getSequenciasPossiveis().size() == 0) {
			// se movimento origem não tem sequência, então o caminho até este
			// movimento deve ser listado como uma jogada
			Jogada j = new Jogada(pecaSelecionada);
			for (MovimentoPeca mov : caminhoAnterior) {
				j.getPercurso().add(mov.getDestino());
				if (mov.getPecaDominada() != null)
					j.getDominadasNoPercurso().add(mov.getPecaDominada());
			}
			jogadasIdentificadas.add(j);
		} else {
			// movimento origem tem sequência
			for (NoMovimentoPeca no : origem.getSequenciasPossiveis()) {
				// inclui ramificação no caminho
				caminhoAnterior.add(no.getElemento());
				// faz chamada recursiva para seguir a execução a partir da
				// ramificação
				listaJogadasSubsequentes(no, caminhoAnterior,
						jogadasIdentificadas);
				// remove ramificação do caminho para continuar a execução
				caminhoAnterior.remove(no.getElemento());
			}
		}
	}

	public void montaRamificacoes() {
		montaRamificacoesNo(null);
	}

	private void adicionaRamificacao(NoMovimentoPeca origem, NoMovimentoPeca no) {
		// se a origem for nula, então trata-se de uma ramificação da
		// raiz
		if (origem == null)
			raiz.add(no);
		// senão, a ramificação deve ser incluída como uma sequência da
		// origem desta chamada
		else
			try {
				origem.addSequencia(no);
			} catch (Exception e) {
				// se houver uma falha no processo, termina
				e.printStackTrace();
				return;
			}
	}

	public void montaRamificacoesNo(NoMovimentoPeca origem) {
		Casa casaAtual = origem != null ? origem.getElemento().getDestino()
				: pecaSelecionada.getLocalizacao();
		Casa casaAnterior = origem != null ? origem.getElemento().getOrigem()
				: null;
		BaseJogador base = pecaSelecionada.getBase();
		CorPeca corSelecionada = pecaSelecionada.getCor();

		// verifica vizinhas de todas as direções permitidas
		for (Direcao dir : Casa.DIRECOES_PERMITIDAS) {
			
			Casa vizinhaDirecao = casaAtual.getVizinhaDirecao(dir);
			if (vizinhaDirecao != null && vizinhaDirecao != casaAnterior) {
				// se a casa vizinha nordeste estiver vazia e a peça puder ser
				// movida neste sentido, então é possível apenas fazer um
				// deslocamento simples da peça para esta casa vizinha (detalhe:
				// isso só é válido para movimentos diretos a partir da posição
				// original da peça selecionada)
				if (vizinhaDirecao.getOcupante() == null
						&& casaAtual == pecaSelecionada.getLocalizacao()
						&& (pecaSelecionada.getEstado() == EstadoPeca.Dama || (pecaSelecionada
								.getEstado() == EstadoPeca.Pedra
								&& ((base == BaseJogador.Baixo && (dir == Direcao.Nordeste || dir == Direcao.Noroeste)) || 
								    (base == BaseJogador.Alto && (dir == Direcao.Sudeste || dir == Direcao.Sudoeste)))))) {
					MovimentoPeca movimento = new MovimentoPeca(casaAtual,
							vizinhaDirecao, null);
					NoMovimentoPeca no = new NoMovimentoPeca(movimento, origem);
	
					adicionaRamificacao(origem, no);
	
					if (pecaSelecionada.getEstado() == EstadoPeca.Dama) {
						// tratamento para movimentos longos da dama: isso deve ser
						// feito por uma avaliação das casas subsequentes, no mesmo
						// sentido, que podem ser saltadas pela dama. se houver,
						// cada uma dessas casas é um destino para um movimento
						// originado no mesmo local atual (da chamada do método). se
						// houver uma peça do próprio jogador atual, trata-se de uma
						// barreira que não pode ser ultrapassada. se houver uma
						// peça do adversário seguida de um ou mais espaços em
						// branco, são mais possibilidades de continuidade de
						// movimento, com dominação da peça saltada. havendo
						// dominação, para cada destino após a dominação deve ser
						// analisada a possibilidade de continuidade da jogada com
						// dominações em outros sentidos. isto é feito com chamadas
						// recursivas a partir de cada destino após a dominação.
						Casa casaSeq = vizinhaDirecao.getVizinhaDirecao(dir);
						ArrayList<NoMovimentoPeca> movPosDominacao = new ArrayList<NoMovimentoPeca>();
						boolean houveDominacao = false;
						Peca pecaDominada = null;
						while (casaSeq != null
								&& (casaSeq.getOcupante() == null || (casaSeq
										.getOcupante() != null
										&& casaSeq.getOcupante().getCor() != corSelecionada
										&& casaSeq.getVizinhaDirecao(dir) != null && casaSeq
										.getVizinhaDirecao(dir).getOcupante() == null))) {
						    
							if (casaSeq.getOcupante() == null) {
								MovimentoPeca seqMovimentoLongo = new MovimentoPeca(
										casaAtual, casaSeq, pecaDominada);
								NoMovimentoPeca noMovAlternativo = new NoMovimentoPeca(
										seqMovimentoLongo, origem);
	
								adicionaRamificacao(origem, noMovAlternativo);
								
								if (houveDominacao)
									movPosDominacao.add(noMovAlternativo);
								
							} else {
								if (houveDominacao || pecaJaDominadaNaJogada(origem,
										casaSeq.getOcupante()))
									break;
	
								MovimentoPeca seqMovimentoLongo = new MovimentoPeca(
										casaAtual,
										casaSeq.getVizinhaDirecao(dir),
										casaSeq.getOcupante());
								NoMovimentoPeca noMovAlternativo = new NoMovimentoPeca(
										seqMovimentoLongo, origem);
	
								adicionaRamificacao(origem, noMovAlternativo);
								movPosDominacao.add(noMovAlternativo);
								houveDominacao = true;
								pecaDominada = casaSeq.getOcupante();
							}
							
							casaSeq = casaSeq.getVizinhaDirecao(dir);
						}
	
						// faz chamadas recursivas para continuidade de movimento,
						// em casos de dominação
						for (NoMovimentoPeca mov : movPosDominacao) {
							montaRamificacoesNo(mov);
						}
					}
				}
				// se a casa nordeste contiver uma peça do adversário do jogador
				// atual e a casa subsequente no mesmo sentido estiver vazia, então
				// é possível mover a peça para esta casa subsequente e dominar a
				// peça adversária no caminho
				else if (vizinhaDirecao.getOcupante() != null
						&& vizinhaDirecao.getOcupante().getCor() != corSelecionada
						&& vizinhaDirecao.getVizinhaDirecao(dir) != null
						&& (vizinhaDirecao.getVizinhaDirecao(dir).getOcupante() == null ||
						    // em casos de movimentos que retornem ao ponto de partida
						    vizinhaDirecao.getVizinhaDirecao(dir).getOcupante() == pecaSelecionada)
						&& !pecaJaDominadaNaJogada(origem,
								vizinhaDirecao.getOcupante())) {
	
					MovimentoPeca movimento = new MovimentoPeca(casaAtual,
							vizinhaDirecao.getVizinhaDirecao(dir),
							vizinhaDirecao.getOcupante());
					NoMovimentoPeca no = new NoMovimentoPeca(movimento, origem);
	
					adicionaRamificacao(origem, no);
	
					// tratamento para movimentos longos da dama, que pode saltar
					// casas livres existentes após a peça dominada.
                    ArrayList<NoMovimentoPeca> movPosDominacao = new ArrayList<NoMovimentoPeca>();
					if (pecaSelecionada.getEstado() == EstadoPeca.Dama) {
    					Casa casaSeq = vizinhaDirecao.getVizinhaDirecao(dir);
    					while (casaSeq != null
    							&& casaSeq.getOcupante() == null) {
    						MovimentoPeca seqMovimentoLongo = new MovimentoPeca(
    								casaAtual, casaSeq,
    								vizinhaDirecao.getOcupante());
    						NoMovimentoPeca noMovAlternativo = new NoMovimentoPeca(
    								seqMovimentoLongo, origem);
    	
    						adicionaRamificacao(origem, noMovAlternativo);
    						movPosDominacao.add(noMovAlternativo);
    						
    						casaSeq = casaSeq.getVizinhaDirecao(dir);
    					}
					}
                    
                    // chamada(s) recursiva(s) para avaliar sequências da jogada a
                    // partir do destino deste movimento com dominação que foi
                    // identificado.
                    montaRamificacoesNo(no);
                    for (NoMovimentoPeca mov : movPosDominacao) {
                        montaRamificacoesNo(mov);
                    }
				}
				// TODO incluir tratamento para casos de dama que pode fazer uma mudança
				//      de direção, após uma dominação, para dominar outra peça do adversário
				//      que pode ser atingida com movimento mais longo do que uma casa depois
				//      da mudança de direção.
				else if (pecaSelecionada.getEstado() == EstadoPeca.Dama &&
				         vizinhaDirecao != null && vizinhaDirecao.getOcupante() == null) {
                    Casa casaSeq = vizinhaDirecao.getVizinhaDirecao(dir);
                    ArrayList<NoMovimentoPeca> movPosDominacao = new ArrayList<NoMovimentoPeca>();
                    boolean houveDominacao = false;
                    Peca pecaDominada = null;
                    while (casaSeq != null
                            && (casaSeq.getOcupante() == null || (casaSeq
                                    .getOcupante() != null
                                    && casaSeq.getOcupante().getCor() != corSelecionada
                                    && casaSeq.getVizinhaDirecao(dir) != null && casaSeq
                                    .getVizinhaDirecao(dir).getOcupante() == null))) {
                        
                        if (casaSeq.getOcupante() == null && houveDominacao) {
                            MovimentoPeca seqMovimentoLongo = new MovimentoPeca(
                                    casaAtual, casaSeq, pecaDominada);
                            NoMovimentoPeca noMovAlternativo = new NoMovimentoPeca(
                                    seqMovimentoLongo, origem);

                            adicionaRamificacao(origem, noMovAlternativo);
                            movPosDominacao.add(noMovAlternativo);
                            
                        } else if (casaSeq.getOcupante() != null) {
                            if (houveDominacao || pecaJaDominadaNaJogada(origem,
                                    casaSeq.getOcupante()))
                                break;

                            MovimentoPeca seqMovimentoLongo = new MovimentoPeca(
                                    casaAtual,
                                    casaSeq.getVizinhaDirecao(dir),
                                    casaSeq.getOcupante());
                            NoMovimentoPeca noMovAlternativo = new NoMovimentoPeca(
                                    seqMovimentoLongo, origem);

                            adicionaRamificacao(origem, noMovAlternativo);
                            movPosDominacao.add(noMovAlternativo);
                            houveDominacao = true;
                            pecaDominada = casaSeq.getOcupante();
                        }
                        
                        casaSeq = casaSeq.getVizinhaDirecao(dir);
                    }

                    // faz chamadas recursivas para continuidade de movimento,
                    // em casos de dominação
                    for (NoMovimentoPeca mov : movPosDominacao) {
                        montaRamificacoesNo(mov);
                    }
				}
			}
		}

	}

	private boolean pecaJaDominadaNaJogada(NoMovimentoPeca origem, Peca aDominar) {
		NoMovimentoPeca no = origem;
		while (no != null) {
			if (no.getElemento().getPecaDominada() == aDominar)
				return true;
			no = no.getAntecessor();
		}
		return false;
	}
}
