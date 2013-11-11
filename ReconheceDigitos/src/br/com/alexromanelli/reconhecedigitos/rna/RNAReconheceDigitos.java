package br.com.alexromanelli.reconhecedigitos.rna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.Random;
import br.com.alexromanelli.reconhecedigitos.model.HandwrittenDigitSetReader;

/**
 * A classe RNAReconheceDigitos implementa uma rede neural artificial (RNA) destinada à
 * atividade de reconhecimento de padrões para identificação de dígitos em figuras (mapas
 * de pixels).<br/>
 * <br/>
 * A estrutura da RNA é feed-forward com cada um dos neurônios de uma camada
 * conectados a todos os neurônios da camada seguinte. A função de ativação dos neurônios
 * é sigmóide, e os sinais de saída são interpretados como bits, sendo 1 para o maior valor
 * de saída de um neurônio da camada de saída, e 0 para os outros neurônios desta camada.<br/>
 * <br/>
 * O aprendizado da RNA é realizado com back-propagation, usando uma taxa de aprendizado
 * informada pelo usuário. A configuração da rede, incluindo o número de camadas e de neurônios
 * por camada, além dos pesos das conexões entre neurônios, é salva para um arquivo
 * externo, para ser usado posteriormente em testes de reconhecimento.<br/>
 * <br/>
 * Em instâncias voltadas para o reconhecimento de padrões, as quantidades de camadas e de
 * neurônios por camada, bem como os pesos das conexões entre neurônios, são carregados de
 * um arquivo externo.<br/>
 * <br/>
 * As instâncias de treinamento e de testes de reconhecimento usam como entradas de dados
 * arquivos de exemplos do NIST. Para obter mais informações, consulte: 
 * http://yann.lecun.com/exdb/mnist/
 * <br/>
 * <br/>
 * @author alexandre
 *
 */
public class RNAReconheceDigitos {
	
	private static final int TAMANHO_QUADRADO_ENTRADA = 28; // figura de 28x28 pixels
	
	private double taxaDeAprendizado;
	
	private String arquivoEntradasTreinamento;
	private String arquivoResultadosTreinamento;
	private String arquivoEntradasTestes;
	private String arquivoResultadosTestes;
	private String arquivoRna;

	public enum OpcaoInicializacaoDePesos {
		PesosAleatoriosParaTreinamento, CarregarDeArquivo
	}

	/**
	 * Vetor bidimensional de neurônios artificiais.
	 * Na primeira dimensão estão representadas as camadas. Cada elemento de uma camada
	 * é um neurônio desta.
	 */
	private Neuronio[][] rna;
	
	public RNAReconheceDigitos(OpcaoInicializacaoDePesos opcao) throws IOException {
		taxaDeAprendizado = 0.3d; // valor padrão que não está sendo usado nesta versão.
		carregarPesos(opcao);
	}
	
	public void carregarPesos(OpcaoInicializacaoDePesos opcao) throws IOException {
		switch (opcao) {
		case PesosAleatoriosParaTreinamento:
			Random rGen = new Random(System.currentTimeMillis());

			for (int i = 0; i < Quantidades.getCamadasOcultas() + 1; i++)
				for (Neuronio n : rna[i])
					for (ConexaoEntreNeuronios c : n.getConexoesSaida()) {
						double sinal = rGen.nextDouble() >= 0.5d ? 1.0d : -1.0d;
						c.setPeso(sinal * rGen.nextDouble() / 10.0d);
					}
			break;
			
		case CarregarDeArquivo:
			carregarRnaDeArquivo();
			break;

		}
	}

	public RNAReconheceDigitos(OpcaoInicializacaoDePesos op, double taxaAprendizado,
			String arquivoEntradaTreino, String arquivoResultadosTreino) throws IOException {

		inicializaVetorRna();
		organizaEstruturaRna();
		
		this.taxaDeAprendizado = taxaAprendizado;
		this.arquivoEntradasTreinamento = arquivoEntradaTreino;
		this.arquivoResultadosTreinamento = arquivoResultadosTreino;

		carregarPesos(op);
	}

	public RNAReconheceDigitos(OpcaoInicializacaoDePesos op, String arquivoRna,
			String arquivoEntradaTestes, String arquivoResultadosTestes) throws IOException {

		this.arquivoRna = arquivoRna;
		this.arquivoEntradasTestes = arquivoEntradaTestes;
		this.arquivoResultadosTestes = arquivoResultadosTestes;
		
		carregarPesos(op);
	}

	/**
	 * Este método inicializa o vetor bidimensional da RNA e as instâncias de neurônios deste vetor.
	 */
	private void inicializaVetorRna() {
		// define dimensões
		rna = new Neuronio[Quantidades.getCamadasOcultas() + 2][]; // + 2: entrada e saída
		rna[0] = new Neuronio[Quantidades.getNeuroniosDeEntrada()];
		for (int i = 1; i <= Quantidades.getCamadasOcultas(); i++)
			rna[i] = new Neuronio[Quantidades.getNeuroniosDeCamadaOculta()];
		rna[Quantidades.getCamadasOcultas() + 1] = new Neuronio[Quantidades.getNeuroniosDeSaida()];
		
		// inicializa elementos
		for (int i = 0; i < Quantidades.getCamadasOcultas() + 2; i++)
			for (int j = 0; j < rna[i].length; j++)
				rna[i][j] = new Neuronio(j);
	}

	/**
	 * Este método configura as conexões entre os neurônios. Esta atividade deve anteceder o
	 * carregamento dos pesos da RNA.
	 */
	private void organizaEstruturaRna() {
		for (int i = 0; i < Quantidades.getCamadasOcultas() + 1; i++)
			for (Neuronio no : rna[i])
				for (Neuronio nd : rna[i + 1]) {
					ConexaoEntreNeuronios c = new ConexaoEntreNeuronios(no, nd, 0.0d);
					no.addConexaoSaida(c);
					nd.addConexaoEntrada(c);
				}
	}

	/**
	 * Define um sinal de entrada de um neurônio da camada de entrada.
	 * 
	 * @param ind O índice do neurônio da camada de entrada que receberá o sinal.
	 * @param sinal O valor sinalizado para o neurônio da camada de entrada.
	 */
	public void setSinalEntrada(int ind, double sinal) {
		rna[0][ind].setSinalEntrada(sinal);
	}

	/**
	 * Define um sinal de entrada de um neurônio da camada de entrada.
	 * Esta sobrecarga do método permite o uso de indexadores bidimensionais, para facilitar
	 * a rotina de configuração da entrada com um mapa de pixels.
	 * 
	 * @param i O número da linha da figura bidimensional que contém o pixel que é o sinal
	 * de entrada.
	 * @param j O número da coluna da figura bidimensional que contém o pixel que é o sinal
	 * de entrada.
	 * @param sinal O valor armazenado no pixel da figura bidimensional que está na linha i
	 * e na coluna j.
	 */
	public void setSinalEntrada(int i, int j, double sinal) {
		int ind = i * TAMANHO_QUADRADO_ENTRADA + j;
		setSinalEntrada(ind, sinal);
	}
	
	/**
	 * Define os sinais de entrada de todos os neurônios da camada de entrada. Esta sobrecarga
	 * do método permite que uma figura completa seja enviada de uma vez para a RNA.
	 * 
	 * @param sinal O vetor bidimensional que contém os pixels da figura a ser reconhecida.
	 */
	public void setSinalEntrada(int[][] sinal) {
		for (int i = 0; i < sinal.length; i++)
			for (int j = 0; j < sinal[i].length; j++)
				setSinalEntrada(i, j, sinal[i][j]);
	}

	/**
	 * Este método executa a propagação de sinais pela RNA. Deve ser executado após a definição
	 * dos sinais de entrada da rede. E deve anteceder a interpretação da saída da rede.
	 */
	public void computaPropagacaoDeSinais() {
		for (int i = 1; i < Quantidades.getCamadasOcultas() + 2; i++)
			for (Neuronio n : rna[i])
				n.propagaSinal();
	}
	
	/**
	 * Interpreta os sinais de saída dos neurônios da camada de saída, para identificar qual
	 * foi o valor reconhecido na figura de entrada. Este método busca o neurônio da camada de
	 * saída com o maior sinal de saída. O índice deste neurônio é usado como o valor reconhecido.
	 * 
	 * @return O número inteiro que representa o valor reconhecido pela RNA.
	 */
	public int interpretaSaida() {
		double max = Double.MIN_VALUE;
		int posMax = -1;
		int indCamadaSaida = Quantidades.getCamadasOcultas() + 1;
		for (int i = 0; i < Quantidades.getNeuroniosDeSaida(); i++) {
			double sinal = rna[indCamadaSaida][i].getSinalSaida();
			if (sinal > max) {
				max = sinal;
				posMax = i;
			}
		}
		
		return posMax;
	}
	
	/**
	 * Este método é um facilitador para a utilização da RNA de reconhecimento de dígitos. Permite
	 * que seja informado um vetor bidimensional de inteiros que representam os pixels da figura a 
	 * ser reconhecida. Esse vetor bidimensional é definido como o conjunto de sinais de entrada da
	 * rede. Em seguida, é feita a propagação de sinais. Por fim, os sinais de saída dos neurônios
	 * da camada de saída são interpretados e o valor reconhecido é retornado como resultado.
	 * 
	 * @param digito O vetor bidimensional de inteiros que representam os pixels da figura a ser
	 * reconhecida.
	 * @return O número inteiro que representa o dígito reconhecido na figura.
	 */
	public int executaReconhecimento(int[][] digito) {
		setSinalEntrada(digito);
		computaPropagacaoDeSinais();
		return interpretaSaida();
	}
	
	/**
	 * Este método faz uso do arquivo MNIST para testes de reconhecimento de dígitos. Resumidamente,
	 * o processo consiste em ler cada exemplo de teste dos arquivos, executar o reconhecimento da
	 * RNA e informar o resultado na tela do console. Ao término, um resumo dos resultados é apresentado.
	 * 
	 * @throws IOException Caso haja algum erro na leitura dos exemplos, uma exceção é lançada.
	 */
	public void executaTestesDeReconhecimento() throws IOException {
		HandwrittenDigitSetReader leitorTestes = 
				new HandwrittenDigitSetReader(arquivoEntradasTestes, arquivoResultadosTestes);
		leitorTestes.processaArquivos();
		
		NumberFormat nfT = NumberFormat.getInstance();
		nfT.setMaximumFractionDigits(2);
		nfT.setMinimumFractionDigits(2);
		nfT.setMinimumIntegerDigits(2);
		
		NumberFormat nfQ = NumberFormat.getIntegerInstance();
		nfQ.setMinimumIntegerDigits(5);
		
		int erros = 0;
		int acertos = 0;
		double taxaAcerto = 0.0d;
		int[] errosDigito = new int[10];

		StringBuilder msg = new StringBuilder();
		
		for (int teste = 0; teste < leitorTestes.getNumeroDeItens(); teste++) {
			msg.delete(0, msg.length());
			msg.append(" Teste nº ");
			msg.append(nfQ.format(teste + 1));
			msg.append(" | ");
			
			Exemplo e = leitorTestes.leExemplo();
			int valorReconhecido = executaReconhecimento(e.getFigura());
			
			msg.append(valorReconhecido == e.getSignificado() ?
					("Dígito " + valorReconhecido   + " reconhecido corretamente.         ") :
					("Dígito " + e.getSignificado() + " reconhecido incorretamente como " + valorReconhecido + "."));
			
			if (valorReconhecido == e.getSignificado())
				acertos++;
			else {
				erros++;
				errosDigito[e.getSignificado()]++;
			}
			taxaAcerto = acertos * 100.0d / (erros + acertos);
			
			msg.append(" | erros: ");
			msg.append(nfQ.format(erros));
			msg.append(" | acertos: ");
			msg.append(nfQ.format(acertos));
			msg.append(" [");
			msg.append(nfT.format(taxaAcerto));
			msg.append("%]");
			
			System.out.println(msg);
		}
		
		System.out.println("\n\n---> Resumo de execução:\n\n Quantidade de testes: " + leitorTestes.getNumeroDeItens() + "\n" +
		        " Erros:           " + erros + "\n Acertos:         " + acertos + "\n Taxa de acertos: " + nfT.format(taxaAcerto) + "%\n" +
				" ---\n Erros por dígito:");
		
		for (int d = 0; d <= 9; d++)
			System.out.println("   " + d + " : " + errosDigito[d]);
		
		System.out.println(" ---\n");
		leitorTestes.close();
	}

	/**
	 * Este método executa o algoritmo de aprendizado através de retro-propagação de erros para 
	 * ajustes de pesos (back-propagation learning). São realizadas "épocas" de treinamento até
	 * que o valor absoluto dos ajustes efetuados nas conexões seja inferior a um milhão, ou até
	 * que se tenha passado por dez "épocas".
	 *   
	 * @throws IOException
	 */
	public void executaAprendizadoComBaseDeTestes() throws IOException {
		boolean ajustesExpressivos = true;
		int contEpocas = 1;
		
		// formatador para valores percentuais
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMinimumIntegerDigits(2);
		
		// formatador para contador de "épocas"
		NumberFormat nfE = NumberFormat.getIntegerInstance();
		nfE.setMinimumIntegerDigits(2);
		
		HandwrittenDigitSetReader leitorTreinamento = 
				new HandwrittenDigitSetReader(arquivoEntradasTreinamento, arquivoResultadosTreinamento);
		leitorTreinamento.processaArquivos();

		do {
			String ep = nfE.format(contEpocas);
			System.out.println("\n---> iniciando época!\n");
			int acertos = 0;
			int erros = 0;
			
			double ajustesEpoca = 0.0d;
			
			for (int indExemplo = 0; 
					indExemplo < leitorTreinamento.getNumeroDeItens();
					indExemplo++) {
				Exemplo exemplo = leitorTreinamento.leExemplo();
				
				setSinalEntrada(exemplo.getFigura());

				computaPropagacaoDeSinais();
				
				int resultado = interpretaSaida();
				String msg = "";
				if (resultado == exemplo.getSignificado()) {
					msg = ep + " | Número " + resultado                + " identificado corretamente.         ";
					acertos++;
				}
				else {
					msg = ep + " | Número " + exemplo.getSignificado() + " identificado incorretamente como " + resultado + ".";
					erros++;
				}
				String taxaAcerto = nf.format(acertos * 100.0d / (acertos + erros));
				System.out.println(msg + " | erros: " + erros + " | acertos: " + acertos + " [" + taxaAcerto + "%]");

				// calcula os desvios de resultados observados na camada de saída
				for (int i = 0; i < Quantidades.getNeuroniosDeSaida(); i++) {
					Neuronio n = rna[Quantidades.getCamadasOcultas() + 1][i];
					n.calculaDesvio(exemplo.getVetorSignificado()[i]);
				}
				
				// calcula os desvios das demais camadas
				for (int l = Quantidades.getCamadasOcultas(); l >= 0; l--)
					for (int j = 0; j < rna[l].length; j++) {
						rna[l][j].calculaDesvio();
						
						// ajusta pesos das conexões para a camada seguinte
						for (ConexaoEntreNeuronios c : rna[l][j].getConexoesSaida()) {
							double novoPeso = c.getPeso() + taxaDeAprendizado *
									c.getOrigem().getSinalSaida() * 
									c.getDestino().getDesvio();
							double ajuste = Math.abs(c.getPeso() - novoPeso);
							c.setPeso(novoPeso);
							
							ajustesEpoca += ajuste;
						}
					}
			}
			
			leitorTreinamento.close();
			
			System.out.println(" --> ajustes efetuados na " + contEpocas + "ª época: " + ajustesEpoca);
			
			ajustesExpressivos = ajustesEpoca >= 1000000.0d;
			contEpocas++;
			
			// muda a sequência de exemplos (visa tornar o processo mais aleatório)
			leitorTreinamento.embaralhaExemplos();
			
		} while (ajustesExpressivos && contEpocas <= 10);
	}
	
	/**
	 * Escreve um arquivo texto com os dados de configuração desta instância da RNA, para possibilitar
	 * seu uso posterior.
	 * 
	 * @throws IOException Caso ocorra algum erro na escrita do arquivo, uma exceção é lançada.
	 */
	public void salvaConfiguracaoRNA() throws IOException {
		String nomeArquivo = "rna.config" + 
				".ne" + Quantidades.getNeuroniosDeEntrada() +
				".co" + Quantidades.getCamadasOcultas() +
				".nco" + Quantidades.getNeuroniosDeCamadaOculta() +
				".ns" + Quantidades.getNeuroniosDeSaida() +
				".ta" + ((int)Math.round(this.taxaDeAprendizado * 100.0d));
		
		BufferedWriter arquivo = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nomeArquivo)));
		arquivo.write("[neuronios de entrada]\n");
		arquivo.write(Quantidades.getNeuroniosDeEntrada() + "\n");
		arquivo.write("[camadas ocultas]\n");
		arquivo.write(Quantidades.getCamadasOcultas() + "\n");
		arquivo.write("[neuronios por camada oculta]\n");
		arquivo.write(Quantidades.getNeuroniosDeCamadaOculta() + "\n");
		arquivo.write("[neuronios de saida]\n");
		arquivo.write(Quantidades.getNeuroniosDeSaida() + "\n");
		
		arquivo.write("[conexoes entre neuronios]\n");

		for (int i = 0; i < rna.length - 1; i++)
			for (int j = 0; j < rna[i].length; j++) {
				Neuronio n = rna[i][j];
				
				for (ConexaoEntreNeuronios c : n.getConexoesSaida()) {
					int camadaOrigem = i;
					int indiceOrigem = j;
					int camadaDestino = i + 1;
					int indiceDestino = c.getDestino().getIndice();
					double peso = c.getPeso();
					
					arquivo.write(camadaOrigem + " " + indiceOrigem + " " + camadaDestino + " " + indiceDestino + " " + peso + "\n");
				}
			}
		
		arquivo.close();
	}

	/**
	 * Faz a carga de uma RNA de um arquivo texto. O arquivo deve ter sido criado com a mesma formatação
	 * usada pelo método "salvaConfiguracaoRNA".<br/>
	 * <br/>
	 * Este método realiza a leitura dos parâmetros de configuração do arquivo informado no construtor,
	 * inicializa o vetor de RNA, organiza a estrutura de interconexões de neurônios e carrega os pesos
	 * destas.<br/>
	 * <br/>
	 * Inclui, ao término, uma chamada à coleta de lixo.
	 *  
	 * @throws IOException Caso ocorra algum erro na leitura do arquivo, uma exceção é lançada.
	 */
	private void carregarRnaDeArquivo() throws IOException {
		BufferedReader config = new BufferedReader(new InputStreamReader(new FileInputStream(arquivoRna)));
		
		config.readLine(); // [neuronios de entrada]
		int qtdeNeuroniosEntrada = Integer.parseInt(config.readLine());
		config.readLine(); // [camadas ocultas]
		int qtdeCamadasOcultas = Integer.parseInt(config.readLine());
		config.readLine(); // [neuronios por camada oculta]
		int qtdeNeuroniosCamadaOculta = Integer.parseInt(config.readLine());
		config.readLine(); // [neuronios de saida]
		int qtdeNeuroniosSaida = Integer.parseInt(config.readLine());
		config.readLine(); // [conexoes entre neuronios]

		Quantidades.setNeuroniosDeEntrada(qtdeNeuroniosEntrada);
		Quantidades.setNeuroniosDeSaida(qtdeNeuroniosSaida);
		Quantidades.setCamadasOcultas(qtdeCamadasOcultas);
		Quantidades.setNeuroniosDeCamadaOculta(qtdeNeuroniosCamadaOculta);
		
		inicializaVetorRna();
		organizaEstruturaRna();
		
		int qtdeConexoes = qtdeNeuroniosEntrada * qtdeNeuroniosCamadaOculta +
				(qtdeCamadasOcultas - 1) * qtdeNeuroniosCamadaOculta * qtdeNeuroniosCamadaOculta +
				qtdeNeuroniosCamadaOculta * qtdeNeuroniosSaida;
		for (int i = 0; i < qtdeConexoes; i++) {
			String[] valores = config.readLine().split(" ");
			int camadaOrigem = Integer.parseInt(valores[0]);
			int indOrigem = Integer.parseInt(valores[1]);
			//int camadaDestino = Integer.parseInt(valores[2]);
			int indDestino = Integer.parseInt(valores[3]);
			double peso = Double.parseDouble(valores[4]);
			
			rna[camadaOrigem][indOrigem].getConexoesSaida().get(indDestino).setPeso(peso);
		}
		
		config.close();
		
		System.gc();
	}

}
