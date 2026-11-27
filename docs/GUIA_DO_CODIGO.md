# Guia de Estudo do Código: Biblioteca Comunitária Vila Leitura

> Material de revisão para a apresentação de POO e Linguagem de Programação I (semana da P2).
> Gerado a partir do código que está em `Biblioteca_System` em 27/11/2026. Se você mudar o código depois, confira se os trechos citados aqui continuam iguais.

## Como usar este guia

1. Leia a **Parte 1** (visão geral e arquitetura) para ter o mapa do sistema na cabeça.
2. Use a **Parte 3** (classe por classe) com o VS Code aberto ao lado, abrindo cada arquivo citado.
3. Estude a **Parte 4** (conceitos de POO): é a parte que o professor de POO mais vai cobrar.
4. A **Parte 7** (regras de negócio x código) responde ao professor de LP1, que conhece a especificação do PDF.
5. Termine com a **Parte 10** (perguntas prováveis) e treine as respostas em voz alta com sua dupla.

## Sumário

- Parte 1. Visão geral e arquitetura
- Parte 2. Como rodar o projeto
- Parte 3. Todas as classes, uma a uma
- Parte 4. Onde está cada conceito de POO
- Parte 5. Camada DAO e banco de dados
- Parte 6. Telas e fluxo de navegação
- Parte 7. Requisitos (RF) e regras de negócio (RN) no código
- Parte 8. Tratamento de exceções
- Parte 9. Fluxos completos passo a passo
- Parte 10. Perguntas prováveis dos professores (com respostas)
- Parte 11. Pontos de atenção e limitações conhecidas
- Parte 12. Glossário rápido

---

# Parte 1. Visão geral e arquitetura

## 1.1 O que o sistema faz

É um sistema desktop para a **Biblioteca Comunitária Vila Leitura** (o Tema 4 da especificação). Ele controla:

- **Livros** (títulos do acervo) e suas **cópias** (exemplares físicos, cada um com etiqueta tipo `101-2`);
- **Leitores**, de três tipos: Estudante, Professor e Comunidade, cada um com prazo e limite próprios;
- **Funcionários**, de dois cargos: Bibliotecário e Voluntário, com login e senha;
- **Empréstimos** e **devoluções**, com cálculo de atraso e multa (R$ 0,50 por dia);
- **Reservas** com fila por livro, separação da cópia por 3 dias e expiração;
- **Relatórios**: acervo, circulação, atrasos e multas, perfil de uso (matriz tipo de leitor x categoria) e exportação da lista de cobrança em `.txt`.

## 1.2 Tecnologias

| Item | Versão / detalhe | Onde aparece |
|---|---|---|
| Java | 25 (LTS) | `pom.xml` → `<maven.compiler.release>25` |
| Build | Maven | `pom.xml` (mesma ideia do `poo12` do professor) |
| Interface | Swing (`JFrame`, `JTable`, `JOptionPane`...) | pacote `ui` |
| Visual | FlatLaf 3.6.2 (tema claro e escuro) | `ui/Estilo.java` |
| Banco | MySQL 8.0.16+ | `dao/*` (cada DAO cria a sua tabela com `CREATE TABLE IF NOT EXISTS`) |
| Driver JDBC | `mysql-connector-j` 8.4.0 | `pom.xml` |
| Datas | `java.time.LocalDate` | entidades, DAOs e telas |
| Execução | `exec-maven-plugin` (`mvn compile exec:java`) | `pom.xml` |

## 1.3 Arquitetura em três camadas

O projeto segue a mesma organização do exemplo `poo12` do professor (`Cliente`, `dao/ClienteDAO`, `view/FrmCliente`), só que com mais classes:

```
            ┌─────────────────────────────────────────────┐
  Usuário → │  ui  (telas Swing: FrmLogin, FrmPrincipal,  │
            │       FrmLivro, FrmEmprestimo, FrmRel*, ...)  │  ← captura erros e mostra mensagens
            └───────────────┬─────────────────────────────┘
                            │ cria objetos e chama métodos
            ┌───────────────▼─────────────────────────────┐
            │  com.fatec.biblioteca (entidades / modelo)   │
            │  Pessoa, Leitor, Funcionario, Livro, Copia,   │  ← regras de negócio (validam
            │  Emprestimo, Reserva, PerfilUso, enums ...   │     e lançam Exception)
            └───────────────┬─────────────────────────────┘
                            │ objetos prontos são gravados/lidos
            ┌───────────────▼─────────────────────────────┐
            │  dao  (DAO abstrata + LivroDAO, CopiaDAO,     │  ← SQL, JDBC, transações
            │        LeitorDAO, FuncionarioDAO,            │     (lança SQLException)
            │        EmprestimoDAO, ReservaDAO, BancoDados)│
            └───────────────┬─────────────────────────────┘
                            ▼
                      MySQL (6 tabelas)
```

**A ideia central para explicar:** a tela não sabe SQL, o DAO não sabe Swing e as entidades não sabem nem de tela nem de banco. Cada camada tem uma responsabilidade.

- **Entidades (raiz do pacote):** guardam os dados e as **regras**. Exemplo: `Leitor.validarNovoEmprestimo(...)` sabe dizer se o leitor pode pegar outro livro; `Emprestimo.registrarDevolucao(...)` calcula a multa.
- **DAO:** só traduz objetos ↔ tabelas. Exemplo: `EmprestimoDAO.registrarEmprestimo(...)` faz o `INSERT` e o `UPDATE` da cópia dentro de uma transação.
- **UI:** lê o que o usuário digitou, monta os objetos, chama as regras e o DAO, e mostra o resultado. Todo `try/catch` que mostra erro ao usuário está aqui.

## 1.4 Estrutura de pastas

```
Biblioteca_System/
├── pom.xml                      dependências (MySQL, FlatLaf) e plugin exec
├── db.properties                url, usuário e senha do MySQL (não vai pro GitHub)
├── README.md                    como rodar e mapa menu x requisitos
├── docs/
│   └── GUIA_DO_CODIGO.md        este guia
└── src/main/
    ├── java/com/fatec/biblioteca/
    │   ├── Main.java
    │   ├── Pessoa, Leitor, Estudante, Professor, Comunidade
    │   ├── Funcionario, Bibliotecario, Voluntario
    │   ├── Livro, Copia, Emprestimo, Reserva, PerfilUso, ListaCobranca
    │   ├── Categoria, SituacaoCopia, SituacaoReserva          (enums)
    │   ├── Constantes, Datas, Validador, Sessao               (utilitárias)
    │   ├── dao/  DAO, BancoDados, LivroDAO, CopiaDAO, LeitorDAO,
    │   │         FuncionarioDAO, EmprestimoDAO, ReservaDAO
    │   └── ui/   FrmLogin, FrmPrincipal, FrmLivro, FrmAdicionarCopias, FrmLeitor,
    │             FrmFuncionario, FrmEmprestimo, FrmDevolucao, FrmReserva,
    │             FrmConsultaLivros, FrmConsultaLeitores, FrmConsultaFuncionarios,
    │             FrmConsultaReservas, FrmEmprestimosAbertos, FrmRelCirculacao,
    │             FrmRelAtrasos, FrmRelPerfilUso, FrmExportarCobranca,
    │             FrmAlterarSenha, Estilo, Mensagem, Tabela, PainelFundo
    └── resources/imagens/       vila_leitura.jpg (fundo), estante.png (cabeçalhos)
```

Números para ter na ponta da língua: **22 tipos no pacote raiz** (19 classes e 3 enums), **8 classes no `dao`** e **23 classes no `ui`** (19 telas `Frm*` e 4 de apoio), totalizando **53 tipos**, e **6 tabelas** no banco.

## 1.5 Diagrama de classes (resumo)

```
                        «abstract» Pessoa
                        - codigo : int
                        - nome : String
                        + getDescricaoTipo() : String  «abstract»
                 ┌──────────────┴───────────────┐
        «abstract» Leitor                «abstract» Funcionario
        + getTipo()        «abstract»     + getCargo()               «abstract»
        + getPrazoDias()   «abstract»     + podeGerenciarCadastros() «abstract»
        + getLimiteLivros()«abstract»     - login, senhaHash
        - emprestimosAbertos: List<Emprestimo>  ◇── agregação
      ┌─────────┼──────────┐               ┌───────┴────────┐
  Estudante  Professor  Comunidade    Bibliotecario     Voluntario
  7d / 2     14d / 4    7d / 1        gerencia: sim     gerencia: não


  Livro ◆────────── Copia            (composição: o livro cria as suas cópias)
   │  1        0..*  - situacao : SituacaoCopia
   │
   ◇── filaReservas : List<Reserva> (agregação)

  Emprestimo ──► Copia, Leitor, Funcionario (empréstimo), Funcionario (devolução)   (associação)
  Reserva    ──► Livro, Leitor, Funcionario, Copia separada                         (associação)

  «abstract» DAO ◄── LivroDAO, CopiaDAO, LeitorDAO, FuncionarioDAO, EmprestimoDAO, ReservaDAO
  JFrame ◄── todas as telas Frm*      DefaultTableModel ◄── Tabela      JPanel ◄── PainelFundo
```

---

# Parte 2. Como rodar o projeto

1. **Pré-requisitos:** JDK 25, MySQL 8 rodando em `localhost:3306`, VS Code com o *Extension Pack for Java*.
2. **Configuração do banco:** crie `db.properties` na mesma pasta do `pom.xml`:
   ```properties
   url=jdbc:mysql://localhost:3306/biblioteca_vila_leitura?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
   usuario=root
   senha=SUA_SENHA
   ```
   - `createDatabaseIfNotExist=true` faz o MySQL criar o banco sozinho.
   - O `db.properties` está no `.gitignore`, então a senha não vai para o GitHub.
3. **Executar:** abra `Main.java` e clique em **Run**, ou no terminal `mvn compile exec:java`.
4. **Primeira execução:** `BancoDados.inicializar()` cria as 6 tabelas e o usuário **admin / admin** (Bibliotecário).
5. **Não há script SQL para rodar:** o banco e as tabelas são criados pelo próprio programa (cada DAO tem `criarNovaTabela()` com `CREATE TABLE IF NOT EXISTS`, igual ao `poo12`). Para a demonstração, cadastre os dados pelas telas e crie um funcionário Voluntário em *Cadastros > Funcionários*, para mostrar que ele não acessa essa tela.

**Como o programa acha o `db.properties`?** No bloco `static { ... }` da classe `DAO` ele tenta, nesta ordem: a pasta atual, `Biblioteca_System/db.properties`, e até 5 pastas acima de onde as classes compiladas estão. Se não achar, tenta o classpath, e se ainda assim não achar usa `root` sem senha. A origem usada é mostrada no console e na mensagem de erro de conexão.

**Roteiro sugerido para a demonstração ao vivo:**

1. Login com `admin` → mostrar a tela principal com os cartões e o menu.
2. Cadastrar um livro com 1 exemplar → mostrar a ficha (RF04).
3. Cadastrar um leitor Comunidade (limite 1).
4. Emprestar o livro → mensagem com nº do empréstimo e data prevista (+7 dias).
5. Tentar emprestar outro livro para o mesmo leitor → erro de limite (RN07).
6. Com outro leitor, tentar emprestar a mesmo livro → "não há exemplares" → fazer reserva.
7. Devolver com data atrasada → multa calculada → mensagem "exemplar separado para a reserva".
8. Mostrar Relatórios: Atrasos e multas, Perfil de uso, Exportar lista de cobrança.
9. Trocar usuário para o Voluntário que você cadastrou → botão "Funcionários" desabilitado (polimorfismo).
10. Ligar o tema escuro em *Sistema > Tema escuro* (detalhe visual, opcional).

---

# Parte 3. Todas as classes, uma a uma

Legenda dos tipos: **classe** (concreta), **classe abstrata**, **enum**, **classe utilitária** (`final` com construtor privado e só membros `static`).

## 3.1 Pacote `com.fatec.biblioteca` (entidades e utilitárias)

### `Pessoa`: classe abstrata

- **Herda de:** `Object` (implicitamente).
- **Subclasses:** `Leitor`, `Funcionario`.
- **Papel:** reúne o que leitores e funcionários têm em comum (código e nome). Existe para não repetir código e para permitir polimorfismo.

| Atributo | Tipo | Visibilidade |
|---|---|---|
| `codigo` | `int` | `private` |
| `nome` | `String` | `private` |

| Método | O que faz |
|---|---|
| `getCodigo()` / `setCodigo(int) throws Exception` | Acesso ao código. O `set` lança `Exception` para que subclasses possam validar (o `Leitor` sobrescreve). |
| `getNome()` / `setNome(String) throws Exception` | O `set` valida pelo `Validador.textoObrigatorio`: não vazio e sem `;` (RN02/RN05, V4.10). |
| `getDescricaoTipo()` **abstrato** | Cada subclasse diz o que é: "Estudante", "Bibliotecário" etc. |
| `toString()` | Retorna `"codigo - nome"`. Sobrescreve o de `Object`. |

```java
public abstract class Pessoa {
    private int codigo;
    private String nome;
    ...
    public abstract String getDescricaoTipo();
}
```

### `Leitor`: classe abstrata

- **Herda de:** `Pessoa`. **Subclasses:** `Estudante`, `Professor`, `Comunidade`.
- **Papel:** concentra as regras de circulação do leitor (limite, atraso, livro repetido, reserva).

| Atributo / constante | Tipo | Observação |
|---|---|---|
| `ESTUDANTE`, `PROFESSOR`, `COMUNIDADE` | `public static final int` (1, 2, 3) | Mesmo código gravado na coluna `leitor.tipo`. |
| `emprestimosAbertos` | `List<Emprestimo>` (`ArrayList`) | **Agregação**: os empréstimos existem por si; o leitor só os referencia. |
| `qtdEmprestimosAbertos` | `int` | Usado na listagem (RF07), quando só o número vem do banco, sem a lista. |

| Método | O que faz |
|---|---|
| `getTipo()`, `getPrazoDias()`, `getLimiteLivros()` **abstratos** | Cada subclasse responde com seus valores (polimorfismo). |
| `static Leitor criar(int tipo) throws Exception` | **Método fábrica**: devolve `new Estudante()`, `new Professor()` ou `new Comunidade()` conforme o tipo. Tipo inválido → exceção. |
| `static String[] nomesTipos()` | Textos do `JComboBox` da tela de leitores. |
| `setCodigo(int)` **@Override** | Valida 1 a 9999 (RN05) e chama `super.setCodigo(codigo)`. |
| `calcularDataPrevista(LocalDate)` | `dataEmprestimo.plusDays(getPrazoDias())` (RN10). A chamada polimórfica decide se soma 7 ou 14. |
| `atingiuLimite()` | `getQtdEmprestimosAbertos() >= getLimiteLivros()` (RN07). |
| `getEmprestimoEmAtraso(LocalDate)` | Procura um empréstimo aberto com data prevista anterior à data informada (RN08). |
| `possuiLivroEmprestado(int codigoLivro)` | Já está com esse livro? (RN09). |
| `validarNovoEmprestimo(Livro, LocalDate) throws Exception` | Junta RN07, RN08 e RN09; lança `Exception` com mensagem amigável na primeira regra violada. |
| `validarNovaReserva(Livro, LocalDate) throws Exception` | Regras de reserva R1 a R3 (atraso, já está com o livro, já tem reserva viva, ainda há cópia disponível). |
| `getSituacao()` | Texto `"1 de 2"` pedido no RF07. |
| `setEmprestimosAbertos(List)` | Guarda a lista e atualiza a quantidade. |
| `getQtdEmprestimosAbertos()` | `Math.max(contador, lista.size())`: funciona tanto quando veio a lista quanto quando veio só a contagem. |

```java
public void validarNovoEmprestimo(Livro livro, LocalDate data) throws Exception {
    if (atingiuLimite()) { throw new Exception("Limite de empréstimos simultâneos atingido ..."); } // RN07
    Emprestimo atrasado = getEmprestimoEmAtraso(data);
    if (atrasado != null) { throw new Exception("O leitor possui empréstimo em atraso ..."); }      // RN08
    if (possuiLivroEmprestado(livro.getCodigo())) { throw new Exception("... deste livro."); }         // RN09
}
```

### `Estudante`, `Professor`, `Comunidade`: classes concretas

- **Herdam de:** `Leitor`. Só sobrescrevem os métodos abstratos com a política do PDF:

| Classe | `getTipo()` | `getPrazoDias()` | `getLimiteLivros()` | `getDescricaoTipo()` |
|---|---|---|---|---|
| `Estudante` | 1 | 7 | 2 | "Estudante" |
| `Professor` | 2 | 14 | 4 | "Professor" |
| `Comunidade` | 3 | 7 | 1 | "Comunidade" |

Ponto para a apresentação: no PDF a política está em **arrays paralelos** (`PRAZO_POR_TIPO = {7, 14, 7}` e `LIMITE_POR_TIPO = {2, 4, 1}`). Aqui ela virou **polimorfismo**: cada tipo de leitor "sabe" seu prazo e seu limite. O comentário em `Constantes.java` explica isso.

### `Funcionario`: classe abstrata

- **Herda de:** `Pessoa`. **Subclasses:** `Bibliotecario`, `Voluntario`.
- **Papel:** quem opera o sistema; tem login e senha (guardada como hash SHA-256).

| Atributo / constante | Tipo |
|---|---|
| `BIBLIOTECARIO = 1`, `VOLUNTARIO = 2` | `public static final int` |
| `login` | `String` (privado) |
| `senhaHash` | `String` (privado, 64 caracteres hexadecimais) |

| Método | O que faz |
|---|---|
| `getCargo()` **abstrato** | 1 ou 2. |
| `podeGerenciarCadastros()` **abstrato** | Bibliotecário `true`, Voluntário `false` (regra de permissão "F1"). Usado na tela principal. |
| `static Funcionario criar(int cargo)` | Método fábrica, igual ao de `Leitor`. |
| `static String[] nomesCargos()` | Itens do combo de cargo. |
| `autenticar(String senha)` | Gera o hash da senha digitada e compara com o guardado. |
| `setSenha(String) throws Exception` | Exige no mínimo 4 caracteres e guarda **só o hash**. |
| `static gerarHash(String)` | `MessageDigest.getInstance("SHA-256")` e converte os bytes para hexadecimal. |
| `setLogin(String) throws Exception` | Obrigatório, sem espaços, até 30 caracteres, salvo em minúsculas. |
| `getSenhaHash()` / `setSenhaHash(String)` | Usados pelo DAO para gravar/ler a coluna `senha_hash`. |

### `Bibliotecario`, `Voluntario`: classes concretas

| Classe | `getCargo()` | `podeGerenciarCadastros()` | `getDescricaoTipo()` |
|---|---|---|---|
| `Bibliotecario` | 1 | `true` | "Bibliotecário" |
| `Voluntario` | 2 | `false` | "Voluntário" |

### `Livro`: classe concreta

- **Papel:** o título do acervo (mesmo nome usado no PDF). Dono das suas cópias e da fila de reservas.

| Atributo | Tipo | Observação |
|---|---|---|
| `codigo` | `int` | 100 a 9999 (RN01) |
| `titulo`, `autor` | `String` | obrigatórios, sem `;` (RN02) |
| `categoria` | `Categoria` (enum) | RN03 |
| `anoPublicacao` | `int` | 1450 até o ano atual (RN03) |
| `copias` | `final List<Copia>` | **composição** |
| `filaReservas` | `final List<Reserva>` | **agregação** (reservas vivas, em ordem de chegada) |

| Método | O que faz |
|---|---|
| `setCodigo`, `setTitulo`, `setAutor`, `setCategoria`, `setAnoPublicacao` (todos `throws Exception`) | Cada setter valida a sua regra. É o **encapsulamento** fazendo a validação. |
| `adicionarCopias(int quantidade)` | Valida 1 a 50 (RN04), descobre o próximo número livre e cria as cópias com `new Copia(this, numero)`. Devolve só as novas, para o DAO gravar. |
| `adicionarCopiaCarregada(Copia)` | Usado pelo DAO ao ler do banco: liga a cópia ao livro. |
| `getTotalCopias()`, `getQtdDisponiveis()`, `getQtdEmprestadas()`, `getQtdReservadas()` | Contam as cópias por situação (método privado `contar(SituacaoCopia)`). |
| `getCopia(int codigoCopia)` | Busca uma cópia pelo código. |
| `getCopiaParaEmprestimo(Leitor) throws Exception` | Se o leitor tem reserva AGUARDANDO, devolve a cópia separada para ele; senão, a primeira DISPONÍVEL; se não houver, lança "Não há exemplares disponíveis... faça uma reserva" (RN06). |
| `getReservaViva(Leitor)` | Reserva ATIVA ou AGUARDANDO desse leitor. |
| `getReservaAguardando(Leitor)` | Só se estiver AGUARDANDO retirada. |
| `getPosicaoNaFila(Reserva)` | 1º, 2º, ... contando só as reservas vivas. |
| `toString()` | `"codigo - titulo"`. |

**Detalhe importante:** o livro **não guarda** "quantidade disponível" num atributo, como o array `exemplaresDisponiveis` do PDF. A quantidade é **calculada** a partir da situação de cada cópia. Por isso a RN15 (disponíveis entre 0 e o total) é sempre verdadeira por construção.

### `Copia`: classe concreta

- **Papel:** um exemplar físico. Parte do `Livro` (**composição**).

| Atributo | Tipo |
|---|---|
| `codigo` | `int` (gerado pelo banco, `AUTO_INCREMENT`) |
| `livro` | `Livro` |
| `numero` | `int` (1, 2, 3... dentro do livro) |
| `situacao` | `SituacaoCopia` (começa `DISPONIVEL`) |

| Método | O que faz |
|---|---|
| construtor `Copia(Livro, int)` **sem `public`** (visibilidade de pacote) | Só classes do mesmo pacote criam cópias "novas", na prática o `Livro.adicionarCopias`. Reforça a composição. |
| construtor `public Copia()` | Usado pelos DAOs ao montar a partir do banco. |
| `emprestar() throws Exception` | Se já está EMPRESTADA → erro; senão vira EMPRESTADA. |
| `devolver() throws Exception` | Só pode devolver se está EMPRESTADA; volta a DISPONIVEL. |
| `reservar() throws Exception` | Só se DISPONIVEL; vira RESERVADA (separada para alguém da fila). |
| `liberar()` | Se RESERVADA, volta a DISPONIVEL (reserva cancelada ou expirada). |
| `getEtiqueta()` | `"codigoLivro-numero"`, ex.: `101-2`. |

A situação só muda **pelos métodos de negócio** (`emprestar`, `devolver`, `reservar`, `liberar`), que conferem se a transição é válida. Isso é uma pequena **máquina de estados** e é o comentário "RN15" no topo da classe.

```
 DISPONIVEL ──emprestar()──► EMPRESTADA ──devolver()──► DISPONIVEL
     │                                                      ▲
     └──reservar()──► RESERVADA ──liberar()─────────────────┘
                         │
                         └── (leitor retira) emprestar() ──► EMPRESTADA
```

### `Emprestimo`: classe concreta

- **Papel:** o registro de uma retirada. **Associação** com `Copia`, `Leitor` e dois `Funcionario` (quem emprestou e quem recebeu a devolução).

| Atributo | Tipo |
|---|---|
| `codigo` | `int` (sequencial, `AUTO_INCREMENT`) |
| `copia` | `Copia` |
| `leitor` | `Leitor` |
| `funcionarioEmprestimo`, `funcionarioDevolucao` | `Funcionario` |
| `dataEmprestimo`, `dataPrevista`, `dataDevolucao` | `LocalDate` (`dataDevolucao == null` → em aberto) |
| `multa` | `double` |

| Método | O que faz |
|---|---|
| construtor `Emprestimo(Copia, Leitor, Funcionario, LocalDate) throws Exception` | Chama `copia.emprestar()` e calcula `dataPrevista = leitor.calcularDataPrevista(data)`. **Polimorfismo**: não precisa de `if` por tipo de leitor. |
| construtor vazio | Usado pelo DAO. |
| `estaAberto()` | `dataDevolucao == null`. |
| `calcularDiasAtraso()` | Atraso usando a data da devolução (para empréstimos devolvidos). |
| `calcularDiasAtraso(LocalDate ref)` | Atraso em relação a uma data de referência. **Sobrecarga** do método acima (RN12). |
| `calcularMulta(LocalDate)` | `diasAtraso * Constantes.MULTA_POR_DIA` (RN12). |
| `registrarDevolucao(LocalDate, Funcionario) throws Exception` | Valida RN11 (não devolvido ainda; data não anterior ao empréstimo), chama `copia.devolver()`, guarda data, funcionário e multa. |
| `getStatus(LocalDate)` | `"NO PRAZO"` ou `"ATRASADO há N dia(s)"` (RF10). |
| `getLivro()` | Atalho para `copia.getLivro()`. |

```java
public int calcularDiasAtraso(LocalDate dataReferencia) {
    long dias = ChronoUnit.DAYS.between(dataPrevista, dataReferencia);
    return dias > 0 ? (int) dias : 0;   // RN12: quando positivo; senão 0
}
```

### `Reserva`: classe concreta

- **Papel:** pedido de um leitor por um livro sem exemplar disponível. **Associação** com `Livro`, `Leitor`, `Funcionario` e a `Copia` separada.
- **Observação:** reservas **não estão no PDF** (que é da disciplina de LP1). Foram acrescentadas porque o professor de POO pediu "controlar reservas". As regras R1 a R7 vêm da proposta do projeto (ver Parte 7.3).

| Atributo | Tipo |
|---|---|
| `codigo` | `int` |
| `livro`, `leitor`, `funcionario` | `Livro`, `Leitor`, `Funcionario` |
| `dataReserva` | `LocalDate` |
| `copiaSeparada` | `Copia` (preenchida quando chega a vez do leitor) |
| `dataLimiteRetirada` | `LocalDate` (separação + 3 dias) |
| `codigoEmprestimo` | `int` (quando a reserva vira empréstimo) |
| `situacao` | `SituacaoReserva` (começa `ATIVA`) |

| Método | O que faz |
|---|---|
| `separarCopia(Copia, LocalDate) throws Exception` | R4: só para reserva ATIVA; chama `copia.reservar()`, define o prazo de 3 dias e muda para AGUARDANDO. |
| `prazoVencido(LocalDate)` | R6: AGUARDANDO e data depois do limite. |
| `expirar()` | R6: libera a cópia e muda para EXPIRADA. |
| `cancelar() throws Exception` | R7: só se ainda viva; libera a cópia e muda para CANCELADA. |
| `atender(Emprestimo)` | R5: muda para ATENDIDA e guarda o código do empréstimo. |

Ciclo de vida da reserva:

```
ATIVA ──(cópia separada)──► AGUARDANDO ──(leitor retira)──► ATENDIDA
  │                              │
  │                              └──(passou 3 dias)──► EXPIRADA
  └────────(cancelar)──────► CANCELADA ◄──(cancelar)──┘
```

### `PerfilUso`: classe concreta

- **Papel:** a **matriz** `int[3][5]` do PDF (tipo de leitor x categoria), RF13.

| Membro | Tipo / o que faz |
|---|---|
| `NOMES_TIPOS` | `public static final String[]` {"Estudante", "Professor", "Comunidade"} |
| `matriz` | `private final int[][] = new int[3][5]` |
| `incrementar(tipo, categoria, qtd)` | Soma em `matriz[tipo-1][categoria-1]` (os códigos começam em 1, os índices em 0). |
| `getValor(linha, coluna)` | Lê uma célula. |
| `totalLinha(l)`, `totalColuna(c)`, `totalGeral()` | Somas com `for`. |
| `categoriaPreferida(linha)` | Maior valor da linha → categoria mais emprestada por aquele tipo. |
| `categoriaMaisProcurada()` | Maior total de coluna. Em empate, fica a primeira (usa `>` e não `>=`). |

A matriz é preenchida no `EmprestimoDAO.carregarPerfilUso()` com um `GROUP BY` que conta **todos** os empréstimos (abertos ou devolvidos). Por isso ela "não é alterada na devolução", como pede a RN13.

### `ListaCobranca`: classe concreta

- **Papel:** gerar e gravar a lista de cobrança (RF14).

| Membro | O que faz |
|---|---|
| `dataReferencia : LocalDate`, `atrasados : List<Emprestimo>` (ambos `final`) | Dados da lista. |
| `getNomeArquivo()` | `"atrasos_" + data + ".txt"` → ex.: `atrasos_2026-11-27.txt` (o PDF usa `atrasos_dia<N>.txt` porque lá a data é o dia do ano). |
| `getQuantidade()`, `getTotalMultas()` | Totais do rodapé. |
| `gerarTexto()` | Monta o relatório com `String.format` em colunas alinhadas. |
| `exportar() throws IOException` | Grava com `BufferedWriter`/`FileWriter` em UTF-8 usando **try-with-resources** e devolve o caminho absoluto. |
| `cortar(String, int)` (privado, estático) | Encurta textos longos para caber na coluna. |

### `Categoria`: enum

```java
public enum Categoria {
    LITERATURA(1, "Literatura"), DIDATICO(2, "Didático"), TECNICO(3, "Técnico"),
    INFANTOJUVENIL(4, "Infantojuvenil"), OUTROS(5, "Outros");
    private final int codigo;
    private final String nome;
    ...
}
```

- É um **enum com atributos e construtor** (o construtor de enum é sempre privado).
- `getCodigo()` (1 a 5, o valor gravado no banco), `getNome()`, `getIndice()` (`codigo - 1`, índice da coluna da matriz).
- `static fromCodigo(int) throws Exception`: converte o número do banco de volta para o enum.
- `toString()` sobrescrito → `"1 - Literatura"` (é o que aparece no `JComboBox`).
- Substitui o array `NOMES_CATEGORIAS` do PDF e garante que só existam 5 categorias válidas (RN03).

### `SituacaoCopia`: enum

`DISPONIVEL`, `EMPRESTADA`, `RESERVADA`. Gravado no banco como texto (`name()`) e lido com `SituacaoCopia.valueOf(...)`.

### `SituacaoReserva`: enum com método

`ATIVA`, `AGUARDANDO`, `ATENDIDA`, `CANCELADA`, `EXPIRADA`, e o método `isViva()` que devolve `true` para ATIVA ou AGUARDANDO. Mostra que enum em Java pode ter comportamento.

### `Constantes`: classe utilitária (`final`, construtor privado)

| Constante | Valor | Regra |
|---|---|---|
| `MAX_LIVROS` | 200 | RN14 (mesmo nome do PDF) |
| `MAX_LEITORES` | 100 | RN14 |
| `MAX_EMPRESTIMOS` | 500 | RN14 |
| `EXEMPLARES_MIN` / `EXEMPLARES_MAX` | 1 / 50 | RN04 |
| `ANO_MINIMO` | 1450 | RN03 |
| `MULTA_POR_DIA` | 0.50 | RN12 |
| `PRAZO_RETIRADA_RESERVA` | 3 | R4 |
| `anoAtual()` | `LocalDate.now().getYear()` | substitui `ANO_ATUAL = 2026` fixo do PDF |

### `Datas`: classe utilitária

- Formato fixo `dd/MM/uuuu` com `ResolverStyle.STRICT` (rejeita 31/02, por exemplo).
- `converter(String) throws Exception`: texto → `LocalDate`, traduzindo `DateTimeParseException` para uma mensagem em português.
- `formatar(LocalDate)`: `LocalDate` → texto (`""` se null).
- `hoje()`: data atual formatada (valor inicial dos campos de data).

### `Validador`: classe utilitária

- `textoObrigatorio(texto, campo)`: não vazio e sem `;` (RN02, V4.10). Devolve o texto sem espaços nas pontas.
- `inteiro(texto, campo)`: `Integer.parseInt`, transformando `NumberFormatException` ou `NullPointerException` em `Exception` com mensagem amigável ("Código deve ser um número inteiro."). Usa **multi-catch** (`catch (A | B e)`).

### `Sessao`: classe utilitária

- Guarda o funcionário logado num atributo `private static Funcionario`.
- `FrmLogin` faz `Sessao.setFuncionario(f)`; as telas usam `Sessao.getFuncionario()` para registrar quem fez o empréstimo, a devolução ou a reserva.

### `Main`: classe concreta (ponto de entrada)

```java
public static void main(String[] args) {
    Estilo.iniciar();                                   // tema FlatLaf
    try {
        List<String> mensagens = new BancoDados().inicializar();   // RF15
        JOptionPane.showMessageDialog(null, String.join("\n", mensagens), ...);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Não foi possível conectar ao banco MySQL..." ...);
        System.exit(1);
    }
    new FrmLogin().setVisible(true);
}
```

## 3.2 Pacote `com.fatec.biblioteca.dao`

### `DAO`: classe abstrata (classe-mãe de todos os DAOs)

| Membro | Tipo / o que faz |
|---|---|
| `URL` | `protected static final String` |
| `USUARIO`, `SENHA` | `private static final String` |
| `origemConfiguracao` | `private static String` (de onde o `db.properties` foi lido) |
| bloco `static { ... }` | **Inicializador estático**: roda uma vez quando a classe é carregada e lê o `db.properties`. |
| `candidatos()` | Lista de caminhos onde procurar o arquivo. |
| `getConnection()` (`protected`) | `DriverManager.getConnection(URL, USUARIO, SENHA)`. |
| `criarNovaTabela()` **abstrato** | Cada DAO cria a sua tabela (mesmo nome de método do `poo12`). |
| `executar(String sql)` (`protected`) | Executa um comando simples com try-with-resources. |
| `paraSql(LocalDate)` / `paraLocalDate(Date)` | Conversão `java.time.LocalDate` ↔ `java.sql.Date`. |
| `montarLeitor`, `montarFuncionario`, `montarLivro` | Reconstroem objetos a partir das colunas. Usam os métodos fábrica (`Leitor.criar`) e os setters (que validam). Se algo vier inválido do banco, a `Exception` vira `SQLException` ("Leitor 5 inválido no banco: ..."). |

### `LivroDAO`: classe (herda `DAO`)

| Método | SQL / o que faz |
|---|---|
| `criarNovaTabela()` | `CREATE TABLE IF NOT EXISTS livro (...)` com CHECKs. |
| `inserir(Livro, List<Copia>)` | **Transação**: `INSERT` do livro + `INSERT` de cada cópia; `commit` ou `rollback` (RF01). |
| `alterar(Livro)` | `UPDATE livro SET titulo, autor, categoria, ano_publicacao`. |
| `remover(int)` | Transação: apaga as cópias e depois o livro. |
| `possuiMovimentacao(int)` | Conta empréstimos e reservas do livro (para impedir exclusão). |
| `carregar(int)` e `carregar(Connection, int)` | **Sobrecarga**. Lê o livro e preenche as cópias (`CopiaDAO.carregarCopias`) e a fila (`ReservaDAO.carregarFila`). A versão com `Connection` serve para participar da transação de outro DAO. |
| `listar(Categoria)` | Lista o acervo (`null` = todas, RF03; com categoria, RF05). Lê livros e cópias em 2 consultas e liga as cópias aos livros com um `HashMap<Integer, Livro>`. |
| `contar()` | `SELECT COUNT(*)` (RN14 e resumo da carga). |

### `CopiaDAO`: classe (herda `DAO`)

| Método | O que faz |
|---|---|
| `criarNovaTabela()` | Tabela `copia` com `UNIQUE(livro_codigo, numero)` e CHECK de situação. |
| `inserir(Connection, Copia)` | `INSERT` com `RETURN_GENERATED_KEYS` para pegar o código gerado. |
| `adicionarCopias(Livro, List<Copia>)` | Transação do RF02: grava as cópias e chama `ReservaDAO.atualizarFila` para separar cópias para quem estava na fila. Devolve quantas foram separadas. |
| `atualizarSituacao(Connection, Copia)` | `UPDATE copia SET situacao=?`. |
| `carregarCopias(Connection, Livro)` | Preenche a lista de cópias do livro. |
| `verificarConsistencia()` | RF15/RN18: compara a situação gravada de cada cópia com a esperada (pelos empréstimos abertos e reservas aguardando), corrige e devolve os avisos. |
| `contar()` | Total de cópias. |

### `LeitorDAO`: classe (herda `DAO`)

| Método | O que faz |
|---|---|
| `criarNovaTabela()` | Tabela `leitor` (código informado pelo usuário, sem AUTO_INCREMENT). |
| `inserir`, `alterar`, `remover` | CRUD padrão do `poo12`, retornando o número de linhas afetadas. |
| `existe(int)` | Verifica duplicidade (RN05). |
| `possuiMovimentacao(int)` | Tem empréstimos ou reservas? (impede exclusão). |
| `carregar(int)` | Lê o leitor **e** a lista de empréstimos em aberto (`EmprestimoDAO.listarAbertosPorLeitor`), montando a **agregação**. |
| `listar()` | RF07: com subconsulta que conta os empréstimos abertos de cada leitor. |
| `contar()` | RN14. |

### `FuncionarioDAO`: classe (herda `DAO`)

| Método | O que faz |
|---|---|
| `criarNovaTabela()` | Tabela `funcionario` com `UNIQUE(login)`. |
| `garantirAdministrador()` | Se a tabela está vazia, cria `admin/admin` (Bibliotecário). |
| `inserir`, `alterar`, `alterarSenha`, `remover` | CRUD. O `alterar` só troca a senha se `senhaHash != null` (campo vazio = manter). |
| `possuiMovimentacao(int)` | Registrou empréstimo, devolução ou reserva? |
| `contarBibliotecarios()` | Garante que sempre sobre pelo menos 1 Bibliotecário. |
| `carregar(int)`, `carregarPorLogin(String)` | Ambos usam o privado `carregarPor(coluna, valor)`. |
| `listar()`, `contar()` | Listagem e contagem. |

### `EmprestimoDAO`: classe (herda `DAO`)

| Método | O que faz |
|---|---|
| constante `SELECT` | Consulta base com `JOIN` em `copia`, `livro`, `leitor` e `funcionario` (duas vezes: `fe` = quem emprestou, `fd` = quem recebeu, com `LEFT JOIN`). |
| `criarNovaTabela()` | Tabela `emprestimo` com a coluna gerada `copia_aberta` + `UNIQUE` (ver Parte 5). |
| `registrarEmprestimo(Emprestimo, Reserva)` | **Transação** do RF08: `INSERT` do empréstimo, `UPDATE` da cópia e, se havia reserva, marca como ATENDIDA. |
| `registrarDevolucao(Emprestimo)` | **Transação** do RF09: `UPDATE ... WHERE codigo=? AND data_devolucao IS NULL` (se não alterar 1 linha → "já foi devolvido"), atualiza a cópia e anda a fila de reservas. |
| `carregar(int)` | Um empréstimo pelo código. |
| `listarAbertosPorLeitor(Leitor)` | Para a agregação do leitor. |
| `listarAbertosPorLivro(int)` | Para a ficha do livro (RF04). |
| `listarAbertos()` | RF10. |
| `listarAtrasados(LocalDate)` | RF12 (b) e RF14. |
| `listarDevolvidosComMulta()` | RF12 (a). |
| `listar()` | Todos. |
| `carregarPerfilUso()` | RF13: `GROUP BY l.tipo, o.categoria` → `PerfilUso.incrementar`. |
| `contar()`, `proximoCodigo()` | RN14 e resumo da carga (RF15). |
| `consultar(filtro, leitorConhecido)` e `montar(rs, leitorConhecido)` (privados) | Executam o `SELECT` base + filtro e montam os objetos. Quando o leitor já é conhecido, ele é reaproveitado em vez de criar outro objeto. |

### `ReservaDAO`: classe (herda `DAO`)

| Método | O que faz |
|---|---|
| `criarNovaTabela()` | Tabela `reserva` com colunas geradas `viva_leitor`/`viva_livro` + `UNIQUE` (1 reserva viva por leitor e livro). |
| `inserir(Reserva)` | Grava e coloca a reserva na fila do livro em memória. |
| `atualizar(Connection, Reserva)` | Grava situação, cópia separada, prazo e código do empréstimo. |
| `salvarCancelamento(Reserva, LocalDate)` | Transação do R7: grava o cancelamento, libera a cópia e anda a fila. |
| `atualizarFila(Connection, int codigoLivro, LocalDate)` | **O coração das reservas**: expira as vencidas (R6) e, enquanto houver cópia DISPONÍVEL, separa para a próxima reserva ATIVA da fila (R4). Devolve as reservas que receberam cópia, para a tela avisar. |
| `expirarVencidas(LocalDate)` | Procura livros com reservas AGUARDANDO vencidas e chama `atualizarFila` para cada uma. É chamada antes de emprestar e antes de reservar. |
| `carregarFila(Connection, Livro)` | Preenche a fila (reservas vivas em ordem de código). |
| `carregarViva(int)` | Carrega o livro inteiro e devolve a reserva viva com aquele código (assim a reserva vem ligada à cópia certa). |
| `listar(boolean somenteVivas)` | Para a tela de consulta de reservas. |

### `BancoDados`: classe concreta

- **Não herda de `DAO`**; ela **usa** os DAOs (composição/uso).
- `inicializar() throws SQLException` faz o RF15: cria as 6 tabelas na ordem certa (por causa das chaves estrangeiras), garante o admin, conta os registros, roda `verificarConsistencia()` (RN18) e devolve as mensagens que o `Main` mostra.

## 3.3 Pacote `com.fatec.biblioteca.ui`

Todas as telas são **classes que herdam de `JFrame`**, no mesmo molde do `FrmCliente` do `poo12`: atributos privados para os componentes (`txtCodigo`, `btnInserir`...), um atributo para o DAO, construtor que monta a tela com `GridLayout`/`BorderLayout`, ouvintes com **lambda** (`btnInserir.addActionListener(e -> inserir())`) e métodos privados com `try { ... } catch (Exception err) { Mensagem.erro(this, err); }`.

| Classe | Tipo | Herda de | Para que serve | RF |
|---|---|---|---|---|
| `FrmLogin` | classe | `JFrame` | Login com `JPasswordField`; autentica e guarda na `Sessao`. | (acesso) |
| `FrmPrincipal` | classe | `JFrame` | Cartões de atalho + barra de menus; aplica permissão por cargo. | todos |
| `FrmLivro` | classe | `JFrame` | Consultar, inserir, alterar, excluir livro; ficha com cópias, empréstimos abertos e fila. | RF01, RF04 |
| `FrmAdicionarCopias` | classe | `JFrame` | Adicionar exemplares a um livro existente. | RF02 |
| `FrmLeitor` | classe | `JFrame` | CRUD de leitor; mostra prazo, limite e empréstimos abertos. | RF06 |
| `FrmFuncionario` | classe | `JFrame` | CRUD de funcionários (só Bibliotecário acessa). | (POO) |
| `FrmEmprestimo` | classe | `JFrame` | Realizar empréstimo; mostra nome do leitor e livro ao sair do campo. | RF08 |
| `FrmDevolucao` | classe | `JFrame` | Registrar devolução, mostrar multa e reservas atendidas. | RF09 |
| `FrmReserva` | classe | `JFrame` | Reservar e cancelar reserva. | R1–R7 |
| `FrmConsultaLivros` | classe | `JFrame` | `JTable` do acervo com filtro de categoria e totais. | RF03, RF05 |
| `FrmConsultaLeitores` | classe | `JFrame` | Leitores com "1 de 2". | RF07 |
| `FrmConsultaFuncionarios` | classe | `JFrame` | Lista de funcionários. | (POO) |
| `FrmConsultaReservas` | classe | `JFrame` | Reservas com posição na fila. | (POO) |
| `FrmEmprestimosAbertos` | classe | `JFrame` | Empréstimos abertos; atrasados em vermelho (renderer anônimo). | RF10 |
| `FrmRelCirculacao` | classe | `JFrame` | Disponíveis x emprestados, percentual, títulos sem exemplar. | RF11 |
| `FrmRelAtrasos` | classe | `JFrame` | (a) multas cobradas e (b) multas previstas. | RF12 |
| `FrmRelPerfilUso` | classe | `JFrame` | Matriz 3x5 com totais e categoria preferida. | RF13 |
| `FrmExportarCobranca` | classe | `JFrame` | Prévia e gravação do `.txt`. | RF14 |
| `FrmAlterarSenha` | classe | `JFrame` | Troca a senha do usuário logado. | (acesso) |
| `Mensagem` | utilitária | — | **Ponto central de exibição de erros**: `info`, `erro`, `confirmar`, `moeda`, `numero`. | — |
| `Tabela` | classe | `DefaultTableModel` | Modelo de `JTable` com colunas por **varargs** e células não editáveis (`isCellEditable` sobrescrito). | — |
| `Estilo` | utilitária | — | Tema FlatLaf, cabeçalho padrão, cor dos botões, preferências do tema escuro. | — |
| `PainelFundo` | classe | `JPanel` | Painel com imagem de fundo e "véu" semitransparente (sobrescreve `paintComponent`). | — |

Detalhes que valem citar:

- **`FrmPrincipal.item(String, Supplier<JFrame>)` e `atalho(...)`** recebem a tela como **referência de construtor** (`FrmLivro::new`). `Supplier` é uma **interface funcional** do Java: cada clique cria uma nova janela com `tela.get()`.
- **`Estilo.padronizar(this)`** é chamado no fim de cada construtor de tela: põe o cabeçalho com a imagem da estante, aplica bordas e pinta botões "primários" (Inserir, Salvar...) de azul e "perigosos" (Excluir, Cancelar reserva) de vermelho, percorrendo os componentes **recursivamente** e usando `instanceof`.
- **`Estilo.aplicarTema(boolean)`** salva a escolha com `java.util.prefs.Preferences`, então o tema escuro continua na próxima vez que abrir.
- **`PainelFundo`** tem um `static Map<String, Image> CACHE` para não ler a imagem do disco toda vez que repinta.
- **`FrmEmprestimo`** usa `FocusAdapter` como **classe anônima** (sobrescreve `focusLost`) para buscar o leitor e o livro quando o usuário sai do campo.
- **`FrmEmprestimosAbertos`** usa `DefaultTableCellRenderer` anônimo (sobrescreve `getTableCellRendererComponent`) para pintar de vermelho (`Estilo.VERMELHO`) a linha cujo status começa com "ATRASADO"; as outras usam a cor do tema (`t.getForeground()`), então funciona no claro e no escuro.

---

# Parte 4. Onde está cada conceito de POO

## 4.1 Classe e objeto

- **Classe** é o molde; **objeto** é a instância. Ex.: `Livro` é a classe; `new Livro()` em `FrmLivro.lerCampos()` cria um objeto com os dados digitados.
- Cada tela também é um objeto: `new FrmEmprestimo().setVisible(true)`.

## 4.2 Encapsulamento

Todos os atributos das entidades são `private`. O acesso é feito por getters e setters, e os **setters validam**. Assim é impossível ter um `Livro` com código 50 ou título vazio.

```java
// Livro.java
public void setCodigo(int codigo) throws Exception {
    if (codigo < 100 || codigo > 9999) {
        throw new Exception("O código do livro deve ser de 100 a 9999.");   // RN01
    }
    this.codigo = codigo;
}
```

Outros exemplos:

- `Copia` **não tem regra pública para mudar a situação direto** no uso normal: o fluxo usa `emprestar()`, `devolver()`, `reservar()`, `liberar()`, que conferem a transição. (O `setSituacao` existe só para o DAO reconstruir o objeto.)
- `Funcionario` nunca guarda a senha em texto: `setSenha` grava só o hash; o hash fica `private`.
- O construtor `Copia(Livro, int)` não é `public` (visibilidade de pacote).
- Listas `copias` e `filaReservas` são `final` (a referência não pode ser trocada).

## 4.3 Herança

| Superclasse | Subclasses | O que é herdado |
|---|---|---|
| `Pessoa` (abstrata) | `Leitor`, `Funcionario` | `codigo`, `nome`, getters/setters, `toString()` |
| `Leitor` (abstrata) | `Estudante`, `Professor`, `Comunidade` | toda a lógica de validação de empréstimo e reserva |
| `Funcionario` (abstrata) | `Bibliotecario`, `Voluntario` | login, senha, `autenticar()` |
| `DAO` (abstrata) | `LivroDAO`, `CopiaDAO`, `LeitorDAO`, `FuncionarioDAO`, `EmprestimoDAO`, `ReservaDAO` | conexão (`getConnection`), `executar`, conversão de datas, `montar*` |
| `JFrame` (Swing) | todas as 19 telas `Frm*` | janela, título, tamanho, layout |
| `DefaultTableModel` (Swing) | `Tabela` | modelo de dados da `JTable` |
| `JPanel` (Swing) | `PainelFundo` | painel, com `paintComponent` sobrescrito |

São **3 níveis** na hierarquia de pessoas: `Object` → `Pessoa` → `Leitor` → `Estudante`.

Uso de **`super`**: `Leitor.setCodigo` valida e depois chama `super.setCodigo(codigo)`, reaproveitando o comportamento da classe-mãe.

```java
// Leitor.java
@Override
public void setCodigo(int codigo) throws Exception {
    if (codigo < 1 || codigo > 9999) {
        throw new Exception("O código do leitor deve ser de 1 a 9999.");  // RN05
    }
    super.setCodigo(codigo);
}
```

## 4.4 Classes e métodos abstratos

- `Pessoa`, `Leitor`, `Funcionario` e `DAO` são `abstract`: **não dá para fazer `new Leitor()`**. Só faz sentido existir um Estudante, um Professor ou um Comunidade.
- Métodos abstratos obrigam as subclasses a implementar:
  - `Pessoa.getDescricaoTipo()`
  - `Leitor.getTipo()`, `getPrazoDias()`, `getLimiteLivros()`
  - `Funcionario.getCargo()`, `podeGerenciarCadastros()`
  - `DAO.criarNovaTabela()`

## 4.5 Polimorfismo

### a) Sobrescrita (polimorfismo dinâmico, em tempo de execução)

O melhor exemplo do projeto é o cálculo da data prevista. O código chama sempre o mesmo método, e o Java decide em tempo de execução qual versão usar:

```java
// Emprestimo.java (construtor)
this.dataPrevista = leitor.calcularDataPrevista(dataEmprestimo);

// Leitor.java
public LocalDate calcularDataPrevista(LocalDate dataEmprestimo) {
    return dataEmprestimo.plusDays(getPrazoDias());   // 7 ou 14, depende do objeto real
}
```

A variável é do tipo `Leitor`, mas o objeto pode ser `Estudante` (7 dias), `Professor` (14) ou `Comunidade` (7). **Não existe nenhum `if (tipo == ...)`** para prazo ou limite.

Outro exemplo, o de **permissão**:

```java
// FrmPrincipal.java
// polimorfismo: Bibliotecario devolve true, Voluntario devolve false
btnFuncionarios.setEnabled(usuario.podeGerenciarCadastros());
```

Outros: `getDescricaoTipo()` (mostrado nas tabelas), `toString()` (em `Pessoa`, `Livro`, `Categoria`), `criarNovaTabela()` (cada DAO cria a sua), `isCellEditable` em `Tabela`, `paintComponent` em `PainelFundo`.

### b) Sobrecarga (polimorfismo estático, em tempo de compilação)

Mesmo nome, parâmetros diferentes:

```java
// Emprestimo.java
public int calcularDiasAtraso()                          // usa a data da devolução
public int calcularDiasAtraso(LocalDate dataReferencia)  // usa uma data de referência
```

Também: `LivroDAO.carregar(int)` e `LivroDAO.carregar(Connection, int)`; os dois construtores de `Copia`, `Emprestimo`, `Reserva` e `PainelFundo` (sobrecarga de construtores, com `this(...)` em `PainelFundo`).

### c) Referência da superclasse apontando para subclasse

```java
Leitor leitor = Leitor.criar(cbTipo.getSelectedIndex() + 1);   // pode ser Estudante, Professor...
Funcionario f = Funcionario.criar(cargo);                       // Bibliotecario ou Voluntario
```

Isso é o **método fábrica** (*factory method*): quem chama não precisa saber qual subclasse foi criada.

## 4.6 Associação, agregação e composição

Os três são relações "tem um", que diferem pela força do vínculo:

| Relação | Onde | Por que é desse tipo |
|---|---|---|
| **Composição** (◆, a mais forte) | `Livro` ◆── `Copia` | A cópia não existe sem o livro: é criada **pelo próprio livro** (`livro.adicionarCopias(qtd)` → `new Copia(this, numero)`), tem o livro na etiqueta (`101-2`) e é apagada junto com ele (`LivroDAO.remover`). |
| **Agregação** (◇) | `Leitor` ◇── `List<Emprestimo>` | O leitor "agrupa" seus empréstimos abertos, mas o empréstimo tem vida própria (continua existindo depois de devolvido, com histórico e multa). |
| **Agregação** (◇) | `Livro` ◇── `List<Reserva>` (`filaReservas`) | O livro agrupa a fila; a reserva existe por conta própria (pode ser cancelada, expirar, virar empréstimo). |
| **Associação** (→) | `Emprestimo` → `Copia`, `Leitor`, `Funcionario` (x2) | O empréstimo "conhece" esses objetos, mas nenhum é dono do outro. |
| **Associação** (→) | `Reserva` → `Livro`, `Leitor`, `Funcionario`, `Copia` | Idem. |
| **Associação** (→) | `Copia` → `Livro` | Navegação de volta da cópia para o livro (`copia.getLivro()`). |
| **Uso / dependência** | Telas → DAOs; `BancoDados` → DAOs | A tela tem um atributo `dao` e chama seus métodos. |

Trechos para mostrar:

```java
// Composição: Livro.adicionarCopias
for (int i = 0; i < quantidade; i++) {
    Copia c = new Copia(this, proximo + i);   // o livro cria as suas partes
    copias.add(c);
    novas.add(c);
}

// Agregação: LeitorDAO.carregar
obj.setEmprestimosAbertos(new EmprestimoDAO().listarAbertosPorLeitor(obj));

// Associação: Emprestimo
private Copia copia;
private Leitor leitor;
private Funcionario funcionarioEmprestimo;
private Funcionario funcionarioDevolucao;
```

## 4.7 Interfaces

O projeto **não declara uma `interface` própria**. A abstração foi feita com **classes abstratas**, porque as subclasses compartilham **atributos e código** (`codigo`, `nome`, `emprestimosAbertos`, as validações), e interface não guarda estado.

Mas o projeto **usa interfaces do Java**:

- `Supplier<JFrame>` em `FrmPrincipal` (interface funcional, implementada com `FrmLivro::new`).
- `ActionListener`, implementada com **lambdas** em todos os botões: `btn.addActionListener(e -> inserir())`.
- `List<...>` (interface) com `ArrayList` (implementação): `private List<Emprestimo> emprestimosAbertos = new ArrayList<>();`. Programar para a interface.
- `Map<...>` com `HashMap` em `LivroDAO.listar` e `PainelFundo`.
- `Connection`, `PreparedStatement`, `ResultSet` do JDBC são interfaces; o driver do MySQL fornece as implementações.
- `LayoutManager` no construtor de `PainelFundo`.

Se o professor perguntar "por que não interface?", ver a resposta na Parte 10.

## 4.8 Enumerações

- `Categoria` (com atributos, construtor, getters, método estático `fromCodigo` e `toString` sobrescrito).
- `SituacaoCopia` (simples).
- `SituacaoReserva` (com o método `isViva()`).

Vantagem: o compilador impede valores inválidos (não existe "categoria 7"), e o `JComboBox<Categoria>` recebe direto `Categoria.values()`.

## 4.9 Membros estáticos e classes utilitárias

- `Constantes`, `Datas`, `Validador`, `Sessao`, `Mensagem`, `Estilo`: `final` + construtor `private` → ninguém instancia, tudo é `static`.
- **Bloco estático** em `DAO` (lê a configuração uma única vez).
- Constantes `static final` em `Leitor` (`ESTUDANTE = 1`...) e `Funcionario` (`BIBLIOTECARIO = 1`...).
- Métodos fábrica estáticos: `Leitor.criar`, `Funcionario.criar`, `Categoria.fromCodigo`.

## 4.10 Classes anônimas e lambdas

- **Lambdas**: todos os `addActionListener(e -> ...)`.
- **Referências de método/construtor**: `FrmLivro::new`.
- **Classes anônimas**: `new java.awt.event.FocusAdapter() { @Override public void focusLost(...) {...} }` em `FrmEmprestimo`; `new DefaultTableCellRenderer() { ... }` em `FrmEmprestimosAbertos`.

## 4.11 Coleções e arrays

- `ArrayList` para cópias, filas, listas de resultados.
- `HashMap` para ligar cópias aos livros na listagem e para o cache de imagens.
- `Set.of(...)` em `Estilo` para os textos dos botões primários/perigosos.
- **Matriz** `int[3][5]` em `PerfilUso` (a mesma estrutura pedida no PDF).
- `String[]` em `nomesTipos()`, `nomesCargos()`, `NOMES_TIPOS`.
- **Varargs** em `Tabela(String... colunas)` e `FrmPrincipal.secao(String, JButton...)`.

## 4.12 Resumo para a apresentação (cola de 1 minuto)

> "Usamos **herança** em três hierarquias (Pessoa → Leitor/Funcionario → subclasses, e DAO → 6 DAOs). **Classes abstratas** obrigam cada tipo a dizer seu prazo, limite e permissão. Isso dá **polimorfismo**: o empréstimo chama `leitor.calcularDataPrevista()` e o próprio objeto decide se são 7 ou 14 dias, sem `if`. Temos **sobrecarga** em `calcularDiasAtraso`. A **composição** é Livro-Cópia, porque o livro cria e apaga as cópias; a **agregação** é o Leitor com os empréstimos abertos e o Livro com a fila de reservas; e a **associação** é o Empréstimo ligando cópia, leitor e funcionário. O **encapsulamento** está nos setters que validam as regras de negócio. O banco é acessado só pela **camada DAO**, e as **exceções** sobem até a tela, onde a classe `Mensagem` mostra o erro."

---

# Parte 5. Camada DAO e banco de dados

## 5.1 Padrão DAO

**DAO (Data Access Object)** isola o acesso ao banco. A tela chama `dao.inserir(livro)` e não sabe se é MySQL, SQLite ou arquivo. Se um dia trocar o banco, só a camada `dao` muda.

Comparação com o `poo12`:

| `poo12` (`ClienteDAO`) | Este projeto |
|---|---|
| SQLite (`jdbc:sqlite:exemplo.db`) | MySQL, configurado por `db.properties` |
| Uma única classe DAO | Classe-mãe abstrata `DAO` + 6 DAOs (herança) |
| `criarNovaTabela()` no construtor | `criarNovaTabela()` abstrato, chamado por `BancoDados.inicializar()` |
| `inserir/alterar/remover/carregar/listar` | Os mesmos nomes, mais métodos específicos (`registrarEmprestimo`, `atualizarFila`...) |
| `catch` dentro do DAO, imprime e retorna 0 | DAO declara `throws SQLException` e **a tela trata** (requisito: tratar o erro o mais perto da UI) |
| Sem transação | Transações com `setAutoCommit(false)` / `commit` / `rollback` nas operações que mexem em mais de uma tabela |

## 5.2 Recursos de JDBC usados

- **`try-with-resources`**: `try (Connection conn = getConnection(); PreparedStatement p = ...)` fecha a conexão e o statement automaticamente, mesmo se der erro.
- **`PreparedStatement` com `?`**: evita SQL Injection e cuida de aspas e datas. Usado em todo lugar onde entra texto digitado pelo usuário.
- **`Statement.RETURN_GENERATED_KEYS`** + `getGeneratedKeys()`: pega o código gerado pelo `AUTO_INCREMENT` (empréstimo, cópia, reserva, funcionário).
- **Transação**: exemplo do empréstimo:

```java
// EmprestimoDAO.registrarEmprestimo
try (Connection conn = getConnection()) {
    conn.setAutoCommit(false);
    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ... INSERT emprestimo ...
        new CopiaDAO().atualizarSituacao(conn, emp.getCopia());      // mesma conexão
        if (reservaAtendida != null) {
            reservaAtendida.atender(emp);
            new ReservaDAO().atualizar(conn, reservaAtendida);       // mesma conexão
        }
        conn.commit();             // tudo ou nada
        return retorno;
    } catch (SQLException e) {
        conn.rollback();           // desfaz o que já tinha feito
        throw e;                   // e avisa a tela
    }
}
```

Os métodos que recebem `Connection conn` como parâmetro (`CopiaDAO.inserir`, `atualizarSituacao`, `ReservaDAO.atualizar`, `atualizarFila`, `LivroDAO.carregar(conn, ...)`) existem justamente para participar da transação de outro DAO.

## 5.3 As 6 tabelas

```
 livro (1) ───< copia (N)
   │              │
   │              └───< emprestimo >─── leitor
   │                       │   └──── funcionario (empréstimo / devolução)
   └───< reserva >─── leitor
           │   └──── funcionario
           ├──── copia (separada, opcional)
           └──── emprestimo (quando atendida, opcional)
```

### `livro`

| Coluna | Tipo | Restrição |
|---|---|---|
| `codigo` | INT | PK, `CHECK 100..9999` (RN01), informado pelo usuário |
| `titulo`, `autor` | VARCHAR(150) | `CHECK TRIM(...) <> ''` (RN02) |
| `categoria` | TINYINT | `CHECK 1..5` (RN03) |
| `ano_publicacao` | SMALLINT | `CHECK >= 1450` (RN03) |

### `copia`

| Coluna | Tipo | Restrição |
|---|---|---|
| `codigo` | INT AUTO_INCREMENT | PK |
| `livro_codigo` | INT | FK → `livro` |
| `numero` | INT | `>= 1`; `UNIQUE(livro_codigo, numero)` |
| `situacao` | VARCHAR(20) | `DISPONIVEL`, `EMPRESTADA` ou `RESERVADA` |

### `leitor`

| Coluna | Tipo | Restrição |
|---|---|---|
| `codigo` | INT | PK, `CHECK 1..9999` (RN05) |
| `nome` | VARCHAR(100) | não vazio |
| `tipo` | TINYINT | `CHECK 1..3` |

### `funcionario`

| Coluna | Tipo | Restrição |
|---|---|---|
| `codigo` | INT AUTO_INCREMENT | PK |
| `nome` | VARCHAR(100) | não vazio |
| `cargo` | TINYINT | `CHECK IN (1, 2)` |
| `login` | VARCHAR(30) | `UNIQUE` |
| `senha_hash` | CHAR(64) | SHA-256 em hexadecimal |

### `emprestimo`

| Coluna | Tipo | Restrição |
|---|---|---|
| `codigo` | INT AUTO_INCREMENT | PK (código sequencial do PDF) |
| `copia_codigo`, `leitor_codigo` | INT | FKs |
| `funcionario_emprestimo` | INT | FK, obrigatório |
| `funcionario_devolucao` | INT NULL | FK, preenchido na devolução |
| `data_emprestimo`, `data_prevista` | DATE | `data_prevista > data_emprestimo` |
| `data_devolucao` | DATE NULL | **NULL = em aberto** (no PDF era o dia 0); `>= data_emprestimo` (RN11) |
| `multa` | DECIMAL(8,2) | `>= 0` |
| `copia_aberta` | INT **gerada** | `IF(data_devolucao IS NULL, copia_codigo, NULL)` + `UNIQUE` |

**Truque da coluna gerada `copia_aberta`:** ela só tem valor enquanto o empréstimo está aberto. Como o MySQL permite vários `NULL` num índice `UNIQUE`, mas não dois valores iguais, o banco **garante que uma mesma cópia nunca tenha dois empréstimos abertos**, mesmo que duas pessoas cliquem ao mesmo tempo. É uma segunda linha de defesa além da validação em Java.

### `reserva`

| Coluna | Tipo | Restrição |
|---|---|---|
| `codigo` | INT AUTO_INCREMENT | PK (a ordem do código é a ordem da fila) |
| `livro_codigo`, `leitor_codigo`, `funcionario_codigo` | INT | FKs |
| `data_reserva` | DATE | |
| `copia_codigo` | INT NULL | FK, cópia separada |
| `data_limite_retirada` | DATE NULL | separação + 3 dias |
| `emprestimo_codigo` | INT NULL | FK, quando ATENDIDA |
| `situacao` | VARCHAR(20) | ATIVA, AGUARDANDO, ATENDIDA, CANCELADA, EXPIRADA |
| `viva_leitor`, `viva_livro` | INT **geradas** | só têm valor se ATIVA/AGUARDANDO; `UNIQUE(viva_leitor, viva_livro)` |

Mesmo truque: o banco impede que um leitor tenha **duas reservas vivas da mesmo livro**.

Índices extras: `idx_emp_leitor_aberto (leitor_codigo, data_devolucao)` e `idx_res_fila (livro_codigo, situacao, codigo)`, para deixar rápidas as consultas mais usadas.

## 5.4 Do arquivo `.txt` do PDF para o banco

| PDF (LP1) | Projeto |
|---|---|
| `livros.txt` (CODIGO;TITULO;AUTOR;CATEGORIA;ANO;TOTAL;DISPONIVEIS) | tabelas `livro` + `copia` (total e disponíveis são **contados** pelas cópias) |
| `leitores.txt` (CODIGO;NOME;TIPO) | tabela `leitor` (a quantidade de abertos é contada pelos empréstimos) |
| `emprestimos.txt` (7 campos, dia do ano) | tabela `emprestimo` (com `DATE` e funcionários) |
| Arrays paralelos em memória | Objetos (`Livro`, `Leitor`, `Emprestimo`) e listas |
| `carregarDados()` (RF15) | `BancoDados.inicializar()` |
| `salvarLivros()` etc. (RF16) | Cada operação grava na hora (com `commit`) |
| `atrasos_dia<N>.txt` | `atrasos_AAAA-MM-DD.txt` (continua `.txt`, RF14) |
| Validação de linhas na carga (RN17) | CHECKs, FKs e UNIQUEs no banco + `montarLivro/montarLeitor` que revalidam ao ler |

---

# Parte 6. Telas e fluxo de navegação

```
Main
 ├─ Estilo.iniciar()                      (tema)
 ├─ BancoDados.inicializar()              (RF15: tabelas, admin, consistência)
 │     └─ JOptionPane com o resumo ("Dados carregados: ...")
 └─ FrmLogin
        └─ (login ok) Sessao.setFuncionario(f) → FrmPrincipal
               ├─ Circulação:  FrmEmprestimo, FrmDevolucao, FrmReserva, FrmEmprestimosAbertos
               ├─ Cadastros:   FrmLivro (→ Listar: FrmConsultaLivros), FrmAdicionarCopias,
               │               FrmLeitor (→ FrmConsultaLeitores),
               │               FrmFuncionario (→ FrmConsultaFuncionarios)  [só Bibliotecário]
               ├─ Consultas:   FrmConsultaLivros, FrmConsultaLeitores, FrmEmprestimosAbertos,
               │               FrmConsultaReservas
               ├─ Relatórios:  FrmRelCirculacao, FrmRelAtrasos, FrmRelPerfilUso, FrmExportarCobranca
               └─ Sistema:     FrmAlterarSenha, Tema escuro, Trocar usuário (→ FrmLogin), Sair
```

- `FrmLogin` e `FrmPrincipal` usam `EXIT_ON_CLOSE` (fechar = sair do programa).
- As demais usam `DISPOSE_ON_CLOSE` (fechar só aquela janela).
- A tela principal tem **duas formas de navegar**: cartões de atalho no centro e a barra de menus. As duas chamam os mesmos construtores.
- "Trocar usuário" limpa a `Sessao`, abre um novo `FrmLogin` e fecha a principal com `dispose()`.

**Padrão de uma tela de cadastro** (igual ao `FrmCliente` do professor):

1. Atributos: campos (`JTextField`, `JComboBox`), botões (`JButton`) e o DAO.
2. Construtor: cria o DAO, define título/tamanho, monta um `JPanel` com `GridLayout(linhas, 2)` (rótulo + campo), monta a barra de botões, liga os eventos, chama `Estilo.padronizar(this)`.
3. Métodos privados: `lerCampos()` (monta o objeto e valida pelos setters), `inserir()`, `consultar()`, `alterar()`, `excluir()`, `limparCampos()`, cada um com `try/catch`.

---

# Parte 7. Requisitos (RF) e regras de negócio (RN) no código

## 7.1 Requisitos funcionais

| RF | Descrição (PDF) | Tela | Regras / métodos principais |
|---|---|---|---|
| RF01 | Cadastrar livro | `FrmLivro.inserir()` | `Livro.set*` (RN01–RN03), `livro.adicionarCopias` (RN04), `dao.contar()` (RN14), `LivroDAO.inserir` (transação) |
| RF02 | Adicionar exemplares | `FrmAdicionarCopias.adicionar()` | `livro.adicionarCopias` (RN04), `CopiaDAO.adicionarCopias` (+ separa para a fila) |
| RF03 | Listar acervo | `FrmConsultaLivros` (todas) | `LivroDAO.listar(null)`, totais de títulos e exemplares |
| RF04 | Consultar livro por código | `FrmLivro.consultar()` / `mostrarFicha()` | `LivroDAO.carregar`, `EmprestimoDAO.listarAbertosPorLivro`; "Livro não encontrado." |
| RF05 | Pesquisar por categoria | `FrmConsultaLivros` (combo) | `LivroDAO.listar(categoria)`; "Nenhum livro nesta categoria." |
| RF06 | Cadastrar leitor | `FrmLeitor.inserir()` | `Leitor.criar`, `setCodigo` (RN05), `dao.existe`, `dao.contar` (RN14); mostra prazo e limite |
| RF07 | Listar leitores | `FrmConsultaLeitores` | `LeitorDAO.listar()`, `getSituacao()` → "1 de 2" |
| RF08 | Realizar empréstimo | `FrmEmprestimo.realizar()` | RN06–RN10, RN13–RN16 (ver 9.1) |
| RF09 | Registrar devolução | `FrmDevolucao.registrar()` | `Emprestimo.registrarDevolucao` (RN11, RN12), `EmprestimoDAO.registrarDevolucao` |
| RF10 | Empréstimos em aberto | `FrmEmprestimosAbertos` | `listarAbertos()`, `getStatus(ref)`, contagem de atrasados |
| RF11 | Disponíveis e emprestados | `FrmRelCirculacao` | totais, percentual emprestado, títulos sem exemplar disponível |
| RF12 | Atrasos e multas | `FrmRelAtrasos` | (a) `listarDevolvidosComMulta`, (b) `listarAtrasados(ref)` + `calcularMulta(ref)` |
| RF13 | Perfil de uso | `FrmRelPerfilUso` | `carregarPerfilUso()` → `PerfilUso` (totais, preferida, mais procurada) |
| RF14 | Exportar lista de cobrança | `FrmExportarCobranca` | `ListaCobranca.gerarTexto()` / `exportar()`; sem atrasos → não gera; `IOException` → mensagem sem fechar o programa |
| RF15 | Carregar dados ao iniciar | `Main` → `BancoDados.inicializar()` | cria tabelas, admin, contagens, `verificarConsistencia()` (RN18), próximo código |
| RF16 | Gravar dados | todos os DAOs | gravação imediata em cada operação; mensagem de sucesso só depois do `commit` |

## 7.2 Regras de negócio do PDF

| RN | Regra | Onde está implementada |
|---|---|---|
| RN01 | Código do livro 100–9999, sem repetir | `Livro.setCodigo`; `FrmLivro.inserir` (verifica se já existe); PK + CHECK no banco |
| RN02 | Título e autor obrigatórios | `Validador.textoObrigatorio` via `Livro.setTitulo/setAutor`; CHECK `TRIM <> ''` |
| RN03 | Categoria 1–5; ano 1450–ano atual | enum `Categoria` + `Livro.setCategoria`; `Livro.setAnoPublicacao`; CHECKs |
| RN04 | Exemplares 1–50, todos começam disponíveis | `Livro.adicionarCopias`; `Copia.situacao = DISPONIVEL` por padrão |
| RN05 | Código do leitor 1–9999, sem repetir; nome obrigatório; tipo 1–3 | `Leitor.setCodigo`; `Pessoa.setNome`; `Leitor.criar`; `LeitorDAO.existe`; CHECKs |
| RN06 | Só empresta livro existente com exemplar disponível | `FrmEmprestimo` (livro == null → erro); `Livro.getCopiaParaEmprestimo` |
| RN07 | Leitor existe e respeita o limite do tipo | `FrmEmprestimo` (leitor == null); `Leitor.atingiuLimite` com `getLimiteLivros()` polimórfico |
| RN08 | Leitor com atraso não pega novo livro | `Leitor.getEmprestimoEmAtraso` em `validarNovoEmprestimo` |
| RN09 | Não pode ter 2 empréstimos abertos do mesmo livro | `Leitor.possuiLivroEmprestado` |
| RN10 | Datas válidas; prevista = empréstimo + prazo do tipo | `Datas.converter` (formato estrito); `Leitor.calcularDataPrevista`; CHECK `data_prevista > data_emprestimo` |
| RN11 | Só devolve empréstimo existente e aberto; data não anterior | `FrmDevolucao.buscar`; `Emprestimo.registrarDevolucao`; `Copia.devolver`; `UPDATE ... AND data_devolucao IS NULL`; CHECK no banco |
| RN12 | Atraso = devolução (ou referência) − prevista, se positivo; multa = atraso × 0,50 | `Emprestimo.calcularDiasAtraso` (2 versões) e `calcularMulta` |
| RN13 | Empréstimo: disponíveis −1, abertos do leitor +1, matriz +1; devolução: o inverso, matriz não muda | Disponíveis = cópias por situação (`Copia.emprestar/devolver`); abertos = `COUNT` dos empréstimos sem devolução; matriz = `GROUP BY` de todos os empréstimos |
| RN14 | Máx. 200 títulos, 100 leitores, 500 empréstimos | `Constantes.MAX_*` + `dao.contar()` em `FrmLivro.inserir`, `FrmLeitor.inserir`, `FrmEmprestimo.realizar` |
| RN15 | Disponíveis entre 0 e o total | Garantido por construção: disponíveis é a contagem de cópias `DISPONIVEL`; transições controladas em `Copia` |
| RN16 | Gravação imediata; sucesso só após gravar | DAOs gravam na hora; a `Mensagem.info` vem depois da chamada ao DAO; transações com `commit` |
| RN17 | Na carga, dados válidos | Restrições do banco (CHECK/FK/UNIQUE) impedem gravar dado inválido; `DAO.montar*` revalida ao ler |
| RN18 | Reconstruir derivados após a carga e avisar correções | `CopiaDAO.verificarConsistencia()` corrige a situação das cópias e gera "AVISO: ..."; abertos e matriz são sempre calculados por consulta |

## 7.3 Regras de reserva (R1–R7) e de funcionários (F1)

O PDF não trata reservas nem funcionários (é da disciplina de LP1). Essas regras foram definidas na proposta do projeto para atender o professor de POO:

| Regra | Descrição | Onde |
|---|---|---|
| R1 | Leitor com empréstimo atrasado não pode reservar | `Leitor.validarNovaReserva` |
| R2 | Não pode reservar livro que já está com ele, nem ter 2 reservas vivas da mesmo livro | `validarNovaReserva` + `UNIQUE(viva_leitor, viva_livro)` |
| R3 | Só reserva se não houver cópia disponível ("faça o empréstimo") | `validarNovaReserva` (`livro.getQtdDisponiveis() > 0`) |
| R4 | Quando uma cópia fica livre (devolução, novos exemplares, cancelamento, expiração), ela é separada para a 1ª reserva ATIVA da fila, que passa a AGUARDANDO com 3 dias para retirar | `ReservaDAO.atualizarFila`, `Reserva.separarCopia`, `Copia.reservar`, `Constantes.PRAZO_RETIRADA_RESERVA` |
| R5 | Quando o leitor da reserva vem emprestar, usa a cópia separada e a reserva vira ATENDIDA | `Livro.getReservaAguardando`, `Livro.getCopiaParaEmprestimo`, `Reserva.atender`, `EmprestimoDAO.registrarEmprestimo` |
| R6 | Passou o prazo de retirada: reserva EXPIRADA e cópia vai para o próximo | `Reserva.prazoVencido/expirar`, `ReservaDAO.expirarVencidas` (chamado em `FrmEmprestimo` e `FrmReserva`) |
| R7 | Cancelar reserva viva libera a cópia e anda a fila | `Reserva.cancelar`, `ReservaDAO.salvarCancelamento` |
| Decisão | Reserva **não conta** no limite de empréstimos do leitor | `atingiuLimite()` só olha empréstimos |
| F1 | Só Bibliotecário cadastra/altera/exclui funcionários | `Funcionario.podeGerenciarCadastros()` (polimórfico) em `FrmPrincipal` |
| Extra | Sempre deve existir pelo menos 1 Bibliotecário; ninguém exclui o próprio usuário; quem já teve movimentação não é excluído | `FrmFuncionario.alterar/excluir`, `FuncionarioDAO.contarBibliotecarios/possuiMovimentacao` |
| Extra | Livro ou leitor com histórico não pode ser excluído (preserva os relatórios) | `LivroDAO/LeitorDAO.possuiMovimentacao` |

---

# Parte 8. Tratamento de exceções

## 8.1 A estratégia em uma frase

**As entidades e os DAOs lançam (`throw`/`throws`); as telas capturam (`try/catch`) e mostram a mensagem com a classe `Mensagem`.** É exatamente o requisito do professor: "tratamento de exceções o mais próximo da UI".

```
Entidade (regra violada)  ── throw new Exception("mensagem amigável") ──┐
DAO (erro de banco)       ── throws SQLException ───────────────────────┤
                                                                         ▼
Tela: try { ... } catch (Exception err) { Mensagem.erro(this, err); }
```

## 8.2 Tipos de exceção usados

| Exceção | Tipo | Quem lança | Exemplo |
|---|---|---|---|
| `Exception` | checada | entidades, `Validador`, `Datas`, telas | "O código do livro deve ser de 100 a 9999." |
| `SQLException` | checada | JDBC e DAOs | falha de conexão, "O empréstimo 3 já foi devolvido." |
| `SQLIntegrityConstraintViolationException` | checada, **subclasse** de `SQLException` | driver MySQL | violou UNIQUE/FK/CHECK |
| `IOException` | checada | `ListaCobranca.exportar()` | não conseguiu gravar o `.txt` |
| `NumberFormatException` | não checada | `Integer.parseInt` | usuário digitou "abc" no código |
| `DateTimeParseException` | não checada | `LocalDate.parse` | data "31/02/2026" |
| `IllegalStateException` | não checada | `Funcionario.gerarHash` | SHA-256 indisponível (praticamente impossível) |

## 8.3 A classe `Mensagem` (tratamento centralizado)

```java
public static void erro(Component tela, Exception e) {
    if (e instanceof SQLIntegrityConstraintViolationException) {
        // "O banco recusou a operação (registro duplicado ou em uso)."
    } else if (e instanceof SQLException) {
        // "Erro no banco de dados. Os dados não foram gravados."
    } else {
        // mensagem de regra de negócio, ícone de aviso, título "Verifique!"
    }
}
```

A **ordem dos `if` importa**: a subclasse (`SQLIntegrityConstraintViolationException`) é testada antes da superclasse (`SQLException`), senão nunca cairia no primeiro caso. Isso também é polimorfismo: a variável é `Exception`, mas o objeto real decide o caminho.

## 8.4 Tradução de exceções

Várias vezes uma exceção "técnica" é convertida em uma mensagem que o usuário entende:

```java
// Validador.inteiro: NumberFormatException → Exception com texto em português (multi-catch)
try {
    return Integer.parseInt(texto.trim());
} catch (NumberFormatException | NullPointerException e) {
    throw new Exception(campo + " deve ser um número inteiro.");
}

// Datas.converter: DateTimeParseException → Exception
catch (DateTimeParseException e) {
    throw new Exception("Data inválida: " + texto + ". Use o formato dd/mm/aaaa.");
}

// DAO.montarLeitor: Exception de validação → SQLException (o dado do banco está errado)
catch (Exception e) {
    throw new SQLException("Leitor " + codigo + " inválido no banco: " + e.getMessage());
}
```

## 8.5 `rollback` e relançamento

Nas transações, o DAO captura a `SQLException` **só para desfazer** (`conn.rollback()`) e relança com `throw e`. Quem decide o que mostrar continua sendo a tela.

## 8.6 Casos especiais

- **`Main`**: se o banco não conecta, mostra um erro explicando onde está o `db.properties` e encerra com `System.exit(1)` (sem banco o sistema não funciona).
- **`FrmExportarCobranca`**: tem um `try/catch (IOException)` **interno**, porque o PDF exige que falha ao gravar o arquivo mostre mensagem **sem encerrar** o programa, mantendo a prévia na tela.
- **`FrmLogin`**: no erro, além da mensagem, limpa o campo de senha.
- **`finally` implícito**: o try-with-resources fecha conexões mesmo quando ocorre exceção.

---

# Parte 9. Fluxos completos passo a passo

## 9.1 Realizar empréstimo (RF08), `FrmEmprestimo.realizar()`

1. `emprestimoDAO.contar() >= MAX_EMPRESTIMOS` → erro (RN14).
2. `Datas.converter(txtData)` → `LocalDate` (data inválida → erro).
3. `reservaDAO.expirarVencidas(data)`: antes de tudo, expira reservas vencidas e libera cópias (R6).
4. `leitorDAO.carregar(codigo)`: vem o objeto **já da subclasse certa** e com a lista de empréstimos abertos. `null` → "Leitor não encontrado." (RN07).
5. `livroDAO.carregar(codigo)`: livro com cópias e fila. `null` → "Livro não encontrado." (RN06).
6. `leitor.validarNovoEmprestimo(livro, data)`: RN07, RN08, RN09.
7. `livro.getReservaAguardando(leitor)`: se ele tinha reserva com cópia separada (R5).
8. `livro.getCopiaParaEmprestimo(leitor)`: cópia separada para ele ou a primeira disponível; nenhuma → erro (RN06).
9. `new Emprestimo(copia, leitor, Sessao.getFuncionario(), data)`: `copia.emprestar()` e `dataPrevista` **polimórfica** (RN10).
10. `emprestimoDAO.registrarEmprestimo(emp, reserva)`: transação com INSERT do empréstimo, UPDATE da cópia e da reserva (RN13, RN16).
11. Só depois: `Mensagem.info("Empréstimo nº X registrado (exemplar 101-2). Devolução prevista para dd/mm/aaaa.")`.

Qualquer `throw` em qualquer passo pula direto para o `catch` → `Mensagem.erro`, e nada é gravado.

## 9.2 Registrar devolução (RF09), `FrmDevolucao.registrar()`

1. `buscar()` → `dao.carregar(codigo)`; `null` → "Empréstimo não encontrado." (RN11).
2. `emp.registrarDevolucao(data, Sessao.getFuncionario())`: já devolvido? data antes do empréstimo? `copia.devolver()`, calcula a multa (RN11, RN12).
3. `dao.registrarDevolucao(emp)`: transação com UPDATE do empréstimo (só se ainda aberto), UPDATE da cópia e `ReservaDAO.atualizarFila` (R4).
4. Mensagem: "Devolução registrada com N dia(s) de atraso. Multa: R$ X" ou "Devolução no prazo." + aviso de cópia separada para reserva, se houver.

## 9.3 Reservar, `FrmReserva.reservar()`

1. Converte a data e expira vencidas.
2. Carrega leitor e livro (erros se não existirem).
3. `leitor.validarNovaReserva(livro, data)` (R1–R3).
4. `new Reserva(livro, leitor, funcionarioLogado, data)` → `dao.inserir(reserva)`.
5. Mensagem com a posição na fila (`livro.getPosicaoNaFila(reserva)`).

## 9.4 Cancelar reserva, `FrmReserva.cancelar()`

`dao.carregarViva(codigo)` → confirmação → `r.cancelar()` (R7) → `dao.salvarCancelamento(r, data)` (libera a cópia e separa para o próximo) → mensagem.

## 9.5 Login, `FrmLogin.entrar()`

`dao.carregarPorLogin(login)` → `f.autenticar(senha)` (compara hashes) → `Sessao.setFuncionario(f)` → abre `FrmPrincipal` → `dispose()` do login. Login ou senha errados geram a mesma mensagem ("Login ou senha inválidos."), para não revelar qual dos dois está errado.

## 9.6 Inicialização (RF15), `BancoDados.inicializar()`

1. `livroDAO.migrarTabelaObra()`: se o banco ainda tem a tabela antiga `obra` (de antes da troca do nome para "Livro"), renomeia `obra` → `livro` e as colunas `obra_codigo` → `livro_codigo` em `copia` e `reserva`, sem perder dados. Em banco novo não faz nada.
2. Cria as tabelas na ordem `livro → copia → leitor → funcionario → emprestimo → reserva` (cada FK precisa que a tabela referenciada já exista).
3. `garantirAdministrador()`.
4. Conta livros, leitores e empréstimos; avisa se estão vazios.
5. `copiaDAO.verificarConsistencia()` (RN18).
6. Resumo: "Dados carregados: X livro(s), Y cópia(s), Z leitor(es), W empréstimo(s). Próximo código de empréstimo: N."

---

# Parte 10. Perguntas prováveis dos professores (com respostas)

### Sobre POO

**1. Onde vocês usaram herança? Por quê?**
Em três hierarquias: `Pessoa` → `Leitor`/`Funcionario`; `Leitor` → `Estudante`/`Professor`/`Comunidade`; `Funcionario` → `Bibliotecario`/`Voluntario`; e `DAO` → os 6 DAOs. Também nas telas (`extends JFrame`). Serve para reaproveitar código (código, nome, conexão com o banco) e para permitir polimorfismo.

**2. Mostre um exemplo de polimorfismo.**
No construtor de `Emprestimo`: `this.dataPrevista = leitor.calcularDataPrevista(dataEmprestimo);`. O método chama `getPrazoDias()`, que é abstrato em `Leitor` e implementado em cada subclasse. Se o objeto é `Professor`, soma 14 dias; se é `Estudante`, 7. Não tem `if` por tipo. O mesmo vale para `podeGerenciarCadastros()` na tela principal.

**3. Qual a diferença entre sobrescrita e sobrecarga? Tem as duas?**
Sobrescrita (`@Override`) é a subclasse redefinir um método da mãe, com a mesma assinatura, e é resolvida em tempo de execução (ex.: `getPrazoDias()`, `setCodigo` em `Leitor`, `toString`). Sobrecarga é ter métodos com o mesmo nome e parâmetros diferentes na mesma classe, resolvida em compilação (ex.: `calcularDiasAtraso()` e `calcularDiasAtraso(LocalDate)`; `LivroDAO.carregar(int)` e `carregar(Connection, int)`).

**4. Por que `Leitor` é abstrata?**
Porque não existe um leitor "genérico" na biblioteca: todo leitor é Estudante, Professor ou Comunidade, e cada um tem prazo e limite diferentes. Ser abstrata impede `new Leitor()` e obriga as subclasses a implementar `getPrazoDias()`, `getLimiteLivros()` e `getTipo()`.

**5. Por que classe abstrata e não interface?**
Porque as subclasses compartilham **estado e código**: atributos (`codigo`, `nome`, `emprestimosAbertos`) e métodos prontos (`validarNovoEmprestimo`, `calcularDataPrevista`). Interface define só o contrato, não guarda atributos de instância. Usamos interfaces do Java onde faziam sentido: `List`, `Map`, `Supplier`, `ActionListener` (lambdas), e as interfaces do JDBC.

**6. Explique a diferença entre associação, agregação e composição no projeto.**
Composição: `Livro` e `Copia`. A cópia é criada pelo livro (`adicionarCopias`), só existe com ele e é apagada junto. Agregação: `Leitor` tem a lista de `Emprestimo` em aberto e `Livro` tem a fila de `Reserva`, mas empréstimos e reservas têm vida própria (ficam no histórico). Associação: `Emprestimo` referencia `Copia`, `Leitor` e `Funcionario`, sem ser dono deles.

**7. Onde está o encapsulamento?**
Todos os atributos são `private`. Os setters validam as regras (ex.: `Livro.setCodigo` só aceita 100 a 9999). A situação da cópia muda por métodos de negócio (`emprestar`, `devolver`), que impedem transições erradas. A senha nunca é guardada em texto puro.

**8. O que é o método `criar` em `Leitor` e `Funcionario`?**
Um método fábrica estático. Recebe o tipo (1, 2, 3) e devolve o objeto da subclasse correta, com retorno do tipo da superclasse. As telas e os DAOs não precisam saber qual subclasse instanciar.

**9. Para que servem os enums?**
`Categoria` substitui o array de nomes do PDF e garante que só existam 5 categorias; tem código, nome e `fromCodigo`. `SituacaoCopia` e `SituacaoReserva` representam os estados possíveis. `SituacaoReserva` tem o método `isViva()`, mostrando que enum pode ter comportamento.

**10. O que é `super` e onde aparece?**
Referência à superclasse. Em `Leitor.setCodigo`, depois de validar o intervalo, chamamos `super.setCodigo(codigo)` para usar a gravação definida em `Pessoa`.

**11. O que é `static` e por que `Sessao` é estática?**
Membro estático pertence à classe, não ao objeto. `Sessao` guarda o funcionário logado num atributo estático, então qualquer tela consegue saber quem está usando o sistema sem precisar passar o objeto de tela em tela.

### Sobre banco e DAO

**12. O que é o padrão DAO e por que usar?**
Data Access Object: uma classe responsável só pelo acesso ao banco de uma entidade. Separa as regras e as telas do SQL. Se trocarmos o banco, só a camada `dao` muda. Seguimos o modelo do `ClienteDAO` do exemplo `poo12`.

**13. Por que existe a classe `DAO` abstrata?**
Para não repetir em cada DAO a leitura do `db.properties`, o `getConnection()`, a conversão de datas e a montagem de objetos. E para obrigar cada DAO a ter `criarNovaTabela()`.

**14. Como vocês evitam SQL Injection?**
Usando `PreparedStatement` com `?` em todo lugar onde entra texto do usuário. Nos poucos lugares em que o SQL é concatenado (filtros em `EmprestimoDAO.consultar` e `LivroDAO.listar`), os valores são `int` ou `LocalDate` gerados pelo próprio programa, nunca texto digitado.

**15. O que acontece se der erro no meio de um empréstimo?**
O empréstimo grava em três tabelas (emprestimo, copia e, se for o caso, reserva) numa **transação**. Se qualquer comando falhar, o `catch` faz `rollback()` e nada fica gravado pela metade. A exceção sobe para a tela, que mostra "Erro no banco de dados. Os dados não foram gravados."

**16. Como garantem que uma cópia não seja emprestada duas vezes?**
Em duas camadas. No Java, `Copia.emprestar()` lança exceção se já está emprestada. No banco, a coluna gerada `copia_aberta` com `UNIQUE` impede dois empréstimos abertos da mesma cópia, mesmo com dois computadores ao mesmo tempo.

**17. Onde fica a "quantidade disponível" do PDF?**
Não é gravada: é **calculada** contando as cópias com situação `DISPONIVEL`. Assim nunca fica inconsistente (RN15). Na abertura, `verificarConsistencia()` ainda confere cada cópia contra os empréstimos e reservas (RN18).

**18. Como a senha é guardada?**
Como hash SHA-256 (`Funcionario.gerarHash`), em `CHAR(64)`. Na autenticação, gera-se o hash do que foi digitado e compara-se com o gravado. Nem quem olha o banco vê a senha.

### Sobre exceções

**19. Onde vocês tratam as exceções?**
Nas telas. Entidades e DAOs só lançam (`throws`). Cada ação da tela tem `try/catch (Exception err)` e chama `Mensagem.erro(this, err)`, que escolhe a mensagem conforme o tipo: violação de integridade, erro de banco ou regra de negócio.

**20. Qual a diferença entre exceção checada e não checada? Exemplos?**
Checada (herda de `Exception` mas não de `RuntimeException`) o compilador obriga a tratar ou declarar com `throws`: `SQLException`, `IOException`, e as nossas `Exception` de regra. Não checada (`RuntimeException`) não é obrigatória: `NumberFormatException`, `DateTimeParseException`. Nós convertemos as não checadas em mensagens amigáveis no `Validador` e em `Datas`.

**21. O que é try-with-resources?**
`try (Connection conn = ...)`: o Java fecha automaticamente o recurso no final, mesmo se ocorrer exceção. Usamos em todos os DAOs e na gravação do arquivo de cobrança.

### Sobre Swing

**22. Como as telas são construídas?**
Cada tela é uma classe que herda de `JFrame`. No construtor montamos os painéis com `GridLayout` e `BorderLayout`, criamos os botões e ligamos os eventos com lambdas (`btn.addActionListener(e -> inserir())`). No final, `Estilo.padronizar(this)` aplica o visual padrão.

**23. Como as tabelas funcionam?**
`JTable` com um modelo `Tabela`, que herda de `DefaultTableModel`, recebe os nomes das colunas por varargs e sobrescreve `isCellEditable` para retornar `false` (somente leitura). Os dados entram com `modelo.addRow(new Object[]{...})`.

**24. Como o Voluntário fica sem acesso aos funcionários?**
Em `FrmPrincipal`: `btnFuncionarios.setEnabled(usuario.podeGerenciarCadastros())`. O `Bibliotecario` retorna `true` e o `Voluntario` retorna `false`. É polimorfismo aplicado a permissão.

**25. O que é o FlatLaf?**
Uma biblioteca de "Look and Feel" para Swing, que só muda a aparência (tema claro/escuro, cantos arredondados). Os componentes continuam sendo Swing puro.

### Sobre a especificação (LP1)

**26. O PDF pede arrays e arquivos .txt. Por que vocês usaram objetos e banco?**
Porque o projeto é apresentado também em POO, e o professor de POO pediu banco via DAO e interface Swing. O professor de LP1 aceitou o uso de POO. As regras do PDF foram mantidas: cada `.txt` virou uma tabela, cada array paralelo virou um atributo de classe, a matriz de perfil continua `int[3][5]`, e o RF14 continua gerando `.txt`.

**27. O PDF usa o dia do ano (1 a 365). E vocês?**
`LocalDate`, que é a forma correta em Java e não quebra na virada do ano. A regra é a mesma: atraso = devolução − prevista, quando positivo (`ChronoUnit.DAYS.between`).

**28. Como funciona o RF15 (carregar) e o RF16 (gravar) com banco?**
RF15: `BancoDados.inicializar()` roda antes do login, cria o que falta, confere a consistência e mostra o resumo. RF16: cada operação grava na hora, dentro de uma transação, e a mensagem de sucesso só aparece depois do `commit` (RN16).

**29. Onde está a matriz de perfil de uso?**
Em `PerfilUso` (`int[3][5]`). `EmprestimoDAO.carregarPerfilUso()` preenche com `GROUP BY tipo, categoria` sobre todos os empréstimos. A tela calcula total de linha, de coluna, geral, a categoria preferida de cada tipo e a mais procurada.

**30. E se não houver atrasos ao exportar a lista de cobrança?**
O arquivo não é gerado e aparece a mensagem "Não há empréstimos em atraso em dd/mm/aaaa. O arquivo não foi gerado.", como pede o RF14.

---

# Parte 11. Pontos de atenção e limitações conhecidas

Vale conhecer para não ser pego de surpresa (e, se quiser, corrigir antes da apresentação):

1. **Cores da tela "Empréstimos em aberto" (corrigido em 27/11/2026).** O renderer usava `Color.BLACK` e o texto sumia no tema escuro. Agora, quando a linha não está selecionada, usa `Estilo.VERMELHO` para os atrasados e `t.getForeground()` (a cor do tema) para os demais; linha selecionada mantém a cor de seleção.
2. **Não há interface (`interface`) própria.** Prepare a resposta da pergunta 5 da Parte 10.
3. **Concatenação em alguns SQL.** `EmprestimoDAO.consultar` e `LivroDAO.listar` concatenam filtros. Não é falha de segurança (são números e datas do próprio programa), mas o professor pode perguntar (pergunta 14).
4. **Datas futuras são aceitas** no empréstimo e na devolução (por exemplo, para simular atrasos na demonstração). Isso é útil na apresentação, mas convém saber explicar.
5. **Capacidade (RN14) conta todos os empréstimos já feitos**, não só os abertos, igual ao `qtdEmprestimos` do PDF.
6. **Reserva não conta no limite** do leitor. Foi uma decisão do projeto.
7. **Multa é só registrada** (não há tela de pagamento). O relatório de atrasos mostra o total arrecadado e o previsto.
8. **`Sessao` estática** funciona porque o sistema é desktop e de um usuário por vez. Num sistema web isso não serviria.
9. **Senha com SHA-256 sem "sal"** é suficiente para um trabalho acadêmico; em produção o ideal seria um algoritmo próprio para senhas (como bcrypt).
10. **A primeira senha é `admin`.** Mostrar que dá para trocar em *Sistema > Alterar minha senha*.

---

# Parte 12. Glossário rápido

| Termo | Significado curto |
|---|---|
| Classe abstrata | Classe que não pode ser instanciada; pode ter métodos abstratos (sem corpo) e concretos. |
| Método abstrato | Declarado sem corpo; a subclasse é obrigada a implementar. |
| Herança (`extends`) | Subclasse recebe atributos e métodos da superclasse. |
| Polimorfismo | Mesma chamada, comportamentos diferentes conforme o objeto real. |
| Sobrescrita (`@Override`) | Subclasse redefine um método herdado. |
| Sobrecarga | Mesmo nome de método com parâmetros diferentes. |
| Encapsulamento | Esconder os atributos (`private`) e controlar o acesso por métodos. |
| Associação | Um objeto conhece outro. |
| Agregação | "Tem um", mas a parte existe sozinha. |
| Composição | "É feito de", a parte não existe sem o todo. |
| Enum | Tipo com um conjunto fixo de valores. |
| DAO | Classe que faz o acesso a dados de uma entidade. |
| JDBC | API do Java para falar com bancos relacionais. |
| `PreparedStatement` | Comando SQL com parâmetros `?`, seguro contra injeção. |
| Transação | Conjunto de comandos que vale "tudo ou nada" (`commit`/`rollback`). |
| try-with-resources | `try (recurso)` que fecha o recurso sozinho. |
| Exceção checada | O compilador obriga a tratar ou declarar (`throws`). |
| Lambda | Função curta escrita inline: `e -> inserir()`. |
| Classe anônima | Classe sem nome criada e instanciada no mesmo ponto. |
| Método fábrica | Método (geralmente estático) que decide qual objeto criar. |
| Look and Feel | Aparência dos componentes Swing (aqui, FlatLaf). |
| Coluna gerada | Coluna do MySQL calculada a partir de outras (`AS (...) STORED`). |
| Hash | Resumo irreversível de um texto (usado para a senha). |

---

*Bons estudos e boa apresentação!*
