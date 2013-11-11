package br.com.alexromanelli.android.reconhecesimbolos;


import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * A classe DrawView foi editada para permitir que o usuário desenhe segmentos de reta na tela do dispositivo. 
 *  
 * http://marakana.com/s/post/1036/android_2d_graphics_example
 *
 * Max Walker
 * Digital Creative Lead
 * Marakana, Inc.
 *
 * DrawView is a view. It listens to mouse click events and draws a point at the point that it was clicked on.
 *
 */
public class DrawView extends View implements OnTouchListener {
    //private static final String TAG = "DrawView"; -> não foi usado

    public enum EstadoApresentacao {
        DESENHO,
        MATRIZ
    }

    Paint paint = new Paint();
    Paint linhaGrid = new Paint();
    Paint fundoBranco = new Paint();

    private EstadoApresentacao estado;
    private ArrayList<ArrayList<Point>> segmentos;
    @SuppressWarnings("unused")
	private int tamanhoSegmento;

    private Bitmap ultimoCanvas;
    private Canvas canvasCopia;
    private int[][] matrizSimbolo;

    public int[][] getMatrizSimbolo() {
		return matrizSimbolo;
	}

	@SuppressLint("NewApi")
	public DrawView(Context context) {
        super(context);
        estado = EstadoApresentacao.DESENHO;
        setFocusable(true);
        setFocusableInTouchMode(true);

        this.setOnTouchListener(this);

        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        //paint.setHinting(Paint.HINTING_ON);
        paint.setStrokeCap(Paint.Cap.ROUND);

        linhaGrid.setColor(Color.LTGRAY);
        fundoBranco.setColor(Color.WHITE);

        segmentos = new ArrayList<ArrayList<Point>>();
        tamanhoSegmento = 0;

        ultimoCanvas = Bitmap.createBitmap(392, 392, Bitmap.Config.ARGB_8888);
        canvasCopia = new Canvas(ultimoCanvas);

        matrizSimbolo = new int[28][28];

        setDrawingCacheEnabled(true);
    }

    public void reiniciaSegmentos() {
        segmentos.clear();
        estado = EstadoApresentacao.DESENHO;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // desenha grid
        for (int i = 0; i <= 392; i += 14) {
            canvas.drawLine(i, 0, i, 392, linhaGrid);
            canvas.drawLine(0, i, 392, i, linhaGrid);
        }

        if (estado == EstadoApresentacao.DESENHO) {

            canvasCopia.drawRect(0, 0, 392, 392, fundoBranco);

            // desenha segmentos
            for (ArrayList<Point> seg : segmentos) {
                Point anterior = null;
                for (Point point : seg) {
                    if (anterior == null) {
                        canvas.drawCircle(point.x, point.y, 7, paint);
                        anterior = point;
                    } else {
                        canvas.drawLine(anterior.x, anterior.y, point.x, point.y, paint);
                        canvasCopia.drawLine(anterior.x, anterior.y, point.x, point.y, paint);
                        anterior = point;
                    }
                }
                canvas.drawCircle(anterior.x, anterior.y, 7, paint);
            }

        } else if (estado == EstadoApresentacao.MATRIZ) {

            // pinta matriz
            for (int i = 0; i < 28; i++) {
                int x0 = 14 * i;
                for (int j = 0; j < 28; j++) {
                    int y0 = 14 * j;

                    int tonalidade = matrizSimbolo[i][j];
                    paint.setColor(Color.rgb(tonalidade, tonalidade, tonalidade));
                    canvas.drawRect(x0, y0, x0 + 13, y0 + 13, paint);
                }
            }
            paint.setColor(Color.BLACK);

        }
    }

    /**
     * Este método é responsável por identificar a sequência de pontos que descreve as linhas
     * desenhadas pelo usuário com o toque na tela.
     */
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Point origem = new Point(event.getX(), event.getY());
                adicionaSegmento(origem);
                break;
            case MotionEvent.ACTION_MOVE:
                ArrayList<Point> trilha = new ArrayList<Point>();
                MotionEvent ev = event;
                final int historySize = ev.getHistorySize();
                final int pointerCount = ev.getPointerCount();
                for (int h = 0; h < historySize; h++) {
                    for (int p = 0; p < pointerCount; p++) {
                        trilha.add(new Point(ev.getHistoricalX(p, h), ev.getHistoricalY(p, h)));
                    }
                }
                for (int p = 0; p < pointerCount; p++) {
                    trilha.add(new Point(ev.getX(p), ev.getY(p)));
                }
                adicionaTrilhaSegmento(trilha);
                break;
            case MotionEvent.ACTION_UP:
                Point fim = new Point(event.getX(), event.getY());
                adicionaPontoSegmento(fim);
                break;
        }
        invalidate();
        return true;
    }

    private void adicionaSegmento(Point origem) {
        segmentos.add(new ArrayList<Point>());
        segmentos.get(segmentos.size() - 1).add(origem);
        tamanhoSegmento = 1;
    }

    private void adicionaTrilhaSegmento(ArrayList<Point> trilha) {
        for (Point p : trilha) {
            segmentos.get(segmentos.size() - 1).add(p);
            tamanhoSegmento++;
        }
    }

    private void adicionaPontoSegmento(Point p) {
        segmentos.get(segmentos.size() - 1).add(p);
        tamanhoSegmento++;
    }

    public void converteParaMatriz() {
        // (aqui está usando força bruta, mas deve ser modificado para usar geometria computacional)
        for (int i = 0; i < 28; i++) {
            int x1 = 14 * i;
            for (int j = 0; j < 28; j++) {
                int y1 = 14 * j;

                int soma = 0;
                for (int x0 = 0; x0 < 14; x0++) {
                    for (int y0 = 0; y0 < 14; y0++) {
                    	soma += Color.red(ultimoCanvas.getPixel(x1 + x0, y1 + y0));
                    }
                }
                int media = soma / (14 * 14);
                matrizSimbolo[i][j] = Color.rgb(media, media, media);
            }
        }
        estado = EstadoApresentacao.MATRIZ;
        invalidate();
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