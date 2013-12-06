package br.com.alexromanelli.android.jogodedamas.dinamica;

public class ConjuntoJogador {

    public enum TipoJogador {
		Humano,
		IA,
		DispositivoRemoto
	}
	
	public enum BaseJogador {
		Alto,
		Baixo
	}
	
	private TipoJogador tipoJogador;
	private Peca.CorPeca corPecasJogador;
	private Peca[] conjuntoPecas;
	private BaseJogador baseJogador;
	
	public ConjuntoJogador(TipoJogador tipoJogador, Peca.CorPeca corPecasJogador,
			BaseJogador baseJogador, Tabuleiro tabuleiro) {
		this.tipoJogador = tipoJogador;
		this.corPecasJogador = corPecasJogador;
		this.baseJogador = baseJogador;
		
		conjuntoPecas = new Peca[12];
		for (int i = 0; i < 12; i++) {
			int linha = (baseJogador == BaseJogador.Alto) ? 
					(i / 4) : 
					(7 - i / 4);
			int coluna = 2 * (i % 4) + (1 - linha % 2);
			
			conjuntoPecas[i] = new Peca(corPecasJogador, tabuleiro.getCasa(linha, coluna), baseJogador);
		}
	}

    public ConjuntoJogador(ConjuntoJogador cj) {
        this.tipoJogador = cj.getTipoJogador();
        this.corPecasJogador = cj.getCorPecasJogador();
        this.conjuntoPecas = new Peca[cj.getConjuntoPecas().length];
        for (int i = 0; i < cj.getConjuntoPecas().length; i++)
            this.conjuntoPecas[i] = new Peca(cj.getConjuntoPecas()[i]);
        this.baseJogador = cj.getBaseJogador();
    }

	public TipoJogador getTipoJogador() {
		return tipoJogador;
	}

	public Peca.CorPeca getCorPecasJogador() {
		return corPecasJogador;
	}

	public Peca[] getConjuntoPecas() {
		return conjuntoPecas;
	}

	public BaseJogador getBaseJogador() {
		return baseJogador;
	}

    public int getQuantidadePorEstado(Peca.EstadoPeca estado) {
        int qtde = 0;

        for (Peca p : conjuntoPecas)
            if (p.getEstado() == estado)
                qtde++;

        return qtde;
    }

}
