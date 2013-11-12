package br.com.alexromanelli.android.reconhecesimbolos;

import java.io.IOException;
import java.util.Random;

import br.com.alexromanelli.android.reconhecesimbolos.model.HandwrittenDigitSetReader;
import br.com.alexromanelli.android.reconhecesimbolos.rna.Exemplo;
import br.com.alexromanelli.android.reconhecesimbolos.rna.RNAReconheceDigitos;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class EntradaDigito extends Activity {
    private DrawView drawView;
    
    private RNAReconheceDigitos rna;
    
    private static final String IMAGENS_TESTES = "t10k-images-idx3-ubyte";
    private static final String ROTULOS_TESTES = "t10k-labels-idx1-ubyte"; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada_digito);

        drawView = new DrawView(this);
        LinearLayout groupviewDraw = (LinearLayout)findViewById(R.id.groupviewDraw);
        groupviewDraw.addView(drawView, 401, 401);
        drawView.requestFocus();

        Button buttonDesenhar = (Button)findViewById(R.id.buttonDesenhar);
        Button buttonFormaMatricial = (Button)findViewById(R.id.buttonFormaMatricial);
        Button buttonReconhecer = (Button)findViewById(R.id.buttonReconhecer);

        buttonDesenhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.reiniciaSegmentos();
            }
        });

        buttonFormaMatricial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.converteParaMatriz();
            }
        });

        buttonReconhecer.setEnabled(false);
        buttonReconhecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	executaReconhecimento();
            }
        });

        try {
			rna = new RNAReconheceDigitos(this,
					RNAReconheceDigitos.OpcaoInicializacaoDePesos.CarregarDeArquivo);
		} catch (IOException e) {
			e.printStackTrace();
		}

        // obtém um exemplo para reconhecimento
		HandwrittenDigitSetReader leitorTreinamento;
		try {
			leitorTreinamento = new HandwrittenDigitSetReader(this, IMAGENS_TESTES, ROTULOS_TESTES);
			leitorTreinamento.processaArquivos();
			Exemplo e = leitorTreinamento.leExemplo();
			Random r = new Random(System.currentTimeMillis());
			r.nextInt(1000);
			for (int i = 0; i < r.nextInt(1000); i++)
				e = leitorTreinamento.leExemplo();
			drawView.converteParaMatriz();
			for (int i = 0; i < e.getFigura().length; i++)
				for (int j = 0; j < e.getFigura()[i].length; j++) {
					drawView.getMatrizSimbolo()[i][j] = 255 - e.getFigura()[j][i];
				}
			drawView.invalidate();
			leitorTreinamento.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void habilitaBotaoReconhecer() {
		Button bReconhecer = (Button)findViewById(R.id.buttonReconhecer);
		bReconhecer.setEnabled(true);
    }
    
	public void desabilitaBotaoReconhecer() {
		Button bReconhecer = (Button)findViewById(R.id.buttonReconhecer);
		bReconhecer.setEnabled(false);
	}

	protected void executaReconhecimento() {
    	int[][] m = drawView.getMatrizSimbolo();
    	int[][] matrizFigura = new int[28][28];
    	
    	// inverte a matriz para igualar a orientação usada no treinamento
    	for (int i = 0; i < m.length; i++)
    		for (int j = 0; j < m[i].length; j++)
    			matrizFigura[j][i] = 255 - m[i][j];
    	
    	rna.setSinalEntrada(matrizFigura);
    	rna.computaPropagacaoDeSinais();
    	
    	EditText editValorReconhecido = (EditText)findViewById(R.id.editTextValorReconhecido);
    	editValorReconhecido.setText(Integer.toString(rna.interpretaSaida()));
	}

	public void exibeProgressoCargaRNA() {
		LinearLayout llCargaRna = (LinearLayout)findViewById(R.id.llCargaRNA);
		llCargaRna.setVisibility(View.VISIBLE);
	}

	public void ocultaProgressoCargaRNA() {
		LinearLayout llCargaRna = (LinearLayout)findViewById(R.id.llCargaRNA);
		llCargaRna.setVisibility(View.GONE);
	}

	public void atualizaProgressoCargaRna(int progresso) {
		ProgressBar pCargaRna = (ProgressBar)findViewById(R.id.pbCargaRNA);
		pCargaRna.setProgress(progresso);
	}

}
