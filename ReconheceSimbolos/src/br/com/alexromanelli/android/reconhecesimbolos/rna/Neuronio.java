package br.com.alexromanelli.android.reconhecesimbolos.rna;

import java.util.ArrayList;

public class Neuronio {
	private int indice;
	private ArrayList<ConexaoEntreNeuronios> conexoesEntrada;
	private double valorEntrada;
	private double sinalSaida;
	private ArrayList<ConexaoEntreNeuronios> conexoesSaida;
	private double desvio;
	
	public Neuronio(int indice) {
		super();
		this.indice = indice;
		conexoesEntrada = new ArrayList<ConexaoEntreNeuronios>();
		conexoesSaida = new ArrayList<ConexaoEntreNeuronios>();
	}
	
	public int getIndice() {
		return indice;
	}
	
	public double getDesvio() {
		return desvio;
	}

	public void calculaEntrada() {
		double entrada = 0.0d;
		for (ConexaoEntreNeuronios c : conexoesEntrada) {
			entrada += c.getPeso() * c.getOrigem().getSinalSaida();
		}

		valorEntrada = entrada;
	}
	
	/**
	 * Este método deve ser usado apenas para informar os sinais da
	 * camada de entrada.
	 */
	public void setSinalEntrada(double sinal) {
		sinalSaida = sinal;
	}
	
	private static double funcaoSigmoide(double x) {
		return 1 / (1 + Math.exp(-x));
	}
	
	/**
	 * Aplica a função sigmóide sobre o valor da entrada e registra
	 * o resultado no atributo sinalSaida.
	 * 
	 * @return o valor calculado para o sinal de saída do neurônio.
	 */
	public double aplicaFuncaoAtivacao() {
		sinalSaida = funcaoSigmoide(valorEntrada);
		return sinalSaida;
	}

	public static double aplicaFuncaoAtivacaoDerivada(double x) {
		// g(x) = sigmoide(x) -> g'(x) = g(1 - g(x))
		return funcaoSigmoide(1 - funcaoSigmoide(x));
	}
	
	public void propagaSinal() {
		calculaEntrada();
		aplicaFuncaoAtivacao();
	}

	public void calculaDesvio(double esperado) {
		desvio = aplicaFuncaoAtivacaoDerivada(valorEntrada) * (esperado - sinalSaida);
	}
	
	public void calculaDesvio() {
		double somaPesoDesvio = 0.0d;
		for (ConexaoEntreNeuronios c : conexoesSaida)
			somaPesoDesvio += c.getPeso() * c.getDestino().getDesvio();
		desvio = aplicaFuncaoAtivacaoDerivada(valorEntrada) * somaPesoDesvio;
	}
	
	public ArrayList<ConexaoEntreNeuronios> getConexoesEntrada() {
		return conexoesEntrada;
	}

	public void addConexaoEntrada(ConexaoEntreNeuronios conexao) {
		this.conexoesEntrada.add(conexao);
	}

	public double getValorEntrada() {
		return valorEntrada;
	}
	
	public double getSinalSaida() {
		return sinalSaida;
	}

	public ArrayList<ConexaoEntreNeuronios> getConexoesSaida() {
		return conexoesSaida;
	}

	public void addConexaoSaida(ConexaoEntreNeuronios conexao) {
		this.conexoesSaida.add(conexao);
	}

}
