package br.com.alexromanelli.android.jogodedamas.dinamica;

public class Relogio {

	private static final int MILISSEGUNDOS_POR_MINUTO = 60000;
	
	private static Relogio INSTANCIA;
	public static Relogio getInstancia() {
		if (INSTANCIA == null)
			INSTANCIA = new Relogio();
		return INSTANCIA;
	}
	
	private long tempoRestanteBranco;
	private long tempoRestantePreto;
	
	private class ThreadCronometro extends Thread {
		private long ultimaMarcacao;
		
		@Override
		public void run() {
			ultimaMarcacao = System.currentTimeMillis();
			do {
				try {
					ThreadCronometro.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (Partida.getInstancia().getJogadorAtual() != null) {
    				long marcacao = System.currentTimeMillis();
    				long tempoDecorrido = marcacao - ultimaMarcacao;
    				if (Partida.getInstancia().getJogadorAtual().getCorPecasJogador() == Peca.CorPeca.Branca)
    					tempoRestanteBranco -= tempoDecorrido;
    				else
    					tempoRestantePreto -= tempoDecorrido;
    
    				ultimaMarcacao = marcacao;
				}
				
			} while (tempoRestanteBranco > 0 && tempoRestantePreto > 0);

			Partida.getInstancia().informaFimDePartida();
		}
	}
	
	private ThreadCronometro cronometro;
	
	public Relogio() {
	    defineValorCronometros();
	    
		Partida.getInstancia().setJogadorAtual(Partida.getInstancia().getPecasBrancas());
		cronometro = new ThreadCronometro();
	}
	
	private void defineValorCronometros() {
        switch (Partida.OPCAO_TEMPO_JOGO) {
        case Relampago5x5:
            tempoRestanteBranco = 5 * MILISSEGUNDOS_POR_MINUTO;
            tempoRestantePreto = 5 * MILISSEGUNDOS_POR_MINUTO;
            break;
        case Curto15x15:
            tempoRestanteBranco = 15 * MILISSEGUNDOS_POR_MINUTO;
            tempoRestantePreto = 15 * MILISSEGUNDOS_POR_MINUTO;
            break;
        case Medio30x30:
            tempoRestanteBranco = 30 * MILISSEGUNDOS_POR_MINUTO;
            tempoRestantePreto = 30 * MILISSEGUNDOS_POR_MINUTO;
            break;
        case Normal45x45:
            tempoRestanteBranco = 45 * MILISSEGUNDOS_POR_MINUTO;
            tempoRestantePreto = 45 * MILISSEGUNDOS_POR_MINUTO;
            break;
        case Ilimitado:
            tempoRestanteBranco = Long.MAX_VALUE;
            tempoRestantePreto = Long.MAX_VALUE;
            break;
        }
    }

    public void iniciaContagem() {
        defineValorCronometros();
	    if (!cronometro.isAlive())
		    cronometro.start();
	}

    public void reiniciaContagem() {
        defineValorCronometros();
        iniciaContagem();
    }
    
	public void encerraContagem() {
	    if (cronometro.isAlive())
	        cronometro.interrupt();
	}

	public long getTempoRestanteBranco() {
		return tempoRestanteBranco;
	}

	public long getTempoRestantePreto() {
		return tempoRestantePreto;
	}

    public void setTempoRestanteBranco(long tempoRestanteBranco) {
        this.tempoRestanteBranco = tempoRestanteBranco;
    }

    public void setTempoRestantePreto(long tempoRestantePreto) {
        this.tempoRestantePreto = tempoRestantePreto;
    }
	
}
