<img height="100" alt="Logo Unifor" width="100" align="left" src="https://github.com/user-attachments/assets/84553f6b-021b-489d-ad7d-332fa0196d7b" />

**UNIVERSIDADE DE FORTALEZA**<br>
CENTRO DE CIÊNCIAS TECNOLÓGICAS<br>
CURSO: CIÊNCIA DA COMPUTAÇÃO


---

## Análise Comparativa de Algoritmos com Uso de Paralelismo

**Autor 1:** BIANCA ORIÁ

**Autor 2:** LORENNA AGUIAR

---

**Palavras-chave:** Paralelização. Desempenho. Busca de Palavras

---

## Resumo

Este trabalho propõe uma análise detalhada do desempenho de diferentes algoritmos de busca em ambientes seriais e paralelos, utilizando a linguagem de programação *Java*. A busca por eficiência computacional é essencial em diversas aplicações, e entender como diferentes algoritmos se comportam em diferentes cenários de processamento é de suma importância. Neste estudo, serão abordados quatro algoritmos de busca populares — *BubbleSort, CountingSort, MergeSort e QuickSort* — , tanto em suas implementações sequenciais quanto em versões paralelizadas. Serão realizadas análises comparativas utilizando uma variedade de conjuntos de dados de entrada e ajustando o número de threads. Os resultados serão registrados em arquivos CSV, permitindo uma análise visual através de gráficos utilizando *Java*.

---

## Introdução

No contexto atual da computação e a demanda por cada vez mais desempenho, a utilização de técnicas de paralelização tornou-se essencial para melhorar o desempenho de programas. Neste contexto, este trabalho tem como objetivo analisar e comparar o desempenho de diferentes algoritmos de ordenação implementados em *Java*, tanto em versões seriais quanto paralelas. Foram escolhidos quatro métodos clássicos: *BubbleSort, CountingSort, MergeSort e QuickSort*, devido às suas diferentes características de complexidade e comportamento em ambientes concorrentes e paralelos.
A análise busca compreender como a divisão de tarefas entre múltiplas threads influencia o tempo total de execução e identificar os cenários em que a paralelização realmente oferece ganhos significativos. O estudo também contribui para reforçar conceitos de programação paralela e concorrente e otimização de desempenho em sistemas.

---

## Metodologia

Os algoritmos foram implementados em *Java*, nas versões serial e paralela, seguindo boas práticas de programação concorrente e paralela. Cada algoritmo foi executado para diferentes tamanhos de conjuntos de dados (1.000, 10.000, 100.000, 200.000, 100.000.000  elementos), variando o número de threads (2, 6, 10). Os tempos de execução foram armazenados em arquivos CSV e posteriormente analisados por meio de gráficos comparativos gerados.
A análise estatística considerou o tempo médio de execução de cada versão, permitindo identificar padrões de desempenho e relacionar o ganho de eficiência com o aumento da carga de processamento. Essa abordagem possibilitou uma avaliação dos efeitos da paralelização sobre cada algoritmo.


---

## Resultados e Discussão

Com base na metodologia descrita, foram obtidos os dados referentes ao desempenho de cada método de ordenação, tanto em sua forma serial quanto paralela. A seguir, são apresentados os resultados e a análise individual e comparativa de cada um.


Counting Sort 

A tabela a seguir detalha os tempos de execução do Counting Sort.

| Qtd. de Threads | Qtd. de Dados | Counting Sort Serial  | Counting Sort Paralelo | Diferença  |
|-----------------|----------------|---------------------------|-----------------------------|----------------|
| 2  | 1.000 | 0,014961 | 0,070853 | 0,055892 |
| 2  | 10.000 | 0,023905 | 0,043092 | 0,019187 |
| 2  | 100.000 | 0,018021 | 0,048928 | 0,030907 |
| 2  | 200.000 | 0,022957 | 0,049037 | 0,026080 |
| 2  | 100.000.000 | 2,045129 | 1,572382 | 0,472747 |
| 6  | 1.000 | 0,011711 | 0,071411 | 0,059700 |
| 6  | 10.000 | 0,010671 | 0,069446 | 0,058775 |
| 6  | 100.000 | 0,015103 | 0,084856 | 0,069753 |
| 6  | 200.000 | 0,029322 | 0,084963 | 0,055641 |
| 6  | 100.000.000 | 2,420321 | 1,651858 | 0,768463 |
| 10 | 1.000 | 0,026133 | 0,108421 | 0,082288 |
| 10 | 10.000 | 0,039272 | 0,108010 | 0,068738 |
| 10 | 100.000 | 0,012613 | 0,150239 | 0,137626 |
| 10 | 200.000 | 0,024120 | 0,161579 | 0,137459 |
| 10 | 100.000.000 | 2,156008 | 1,183859 | 0,972149 |

Gráficos de Desempenho (Counting Sort)

IMGS
Para todas as bases de dados de 1.000 a 200.000 do Counting Sort a versão paralela foi mais lenta que a serial. O overhead de paralelização o prejudicou, visto que ele já  é extremamente rápido em modo serial para esses volumes de dados. O ganho da paralelização só apareceu no teste de escala massiva (100.000.000 de elementos).

A maior redução de tempo ocorreu com 10 threads em 100.000.000 elementos, onde o tempo caiu de 2,16 s (serial) para 1,18 s (paralelo), tendo uma melhora absoluta de 0,97 segundos e percentual de 45,09%.

Merge Sort

A tabela a seguir detalha os tempos de execução do Merge Sort.

| Qtd. de Threads | Qtd. de Dados | Merge Sort Serial | Merge Sort Paralelo | Diferença |
|-----------------|----------------|------------------------|---------------------------|----------------|
| 2  | 1.000 | 0,00154 | 0,00316 | - 0,00162 |
| 2  | 10.000 | 0,00135 | 0,00308 | - 0,00173 |
| 2  | 100.000 | 0,01699 | 0,01126 |  0,00573 |
| 2  | 200.000 | 0,03005 | 0,01584 |  0,01421 |
| 2  | 100.000.000 | 20,17215 | 12,04038 | 8,13177 |
| 6  | 1.000 | 0,00009 | 0,00144 | - 0,00135 |
| 6  | 10.000 | 0,00326 | 0,00352 | - 0,00026 |
| 6  | 100.000 | 0,01250 | 0,00524 |  0,00726 |
| 6  | 200.000 | 0,02780 | 0,00980 |  0,01800 |
| 6  | 100.000.000 | 17,97769 | 6,11711 | 11,86058 |
| 10 | 1.000 | 0,00007 | 0,00155 | - 0,00148 |
| 10 | 10.000 | 0,00085 | 0,00154 | - 0,00069 |
| 10 | 100.000 | 0,01113 | 0,00484 | 0,00629 |
| 10 | 200.000 | 0,02340 | 0,00722 |  0,01618 |
| 10 | 100.000.000 | 17,98048 | 4,36510 | 13,61538 |

Gráficos de Desempenho (Merge Sort)
IMGS
O Merge Sort seguiu o padrão esperado: sofreu com o overhead em volumes de dados pequenos (1.000 e 10.000), mas escalou excelentemente com o aumento dos dados. Sua natureza "dividir para conquistar" mostrou-se ideal para a paralelização.

A maior redução de tempo ocorreu com 10 threads em 100.000.000 elementos, onde o tempo caiu de 17,98 s (serial) para 4,37 s (paralelo), tendo uma melhora absoluta de 13,61 segundos e percentual de 75,72%.

Quick Sort

A tabela a seguir detalha os tempos de execução do Quick Sort.

| Qtd. de Threads | Qtd. de Dados | Quick Sort Serial| Quick Sort Paralelo | Diferença  |
|-----------------|----------------|-------------------------|---------------------------|----------------|
| 2 | 1.000 | 0,00199 | 0,00379 | - 0,0018 |
| 2 | 10.000 | 0,00074 | 0,00123 | - 0,0005 |
| 2 | 100.000 | 0,00663 | 0,00503 | 0,0016 |
| 2 | 200.000 | 0,01366 | 0,00964 | 0,0040 |
| 2 | 100.000.000 | 10,32257 | 8,28727 | 2,0353 |
| 6 | 1.000 | 0,00007 | 0,00111 | - 0,0010 |
| 6 | 10.000 | 0,00058 | 0,00138 | - 0,0008 |
| 6 | 100.000 | 0,00715 | 0,00614 | 0,0010 |
| 6 | 200.000 | 0,01418 | 0,00630 | 0,0079 |
| 6 | 100.000.000 | 9,61337 | 4,71663 | 4,8967 |
| 10 | 1.000 | 0,00005 | 0,00145 | - 0,0014 |
| 10 | 10.000 | 0,00058 | 0,00162 | - 0,0010 |
| 10 | 100.000 | 0,00736 | 0,00621 | 0,0011 |
| 10 | 200.000 | 0,01376 | 0,00617 | 0,0076 |
| 10 | 100.000.000 | 9,62271 | 3,74886 | 5,8738 |


Gráficos de Desempenho (Quick Sort)
IMGS
O Quick Sort também exibiu o padrão de overhead para dados pequenos, com a versão paralela sendo mais lenta até 10.000 elementos. Os ganhos de desempenho aconteceram a partir de 100.000 elementos, com o melhor resultado no limite do teste.

A maior redução de tempo ocorreu com 10 threads em 100.000.000 elementos, onde o tempo caiu de 9,62 s (serial) para 3,75 s (paralelo), tendo uma melhora absoluta de 5,87 segundos e percentual de 61,04%.

---

## Conclusão

Com base nos experimentos realizados, conclui-se que a paralelização pode trazer benefícios consideráveis em algoritmos adequados à divisão de tarefas, porém a  paralelização só tende a ser vantajosa para grandes volumes de dados, pois em volumes pequenos, o overhead torna o processo mais lento que a execução serial. A análise também evidenciou a importância de ajustar o número de threads e o tamanho dos dados para otimizar o desempenho. Isso se comprova ao ver que 10 threads em dados pequenos (ex: 10.000) foi mais lento do que 2 threads, mostrando que a configuração deve ser balanceada.
O Merge Sort foi o que melhor se adaptou à paralelização, tendo a maior redução percentual (75,72%).
O Bubble Sort teve a maior redução de tempo absoluto (25,96s), mas isso se deve ao seu péssimo desempenho serial para um grande volume de dados.
Embora o Merge Sort tenha o maior ganho percentual, o Counting Sort foi o algoritmo mais rápido de todo o experimento. Sua versão serial (aprox. 2,15s) superou as versões paralelas do Merge (4,37s) e Quick Sort (3,75s). Isso prova que a escolha do algoritmo certo para o problema é mais impactante do que a paralelização em si.
A análise também evidenciou a importância de ajustar o número de threads e o tamanho dos dados para otimizar o desempenho.
Como continuidade, sugere-se expandir o estudo para ambientes distribuídos, visto que a paralelização em um único sistema mostrou ganhos, o próximo passo lógico é investigar se esses ganhos se mantêm ou se ampliam em sistemas distribuídos, que podem lidar com volumes de dados ainda maiores.

---

## Referências

ORACLE. *Java Platform, Standard Edition Documentation.* Disponível em: https://docs.oracle.com/javase/.

DEV MEDIA. Threads: paralelizando tarefas com os diferentes recursos do Java. Disponível em: https://www.devmedia.com.br/threads-paralelizando-tarefas-com-os-diferentes-recursos-do-java/34309. Acesso em: 28 out. 2025.

NORÕES, Izequiel. Aula 11 – Computação Paralela e Concorrente. Fortaleza: Universidade de Fortaleza – UNIFOR, 2024. Material de aula (Sistemas Operacionais).


---

## Anexos

Códigos das implementações disponíveis em: https://github.com/BiancaOria/BuscaLivros


