package br.com.alexromanelli.reconhecedigitos.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;

import br.com.alexromanelli.reconhecedigitos.rna.Exemplo;

/**
 * Esta classe cuida da leitura de dois arquivos para treinamento ou testes de RNA para reconhecimento
 * de dígitos. Um dos arquivos contém figuras que devem ser reconhecidas, e o outro arquivo contém os
 * valores esperados para o reconhecimento. Os arquivos devem estar no formato estabelecido pelo NIST
 * para este propósito.<br/>
 * <br/>
 * Para mais detalhes, consulte: http://yann.lecun.com/exdb/mnist/
 *   
 * @author alexandre
 *
 */
public class HandwrittenDigitSetReader {

	private int numeroDeItens;
	private int numeroDeLinhas;
	private int numeroDeColunas;

	private BufferedInputStream imagens;
	private BufferedInputStream rotulos;

	private static boolean bufferCompleto = false;
	private static ArrayList<Exemplo> conjuntoDeDigitos;
	private static int posBuffer = 0;
	private static Exemplo exemploLido;
	
	public static boolean extrairAmostra = false;
	private boolean[] amostragem;
	
	private Random gen; 

	public HandwrittenDigitSetReader(String nomeArquivoImagens,
			String nomeArquivoRotulos) throws IOException {
		imagens = new BufferedInputStream(new FileInputStream(
				nomeArquivoImagens));
		rotulos = new BufferedInputStream(new FileInputStream(
				nomeArquivoRotulos));

		if (conjuntoDeDigitos == null) {
			conjuntoDeDigitos = new ArrayList<Exemplo>();
			bufferCompleto = false;
		}
		
		if (extrairAmostra) {
			amostragem = new boolean[10];
			for (int i = 0; i < 10; i++)
				amostragem[i] = false;
			gen = new Random(System.currentTimeMillis());
		}
	}

	public void processaArquivos() throws IOException {
		leNumeroMagico();
		leNumeroDeItens();
		leDimensoesDasImagens();
	}

	/**
	 * Como nesta implementação não é útil o número mágico, pois já se conhece a
	 * estrutura dos dados, basta passar pelos quatro bytes e ignorá-los.
	 * 
	 * @throws IOException
	 */
	private void leNumeroMagico() throws IOException {
		byte[] numeroMagico = new byte[4];
		imagens.read(numeroMagico);
		rotulos.read(numeroMagico);
	}

	private void leNumeroDeItens() throws IOException {
		int numRotulo = leInteiro4Bytes(rotulos);
		int numImagens = leInteiro4Bytes(imagens);
		if (numRotulo == numImagens)
			numeroDeItens = numRotulo;
	}

	public int getNumeroDeItens() {
		return numeroDeItens;
	}

	/**
	 * Este método auxilia na conversão de um número codificado em uma sequência de bytes para um
	 * número inteiro.
	 * 
	 * @param bytes Sequência de bytes. 
	 * @return O número inteiro codificado na sequência de bytes.
	 */
	private static int byteArrayToInt(int[] bytes) {
		int valor = 0;
		int base = 1;
		for (int i = bytes.length - 1; i >= 0; i--) {
			valor += bytes[i] * base;
			base *= 256;
		}
		return valor;
	}

	/**
	 * Este método auxilia na leitura de um número inteiro codificado em uma sequência de 4 bytes.
	 * 
	 * @param in O arquivo de onde os 4 bytes serão lidos para obter o número inteiro.
	 * @return O resultado da conversão da sequência de bytes para um número inteiro. Ou seja, retorna
	 * o número inteiro lido.
	 * @throws IOException Se houver algum erro na leitura do arquivo, uma exceção é lançada.
	 */
	private static int leInteiro4Bytes(BufferedInputStream in)
			throws IOException {
		int[] byteArray = new int[4];
		for (int i = 0; i < 4; i++) {
			byteArray[i] = in.read();
		}
		return byteArrayToInt(byteArray);
	}

	private void leDimensoesDasImagens() throws IOException {
		numeroDeLinhas = leInteiro4Bytes(imagens);
		numeroDeColunas = leInteiro4Bytes(imagens);
	}

	/**
	 * Faz a leitura de um exemplo completo dos arquivos. Ou seja, lê do arquivo de imagens uma 
	 * sequência de bytes que representam os pixels da figura a ser reconhecida, e lê um número
	 * do arquivo de rótulos que representa o resultado correto para o reconhecimento da figura.
	 * <br/>
	 * <br/>
	 * Os dados são armazenados em um objeto da classe Exemplo, e são armazenados em uma coleção,
	 * para evitar a necessidade de ler novamente do arquivo em caso de repetição de uso, em
	 * caso de treinamento de RNA.
	 *  
	 * @return O exemplo que foi lido dos arquivos.
	 * @throws IOException Se houver algum erro na leitura do arquivo, uma exceção é lançada.
	 */
	public Exemplo leExemplo() throws IOException {
		if (bufferCompleto) {
			exemploLido = conjuntoDeDigitos.get(posBuffer);
			posBuffer = ++posBuffer % numeroDeItens;
		} else {
			// lê um exemplo
			int significado = rotulos.read();
			if (exemploLido == null) {
				int[][] figura = new int[numeroDeLinhas][numeroDeColunas];
				exemploLido = new Exemplo(figura, significado);
			}
			exemploLido.setSignificado(significado);
			
			int[][] fig = new int[numeroDeLinhas][numeroDeColunas];
			
			for (int l = 0; l < numeroDeLinhas; l++)
				for (int c = 0; c < numeroDeColunas; c++) {
					int pixel = imagens.read();
					exemploLido.getFigura()[l][c] = pixel;
					fig[l][c] = pixel;
				}

			conjuntoDeDigitos.add(new Exemplo(fig, significado));
			bufferCompleto = conjuntoDeDigitos.size() == numeroDeItens;
			
			if (extrairAmostra && gen.nextBoolean()) {
				if (!amostragem[significado]) {
					geraVisualizacaoDigito(exemploLido);
					amostragem[significado] = true;
				}
			}
		}

		return exemploLido;
	}

	/**
	 * Este método gera uma figura representativa de um exemplo dos arquivos de valores. A
	 * figura é salva em formato PNG, com dimensões estendidas para facilitar a visualização.
	 * 
	 * @param ex O exemplo que será exportado para uma figura.
	 * @throws IOException Se houver algum problema na escrita do arquivo da figura, uma
	 * exceção é lançada.
	 */
	private void geraVisualizacaoDigito(Exemplo ex) throws IOException {
		int dimPx = 28 * 14 + 29;
		
		BufferedImage off_Image = 
				new BufferedImage(dimPx, dimPx, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = off_Image.createGraphics();
		
		// desenha grade
		for (int a = 0; a <= 28; a++) {
			g2.setPaint(Color.BLUE);
			g2.drawLine(15 * a, 0, 15 * a, dimPx - 1);
			g2.drawLine(0, 15 * a, dimPx - 1, 15 * a);
		}
		
		// pinta quadros
		for (int l = 0; l < 28; l++)
			for (int c = 0; c < 28; c++) {
				int valor = ex.getFigura()[l][c];
				Color cor = new Color(255 - valor, 255 - valor, 255 - valor);
				g2.setPaint(cor);
				g2.fillRect(c * 15 + 1, l * 15 + 1, 14, 14);
			}
		
		// salva em arquivo
		File outputfile = new File("amostra-" + ex.getSignificado() + ".png");
        ImageIO.write(off_Image, "png", outputfile);
	}

	public ArrayList<Exemplo> getConjuntoDeDigitos() {
		return conjuntoDeDigitos;
	}

	public void close() throws IOException {
		imagens.close();
		rotulos.close();
	}

	/**
	 * Este método é responsável por reposicionar os exemplos na coleção de forma aleatória.
	 * O objetivo é possibilitar que as diferentes "épocas" de treinamento sejam diversificadas.
	 * 
	 */
	public void embaralhaExemplos() {
		Collections.shuffle(conjuntoDeDigitos, new Random(System.currentTimeMillis()));
	}
}
