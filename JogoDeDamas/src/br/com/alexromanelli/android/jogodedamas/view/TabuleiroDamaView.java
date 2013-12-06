package br.com.alexromanelli.android.jogodedamas.view;

import br.com.alexromanelli.android.jogodedamas.dinamica.Casa;
import br.com.alexromanelli.android.jogodedamas.dinamica.Partida;
import br.com.alexromanelli.android.jogodedamas.dinamica.Peca;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * A classe TabuleiroDamaView é uma fronteira para o usuário final, e representa a manifestação física de
 * um tabuleiro de jogo de damas. É responsável por exibir o tabuleiro e as peças que estão sobre ele.
 * Adicionalmente, também exibe destaques para a peça selecionada para o movimento, e para as casas que
 * podem ser percorridas no percurso da peça selecionada em uma jogada.<br/>
 * <br/>
 * Parte da classe TabuleiroDamaView foi inspirada na classe DrawView, obtida em:<br/>
 * <br/>
 * http://marakana.com/s/post/1036/android_2d_graphics_example<br/>
 * <br/>
 * A classe DrawView é propriedade de:<br/>
 * <br/>
 * Max Walker<br/>
 * Digital Creative Lead<br/>
 * Marakana, Inc.<br/>
 * <br/>
 */
public class TabuleiroDamaView extends View implements OnTouchListener {
	
	public enum OrigemSelecaoPeca {
		Humano,
		IA,
		DispositivoRemoto
	}

    private Paint paintFundoClaro = new Paint();
    private Paint paintFundoEscuro = new Paint();
    private Paint paintPedraBranca = new Paint();
    private Paint paintPedraPreta = new Paint();
    
    private int tamanhoTabuleiro;
    private int afastamentoEsquerdo;
    private int afastamentoTopo;

	private Rect casa;
	private int tamanhoCasa;
	private Paint paintDestaquePecaSelecionada = new Paint();
    private Paint paintDestaqueJogadaValida = new Paint();
	
	public TabuleiroDamaView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        this.setOnTouchListener(this);

        paintFundoClaro.setColor(Color.rgb(250, 245, 202));
        paintFundoClaro.setStrokeWidth(2);
        paintFundoClaro.setStrokeCap(Paint.Cap.ROUND);

        paintFundoEscuro.setColor(Color.rgb(107, 84, 10));
        paintFundoEscuro.setStrokeWidth(2);
        paintFundoEscuro.setStrokeCap(Paint.Cap.ROUND);

        paintPedraBranca.setColor(Color.WHITE);
        paintPedraBranca.setAntiAlias(true);
        paintPedraBranca.setStrokeWidth(4);
        paintPedraBranca.setStrokeCap(Paint.Cap.ROUND);

        paintPedraPreta.setColor(Color.BLACK);
        paintPedraPreta.setAntiAlias(true);
        paintPedraPreta.setStrokeWidth(4);
        paintPedraPreta.setStrokeCap(Paint.Cap.ROUND);
        
        paintDestaquePecaSelecionada.setColor(Color.rgb(57, 85, 146));
        paintDestaqueJogadaValida.setColor(Color.rgb(85, 148, 183));

        setDrawingCacheEnabled(true);
        
        casa = new Rect();
    }
	
	private void desenhaTabuleiro(Canvas canvas, int tamanhoCasa) {
    	canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paintFundoEscuro);

    	int limEsqBorda = afastamentoEsquerdo - 1;
    	int limDirBorda = afastamentoEsquerdo + tamanhoTabuleiro + 1;
    	int limTopBorda = afastamentoTopo - 1;
    	int limFunBorda = afastamentoTopo + tamanhoTabuleiro + 1;
    	
    	canvas.drawLine(limEsqBorda, limTopBorda, limDirBorda, limTopBorda, paintFundoClaro);
    	canvas.drawLine(limDirBorda, limTopBorda, limDirBorda, limFunBorda, paintFundoClaro);
    	canvas.drawLine(limDirBorda, limFunBorda, limEsqBorda, limFunBorda, paintFundoClaro);
    	canvas.drawLine(limEsqBorda, limFunBorda, limEsqBorda, limTopBorda, paintFundoClaro);
    	
    	canvas.drawRect(afastamentoEsquerdo, 
    			afastamentoTopo, 
    			afastamentoEsquerdo + tamanhoTabuleiro, 
    			afastamentoTopo + tamanhoTabuleiro, 
    			paintFundoClaro);
        for (int linha = 0; linha < 8; linha++) {
        	for (int coluna = 0; coluna < 4; coluna++) {
        		casa.left = afastamentoEsquerdo + (2 * coluna * tamanhoCasa + tamanhoCasa * ((linha + 1) % 2));
        		casa.top = afastamentoTopo + linha * tamanhoCasa;
        		casa.right = casa.left + tamanhoCasa;
        		casa.bottom = casa.top + tamanhoCasa;
        		
        		Casa instCasa = Partida.getInstancia().getTabuleiro().getCasa(linha, coluna * 2 + (1 - linha % 2));
        		if (instCasa != null) {
        		    if (instCasa.getOcupante() != null) {
        		        if (instCasa.getOcupante().isSelecionada())
                            canvas.drawRect(casa, paintDestaquePecaSelecionada);
                        else
                            canvas.drawRect(casa, paintFundoEscuro);
        		    }
                    else if (instCasa.isParteDeJogadaValida())
                        canvas.drawRect(casa, paintDestaqueJogadaValida);
                    else
                        canvas.drawRect(casa, paintFundoEscuro);
        		}
        	}
        }
	}
	
	private void desenhaPeca(Canvas canvas, Peca.CorPeca cor, Peca.EstadoPeca estado, int linha, int coluna) {
		int xCentro = afastamentoEsquerdo + tamanhoCasa * coluna + tamanhoCasa / 2;
		int yCentro = afastamentoTopo + tamanhoCasa * linha + tamanhoCasa / 2;
		int raio = Math.round((tamanhoCasa / 2) * 0.8f);
		
		// desenha interior da peça
		canvas.drawCircle(xCentro, yCentro, raio, cor == Peca.CorPeca.Branca ? paintPedraBranca : paintPedraPreta);
		// desenha contorno da peça
		//canvas.drawCircle(xCentro, yCentro, raio, paint);
		
		// desenha destaque de peça "dama"
		if (estado == Peca.EstadoPeca.Dama)
			canvas.drawCircle(xCentro, yCentro, raio / 3, cor == Peca.CorPeca.Branca ? paintPedraPreta : paintPedraBranca);
	}

	private void desenhaConjuntoDePecas(Canvas canvas, Peca[] conjunto) {
		for (Peca p : conjunto) {
			// desenha a peça, se estiver no tabuleiro
			if (p.getEstado() != Peca.EstadoPeca.Perdida) {
				int linha = p.getLocalizacao().getLinha();
				int coluna = p.getLocalizacao().getColuna();
				
				desenhaPeca(canvas, p.getCor(), p.getEstado(), linha, coluna);
			}
		}
	}
	
	private void desenhaPecas(Canvas canvas) {
	    if (!Partida.getInstancia().isPartidaEmCurso())
	        return;
	    
		Peca[] pecasBrancas = Partida.getInstancia().getPecasBrancas().getConjuntoPecas();
		Peca[] pecasPretas = Partida.getInstancia().getPecasPretas().getConjuntoPecas();

		desenhaConjuntoDePecas(canvas, pecasBrancas);
		desenhaConjuntoDePecas(canvas, pecasPretas);
	}
	
	private void desenhaDestaques(Canvas canvas) {
	    
	}

    @SuppressLint("NewApi")
	@Override
    public void onDraw(Canvas canvas) {
    	// obtém dimensões e afastamentos
    	tamanhoTabuleiro = Math.min(canvas.getHeight(), canvas.getWidth()) - 12;
    	afastamentoEsquerdo = (canvas.getWidth() - tamanhoTabuleiro) / 2;
    	afastamentoTopo = (canvas.getHeight() - tamanhoTabuleiro) / 2;
    	
    	tamanhoCasa = tamanhoTabuleiro / 8;
    	tamanhoTabuleiro = tamanhoCasa * 8;
    	
        // desenha tabuleiro
    	desenhaTabuleiro(canvas, tamanhoCasa);
        
        // desenha peças
        desenhaPecas(canvas);

        // desenha destaques
        desenhaDestaques(canvas);
    }
    
    public boolean selecionaPeca(int linha, int coluna, OrigemSelecaoPeca origem) {
    	Casa casa = Partida.getInstancia().getTabuleiro().getCasa(linha, coluna);
    	if ((casa != null) && (casa.getOcupante() != null) && Partida.getInstancia().getJogadorAtual() != null &&

    			((Partida.OPCAO_ADVERSARIO == Partida.OpcaoAdversario.HumanoLocal && 
    			  casa.getOcupante().getCor() == Partida.getInstancia().getJogadorAtual().getCorPecasJogador()) ||

    			 (Partida.OPCAO_ADVERSARIO == Partida.OpcaoAdversario.Computador &&
    			  origem == OrigemSelecaoPeca.Humano &&
    			  casa.getOcupante().getCor() == Peca.CorPeca.Branca) ||
    			 
     			 (Partida.OPCAO_ADVERSARIO == Partida.OpcaoAdversario.Computador &&
     			  origem == OrigemSelecaoPeca.IA &&
    			  casa.getOcupante().getCor() == Peca.CorPeca.Preta) ||
   			 
    			 (Partida.OPCAO_ADVERSARIO == Partida.OpcaoAdversario.HumanoRemoto &&
     			  origem == OrigemSelecaoPeca.Humano &&
    			  casa.getOcupante().getCor() == Partida.getInstancia().getJogadorAtual().getCorPecasJogador()) ||
    	   			 
     			 (Partida.OPCAO_ADVERSARIO == Partida.OpcaoAdversario.HumanoRemoto &&
     			  origem == OrigemSelecaoPeca.DispositivoRemoto &&
     			  casa.getOcupante().getCor() != Partida.getInstancia().getJogadorAtual().getCorPecasJogador()))) {
    		
    		Partida.getInstancia().selecionaPeca(casa.getOcupante());
    		return true;
    	}
    	return false;
    }
    
    private boolean selecionaPeca(Point pontoToque) {
    	int linha = ((int)pontoToque.y - afastamentoTopo) / tamanhoCasa;
    	int coluna = ((int)pontoToque.x - afastamentoEsquerdo) / tamanhoCasa;
    	if (linha >= 0 && linha < 8 && 
    			coluna >= 0 && coluna < 8)
    		return selecionaPeca(linha, coluna, OrigemSelecaoPeca.Humano);
    	else
    		return false;
    }

    /**
     * Este método é responsável por identificar a sequência de pontos que descreve as linhas
     * desenhadas pelo usuário com o toque na tela.
     */
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Point pontoToque = new Point(event.getX(), event.getY());
                interpretaToque(pontoToque);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    private void interpretaToque(Point pontoToque) {
        int linha = ((int)pontoToque.y - afastamentoTopo) / tamanhoCasa;
        int coluna = ((int)pontoToque.x - afastamentoEsquerdo) / tamanhoCasa;
        if (linha >= 0 && linha < 8 && 
                coluna >= 0 && coluna < 8) {
            Casa casa = Partida.getInstancia().getTabuleiro().getCasa(linha, coluna);
            if (casa != null && casa.getOcupante() != null)
                selecionaPeca(linha, coluna, OrigemSelecaoPeca.Humano);
            else if (casa != null)
                tentaMoverPecaSelecionada(linha, coluna);
        }
    }

    public void tentaMoverPecaSelecionada(int linha, int coluna) {
        Casa casa = Partida.getInstancia().getTabuleiro().getCasa(linha, coluna);
        if (casa.isParteDeJogadaValida())
            Partida.getInstancia().movePecaSelecionada(linha, coluna);
    }

}

class Point {
    float x, y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}