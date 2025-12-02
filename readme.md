<img height="100" alt="Logo Unifor" width="100" align="left" src="https://github.com/user-attachments/assets/84553f6b-021b-489d-ad7d-332fa0196d7b" />

**UNIVERSIDADE DE FORTALEZA**<br>
CENTRO DE CIÊNCIAS TECNOLÓGICAS<br>
CURSO: CIÊNCIA DA COMPUTAÇÃO

---

## Análise Comparativa de Algoritmos com Uso de Paralelismo 

**Autor 1:** BIANCA ORIÁ

**Autor 2:** LORENNA AGUIAR

---

**Palavras-chave:** Computação Paralela. Processamento de Texto. Desempenho. 

---

## Resumo

Este trabalho apresenta uma análise de desempenho de algoritmos de busca e contagem de palavras em grandes volumes de texto, utilizando obras literárias clássicas (*Dracula*, *Don Quixote* e *Moby Dick*). O objetivo foi comparar a eficiência de diferentes abordagens computacionais: execução serial, execução paralela em CPU com variação de *threads* e execução massivamente paralela em GPU via OpenCL. Os resultados demonstraram que, para operações de baixa complexidade aritmética em arquivos de tamanho moderado, o *overhead* (custo de gerenciamento) da paralelização — especialmente a transferência de dados para a GPU — pode superar os ganhos de processamento, tornando a execução serial ou com poucas threads a opção mais eficiente.

---

## Introdução

A busca por padrões em textos é uma tarefa fundamental na computação, servindo de base para motores de busca, análise de dados e processamento de linguagem natural. Com os processadores *multicore* e placas gráficas de uso geral, torna-se essencial entender quando migrar uma tarefa da execução sequencial para a paralela.
Neste estudo, utilizamos três livros de domínio público com características distintas de idioma e vocabulário para testar a busca de palavras específicas (como "the", "que", "and"). O experimento visa responder se a utilização de *threads* em Java ou o uso de GPU via JOCL oferece vantagens de tempo para contagens simples de ocorrências.

---

## Metodologia

O experimento foi desenvolvido em linguagem *Java*. O *dataset* consiste nos textos integrais dos livros:
1.  **Don Quixote** (Miguel de Cervantes)
2.  **Dracula** (Bram Stoker)
3.  **Moby Dick** (Herman Melville)

Para cada livro, foram buscadas palavras de alta frequência. As métricas de tempo foram coletadas utilizando `System.nanoTime()` em três cenários distintos:
* **Serial (CPU):** Iteração simples percorrendo o vetor de palavras.
* **Paralelo (CPU):** Divisão do vetor de palavras em blocos, processados concorrentemente por 4, 6 e 12 *threads*.
* **Paralelo (GPU):** Utilização da biblioteca JOCL (OpenCL) para realizar o mapeamento de *hashes* e redução (soma) diretamente na placa de vídeo.

---

## Resultados e Discussão

Os gráficos a seguir ilustram o tempo de execução (em milissegundos) para as diferentes abordagens.

### 1. Execução Serial
A abordagem serial mostrou-se surpreendentemente eficiente, com tempos variando entre **0,8ms e 2,7ms**. Devido à simplicidade da operação (comparação de *strings*), a CPU moderna consegue processar os dados aproveitando eficientemente os caches, sem o custo de gerenciar múltiplos contextos.

<div align="center">
  
  ### 1. Execução Serial
  <img width="1200" height="1200" alt="Image" src="https://github.com/user-attachments/assets/cdd16403-e8c0-4103-b573-d30f6fafa1f0" />
  Fonte: Elaborado pelo autor.
  
</div>


### 2. Execução Paralela em CPU (4, 6 e 12 Threads)
Ao dividir a tarefa, observou-se um comportamento não linear:
* **4 e 6 Threads:** O desempenho manteve-se próximo ao serial, com ligeiros ganhos em algumas execuções, mas frequentemente o custo de criação das *threads* anulou o ganho da divisão do trabalho.
* **12 Threads:** Houve uma degradação de desempenho (tempos subindo para médias superiores a 2,0ms). Isso ocorre porque o sistema operacional gasta mais tempo alternando entre as 12 *threads* do que elas gastam efetivamente contando palavras, já que a tarefa é extremamente rápida.


<div align="center">
  
  ### 2. Execução Paralela em CPU com 4 Threads
  <img width="1200" height="1200" alt="Image" src="https://github.com/user-attachments/assets/c0c9d542-1313-4ce2-8faa-22ad174e8f03" />
  Fonte: Elaborado pelo autor.
  
</div>

<div align="center">
  
  ### 3. Execução Paralela em CPU com 6 Threads
  <img width="1200" height="1200" alt="Image" src="https://github.com/user-attachments/assets/8ad949ab-ffd0-49ac-b2cf-8497378d91b3" />
  Fonte: Elaborado pelo autor.
  
</div>


<div align="center">

  ### 4. Execução Paralela em CPU com 12 Threads
  <img width="1200" height="1200" alt="Image" src="https://github.com/user-attachments/assets/6c2e137f-ebe9-40e3-9255-e9622824c708" />
  Fonte: Elaborado pelo autor.
  
</div>


### 3. Execução em GPU (OpenCL)
A execução em GPU apresentou os maiores tempos, variando entre **2,1ms e 4,7ms**.
Embora a GPU possua milhares de núcleos, o gargalo neste experimento foi a latência de transferência de memória. O tempo necessário para copiar o vetor de palavras da memória RAM para a memória de vídeo é superior ao tempo que a CPU leva para ler e contar as palavras diretamente. Além disso, há o custo de compilação do *kernel* OpenCL e o pré-processamento dos dados (conversão para *hash*).

<div align="center">

  ### 5. Execução em GPU
  <img width="1200" height="1200" alt="Image" src="https://github.com/user-attachments/assets/ead1d26c-78fd-4f1e-844c-b2c735e45a1d" />
  Fonte: Elaborado pelo autor.
  
</div>


---

## Conclusão

A análise dos dados permite concluir que a paralelização não é uma solução universal para todos os problemas de desempenho. Para a tarefa de **Busca de Palavras** em arquivos de texto que cabem na memória principal:

1.  A execução **Serial** ou com **baixo paralelismo (4 threads)** é a mais indicada, oferecendo o melhor equilíbrio entre simplicidade e velocidade.
2.  O uso excessivo de threads (12 ou mais) em tarefas leves causa perda de desempenho devido à troca de contexto.
3.  O uso de **GPU** é ineficiente para este volume de dados específico. A GPU seria vantajosa apenas em cenários de *Big Data* (gigabytes de texto), justificando o custo de transferência de dados.

---

## Referências

ORACLE. *Java Documentation: Concurrency*. Disponível em: https://docs.oracle.com/javase/tutorial/essential/concurrency/.

JOCL. *Java Bindings for OpenCL*. Disponível em: http://www.jocl.org/.

SILBERSCHATZ, A.; GALVIN, P. B.; GAGNE, G. *Sistemas Operacionais com Java*. Rio de Janeiro: Elsevier, 2008.

---

## Anexos

Códigos das implementações disponíveis em: https://github.com/BiancaOria/BuscaLivros
