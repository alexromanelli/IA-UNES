package br.com.alexromanelli.android.jogodedamas.dinamica;

import java.util.ArrayList;

public class Jogada {
	
	public enum EstadoJogada {
		Sugestao,
		Executando,
		Executada
	}

	private Peca pecaMovimentada;
	private ArrayList<Casa> percurso;
	private ArrayList<Peca> dominadasNoPercurso;
	
	private EstadoJogada estado;
	private int passoJogada; // útil apenas enquanto o estado é "Executando", para auxiliar a exibição
	
	private boolean houveMudancaParaDama;
	
	public Jogada(Peca pecaMovimentada) {
		this.pecaMovimentada = pecaMovimentada;
		percurso = new ArrayList<Casa>();
		dominadasNoPercurso = new ArrayList<Peca>();
		estado = EstadoJogada.Sugestao;
	}

	public Peca getPecaMovimentada() {
		return pecaMovimentada;
	}

	public void setPecaMovimentada(Peca pecaMovimentada) {
		this.pecaMovimentada = pecaMovimentada;
	}

	public ArrayList<Casa> getPercurso() {
		return percurso;
	}

	public void setPercurso(ArrayList<Casa> percurso) {
		this.percurso = percurso;
	}

	public ArrayList<Peca> getDominadasNoPercurso() {
		return dominadasNoPercurso;
	}

	public void setDominadasNoPercurso(ArrayList<Peca> dominadasNoPercurso) {
		this.dominadasNoPercurso = dominadasNoPercurso;
	}

	public EstadoJogada getEstado() {
		return estado;
	}

	public void setEstado(EstadoJogada estado) {
		this.estado = estado;
	}

	public int getPassoJogada() {
		return passoJogada;
	}

	public void setPassoJogada(int passoJogada) {
		this.passoJogada = passoJogada;
	}

	public boolean isHouveMudancaParaDama() {
		return houveMudancaParaDama;
	}

	public void setHouveMudancaParaDama(boolean houveMudancaParaDama) {
		this.houveMudancaParaDama = houveMudancaParaDama;
	}
	
	public int getPassosJogada() {
		return percurso.size();
	}

}
