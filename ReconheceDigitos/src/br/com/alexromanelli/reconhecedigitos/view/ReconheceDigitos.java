package br.com.alexromanelli.reconhecedigitos.view;

import java.io.IOException;

import br.com.alexromanelli.reconhecedigitos.model.HandwrittenDigitSetReader;
import br.com.alexromanelli.reconhecedigitos.rna.Quantidades;
import br.com.alexromanelli.reconhecedigitos.rna.RNAReconheceDigitos;
import br.com.alexromanelli.reconhecedigitos.rna.RNAReconheceDigitos.OpcaoInicializacaoDePesos;

/**
 * A classe ReconheceDigitos destina-se a oferecer uma interface para o usuário utilizar a RNA
 * de reconhecimento de dígitos implementada na classe RNAReconheceDigitos. O método "main"
 * permite o uso de parâmetros da linha de comandos para configurar a execução da rede neural
 * artificial, que pode ser, em resumo, para treinamento ou para testes de reconhecimento de
 * padrões. 
 * 
 * @author alexandre
 *
 */
public class ReconheceDigitos {

	private static RNAReconheceDigitos rna;

	/**
	 * Método público disponível para execução pelo usuário, através do SO. Sua operação pode
	 * assumir duas possíveis atividades:<br/>
	 * <ul>
	 * <li>Treinamento de uma rede neural artificial feed-forward;</li>
	 * <li>Testes de uma RNA (supostamente treinada com antecedência).</li>
	 * </ul>
	 * Parâmetros usados para configurar a execução:<br/>
	 * <pre>op=opcao </pre> indica a opção, que pode ser treino ou testes.<br/><br/>
	 * Se for testes, os parâmetros seguintes devem ser usados:<br/>
	 *     <>rna=f1</pre> indica o caminho do arquivo com as informações para configuração da RNA;<br/>
	 *     <pre>testes=f2</pre> indica o caminho do arquivo com os valores de entrada para os testes;<br/>
	 *     <pre>resultados=f3</pre> indica o caminho do arquivo com os resultados para os testes;<br/>
	 * <br/>
	 * <br/>
	 * Se for treino, os seguintes parâmetros devem ser usados:<br/>
	 *     <pre>entrada=n1 </pre> determina o número de neurônios da camada de entrada;<br/>
	 *     <pre>saida=n2</pre> determina o número de neurônios da camada de saída;<br/>
	 *     <pre>oculta=n3    </pre> determina o número de camadas ocultas;<br/>
	 *     <pre>tamanho_oculta=n4   </pre> determina o número de neurônios de cada camada oculta;<br/>
	 *     <pre>taxa_aprendizado=n5   </pre> determina a taxa (%) aplicada a cada ajuste de peso de conexão entre neurônios;<br/>
	 *     <pre>exemplos=path   </pre> indica o caminho do arquivo com os valores de entrada para o treinamento;<br/>
	 *     <pre>resultados=path </pre> indica o caminho do arquivo com os resultados para o treinamento.<br/>
	 * <br/>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean extrairAmostra = true;
		
		RNAReconheceDigitos.OpcaoInicializacaoDePesos op = OpcaoInicializacaoDePesos.CarregarDeArquivo;
		
		String arquivoRna = "";
		String arquivoEntradaTestes = "";
		String arquivoResultadosTestes = "";
		String arquivoEntradaTreino = "";
		String arquivoResultadosTreino = "";
		
		int qtdeNeuroniosCamadaEntrada = 0;
		int qtdeNeuroniosCamadaSaida = 0;
		int qtdeCamadasOcultas = 0;
		int qtdeNeuroniosCamadaOculta = 0;
		double taxaAprendizado = 0.0d;
		
		if (args.length >= 1) {
			if (args[0].split("=")[0].equals("op")) {
				if (args[0].split("=")[1].equals("testes")) {
					op = OpcaoInicializacaoDePesos.CarregarDeArquivo;
					
					if (args.length >= 4) {
						for (String arg : args) {
							if (arg.split("=")[0].equals("rna"))
								arquivoRna = arg.split("=")[1];
							if (arg.split("=")[0].equals("testes"))
								arquivoEntradaTestes = arg.split("=")[1];
							if (arg.split("=")[0].equals("resultados"))
								arquivoResultadosTestes = arg.split("=")[1];
						}
					}
				}
				else if (args[0].split("=")[1].equals("treino")) {
					op = OpcaoInicializacaoDePesos.PesosAleatoriosParaTreinamento;
					
					if (args.length >= 8) {
						for (String arg : args) {
							if (arg.split("=")[0].equals("entrada"))
								qtdeNeuroniosCamadaEntrada = Integer.parseInt(arg.split("=")[1]);
							if (arg.split("=")[0].equals("saida"))
								qtdeNeuroniosCamadaSaida = Integer.parseInt(arg.split("=")[1]);
							if (arg.split("=")[0].equals("oculta"))
								qtdeCamadasOcultas = Integer.parseInt(arg.split("=")[1]);
							if (arg.split("=")[0].equals("tamanho_oculta"))
								qtdeNeuroniosCamadaOculta = Integer.parseInt(arg.split("=")[1]);
							if (arg.split("=")[0].equals("taxa_aprendizado"))
								taxaAprendizado = Double.parseDouble(arg.split("=")[1]) / 100.0d;
							if (arg.split("=")[0].equals("exemplos"))
								arquivoEntradaTreino = arg.split("=")[1];
							if (arg.split("=")[0].equals("resultados"))
								arquivoResultadosTreino = arg.split("=")[1];
						}
						
						Quantidades.setNeuroniosDeEntrada(qtdeNeuroniosCamadaEntrada);
						Quantidades.setNeuroniosDeSaida(qtdeNeuroniosCamadaSaida);
						Quantidades.setCamadasOcultas(qtdeCamadasOcultas);
						Quantidades.setNeuroniosDeCamadaOculta(qtdeNeuroniosCamadaOculta);
					}
				}
				
			}
		}
		
		HandwrittenDigitSetReader.extrairAmostra = extrairAmostra;
		
		System.out.println("---\n----> Reconhecimento de dígitos com RNA!\n---\n");
		try {
			switch (op) {
			case PesosAleatoriosParaTreinamento:
				rna = new RNAReconheceDigitos(op, taxaAprendizado, arquivoEntradaTreino, arquivoResultadosTreino);
				rna.executaAprendizadoComBaseDeTestes();
				rna.salvaConfiguracaoRNA();
				break;
			case CarregarDeArquivo:
				rna = new RNAReconheceDigitos(op, arquivoRna, arquivoEntradaTestes, arquivoResultadosTestes);
				rna.executaTestesDeReconhecimento();
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
