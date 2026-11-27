# Biblioteca Comunitária Vila Leitura

Sistema desktop de gestão de biblioteca em **Java 25 (LTS) + Swing + MySQL**, feito para as disciplinas de
Programação Orientada a Objetos e Linguagem de Programação I (Fatec Ipiranga, ADS).

- Regras de negócio: `Especificacao_Projeto_SistemBibliotecario.pdf` (Tema 4), adaptadas para banco de dados e telas.
- Estrutura: igual ao exemplo `poo12` do professor (Maven, entidades na raiz do pacote, `dao/` e as telas Swing em `ui/Frm*`).

## Pré-requisitos

- JDK 25 (LTS) ou mais novo
- MySQL 8.0.16 ou mais novo, rodando em `localhost:3306`
- VS Code com o **Extension Pack for Java** (já traz suporte a Maven)

## Como rodar

1. Crie um arquivo `db.properties` na mesma pasta do `pom.xml` com o conteúdo abaixo, trocando `usuario` e `senha` pelos do seu MySQL:

   ```properties
   url=jdbc:mysql://localhost:3306/biblioteca_vila_leitura?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
   usuario=root
   senha=
   ```
   O `db.properties` não vai para o GitHub (está no `.gitignore`).
2. Abra a pasta `Biblioteca_System` no VS Code, espere o Maven baixar o driver e rode `com.fatec.biblioteca.Main`
   (botão **Run** em cima do `main`). Pelo terminal: `mvn compile exec:java`.
3. Na primeira execução o sistema cria o banco `biblioteca_vila_leitura`, as tabelas e o usuário
   **`admin` / senha `admin`** (Bibliotecário). Troque a senha em *Sistema > Alterar minha senha*.
4. (Opcional) Para ter dados de exemplo, rode `docs/seed_exemplo.sql` no banco ainda vazio
   (MySQL Workbench ou `mysql -u root -p < seed_exemplo.sql`). Ele também cria o login `voluntario` / `1234`.

## Organização

```
src/main/java/com/fatec/biblioteca/
├── Main.java                 carrega o banco (RF15) e abre o login
├── Pessoa, Leitor, Estudante, Professor, Comunidade
├── Funcionario, Bibliotecario, Voluntario
├── Obra, Copia, Emprestimo, Reserva, PerfilUso, ListaCobranca
├── Categoria, SituacaoCopia, SituacaoReserva (enums)
├── Constantes, Datas, Validador, Sessao
├── dao/   DAO (classe-mãe), ObraDAO, CopiaDAO, LeitorDAO, FuncionarioDAO,
│          EmprestimoDAO, ReservaDAO, BancoDados
└── ui/    FrmLogin, FrmPrincipal, FrmObra, FrmLeitor, FrmFuncionario, FrmEmprestimo,
           FrmDevolucao, FrmReserva, FrmConsulta*, FrmRel*, FrmExportarCobranca, ...
```

## Menu x requisitos do PDF

| Menu | Telas | RF |
|---|---|---|
| Cadastros | Obras, Adicionar exemplares, Leitores, Funcionários (só Bibliotecário) | RF01, RF02, RF04, RF06 |
| Circulação | Realizar empréstimo, Registrar devolução, Reservas | RF08, RF09 |
| Consultas | Acervo (filtro por categoria), Leitores, Empréstimos em aberto, Reservas | RF03, RF05, RF07, RF10 |
| Relatórios | Disponíveis e emprestados, Atrasos e multas, Perfil de uso, Exportar lista de cobrança | RF11–RF14 |
| Sistema | Alterar minha senha, Trocar usuário, Sair | — |

RF15 (carregar) roda ao abrir o programa e RF16 (gravar) acontece em cada operação, que já grava no banco na hora.
