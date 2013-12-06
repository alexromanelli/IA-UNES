package br.com.alexromanelli.android.jogodedamas.dinamica;

import java.util.ArrayList;

public class Peca {

	public enum EstadoPeca {
		Pedra,
		Dama,
		Perdida;

        public int toInt() {
            if (this == Pedra)
                return 0;
            else if (this == Dama)
                return 1;
            else
                return 2;
        }
	}
	
	public enum CorPeca {
		Branca,
		Preta;

        public char toChar() {
            if (this == Branca)
                return 'b';
            else
                return 'p';
        }
	}
	
	private CorPeca cor;
	private EstadoPeca estado;
	private Casa localizacao;
	private ConjuntoJogador.BaseJogador base;
	private boolean selecionada;
	
	private ArrayList<Jogada> jogadasValidas;
	
	public Peca(CorPeca cor, Casa localizacao, ConjuntoJogador.BaseJogador base) {
		this.cor = cor;
		this.estado = EstadoPeca.Pedra;
		this.localizacao = localizacao;
		localizacao.setOcupante(this);
		this.base = base;
		this.selecionada = false;
		
		jogadasValidas = new ArrayList<Jogada>();
	}

    public Peca(Peca p) {
        this.cor = p.getCor();
        this.estado = p.getEstado();
        this.localizacao = p.getLocalizacao();
        this.base = p.getBase();
        this.selecionada = false;

        this.jogadasValidas = new ArrayList<Jogada>();
    }

	public CorPeca getCor() {
		return cor;
	}

	public void setCor(CorPeca cor) {
		this.cor = cor;
	}

	public EstadoPeca getEstado() {
		return estado;
	}

	public void setEstado(EstadoPeca estado) {
		this.estado = estado;
	}

	public Casa getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Casa localizacao) {
	    if (this.localizacao != null)
	        this.localizacao.setOcupante(null);
		this.localizacao = localizacao;
		if (this.localizacao != null) // peça não foi dominada
		    this.localizacao.setOcupante(this);
	}

    /**
     * Este método faz o ajuste da localização para o processo de construção do novo tabuleiro
     * para um estado obtido durante a busca por uma jogada pelo adversário IA. A diferença para
     * o método setLocalizacao é que o setLocalizacaoEmBusca não libera a casa anteriormente
     * ocupada pela peça atual. Isto porque a nova localização é feita em um novo tabuleiro,
     * criado pelo processo de busca.
     *
     * @param localizacao é a casa a ser ocupada no tabuleiro em construção durante rotina de busca.
     */
    public void setLocalizacaoEmBusca(Casa localizacao) {
        this.localizacao = localizacao;
        localizacao.setOcupante(this);
    }

	public boolean isSelecionada() {
		return selecionada;
	}

	public void setSelecionada(boolean selecionada) {
		this.selecionada = selecionada;
	}

	public ConjuntoJogador.BaseJogador getBase() {
		return base;
	}
	
	public void setJogadasValidas(ArrayList<Jogada> jogadasValidas) {
	    this.jogadasValidas.clear();
	    this.jogadasValidas.addAll(jogadasValidas);
	}
	
	public ArrayList<Jogada> getJogadasValidas() {
	    return jogadasValidas;
	}

    public Peca atualizaJogadasValidas(Casa destino) {
        ArrayList<Jogada> jogadasFiltradas = new ArrayList<Jogada>();
        Peca pecaDominada = null;
        for (Jogada jogada : jogadasValidas) {
            ArrayList<Casa> percurso = jogada.getPercurso();
            
            // verifica se o percurso da jogada inicia com o movimento para o destino indicado
            if (percurso != null && percurso.size() > 0 && percurso.get(0) == destino) {
                
                // remove o primeiro passo do percurso (já feito)
                percurso.remove(0);

                // se o percurso tiver sequência, manter a jogada válida
                if (percurso.size() > 0)
                    jogadasFiltradas.add(jogada);
                
                // se houver dominação neste percurso, remover a peça dominada da lista
                if (jogada.getDominadasNoPercurso() != null && 
                        jogada.getDominadasNoPercurso().size() > 0) {
                    pecaDominada = jogada.getDominadasNoPercurso().get(0);
                    jogada.getDominadasNoPercurso().remove(0);
                }
            }
        }
        
        // atualiza lista de jogadas válidas
        jogadasValidas.clear();
        System.gc();
        jogadasValidas.addAll(jogadasFiltradas);
        
        // retorna a peça dominada no movimento realizado, se houver
        return pecaDominada;
    }
    
    public boolean movePeca(Casa destino) {
        setLocalizacao(destino);
        
        // atualiza jogadas válidas de acordo com este movimento
        Peca pecaDominada = atualizaJogadasValidas(destino);
        
        // se houve dominação de peça, marque-a como tal
        if (pecaDominada != null) {
            pecaDominada.estado = EstadoPeca.Perdida;
            pecaDominada.getLocalizacao().setOcupante(null);
            pecaDominada.setLocalizacao(null);
        }
        
        // retorna verdadeiro se há sequência de jogada válida, ou falso,
        // se a jogada estiver concluída
        return jogadasValidas.size() > 0;
    }

    public boolean haSequenciaRestanteJogada() {
        int contagemSequenciasRestante = 0;
        for (Jogada jogada : jogadasValidas)
            contagemSequenciasRestante += jogada.getPercurso().size() > 0 ? 1 : 0;
        return contagemSequenciasRestante > 0;
    }

}
