package br.com.alexromanelli.android.reconhecesimbolos.rna;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import br.com.alexromanelli.android.reconhecesimbolos.EntradaDigito;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

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
	
	public enum OpcaoInicializacaoDePesos {
		PesosAleatoriosParaTreinamento, CarregarDeArquivo
	}

	/**
	 * Vetor bidimensional de neurônios artificiais.
	 * Na primeira dimensão estão representadas as camadas. Cada elemento de uma camada
	 * é um neurônio desta.
	 */
	private Neuronio[][] rna;
	
	private EntradaDigito ctx;

	public RNAReconheceDigitos(Context ctx, OpcaoInicializacaoDePesos opcao) throws IOException {
		this.ctx = (EntradaDigito) ctx;
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
			new LeitorArquivoRNA().execute(); // carrega em uma thread separada
			break;

		}
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
	
	Handler handler = new Handler();
	
	/**
	 * A classe LeitorArquivoRNA fornece um meio de executar o carregamento da RNA em uma thread
	 * separada. Ao completar a carga, é exibida uma mensagem em um Toast e o botão de reconhecimento
	 * torna-se habilitado para uso.
	 * 
	 * @author alexandre
	 *
	 */
	private class LeitorArquivoRNA extends AsyncTask<Void, Integer, Void> {
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				requisitarExibicaoBarraProgressoCargaRna();
				carregarRnaDeArquivo();
			} catch (IOException e) {
				this.cancel(true);
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			requisitarAvisoCancelamentoCargaRna();
		}

		@Override
		protected void onPostExecute(Void result) {
			requisitarHabilitacaoBotaoReconhecer();
			requisitarExibicaoMensagemConclusaoCargaRna();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int progresso = values[0];
			requisitarAtualizacaoProgressoCargaRna(progresso);
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
			BufferedReader config = new BufferedReader(new InputStreamReader(ctx.getAssets().open("rna.config.ne784.co1.nco100.ns10.ta10")));
			
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
			
			int percentualConcluido = 0;
			
			for (int i = 0; i < qtdeConexoes; i++) {
				String[] valores = config.readLine().split(" ");
				int camadaOrigem = Integer.parseInt(valores[0]);
				int indOrigem = Integer.parseInt(valores[1]);
				//int camadaDestino = Integer.parseInt(valores[2]);
				int indDestino = Integer.parseInt(valores[3]);
				double peso = Double.parseDouble(valores[4]);
				
				rna[camadaOrigem][indOrigem].getConexoesSaida().get(indDestino).setPeso(peso);
				
				// ajusta exibição progresso
				percentualConcluido = (int)Math.round(i * 100.0 / qtdeConexoes);
				if (percentualConcluido % 4 == 0)
					publishProgress(percentualConcluido);
			}
			
			config.close();
			
			System.gc();
		}
		
		private void requisitarExibicaoBarraProgressoCargaRna() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					ctx.exibeProgressoCargaRNA();
				}
			});
		}
		
		private void requisitarHabilitacaoBotaoReconhecer() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					ctx.habilitaBotaoReconhecer();
				}
			});
		}

		private void requisitarAvisoCancelamentoCargaRna() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					ctx.ocultaProgressoCargaRNA();
					ctx.desabilitaBotaoReconhecer();
					Toast.makeText(ctx, "Ocorreu algum erro ao carregar RNA. Chupa essa manga!", Toast.LENGTH_LONG).show();
				}
			});
		}
		
		private void requisitarExibicaoMensagemConclusaoCargaRna() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					ctx.ocultaProgressoCargaRNA();
					Toast.makeText(ctx, "RNA pronta!", Toast.LENGTH_LONG).show();
				}
			});
		}

		private void requisitarAtualizacaoProgressoCargaRna(final int progresso) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					ctx.atualizaProgressoCargaRna(progresso);
				}
			});
		}

	}

}
