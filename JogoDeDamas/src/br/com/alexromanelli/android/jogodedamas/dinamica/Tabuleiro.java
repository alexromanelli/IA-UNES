package br.com.alexromanelli.android.jogodedamas.dinamica;

public class Tabuleiro {

	private Casa[][] tabuleiro;
	
	public Tabuleiro() {
		tabuleiro = new Casa[8][8]; // tabuleiro 8x8
		configuraTabuleiro();
	}

    private void configuraTabuleiro() {
		// inicia instâncias
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				if ((i % 2 == 0 && j % 2 == 1) || (i % 2 == 1 && j % 2 == 0))
					tabuleiro[i][j] = new Casa(j, i, null, null, null, null);
				else
					tabuleiro[i][j] = null;

		// ajusta conexões a vizinhas
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++) {
				if ((i % 2 == 0 && j % 2 == 1) || (i % 2 == 1 && j % 2 == 0)) {
					Casa vizinhaNordeste = (i > 0) ? ((j < 7) ? tabuleiro[i - 1][j + 1] : null) : null; 
					Casa vizinhaSudeste  = (i < 7) ? ((j < 7) ? tabuleiro[i + 1][j + 1] : null) : null; 
					Casa vizinhaSudoeste = (i < 7) ? ((j > 0) ? tabuleiro[i + 1][j - 1] : null) : null; 
					Casa vizinhaNoroeste = (i > 0) ? ((j > 0) ? tabuleiro[i - 1][j - 1] : null) : null;
					
					tabuleiro[i][j].setVizinhaNordeste(vizinhaNordeste);
					tabuleiro[i][j].setVizinhaSudeste(vizinhaSudeste);
					tabuleiro[i][j].setVizinhaSudoeste(vizinhaSudoeste);
					tabuleiro[i][j].setVizinhaNoroeste(vizinhaNoroeste);
				}
			}
	}
	
	public Casa getCasa(int linha, int coluna) {
		return tabuleiro[linha][coluna];
	}

    public String getRepresentacaoTexto() {
        char[][] texto = new char[8][8];
        for (int l = 0; l < 8; l++)
            for (int c = 0; c < 8; c++)
                if (getCasa(l, c) == null) {
                    texto[l][c] = ' ';
                } else if (getCasa(l, c).getOcupante() == null) {
                    texto[l][c] = '+';
                } else if (getCasa(l, c).getOcupante().getCor() == Peca.CorPeca.Branca) {
                    if (getCasa(l, c).getOcupante().getEstado() == Peca.EstadoPeca.Pedra) {
                        texto[l][c] = 'b';
                    } else {
                        texto[l][c] = 'B';
                    }
                } else if (getCasa(l, c).getOcupante().getCor() == Peca.CorPeca.Preta) {
                    if (getCasa(l, c).getOcupante().getEstado() == Peca.EstadoPeca.Pedra) {
                        texto[l][c] = 'p';
                    } else {
                        texto[l][c] = 'P';
                    }
                }

        StringBuilder s = new StringBuilder("");
        s.append(".\n+ - - - - - - - - +\n");
        for (int l = 0; l < 8; l++) {
            s.append("|");
            for (int c = 0; c < 8; c++)
                s.append(" ").append(texto[l][c]);
            s.append(" |\n");
        }
        s.append("+ - - - - - - - - +");

        return s.toString();
    }
}
