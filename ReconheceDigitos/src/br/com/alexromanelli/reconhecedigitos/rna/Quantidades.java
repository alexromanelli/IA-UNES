package br.com.alexromanelli.reconhecedigitos.rna;

/**
 * A classe Quantidades apenas retém os dados necessários para configurar uma RNA
 * feed-forward com conexões entre todos os neurônios de uma camada e todos os neurônios
 * da camada seguinte.
 * 
 * @author alexandre
 *
 */
public class Quantidades {
	private static int CAMADAS_OCULTAS = 1;
	private static int NEURONIOS_DE_ENTRADA = 784;
	private static int NEURONIOS_DE_SAIDA = 10;
	private static int NEURONIOS_DE_CAMADA_OCULTA = 120;

	public static int getCamadasOcultas() {
		return CAMADAS_OCULTAS;
	}

	public static int getNeuroniosDeEntrada() {
		return NEURONIOS_DE_ENTRADA;
	}

	public static int getNeuroniosDeSaida() {
		return NEURONIOS_DE_SAIDA;
	}

	public static int getNeuroniosDeCamadaOculta() {
		return NEURONIOS_DE_CAMADA_OCULTA;
	}

	public static void setNeuroniosDeCamadaOculta(int qtdeNeuroniosCamadaOculta) {
		NEURONIOS_DE_CAMADA_OCULTA = qtdeNeuroniosCamadaOculta;
	}

	public static void setCamadasOcultas(int qtdeCamadasOcultas) {
		CAMADAS_OCULTAS = qtdeCamadasOcultas;
	}

	public static void setNeuroniosDeSaida(int qtdeNeuroniosCamadaSaida) {
		NEURONIOS_DE_SAIDA = qtdeNeuroniosCamadaSaida;
	}

	public static void setNeuroniosDeEntrada(int qtdeNeuroniosCamadaEntrada) {
		NEURONIOS_DE_ENTRADA = qtdeNeuroniosCamadaEntrada;
	}

}
