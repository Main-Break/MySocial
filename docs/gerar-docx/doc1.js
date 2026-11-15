// Gera docs/estrutura-do-projeto.docx em formatação ABNT. Pra atualizar depois de
// mexer no projeto: edite o conteúdo abaixo (ou o conteúdo de doc2.js) e rode
// "npm install && node doc1.js" dentro desta pasta. Se o sumário (array PAGINAS,
// quando houver) ficar errado, confira as páginas reais renderizando o .docx com
// soffice + pdftoppm e corrija os números à mão.
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Header, Footer, PageNumber,
  CM, FONT, SIZE, LINE_15, FIRST_LINE_INDENT,
  h1, h2, subtitulo, p, bullet, step, code, caixa, table, spacer,
  NUMBERING_CONFIG,
} = require("./helpers");
const { TabStopType, TabStopPosition } = require("docx");
const fs = require("fs");

const capa = [
  new Paragraph({ spacing: { before: 800 }, children: [] }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "DESENVOLVIMENTO DE APLICATIVOS MÓVEIS", font: FONT, size: SIZE })],
  }),
  new Paragraph({ spacing: { before: 1600 }, children: [] }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "MYSOCIAL", bold: true, font: FONT, size: SIZE + 12 })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120 },
    children: [new TextRun({ text: "Estrutura do Projeto", bold: true, font: FONT, size: SIZE + 4 })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 200 },
    children: [new TextRun({
      text: "Documentação técnica de apoio para a defesa de código em equipe",
      italics: true, font: FONT, size: SIZE,
    })],
  }),
  new Paragraph({ spacing: { before: 1800 }, children: [] }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "[Nome dos integrantes da equipe]", font: FONT, size: SIZE })],
  }),
  new Paragraph({ spacing: { before: 1800 }, children: [] }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "2026", font: FONT, size: SIZE })],
  }),
  new Paragraph({ children: [], pageBreakBefore: true }),
];

const sumario = [
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 400 },
    children: [new TextRun({ text: "SUMÁRIO", bold: true, font: FONT, size: SIZE + 2 })],
  }),
  ...[
    ["1 VISÃO GERAL DO APP", 3],
    ["2 SEPARAÇÃO DOS PACOTES (PASTAS)", 3],
    ["3 O PADRÃO MVVM APLICADO NESTE PROJETO", 5],
    ["4 FLUXO COMPLETO DE UMA AÇÃO (EXEMPLO: CRIAR UM POST)", 5],
    ["5 BANCO DE DADOS E MIGRAÇÃO AUTOMÁTICA", 6],
    ["6 DECISÕES DE SEGURANÇA", 7],
    ["7 MAPA DE TELAS E ROTAS", 7],
  ].map(
    ([t, pagina]) =>
      new Paragraph({
        spacing: { after: 160, line: LINE_15 },
        tabStops: [{ type: TabStopType.RIGHT, position: 9072, leader: "dot" }],
        children: [
          new TextRun({ text: t, font: FONT, size: SIZE }),
          new TextRun({ text: "\t", font: FONT, size: SIZE }),
          new TextRun({ text: `${pagina}`, font: FONT, size: SIZE }),
        ],
      })
  ),
  new Paragraph({ children: [], pageBreakBefore: true }),
];

const corpo = [
  h1("1", "Visão geral do app"),
  p("O MySocial é um aplicativo de rede social no estilo do X (antigo Twitter): qualquer pessoa cadastrada pode publicar posts de até 280 caracteres, seguir outras pessoas e visualizar um feed com os posts mais recentes."),
  p("As funcionalidades implementadas no projeto são:"),
  bullet("Cadastro e login (com e-mail ou `@username`) com senha protegida por hash (nunca texto puro)."),
  bullet("Controle de sessão (identificação de quem está logado no momento)."),
  bullet("Feed de posts, ordenado do mais recente para o mais antigo."),
  bullet("Criação, edição e exclusão de post (CRUD completo), com compositor no estilo do X (campo grande, sem caixa visível, botão \"X\" para fechar)."),
  bullet("Perfil de usuário com bio, endereço e contagem de seguidores e seguindo."),
  bullet("Edição do próprio perfil (bio e endereço: CEP, rua, número, bairro, cidade e estado)."),
  bullet("Busca de usuários por `username`, para localizar e seguir outra pessoa."),
  bullet("Seguir e deixar de seguir outro usuário."),
  bullet("Lista de seguidores e de quem o usuário segue, com abas para alternar entre as duas (igual ao X)."),
  bullet("Tela de detalhe de um post, com curtida e comentários."),
  bullet("Curtir e descurtir um post."),
  bullet("Comentar um post e excluir o próprio comentário."),
  bullet("Perfil com capa colorida (cor própria de cada usuário) e avatar."),
  spacer(),

  h1("2", "Separação dos pacotes (pastas)"),
  p("Todo o código do projeto está organizado dentro do pacote `com.mysocial`, dividido por responsabilidade, conforme descrito a seguir."),
  h2("auth/"),
  p("Reúne tudo relacionado à autenticação e à sessão do usuário. A classe `AuthManager` é responsável por gerar e conferir o hash da senha. O objeto `UserSession` guarda em memória qual usuário está logado no momento."),
  h2("data/"),
  p("Contém as \"entidades\" do aplicativo (`User`, `Post`, `Comment`, `PostStats`), o acesso ao banco SQLite (`data/local/AppDatabase.kt`) e os DAOs (`data/dal/`), que são as únicas classes que executam comandos SQL de verdade: `UserDAO`, `PostDAO`, `FollowDAO`, `LikeDAO` e `CommentDAO`."),
  h2("navigation/"),
  p("O arquivo `Routes.kt` define cada tela do aplicativo como uma classe ou objeto serializável. O arquivo `AppNavigation.kt` monta o `NavHost`, que liga cada rota à tela correspondente e decide para onde navegar quando uma ação é concluída."),
  h2("ui/screen/"),
  p("Reúne as telas propriamente ditas (uma função `@Composable` por arquivo): Login, Cadastro, Feed, Post (criação/edição), Perfil, Editar Perfil, Seguidores/Seguindo, Busca de Usuários e Detalhe do Post."),
  h2("ui/component/"),
  p("Reúne pedaços de interface reaproveitados em mais de uma tela: `CardPost` (linha de um post exibida no feed, no perfil e no detalhe), `UserRow` (linha de um usuário exibida na busca e na lista de seguidores/seguindo) e `TopNavBar` (barra de navegação do topo)."),
  h2("ui/viewmodel/"),
  p("Funciona como a \"ponte\" entre a tela e o banco de dados: `AuthViewModel`, `PostViewModel`, `ProfileViewModel`, `FollowsViewModel`, `CommentViewModel` e `UsersViewModel`. Nenhuma tela acessa um DAO diretamente; o acesso sempre passa por um ViewModel."),
  h2("ui/theme/"),
  p("Centraliza cores, espaçamentos (`Dimens`) e tipografia do aplicativo, evitando espalhar números e cores fixas pelo código das telas."),
  h2("validation/"),
  p("Reúne as regras de \"o que é um dado válido\": `PostValidator` (limite de 280 caracteres) e `RegisterValidator` (regras de cadastro)."),
  spacer(),

  h1("3", "O padrão MVVM aplicado neste projeto"),
  p("MVVM é a sigla para Model, View, ViewModel: uma forma de separar \"o que o aplicativo mostra\" de \"como o aplicativo funciona por dentro\"."),
  table(
    ["Camada", "Onde fica", "Responsabilidade"],
    [
      ["Model", "data/", "Dados puros (Post, User) e acesso ao banco (DAOs)"],
      ["View", "ui/screen/, ui/component/", "Desenha a tela; não possui nenhuma lógica de SQL"],
      ["ViewModel", "ui/viewmodel/", "Liga a View ao Model e guarda o estado da tela"],
    ],
    [2400, 3672, 3000]
  ),
  spacer(),
  p("A regra fundamental é: a tela nunca se comunica diretamente com o banco de dados. Ela apenas chama uma função do ViewModel (como `postViewModel.create(texto)`) e observa um estado (`postViewModel.posts`) para saber o que desenhar. Isso significa que é possível trocar a tela inteira sem alterar a lógica de banco, e vice-versa."),
  spacer(),

  h1("4", "Fluxo completo de uma ação (exemplo: criar um post)"),
  p("A seguir, o passo a passo do que ocorre quando o usuário toca em \"Postar\":"),
  step("`PostScreen` chama `postViewModel.create(texto)`."),
  step("`PostViewModel` obtém o id do usuário logado em `UserSession`."),
  step("O texto é validado com `PostValidator.validate(...)`. Caso seja inválido, a execução é interrompida e o erro é exibido."),
  step("`PostDAO.create(...)` é chamado dentro de uma coroutine na thread de entrada/saída (`Dispatchers.IO`), para não travar a interface."),
  step("`PostDAO` monta um `ContentValues` e grava o registro na tabela `posts`, usando `insertOrThrow`."),
  step("Em caso de sucesso, o ViewModel marca `published = true`."),
  step("A tela observa esse valor por meio de um `LaunchedEffect`, percebe a mudança e navega de volta para o Feed."),
  spacer(),
  caixa("Esse mesmo padrão (a tela chama uma função, o ViewModel valida e grava, o estado muda, a tela reage) se repete em todas as demais operações do aplicativo: login, cadastro, edição de post, seguir outro usuário, entre outras."),
  spacer(),

  h1("5", "Banco de dados e migração automática"),
  p("O banco é SQLite puro, sem uso de Room ou de qualquer ORM, acessado por meio de `SQLiteOpenHelper` (classe `AppDatabase`). São cinco tabelas: `users` (contendo `cover_color`, a cor de capa do perfil, além dos campos de bio e endereço: `bio`, `cep`, `rua`, `numero`, `bairro`, `cidade` e `estado`), `posts`, `follows`, `likes` e `comments`."),
  p("O schema completo e atualizado está concentrado no método `onCreate`, em um único lugar (nunca espalhado em diversos arquivos do tipo \"update_1.sql\", \"update_2.sql\" etc.). Quando uma tabela ou coluna nova é adicionada posteriormente (como ocorreu com `follows` na versão 2, com `cover_color`/`likes`/`comments` na versão 3 e com bio/endereço na versão 4), o número da versão do banco é incrementado, e o método `onUpgrade` aplica a diferença automaticamente, no momento em que o aplicativo é aberto, sem exigir nenhum passo manual."),
  ...code([
    "override fun onUpgrade(db, oldVersion, newVersion) {",
    "    if (oldVersion < 2) {",
    "        db.execSQL(\"CREATE TABLE follows(...)\")",
    "    }",
    "    if (oldVersion < 3) {",
    "        db.execSQL(\"ALTER TABLE users ADD COLUMN cover_color ...\")",
    "        db.execSQL(\"CREATE TABLE likes(...)\")",
    "        db.execSQL(\"CREATE TABLE comments(...)\")",
    "    }",
    "    if (oldVersion < 4) {",
    "        db.execSQL(\"ALTER TABLE users ADD COLUMN bio ...\")",
    "        // (e o restante dos campos de endereço, mesma lógica)",
    "    }",
    "}",
  ]),
  spacer(),
  p("Essa abordagem evita o problema clássico de \"a migração foi esquecida em produção\": quem já possuía o aplicativo instalado em uma versão antiga recebe a tabela ou coluna nova automaticamente, sem perder os dados que já existiam."),
  spacer(),

  h1("6", "Decisões de segurança"),
  bullet("**SQL Injection:** toda consulta utiliza `?` no lugar do valor (bind parameters). Em nenhum momento o texto digitado pelo usuário é concatenado diretamente a uma query."),
  bullet("**Senha:** nunca é armazenada em texto puro. É transformada em um hash por meio de PBKDF2 (60 mil iterações), somado a um \"salt\" aleatório por usuário, de modo que duas pessoas com a mesma senha jamais produzem o mesmo hash salvo."),
  bullet("**Dono do dado:** editar ou excluir um post só é permitido se o `user_id` do post corresponder ao usuário logado. Essa verificação ocorre dentro do próprio `PostDAO`, não apenas na tela, o que impede que seja contornada."),
  bullet("**Banco fora do backup:** o arquivo do banco é armazenado em `noBackupFilesDir`, para que não seja incluído em backups automáticos na nuvem do Android."),
  spacer(),

  h1("7", "Mapa de telas e rotas"),
  table(
    ["Rota", "Tela", "Requer login?"],
    [
      ["LoginUser", "Login", "Não"],
      ["RegisterUser", "Cadastro", "Não"],
      ["Feed", "Lista de posts", "Sim"],
      ["RegisterPost", "Criar post", "Sim"],
      ["EditPost(postId)", "Editar post", "Sim, apenas o dono"],
      ["DetailPost(postId)", "Detalhe do post", "Sim"],
      ["Profile(userId)", "Perfil de um usuário", "Sim"],
      ["EditProfile", "Editar o próprio perfil", "Sim"],
      ["Follows(userId, type)", "Seguidores ou seguindo", "Sim"],
      ["Users", "Busca de usuários", "Sim"],
    ],
    [3072, 3100, 2900]
  ),
  spacer(),
  p("Quem tenta acessar uma rota protegida sem estar logado é redirecionado automaticamente para a tela de Login (lógica implementada dentro de um `LaunchedEffect` em `AppNavigation.kt`). As telas de Login e Cadastro são as únicas que não exibem a barra de navegação do topo."),
];

const doc = new Document({
  numbering: NUMBERING_CONFIG,
  styles: {
    default: {
      document: { run: { font: FONT, size: SIZE, color: "000000" } },
      heading1: { run: { color: "000000", bold: true } },
      heading2: { run: { color: "000000", bold: true } },
    },
  },
  sections: [
    {
      properties: {
        page: {
          margin: { top: CM * 3, right: CM * 2, bottom: CM * 2, left: CM * 3 },
        },
        titlePage: true,
      },
      headers: {
        default: new Header({ children: [new Paragraph({})] }),
        first: new Header({ children: [new Paragraph({})] }),
      },
      footers: {
        default: new Footer({
          children: [
            new Paragraph({
              alignment: AlignmentType.RIGHT,
              children: [new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: SIZE - 2 })],
            }),
          ],
        }),
        first: new Footer({ children: [new Paragraph({})] }),
      },
      children: [...capa, ...sumario, ...corpo],
    },
  ],
});

Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync("../estrutura-do-projeto.docx", buf);
  console.log("ok: estrutura-do-projeto.docx");
});
