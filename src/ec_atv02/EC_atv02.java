/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec_atv02;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gomes
 */
public class EC_atv02 {

    /**
     */
    public final float[] PALAVRA_ALVO
            = {0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f};
    private final MersenneTwisterFast twister = new MersenneTwisterFast();
    private MersenneTwisterFast twisterMutacao;
    private final PrintWriter writer;

    private List<Individuo> nova_geracao = new ArrayList<>();
    private final int tam_populacao, qtd_alelos = 11;
    private float somaFitness = 0f, taxa_mutacao, erro_minimo, tabela_mutacoes[];
    private List<Individuo> populacao;
    private int num_iteracoes = 0, max_iteracoes;
    
    // Para fins estatisticos
    int cont_mutacoes=0, pos_melhorFitness=0;

    public EC_atv02(int tam_Populacao, float taxa_Mutacao) throws FileNotFoundException, UnsupportedEncodingException {
        this.populacao = new ArrayList<>();
        this.tam_populacao = tam_Populacao;
        this.taxa_mutacao = taxa_Mutacao;
        // Será implementado se der tempo,tabela de probabilidades de mutacao
        //calculaTabelaDeMutacoes();

        //
        this.erro_minimo = 2f;
        this.max_iteracoes = 1000;

        for (int i = 0; i < tam_populacao; i++) {
            Individuo novoIndv = new Individuo();
            this.populacao.add(novoIndv);
        }

        writer = new PrintWriter("EC_atv02.csv", "UTF-8");
        writer.println("fitnessTotal;fitnessParcial;fitnessUltimos;melhorFitness");

        calculaFitness();
        run();

        writer.close();
    }

    public void run() {
        twisterMutacao = new MersenneTwisterFast();

        while (num_iteracoes < max_iteracoes && populacao.get(pos_melhorFitness).fitness < erro_minimo) {
            
            // TODO Seleção dos pais
            nova_geracao = selectParents();
            // TODO Aplicar crossover
            nova_geracao = aplicaCrossover(nova_geracao);
            
            // TODO Aplicar mutação
            aplicaMutacao(nova_geracao);
            

            // TODO Replace população
            populacao = nova_geracao;

            calculaFitness();
            num_iteracoes++;
            /*if (num_iteracoes % 100 == 0) {
                System.out.println("\tIterações: " + num_iteracoes);
            }*/
        }
        System.out.println("Iterações: " + num_iteracoes + ", Mutações: " + cont_mutacoes);
        System.out.println("Melhor fitness: " + populacao.get(pos_melhorFitness).fitness);

    }

    private float f(Individuo palavra) {
        float soma = 0;
        for (int indexAlelo = 0; indexAlelo < palavra.alelos.length; indexAlelo++) {
            soma += Math.abs(palavra.alelos[indexAlelo] - PALAVRA_ALVO[indexAlelo]);
        }
        
        return 1 / soma;
        //return twister.nextFloat();
    }

    private void calculaFitness(){
        somaFitness = 0;
        
        // Calcula soma de fitness
        for (int i=0; i<populacao.size() ; i++) {
            Individuo indv = populacao.get(i);
            
            indv.fitness = f(indv);
            somaFitness += indv.fitness;
            if (indv.fitness > indv.fitness) {
                pos_melhorFitness = i;
            }
        }
        
    }
    private List<Individuo> selectParents() {
        List<Individuo> l_selecionados = new ArrayList<>();
        // Calcula fitness
        //Collections.sort(populacao);
        

        // Gera linha de probabilidade
        float l_lkAnterior = 0;
        for (Individuo i : populacao) {
            i.likelihood = l_lkAnterior + (i.fitness / somaFitness);
            l_lkAnterior = i.likelihood;
        }

        // Roda Roleta
        float l_rnd;
        while(l_selecionados.size() < 60){
            l_rnd = twister.nextFloat();
            for (Individuo sorteado : populacao) {
                if (l_rnd <= sorteado.likelihood) {
                    l_selecionados.add(sorteado);
                    break;
                }
            }
        }

        // Finaliza
        return l_selecionados;
    }

    private List<Individuo> aplicaCrossover(List<Individuo> geracao) {
        List<Individuo> l_novaGeracao = new ArrayList<>();
        
        int l_pontoDeCorte;
        for (int indv = 0; indv < geracao.size(); indv += 2) {

            l_pontoDeCorte = twister.nextInt(10);
            Individuo filho1 = new Individuo(), filho2 = new Individuo();

            for (int i = 0; i < qtd_alelos; i++) {
                if (i <= l_pontoDeCorte) {
                    filho1.alelos[i] = geracao.get(indv).alelos[i];
                    filho2.alelos[i] = geracao.get(indv + 1).alelos[i];
                } else {
                    filho1.alelos[i] = geracao.get(indv + 1).alelos[i];
                    filho2.alelos[i] = geracao.get(indv).alelos[i];
                }
            }
            l_novaGeracao.add(filho1);
            l_novaGeracao.add(filho2);

        }

        return l_novaGeracao;
    }

    private void aplicaMutacao(List<Individuo> geracao) {
        

        float l_rnd;
        for (Individuo indv : geracao) {
            l_rnd = twisterMutacao.nextFloat();

            for (int i = 0; i < qtd_alelos; i++) {
                if (l_rnd <= taxa_mutacao) {
                    indv.alelos[i] = (indv.alelos[i] + 1) % 2;
                    //System.out.println("![M]");
                    cont_mutacoes++;
                }
            }
        }
    }

    private class Individuo {

        protected float[] alelos = new float[qtd_alelos];
        protected float fitness = 0f, likelihood = 0f;

        public Individuo() {
            for (int i=0; i < qtd_alelos; i++) {
                alelos[i] = twister.nextFloat();
            }
            //System.out.println(alelos[0] + "," + alelos[1]);
        }

    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        EC_atv02 atv = new EC_atv02(60, 0.002f);
        
        //atv.run();
    }

}
