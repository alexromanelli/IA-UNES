package br.com.alexromanelli.android.jogodedamas.dinamica;

import java.util.ArrayList;

public interface RegraDoJogo {
	
	/**
	 * Identifica as possíveis jogadas para uma determinada peça selecionada. As jogadas identificadas
	 * são compostas de movimentos únicos, com ou sem dominação de peça do adversário.
	 * 
	 * @param pecaSelecionada é a peça selecionada para a qual os movimentos serão avaliados.
	 * @param jogadas é uma coleção pré-inicializada que receberá as jogadas que forem identificadas.
	 * @return retorna true se houver jogada válida para a peça selecionada, ou false se não for
	 *         possível mover a peça. 
	 */
	public boolean identificaJogadasValidas(Peca pecaSelecionada, ArrayList<Jogada> jogadas);
	
	/**
	 * Analisa o estado de uma partida e determina o resultado.
	 *  
	 * @param jogador1 é um dos jogadores da partida (jogador1 != jogador2).
	 * @param jogador2 é um dos jogadores da partida (jogador1 != jogador2).
	 * @param vencedor é uma coleção pré-inicializada que receberá o jogador vencedor da partida ou
	 *        ambos, em caso de empate.
	 * @return retorna o resultado da partida, que pode ser uma vitória individual ou um empate.
	 */
	public Partida.ResultadoPartida determinaResultadoPartida(ConjuntoJogador jogador1, 
			ConjuntoJogador jogador2, ArrayList<ConjuntoJogador> vencedor);
	
	/**
	 * Avalia o estado da partida, resultante do último movimento feito, para identificar 
	 * possível ocorrência de empate.
	 * 
	 * @param pecaMovida é a última peça que foi movida.
	 * @param jogador1 é um dos jogadores da partida (jogador1 != jogador2).
	 * @param jogador2 é um dos jogadores da partida (jogador1 != jogador2).
	 * @return retorna true, se a partida pode ser declarada empatada, ou false,
	 *         caso contrário.
	 */
	public boolean avaliaPossivelEmpate(Peca pecaMovida, boolean houveDominacao,
	        ConjuntoJogador jogador1, ConjuntoJogador jogador2);
}
