package br.com.alexromanelli.android.jogodedamas.dinamica;

public class Casa {
	
	public enum Direcao {
		Nordeste,
		Sudeste,
		Sudoeste,
		Noroeste
	}
	
	public static Direcao[] DIRECOES_PERMITIDAS = new Direcao[] {
		Direcao.Nordeste,
		Direcao.Sudeste,
		Direcao.Sudoeste,
		Direcao.Noroeste
	};

	private int coluna;
	private int linha;
	private Casa vizinhaNordeste;
	private Casa vizinhaSudeste;
	private Casa vizinhaSudoeste;
	private Casa vizinhaNoroeste;
	
	private boolean parteDeJogadaValida;
	
	private Peca ocupante;

	public Casa(int coluna, int linha, Casa vizinhaNordeste,
			Casa vizinhaSudeste, Casa vizinhaSudoeste, Casa vizinhaNoroeste) {
		super();
		this.coluna = coluna;
		this.linha = linha;
		this.vizinhaNordeste = vizinhaNordeste;
		this.vizinhaSudeste = vizinhaSudeste;
		this.vizinhaSudoeste = vizinhaSudoeste;
		this.vizinhaNoroeste = vizinhaNoroeste;
		this.ocupante = null;
		
		this.parteDeJogadaValida = false;
	}

	public int getColuna() {
		return coluna;
	}

	public void setColuna(int coluna) {
		this.coluna = coluna;
	}

	public int getLinha() {
		return linha;
	}

	public void setLinha(int linha) {
		this.linha = linha;
	}

	public Casa getVizinhaNordeste() {
		return vizinhaNordeste;
	}

	public void setVizinhaNordeste(Casa vizinhaNordeste) {
		this.vizinhaNordeste = vizinhaNordeste;
	}

	public Casa getVizinhaSudeste() {
		return vizinhaSudeste;
	}

	public void setVizinhaSudeste(Casa vizinhaSudeste) {
		this.vizinhaSudeste = vizinhaSudeste;
	}

	public Casa getVizinhaSudoeste() {
		return vizinhaSudoeste;
	}

	public void setVizinhaSudoeste(Casa vizinhaSudoeste) {
		this.vizinhaSudoeste = vizinhaSudoeste;
	}

	public Casa getVizinhaNoroeste() {
		return vizinhaNoroeste;
	}

	public void setVizinhaNoroeste(Casa vizinhaNoroeste) {
		this.vizinhaNoroeste = vizinhaNoroeste;
	}
	
	public Casa getVizinhaDirecao(Direcao direcao) {
		switch (direcao) {
		case Nordeste:
			return getVizinhaNordeste();
		case Sudeste:
			return getVizinhaSudeste();
		case Sudoeste:
			return getVizinhaSudoeste();
		case Noroeste:
			return getVizinhaNoroeste();
		}
		return null;
	}

	public Peca getOcupante() {
		return ocupante;
	}

	public void setOcupante(Peca ocupante) {
		this.ocupante = ocupante;
	}

    public boolean isParteDeJogadaValida() {
        return parteDeJogadaValida;
    }

    public void setParteDeJogadaValida(boolean parteDeJogadaValida) {
        this.parteDeJogadaValida = parteDeJogadaValida;
    }

	
}
