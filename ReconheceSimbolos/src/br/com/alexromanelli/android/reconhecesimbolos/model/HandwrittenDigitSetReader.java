package br.com.alexromanelli.android.reconhecesimbolos.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import br.com.alexromanelli.android.reconhecesimbolos.rna.Exemplo;

import android.content.Context;

/**
 * A classe HandwrittenDigitSetReader é responsável por fazer a leitura 
 * de uma base de dados para treinamento e testes de redes neurais para 
 * o reconhecimento de caracteres. A base é conhecida como MNIST 
 * Handwritten Digit Database, e usa o formato IDX para codificação dos 
 * dados.
 * 
 * @author alexandre
 *
 */
public class HandwrittenDigitSetReader {

	private int numeroDeItens;
	public int getNumeroDeItens() {
		return numeroDeItens;
	}

	private int numeroDeLinhas;
	private int numeroDeColunas;
	
	private BufferedInputStream imagens;
	private BufferedInputStream rotulos;
	
	private ArrayList<Exemplo> conjuntoDeDigitos;
	private static Exemplo exemploLido;
	
	public HandwrittenDigitSetReader(Context ctx, 
			String nomeArquivoImagens, String nomeArquivoRotulos) 
					throws IOException {
		imagens = new BufferedInputStream(ctx.getAssets().open(nomeArquivoImagens));
		rotulos = new BufferedInputStream(ctx.getAssets().open(nomeArquivoRotulos));
		
		conjuntoDeDigitos = new ArrayList<Exemplo>();
	}

	public void processaArquivos() throws IOException {
		leNumeroMagico();
		leNumeroDeItens();
		leDimensoesDasImagens();
		//leExemplos();
	}

	/**
	 * Como nesta implementação não é útil o número mágico, pois
	 * já se conhece a estrutura dos dados, basta passar pelos
	 * quatro bytes e ignorá-los.
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

	private static int byteArrayToInt(int[] bNumRotulos) {
		int valor = 0;
		int base = 1;
		for (int i = bNumRotulos.length - 1; i >= 0; i--) {
			valor += bNumRotulos[i] * base;
			base *= 256;
		}
		return valor;
	}
	
	private static int leInteiro4Bytes(BufferedInputStream in) throws IOException {
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

	public Exemplo leExemplo() throws IOException {
		// lê um exemplo
		int significado = rotulos.read();
		if (exemploLido == null) {
			int[][] figura = new int[numeroDeLinhas][numeroDeColunas];
			exemploLido = new Exemplo(figura, significado);
		}
		exemploLido.setSignificado(significado);
		for (int l = 0; l < numeroDeLinhas; l++)
			for (int c = 0; c < numeroDeColunas; c++) {
				int pixel = imagens.read();
				exemploLido.getFigura()[l][c] = pixel;
			}
		
		return exemploLido;
	}
	
	/*
	private void leExemplos() throws IOException {
		for (int i = 0; i < numeroDeItens; i++) {
			// lê um exemplo
			int significado = rotulos.read();
			int[][] figura = new int[numeroDeLinhas][numeroDeColunas];
			for (int l = 0; l < numeroDeLinhas; l++)
				for (int c = 0; c < numeroDeColunas; c++) {
					int pixel = imagens.read();
					figura[l][c] = pixel;
				}
			
			conjuntoDeDigitos.add(new Exemplo(figura, significado));
		}
	}
	*/

	public ArrayList<Exemplo> getConjuntoDeDigitos() {
		return conjuntoDeDigitos;
	}
	
	public void close() throws IOException {
		imagens.close();
		rotulos.close();
	}
}
