package br.com.alexromanelli.android.reconhecesimbolos.rna;

class ConexaoEntreNeuronios {
	private Neuronio origem;
	private Neuronio destino;
	private double peso;

	public ConexaoEntreNeuronios(Neuronio origem, Neuronio destino, double peso) {
		super();
		this.origem = origem;
		this.destino = destino;
		this.peso = peso;
	}

	public Neuronio getOrigem() {
		return origem;
	}

	public void setOrigem(Neuronio origem) {
		this.origem = origem;
	}

	public Neuronio getDestino() {
		return destino;
	}

	public void setDestino(Neuronio destino) {
		this.destino = destino;
	}

	public double getPeso() {
		return peso;
	}

	public void setPeso(double peso) {
		this.peso = peso;
	}
}
