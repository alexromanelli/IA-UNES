package br.com.alexromanelli.reconhecedigitos.rna;

public class Exemplo {
	private int[][] figura;
	private int significado;
	private int[] vetorSignificado;

	public Exemplo(int[][] figura, int significado) {
		super();
		this.figura = figura;
		this.significado = significado;
		vetorSignificado = new int[10];
		vetorSignificado[significado] = 1;
	}

	public int[][] getFigura() {
		return figura;
	}

	public void setFigura(int[][] figura) {
		this.figura = figura;
	}

	public int getSignificado() {
		return significado;
	}

	public void setSignificado(int significado) {
		this.significado = significado;
		vetorSignificado = new int[10];
		vetorSignificado[significado] = 1;
	}

	public int[] getVetorSignificado() {
		return vetorSignificado;
	}

}
