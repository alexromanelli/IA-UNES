package br.com.alexromanelli.android.jogodedamas.dinamica.ia;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import br.com.alexromanelli.android.jogodedamas.MainActivity;
import br.com.alexromanelli.android.jogodedamas.dinamica.*;
import br.com.alexromanelli.android.jogodedamas.view.TabuleiroDamaView;

import java.util.ArrayList;

/**
 * Created by alexandre on 11/30/13.
 */
public class JogadorIA {

    private MainActivity ctx;
    private Handler handlerMain;

    class TarefaJogadorIA extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            exibeMensagem("Pronto! É a sua vez.");
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            handlerMain.post(new Runnable() {
                @Override
                public void run() {
                    ctx.atualizaProgresso(values[0].intValue());
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Partida.getInstancia().isPartidaEmCurso()) {
                if (Partida.getInstancia().getJogadorAtual() == conjuntoJogador) {
                    // é minha vez!
                    Jogada j = indicaJogada();
                    if (j != null) {
                        j.setEstado(Jogada.EstadoJogada.Executando);

                        Casa casaJ = j.getPecaMovimentada().getLocalizacao();
                        selecionaPeca(casaJ.getLinha(), casaJ.getColuna());

                        for (Casa c : j.getPercurso()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                exibeMensagem("Desculpe-me. Ocorreu um erro de execução. Problema no método \"executaJogada$Thread$Runnable.run()\"");
                            }
                            tentaMoverPecaSelecionada(c.getLinha(), c.getColuna());
                        }

                        j.setEstado(Jogada.EstadoJogada.Executada);
                    } else
                        exibeMensagem("Deu pau.");
                }
            }
            return null;
        }
    }

    public void executaJogada() {
        TarefaJogadorIA tarefa = new TarefaJogadorIA();
        tarefa.execute();
    }

    private void tentaMoverPecaSelecionada(final int linha, final int coluna) {
        handlerMain.post(new Runnable() {
            @Override
            public void run() {
                ctx.getTabuleiroDamaView().tentaMoverPecaSelecionada(linha, coluna);
                ctx.getTabuleiroDamaView().invalidate();
            }
        });
    }

    private void selecionaPeca(final int linha, final int coluna) {
        handlerMain.post(new Runnable() {
            @Override
            public void run() {
                ctx.getTabuleiroDamaView().selecionaPeca(linha, coluna, TabuleiroDamaView.OrigemSelecaoPeca.IA);
                ctx.getTabuleiroDamaView().invalidate();
            }
        });
    }

    private void exibeMensagem(final String mensagem) {
        handlerMain.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public enum NivelJogadorIA {
        Facil,
        Medio,
        Dificil
    }

    private static final int PROFUNDIDADE_BUSCA_MINIMAX_FACIL = 1;
    private static final int PROFUNDIDADE_BUSCA_MINIMAX_MEDIO = 2;
    private static final int PROFUNDIDADE_BUSCA_MINIMAX_DIFICIL = 4;

    private NivelJogadorIA nivelJogadorIA;
    private ConjuntoJogador conjuntoJogador;

    public JogadorIA(NivelJogadorIA nivelJogadorIA, ConjuntoJogador conjuntoJogador, MainActivity ctx, Handler handlerMain) {
        this.nivelJogadorIA = nivelJogadorIA;
        this.conjuntoJogador = conjuntoJogador;
        this.ctx = ctx;
        this.handlerMain = handlerMain;
    }

    public Jogada indicaJogada() {
        Jogada jogadaIndicada = null;

        switch (nivelJogadorIA) {
            case Facil:
                jogadaIndicada = buscaAlfaBeta(PROFUNDIDADE_BUSCA_MINIMAX_FACIL);
                break;
            case Medio:
                jogadaIndicada = buscaAlfaBeta(PROFUNDIDADE_BUSCA_MINIMAX_MEDIO);
                break;
            case Dificil:
                jogadaIndicada = buscaAlfaBeta(PROFUNDIDADE_BUSCA_MINIMAX_DIFICIL);
                break;
        }

        return jogadaIndicada;
    }

    /**
     * A busca Alfa-Beta é o resultado da modificação da busca Mini-Max, para eliminar ramificações
     * que não são promissoras. Ou seja, mantém dois valores coringas, alfa e beta, sendo que o
     * primeiro representa a melhor alternativa para MAX, enquanto o segundo representa a melhor
     * alternativa para MIN.
     *
     * @return A jogada com o máximo de utilidade.
     */
    private Jogada buscaAlfaBeta(int profundidadeBusca) {

        Jogada melhorJogada = null;

        ConjuntoJogador pecasBrancas = Partida.getInstancia().getPecasBrancas();
        ConjuntoJogador pecasPretas = Partida.getInstancia().getPecasPretas();
        int contMovSucDeDamasSemDominacao =
                ((RegraClassica) Partida.getInstancia().getRegra()).getContMovSucDeDamasSemDominacao();
        boolean estadoDeEmpateEm5 =
                ((RegraClassica) Partida.getInstancia().getRegra()).isEstadoDeEmpateEm5();
        int contMovEmEstadoDeEmpateEm5 =
                ((RegraClassica) Partida.getInstancia().getRegra()).getContMovEmEstadoDeEmpateEm5();

        Cenario atual = new Cenario(pecasBrancas, pecasPretas, conjuntoJogador, null, contMovSucDeDamasSemDominacao,
                contMovEmEstadoDeEmpateEm5, estadoDeEmpateEm5);

        ArrayList<Cenario> sucessores = atual.getListaSucessores();

        if (sucessores != null && sucessores.size() > 0)
            melhorJogada = sucessores.get(0).getOrigem();

        double max = -Double.MAX_VALUE;
        if (sucessores.size() > 1)
            for (Cenario s : sucessores) {
                double v = valorMax(s, -Double.MAX_VALUE, Double.MAX_VALUE, 1, profundidadeBusca);

                if (v > max) {
                    max = v;
                    melhorJogada = s.getOrigem();
                }
            }

        return melhorJogada;
    }

    private boolean isTesteTerminal(Cenario s, int nivel, int profundidadeBusca) {
        // (sem movimentos possíveis a partir dele ou profundidade limite da busca atingido)
        return nivel == profundidadeBusca || s.getListaSucessores().size() == 0;
    }

    private double funcaoUtilidade(Cenario s) {
        return s.computaFuncaoDeUtilidade();
    }

    private double valorMax(Cenario estado, double alfa, double beta, int nivel, int profundidadeBusca) {

        // calcula o máximo valor de utilidade obtido de um cenário
        if (isTesteTerminal(estado, nivel, profundidadeBusca))
            return funcaoUtilidade(estado);

        double v = -Double.MAX_VALUE;
        for (Cenario s : estado.getListaSucessores()) {
            v = Math.max(v, valorMin(s, alfa, beta, nivel + 1, profundidadeBusca));
            if (v >= beta)
                return v;
            alfa = Math.max(alfa, v);
        }

        return v;
    }

    private double valorMin(Cenario estado, double alfa, double beta, int nivel, int profundidadeBusca) {

        if (isTesteTerminal(estado, nivel, profundidadeBusca))
            return funcaoUtilidade(estado);

        double v = Double.MAX_VALUE;
        for (Cenario s : estado.getListaSucessores()) {
            v = Math.min(v, valorMax(s, alfa, beta, nivel + 1, profundidadeBusca));
            if (v <= alfa)
                return v;
            beta = Math.min(beta, v);
        }

        return v;
    }
}

/**
 * Define-se por cenário uma visão instantânea do jogo. No jogo de damas, o cenário compreende
 * os dados dos conjuntos de peças dispostas sobre o tabuleiro, sua localização e os estados
 * individuais de cada peça.<br/>
 * <br/>
 * A classe Cenário representa uma possível ramificação da árvore de decisão de movimentos para
 * o jogo de damas. A ramificação é a representação da aplicação de um movimento válido a um
 * cenário previamente configurado.
 */
class Cenario {
    private ConjuntoJogador pecasBrancas;
    private ConjuntoJogador pecasPretas;
    private ConjuntoJogador jogadorAtual;
    private Jogada origem;
    private int contMovSucDeDamasSemDominacao;
    private int contMovEmEstadoDeEmpateEm5;
    private boolean estadoDeEmpateEm5;

    private RegraClassica regra;

    private Tabuleiro tabuleiro;

    private ArrayList<Cenario> sucessores;

    public Cenario(ConjuntoJogador pecasBrancas, ConjuntoJogador pecasPretas,
                   ConjuntoJogador jogadorAtual, Jogada origem,
                   int contMovSucDeDamasSemDominacao, int contMovEmEstadoDeEmpateEm5,
                   boolean estadoDeEmpateEm5) {
        this.pecasBrancas = new ConjuntoJogador(pecasBrancas);
        this.pecasPretas = new ConjuntoJogador(pecasPretas);
        this.jogadorAtual = jogadorAtual == pecasBrancas ? this.pecasBrancas : this.pecasPretas;
        this.origem = origem;
        this.contMovSucDeDamasSemDominacao = contMovSucDeDamasSemDominacao;
        this.contMovEmEstadoDeEmpateEm5 = contMovEmEstadoDeEmpateEm5;
        this.estadoDeEmpateEm5 = estadoDeEmpateEm5;

        tabuleiro = new Tabuleiro();
        posicionaPecas(this.pecasBrancas);
        posicionaPecas(this.pecasPretas);

        if (origem != null)
            aplicaJogadaOrigem();

        String estadoJogo = tabuleiro.getRepresentacaoTexto();
        Log.i("JogoDeDamas-Cenário", estadoJogo);

        this.regra = new RegraClassica(contMovSucDeDamasSemDominacao, estadoDeEmpateEm5,
                contMovEmEstadoDeEmpateEm5);
        if (origem != null)
            regra.avaliaPossivelEmpate(origem.getPecaMovimentada(),
                    origem.getDominadasNoPercurso().size() > 0,
                    this.pecasBrancas, this.pecasPretas);

        sucessores = new ArrayList<Cenario>();
    }

    private void aplicaJogadaOrigem() {
        int linha = origem.getPecaMovimentada().getLocalizacao().getLinha();
        int coluna = origem.getPecaMovimentada().getLocalizacao().getColuna();
        Peca pecaSelecionada = tabuleiro.getCasa(linha, coluna).getOcupante();

        if (pecaSelecionada == null)
            return; // houve erro!!!

        // desloca para posição final da jogada
        int linhaFinal = origem.getPercurso().get(origem.getPercurso().size() - 1).getLinha();
        int colunaFinal = origem.getPercurso().get(origem.getPercurso().size() - 1).getColuna();
        pecaSelecionada.setLocalizacao(tabuleiro.getCasa(linhaFinal, colunaFinal));

        // remove peças dominadas no percurso
        for (Peca dominada : origem.getDominadasNoPercurso()) {
            int linhaDominada = dominada.getLocalizacao().getLinha();
            int colunaDominada = dominada.getLocalizacao().getColuna();
            Peca dominadaAtual = tabuleiro.getCasa(linhaDominada, colunaDominada).getOcupante();
            dominadaAtual.setLocalizacao(null);
            dominadaAtual.setEstado(Peca.EstadoPeca.Perdida);
        }

        // verifica se a peça tornou-se uma "dama"
        if (RegraClassica.pecaTornouSeDama(pecaSelecionada))
            pecaSelecionada.setEstado(Peca.EstadoPeca.Dama);
    }

    private void posicionaPecas(ConjuntoJogador pecas) {
        for (Peca p : pecas.getConjuntoPecas()) {
            if (p.getEstado() == Peca.EstadoPeca.Perdida)
                continue;

            int linha = p.getLocalizacao().getLinha();
            int coluna = p.getLocalizacao().getColuna();
            p.setLocalizacaoEmBusca(tabuleiro.getCasa(linha, coluna));
        }
    }

    public Jogada getOrigem() {
        return origem;
    }

    public ArrayList<Cenario> getListaSucessores() {
        if (sucessores.size() == 0) {
            ArrayList<Jogada> jogadas = identificaJogadasValidas();

            for (Jogada jogada : jogadas) {
                Cenario s = new Cenario(pecasBrancas, pecasPretas,
                        pecasBrancas == jogadorAtual ? pecasPretas : pecasBrancas,
                        jogada, regra.getContMovSucDeDamasSemDominacao(),
                        regra.getContMovEmEstadoDeEmpateEm5(),
                        regra.isEstadoDeEmpateEm5());
                sucessores.add(s);
            }
        }

        return sucessores;
    }

    private ArrayList<Jogada> identificaJogadasValidas() {
        int pecasLivres = 0;
        int maxDominadas = 0;
        // percorre as peças do conjunto do jogador para identificar jogadas válidas
        for (Peca peca : jogadorAtual.getConjuntoPecas()) {
            if (peca.getEstado() != Peca.EstadoPeca.Perdida) {
                ArrayList<Jogada> jogadasValidas = new ArrayList<Jogada>();
                pecasLivres += regra.identificaJogadasValidas(peca, jogadasValidas) ? 1 : 0;
                peca.setJogadasValidas(jogadasValidas);

                if (jogadasValidas.size() > 0) {
                    int pecasDominadas = jogadasValidas.get(0).getDominadasNoPercurso().size();
                    if (pecasDominadas > maxDominadas)
                        maxDominadas = pecasDominadas;
                }
            }
        }

        ArrayList<Jogada> jogadas = new ArrayList<Jogada>();

        if (pecasLivres == 0)
            return jogadas;

        // percorre novamente as peças, mas para eliminar as que tenham movimentos com menos dominação (se houver algum com dominação)
        for (Peca peca : jogadorAtual.getConjuntoPecas()) {
            if (peca.getEstado() != Peca.EstadoPeca.Perdida &&
                    peca.getJogadasValidas() != null &&
                    peca.getJogadasValidas().size() > 0 &&
                    peca.getJogadasValidas().get(0).getDominadasNoPercurso().size() < maxDominadas) {
                peca.getJogadasValidas().clear();
            } else if (peca.getEstado() != Peca.EstadoPeca.Perdida) {
                jogadas.addAll(peca.getJogadasValidas());
            }
        }

        return jogadas;
    }

    /**
     * Este método computa a função de utilidade de um cenário com base no trabalho de
     * Héctor Poblete Rojas sobre o jogo de Damas em Introdução à Inteligência Artificial,
     * disponível para download em:<br/>
     * <br/>
     * http://www.ic.unicamp.br/~rocha/teaching/2011s1/mc906/trabalhos/tp/tp-gr-10.pdf<br/>
     * (<em>download feito em 30/11/2013</em>)<br/>
     * <br/>
     * A implementação em uso aplica os seguintes pesos:<br/>
     * <br/>
     * <ul>
     * <li>Pedra (peça normal) = 3;</li>
     * <li>Dama = 6;</li>
     * <li>Ameaça = 1 (pedra) ou 3 (dama);</li>
     * <li>Cobertura sem ameaça = 0,5;</li>
     * <li>Cobertura com ameaça = 1,5 (sem envover dama) ou 2 (envolvendo dama);</li>
     * <li>Bônus pela iminência de coroação = 3,5.</li>
     * </ul>
     * <br/>
     * Observação: houve dúvidas ao tentar compreender a ameaça de 3 pontos e a cobertura
     * 2. Essas utilidades foram convertidas para diferenciar ameaças a Damas e cobertura
     * a Damas, respectivamente.<br/>
     *
     * @return a utilidade do cenário expressa em termo de um número real.
     */
    public double computaFuncaoDeUtilidade() {
        double utilidade = 0.0E00;

        // contagem de pedras e damas
        int contPedrasBrancas = pecasBrancas.getQuantidadePorEstado(Peca.EstadoPeca.Pedra);
        int contPedrasPretas = pecasPretas.getQuantidadePorEstado(Peca.EstadoPeca.Pedra);
        int contDamasBrancas = pecasBrancas.getQuantidadePorEstado(Peca.EstadoPeca.Dama);
        int contDamasPretas = pecasPretas.getQuantidadePorEstado(Peca.EstadoPeca.Dama);

        utilidade += -6.0e00 * contPedrasBrancas - 12.0e00 * contDamasBrancas +
                6.0e00 * contPedrasPretas + 12.0e00 * contDamasPretas;

        // contagem de ameaças
        utilidade -= pontosPorAmeacas(pecasBrancas);
        utilidade += pontosPorAmeacas(pecasPretas);

        // contagem de coberturas
        utilidade -= pontosPorCoberturas(pecasBrancas);
        utilidade += pontosPorCoberturas(pecasPretas);

        // bônus pela iminência de coroação (mudança de estado para dama)
        utilidade -= pontosPorCoroacoesIminentes(pecasBrancas);
        utilidade += pontosPorCoroacoesIminentes(pecasPretas);

        return utilidade;
    }

    private double pontosPorAmeacas(ConjuntoJogador pecas) {
        double pontos = 0.0E00;
        double incPedra = 2.0e00;
        double incDama = 6.0e00;

        Peca.CorPeca corP = pecas.getCorPecasJogador();
        for (Peca p : pecas.getConjuntoPecas()) {
            if (p.getEstado() == Peca.EstadoPeca.Perdida)
                continue;

            Casa casaP = p.getLocalizacao();
            Peca.EstadoPeca estado = p.getEstado();

            for (Casa.Direcao dir : Casa.DIRECOES_PERMITIDAS) {
                Casa advDir = casaP.getVizinhaDirecao(dir);
                if (estado == Peca.EstadoPeca.Dama)
                    while (advDir != null && advDir.getOcupante() == null)
                        advDir = advDir.getVizinhaDirecao(dir);

                if (advDir != null && advDir.getOcupante() != null &&
                        advDir.getOcupante().getCor() != corP &&
                        advDir.getVizinhaDirecao(dir) != null &&
                        advDir.getVizinhaDirecao(dir).getOcupante() == null)
                    pontos += advDir.getOcupante().getEstado() == Peca.EstadoPeca.Pedra ? incPedra : incDama;
            }
        }

        return pontos;
    }

    private double pontosPorCoberturas(ConjuntoJogador pecas) {
        double pontos = 0.0E00;

        double ptsCoberturaSimples = 1.5E-01;
        double ptsCobrirPedraAmeacada = 3.0E-01;
        double ptsCobrirDamaAmeacada = 3.5E-01;

        for (Peca p : pecas.getConjuntoPecas()) {
            if (p.getEstado() == Peca.EstadoPeca.Perdida)
                continue;

            Peca.CorPeca corP = p.getCor();

            for (Casa.Direcao dir : Casa.DIRECOES_PERMITIDAS) {
                Casa viz = p.getLocalizacao().getVizinhaDirecao(dir);

                if (viz != null && viz.getOcupante() != null && viz.getOcupante().getCor() == corP) {
                    // cobertura simples
                    pontos += ptsCoberturaSimples;

                    Casa seguinte = viz.getVizinhaDirecao(dir);
                    if (seguinte != null && seguinte.getOcupante() != null && seguinte.getOcupante().getCor() != corP)
                        // cobertura com ameaca
                        pontos += viz.getOcupante().getEstado() == Peca.EstadoPeca.Pedra ?
                                ptsCobrirPedraAmeacada : ptsCobrirDamaAmeacada;
                }
            }
        }

        return pontos;
    }

    private double pontosPorCoroacoesIminentes(ConjuntoJogador pecas) {
        double pontos = 0.0E00;

        double bonus2 = 2.5E00;
        double bonus3 = 3.5E00;

        for (Peca p : pecas.getConjuntoPecas())
            if (p.getEstado() == Peca.EstadoPeca.Pedra) {
                Casa c = p.getLocalizacao();

                // condição para possível coroação na próxima jogada
                boolean cond1 = ((pecas.getBaseJogador() == ConjuntoJogador.BaseJogador.Alto &&
                        c.getLinha() == 6) ||
                        (pecas.getBaseJogador() == ConjuntoJogador.BaseJogador.Baixo &&
                                c.getLinha() == 1)) &&
                        isAvancoLivre(p, c, pecas.getBaseJogador());

                if (cond1)
                    pontos += bonus3;

                else {
                    // condição para possível coroação em duas jogadas
                    boolean cond2 = ((pecas.getBaseJogador() == ConjuntoJogador.BaseJogador.Alto &&
                            c.getLinha() == 5) ||
                            (pecas.getBaseJogador() == ConjuntoJogador.BaseJogador.Baixo &&
                                    c.getLinha() == 2)) &&
                            isAvancoDuploLivre(p, c, pecas.getBaseJogador());

                    if (cond2)
                        pontos += bonus2;
                }

            }

        return pontos;
    }

    private boolean isAvancoDuploLivre(Peca p, Casa c, ConjuntoJogador.BaseJogador base) {
        if (!isAvancoLivre(p, c, base))
            return false;

        Casa.Direcao[] adiante = new Casa.Direcao[2];
        Casa.Direcao[] paraTras = new Casa.Direcao[2];

        if (base == ConjuntoJogador.BaseJogador.Alto) {
            adiante[0] = Casa.Direcao.Sudeste;
            adiante[1] = Casa.Direcao.Sudoeste;
            paraTras[0] = Casa.Direcao.Nordeste;
            paraTras[1] = Casa.Direcao.Noroeste;
        } else {
            paraTras[0] = Casa.Direcao.Sudeste;
            paraTras[1] = Casa.Direcao.Sudoeste;
            adiante[0] = Casa.Direcao.Nordeste;
            adiante[1] = Casa.Direcao.Noroeste;
        }

        // posição atual está livre para avanço
        Casa vizAdLeste = c.getVizinhaDirecao(adiante[0]);
        Casa vizAdOeste = c.getVizinhaDirecao(adiante[1]);

        if ((vizAdLeste == null || (vizAdLeste != null && isAvancoLivre(p, vizAdLeste, base))) &&
                (vizAdOeste == null || (vizAdOeste != null && isAvancoLivre(p, vizAdOeste, base))))
            return true;

        return false;
    }

    private boolean isAvancoLivre(Peca p, Casa c, ConjuntoJogador.BaseJogador base) {
        Casa.Direcao[] adiante = new Casa.Direcao[2];
        Casa.Direcao[] paraTras = new Casa.Direcao[2];

        if (base == ConjuntoJogador.BaseJogador.Alto) {
            adiante[0] = Casa.Direcao.Sudeste;
            adiante[1] = Casa.Direcao.Sudoeste;
            paraTras[0] = Casa.Direcao.Nordeste;
            paraTras[1] = Casa.Direcao.Noroeste;
        } else {
            paraTras[0] = Casa.Direcao.Sudeste;
            paraTras[1] = Casa.Direcao.Sudoeste;
            adiante[0] = Casa.Direcao.Nordeste;
            adiante[1] = Casa.Direcao.Noroeste;
        }

        // posição atual está livre para avanço
        Casa vizAdLeste = c.getVizinhaDirecao(adiante[0]);
        Casa vizAdOeste = c.getVizinhaDirecao(adiante[1]);
        Casa vizTrLeste = c.getVizinhaDirecao(paraTras[0]);
        Casa vizTrOeste = c.getVizinhaDirecao(paraTras[1]);

        boolean atualLivre =
                (vizAdLeste == null ||
                        (vizAdLeste != null &&
                                (vizAdLeste.getOcupante() == null))) &&
                        (vizAdOeste == null ||
                                (vizAdOeste != null &&
                                        (vizAdOeste.getOcupante() == null))) &&
                        (vizTrLeste == null ||
                                (vizTrLeste.getOcupante() == null ||
                                        (vizTrLeste.getOcupante() != null &&
                                                vizTrLeste.getOcupante().getCor() == p.getCor()))) &&
                        (vizTrOeste == null ||
                                (vizTrOeste.getOcupante() == null ||
                                        (vizTrOeste.getOcupante() != null &&
                                                vizTrOeste.getOcupante().getCor() == p.getCor())));

        return atualLivre;
    }

}
