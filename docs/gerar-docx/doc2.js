const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Header, Footer, PageNumber,
  CM, FONT, SIZE, LINE_15,
  h1, h2, subtitulo, p, bullet, step, code, caixa, table, spacer,
  NUMBERING_CONFIG,
} = require("./helpers");
const { TabStopType } = require("docx");
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
    children: [new TextRun({ text: "Componentes de Android e Kotlin Usados no Projeto", bold: true, font: FONT, size: SIZE + 4 })],
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 200 },
    children: [new TextRun({
      text: "Guia de apoio para quem tem menos familiaridade com Android/Compose",
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

// Páginas preenchidas depois de uma primeira renderização de conferência.
const PAGINAS = {
  "1 INTERFACE DECLARATIVA COM COMPOSE": 3,
  "2 ESTADO DA TELA": 3,
  "3 LISTAS": 5,
  "4 VIEWMODEL E COROUTINES": 6,
  "5 NAVEGAÇÃO ENTRE TELAS": 8,
  "6 AÇÕES E MENUS": 9,
  "7 BANCO DE DADOS": 11,
};

function sumarioParagrafo(texto) {
  return new Paragraph({
    spacing: { after: 160, line: LINE_15 },
    tabStops: [{ type: TabStopType.RIGHT, position: 9072, leader: "dot" }],
    children: [
      new TextRun({ text: texto, font: FONT, size: SIZE }),
      new TextRun({ text: "\t", font: FONT, size: SIZE }),
      new TextRun({ text: `${PAGINAS[texto] || ""}`, font: FONT, size: SIZE }),
    ],
  });
}

const sumario = [
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 400 },
    children: [new TextRun({ text: "SUMÁRIO", bold: true, font: FONT, size: SIZE + 2 })],
  }),
  ...Object.keys(PAGINAS).map(sumarioParagrafo),
  new Paragraph({ children: [], pageBreakBefore: true }),
];

const intro = [
  p("Este guia explica o que cada componente de Android e Kotlin utilizado no projeto MySocial faz, por que foi utilizado e como funciona por baixo dos panos, sempre acompanhado de um trecho real do código do projeto. É direcionado a quem possui menos familiaridade com Android e Jetpack Compose."),
  spacer(),
];

// Cada item: título, subtítulo (origem/pacote), parágrafos e blocos de código,
// na ordem em que devem aparecer.
function item(titulo, origem, blocos, mono = true) {
  return [h2(titulo, mono), subtitulo(origem), ...blocos, spacer()];
}

const corpo = [
  h1("1", "Interface declarativa com Compose"),
  ...item("@Composable", "Anotação do Jetpack Compose", [
    p("É a anotação que transforma uma função Kotlin comum em uma \"peça de interface\". Em vez de montar a tela de forma imperativa (criar um botão, adicioná-lo à tela e depois configurar o clique), descreve-se apenas \"como a tela deveria ser\" para o estado atual, e o Compose se encarrega de desenhá-la."),
    p("Toda tela do projeto (`FeedScreen`, `LoginScreen` etc.) e todo componente reaproveitado (`CardPost`, `TopNavBar`) é uma função marcada com `@Composable`."),
    ...code(["@Composable", "fun CardPost(username: String, ...) {", "    Card(...) { ... }", "}"]),
    p("Sempre que algum dado utilizado pela função é alterado, o Compose chama a função novamente (processo chamado de \"recomposição\") e atualiza na tela apenas o que de fato mudou."),
  ]),

  h1("2", "Estado da tela"),
  ...item("remember e mutableStateOf", "androidx.compose.runtime", [
    p("`mutableStateOf(valorInicial)` cria uma \"caixa\" de estado: um valor que, ao ser alterado, avisa o Compose para redesenhar quem o utiliza. `remember { ... }` garante que esse valor não seja recriado do zero a cada recomposição da função, preservando-o entre uma recomposição e outra."),
    ...code(['var email by remember { mutableStateOf("") }']),
    p("Utilizado em praticamente toda tela com campo de texto (`LoginScreen`, `RegisterScreen`, `PostScreen`) para armazenar o que o usuário está digitando."),
  ], false),
  ...item("O operador \"by\" (property delegation)", "Recurso da linguagem Kotlin", [
    p("Sem o `by`, para utilizar o valor contido em um `mutableStateOf` seria necessário escrever `email.value` a cada acesso. Com `by`, o Kotlin \"delega\" a leitura e a escrita da variável diretamente ao `mutableStateOf`, de modo que `email` se comporta como uma String comum."),
    ...code(["var loading by mutableStateOf(false)", "    private set"]),
    p("Esse `private set` aparece em praticamente todo ViewModel do projeto: qualquer código pode ler o valor, mas apenas o próprio ViewModel pode alterá-lo. É assim que a tela nunca consegue alterar o estado \"por fora\", apenas por meio de uma função do ViewModel."),
  ], false),
  ...item("LaunchedEffect", "androidx.compose.runtime", [
    p("Executa um bloco de código suspenso (uma coroutine) quando a tela aparece, e o executa novamente toda vez que a \"chave\" passada a ele é alterada. É a forma correta, dentro do Compose, de reagir a mudanças de estado ou de disparar uma ação uma única vez."),
    ...code(["LaunchedEffect(Unit) {", "    postViewModel.list()", "}"]),
    p("Com a chave `Unit` (que nunca se altera), o bloco é executado uma única vez, quando a tela é exibida pela primeira vez. É assim que `FeedScreen` busca os posts assim que é aberta."),
    ...code(["LaunchedEffect(authViewModel.done) {", "    if (authViewModel.done) {", "        authViewModel.consumeResult()", "        onLoginSuccess()", "    }", "}"]),
    p("Neste segundo caso, a chave é o próprio estado `done`. Toda vez que ele é alterado (de `false` para `true`, quando o login é bem-sucedido), o bloco é executado novamente e a navegação ocorre. É o mesmo mecanismo utilizado para exigir login: em `AppNavigation.kt`, um `LaunchedEffect(destination, currentUser)` redireciona para o Login sempre que alguém tenta abrir uma rota protegida sem estar autenticado."),
  ]),

  h1("3", "Listas"),
  ...item("LazyColumn e items", "androidx.compose.foundation.lazy", [
    p("É a versão \"preguiçosa\" (lazy) de uma coluna com rolagem. A diferença em relação a uma `Column` comum dentro de um `scroll` é que a `LazyColumn` desenha na memória apenas os itens efetivamente visíveis na tela (mais uma pequena margem), em vez de desenhar os quinhentos posts do banco de uma só vez. Isso é essencial para a performance de uma lista que pode crescer consideravelmente, como o feed."),
    ...code(["LazyColumn {", "    item { CircularProgressIndicator() }", "    items(postViewModel.posts, key = { it.id }) { post ->", "        CardPost(...)", "    }", "}"]),
    p("`item { }` adiciona um elemento único (como um cabeçalho). `items(lista, key = ...)` adiciona um elemento para cada posição da lista. O parâmetro `key` ajuda o Compose a identificar \"qual item é qual\" mesmo que a lista seja reordenada, evitando redesenhos desnecessários."),
  ], false),
  ...item("rememberSaveable", "androidx.compose.runtime.saveable", [
    p("Equivalente ao `remember`, porém também sobrevive a uma troca de configuração do sistema (como a rotação da tela), pois o valor é salvo em um `Bundle`, não apenas em memória. Utilizado para armazenar qual post está aguardando confirmação de exclusão, de modo a não perder esse estado caso o usuário gire o aparelho durante a confirmação."),
    ...code(["var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }"]),
  ], false),
  ...item("TextField sem caixa (compositor estilo X)", "androidx.compose.material3", [
    p("`TextField` é equivalente ao `OutlinedTextField`: ambos pertencem ao mesmo pacote e funcionam de forma idêntica (`value`, `onValueChange` etc.), diferindo apenas no visual padrão. O compositor de post/comentário utiliza `TextField` com as cores do fundo e da linha inferior definidas como transparentes, de modo que nenhuma caixa seja exibida, apenas o texto digitado, como o campo \"O que está acontecendo?\" do X."),
    ...code(["TextField(", "    value = content,", "    onValueChange = { ... },", '    placeholder = { Text("O que está acontecendo?") },', "    minLines = 6,", "    colors = TextFieldDefaults.colors(", "        focusedContainerColor = Color.Transparent,", "        unfocusedContainerColor = Color.Transparent,", "        focusedIndicatorColor = Color.Transparent,", "        unfocusedIndicatorColor = Color.Transparent", "    )", ")"]),
    p("`minLines` faz o campo já surgir com diversas linhas de altura, em vez de uma única linha que cresce aos poucos. Os demais comportamentos (limite de caracteres, validação) permanecem idênticos aos do `OutlinedTextField` utilizado nos demais formulários do projeto (Login, Cadastro, Editar Perfil)."),
  ]),

  h1("4", "ViewModel e coroutines"),
  ...item("ViewModel e AndroidViewModel", "androidx.lifecycle", [
    p("Um `ViewModel` é uma classe que armazena o estado de uma tela de forma que sobrevive a recomposições e a mudanças de configuração (como a rotação do aparelho), pois não é recriado junto com a tela: permanece atrelado ao ciclo de vida da tela/rota, não ao seu desenho."),
    p("`AndroidViewModel` é um `ViewModel` que já recebe o `Application` (contexto da aplicação) no construtor, necessário neste projeto porque os DAOs (`UserDAO`, `PostDAO` etc.) precisam de um `Context` para abrir o banco."),
    ...code(["class PostViewModel(application: Application)", "    : AndroidViewModel(application) {", "    private val dao = PostDAO(application)", "    var posts by mutableStateOf<List<Post>>(emptyList())", "        private set", "    ...", "}"]),
    p("A tela obtém uma instância dele da seguinte forma: `postViewModel: PostViewModel = viewModel()`. O Compose se encarrega de criar e reaproveitar essa instância automaticamente."),
  ], false),
  ...item("viewModelScope e coroutines (launch)", "androidx.lifecycle / kotlinx.coroutines", [
    p("Uma coroutine é uma forma de executar código que pode \"pausar\" (por exemplo, aguardando a resposta do banco) sem travar a interface do usuário enquanto aguarda. `viewModelScope` é um escopo de coroutine automaticamente cancelado caso o ViewModel seja destruído, evitando que alguma tarefa continue sendo executada sem necessidade."),
    ...code(["viewModelScope.launch {", "    try {", "        done = withContext(Dispatchers.IO) { op() }", '    } catch (e: Exception) {', '        message = "Ocorreu um erro inesperado."', "    } finally {", "        loading = false", "    }", "}"]),
    p("Esse padrão (uma função `run` privada dentro de cada ViewModel) se repete em `AuthViewModel`, `PostViewModel`, `ProfileViewModel` e `FollowsViewModel`: controla o estado `loading`, executa a operação e transforma qualquer erro em uma mensagem compreensível para a tela."),
  ], false),
  ...item("Dispatchers.IO e withContext", "kotlinx.coroutines", [
    p("Acessar o banco de dados é uma operação de entrada e saída (I/O) que pode levar alguns milissegundos. Caso fosse executada na thread principal (a responsável por desenhar a tela), o aplicativo congelaria momentaneamente. `Dispatchers.IO` é um conjunto de threads destinado a esse tipo de tarefa."),
    ...code(["withContext(Dispatchers.IO) {", "    dao.create(userId, trimmedContent)", "}"]),
    p("`withContext` alterna para essa thread apenas durante a execução do bloco, executa o código ali e retorna automaticamente à thread principal assim que termina, já com o resultado pronto para atualizar o estado da tela com segurança."),
  ]),

  h1("5", "Navegação entre telas"),
  ...item("NavHost, NavController e rotas @Serializable", "androidx.navigation.compose", [
    p("`NavController` é o objeto responsável por saber para onde o aplicativo está navegando e por manter o histórico de telas (necessário para o funcionamento do botão \"voltar\"). `NavHost` é o componente que declara todas as rotas possíveis e qual tela corresponde a cada uma delas."),
    p("Cada rota é uma classe ou objeto Kotlin marcado com `@Serializable`, em vez de uma simples String, como em projetos mais antigos. Isso garante segurança de tipos: é impossível, por exemplo, navegar para a tela de detalhe do post esquecendo de informar o `postId`, pois isso sequer compila."),
    ...code(["@Serializable", "data class DetailPost(val postId: Long)", "", "composable<DetailPost> { backStackEntry ->", "    val rota = backStackEntry.toRoute<DetailPost>()", "    DetailPostScreen(postId = rota.postId, ...)", "}"]),
    p("Para navegar, basta criar uma instância da rota: `navController.navigate(DetailPost(post.id))`."),
  ]),

  h1("6", "Ações e menus"),
  ...item("Scaffold e TopAppBar", "androidx.compose.material3", [
    p("`Scaffold` é um \"esqueleto\" de tela padrão do Material Design: já reserva o espaço adequado para uma barra do topo, o conteúdo e, eventualmente, uma barra inferior, cuidando do espaçamento (`innerPadding`) automaticamente. O projeto possui um único `Scaffold`, localizado em `MainActivity`, que envolve toda a navegação."),
    ...code(["Scaffold(", "    topBar = { if (showTopBar) TopNavBar(navController) }", ") { innerPadding ->", "    AppNavigation(modifier = Modifier.padding(innerPadding))", "}"]),
    p("`TopAppBar` (utilizado dentro de `TopNavBar`) é a barra fixa no topo com o nome do aplicativo e os ícones de ação (novo post, feed, perfil, sair). É exibida apenas quando a rota atual não corresponde a Login nem a Cadastro."),
  ]),
  ...item("AlertDialog (e como virou um menu de ações)", "androidx.compose.material3", [
    p("É uma janela que flutua sobre o restante da tela. O uso mais comum consiste em confirmar uma ação irreversível, como a exclusão de um post: em vez de excluir no primeiro toque, o botão \"Excluir\" apenas registra qual item deveria ser excluído, e o `AlertDialog` solicita confirmação antes de acionar o ViewModel de fato."),
    ...code(["AlertDialog(", "    onDismissRequest = { deleteId = null },", '    title = { Text("Excluir post?") },', "    confirmButton = {", "        TextButton(onClick = {", "            deleteId = null", "            postViewModel.delete(post.id)", '        }) { Text("Excluir") }', "    },", "    dismissButton = {", '        TextButton(onClick = { deleteId = null }) { Text("Cancelar") }', "    }", ")"]),
    p("No `CardPost` e no `DetailPostScreen`, o mesmo componente é reaproveitado de forma diferente: em vez de \"confirmar ou cancelar\", o conteúdo do diálogo passa a ser uma lista de botões (\"Editar\", \"Excluir\"), funcionando como um pequeno menu de ações. Ele é aberto apenas quando a pessoa toca no ícone de três pontos (`Icons.Default.MoreVert`) no cabeçalho do post, e é exibido somente para quem é o dono dele."),
  ]),
  ...item("PrimaryTabRow e Tab", "androidx.compose.material3 (não consta no material do professor, porém é apenas um menu de abas)", [
    p("É o menu de abas que alterna o conteúdo da tela sem a necessidade de navegar para outra rota. Utilizado na tela de Seguidores/Seguindo: um `PrimaryTabRow` com dois `Tab` (\"Seguidores\" e \"Seguindo\"), em que a aba marcada como `selected = true` determina qual lista é carregada."),
    ...code(["var abaSelecionada by remember(userId) { mutableStateOf(type) }", "", "PrimaryTabRow(selectedTabIndex = if (abaSelecionada == FOLLOWERS) 0 else 1) {", "    Tab(", "        selected = abaSelecionada == FOLLOWERS,", "        onClick = { abaSelecionada = FOLLOWERS },", '        text = { Text("Seguidores") }', "    )", "    Tab(", "        selected = abaSelecionada == FOLLOW,", "        onClick = { abaSelecionada = FOLLOW },", '        text = { Text("Seguindo") }', "    )", "}"]),
    p("A troca de aba apenas altera o valor de `abaSelecionada` (um estado comum, com `remember`), e um `LaunchedEffect(userId, abaSelecionada)` recarrega a lista correspondente sempre que esse valor é alterado, sem necessidade de retornar à tela de perfil e tocar novamente."),
  ]),
  ...item("Image e painterResource", "androidx.compose.foundation / androidx.compose.ui.res (mesma abordagem utilizada pelo professor)", [
    p("É a forma como o Compose desenha uma imagem proveniente de um recurso do projeto (pasta `res/drawable/`). `painterResource(id = R.drawable.algumacoisa)` carrega o arquivo, e `Image(painter = ..., contentDescription = ...)` o desenha na tela. Esse é exatamente o padrão utilizado pelo professor em seu material."),
    ...code(["Image(", "    painter = painterResource(id = R.drawable.ic_profile_placeholder),", '    contentDescription = "Foto de perfil",', "    modifier = Modifier.size(Dimens.avatarSize)", ")"]),
    p("O projeto utiliza um ícone de pessoa (`ic_profile_placeholder.xml`) como foto de perfil provisória, dentro de um círculo colorido (a cor de capa de cada usuário). Trata-se de um vector drawable, ou seja, um desenho vetorial (equivalente, no Android, a um SVG), armazenado localmente no projeto, sem depender de nenhum arquivo obtido externamente. No futuro, a substituição desse ícone por uma fotografia enviada pelo usuário consiste apenas em trocar o `painterResource` por um `painter` que carregue a fotografia, sem alterar o restante da tela."),
  ]),

  h1("7", "Banco de dados"),
  ...item("SQLiteOpenHelper", "android.database.sqlite", [
    p("É a classe base do Android para criar e manter um banco SQLite, sem a necessidade de nenhuma biblioteca externa (sem uso de Room). Estende-se essa classe e implementam-se dois métodos: `onCreate` (executado apenas na primeira vez que o aplicativo é aberto, cria todas as tabelas do zero) e `onUpgrade` (executado quando o número de versão do banco é incrementado, aplicando as alterações de schema aos dados já existentes)."),
    ...code(["class AppDatabase(context: Context) : SQLiteOpenHelper(", "    context.applicationContext,", '    File(context.noBackupFilesDir, "mysocial.db").absolutePath,', "    null,", "    4 // versão do schema", ") {", "    override fun onCreate(db: SQLiteDatabase) { ... }", "    override fun onUpgrade(db, oldVersion, newVersion) { ... }", "}"]),
    p("`writableDatabase` abre o banco em modo leitura e escrita (utilizado para INSERT/UPDATE/DELETE). `readableDatabase` abre apenas para leitura (utilizado nas buscas). Os DAOs sempre utilizam `AppDatabase(context).use { db -> ... }`; o `use` garante que a conexão seja fechada automaticamente ao final, mesmo em caso de erro."),
  ]),
  ...item("ContentValues e Cursor", "android.content / android.database", [
    p("`ContentValues` é um mapa de \"nome da coluna\" para \"valor\", utilizado para montar um INSERT ou UPDATE sem concatenar texto (o que abriria brecha para SQL Injection)."),
    ...code(["val values = ContentValues().apply {", '    put("username", username)', '    put("email", email)', "}", 'db.writableDatabase.insertOrThrow("users", null, values)']),
    p("`Cursor` é o objeto que representa o resultado de uma consulta (`query` ou `rawQuery`), uma linha por vez. `cursor.moveToNext()` avança para a próxima linha, e `cursor.getLong(0)`/`getString(1)` leem o valor de cada coluna pela posição."),
    ...code(['db.readableDatabase.query(', '    "users", arrayOf("id", "username", "email"),', '    "id = ?", arrayOf(id.toString()),', "    null, null, null", ").use { cursor ->", "    if (cursor.moveToFirst())", "        User(cursor.getLong(0), cursor.getString(1), cursor.getString(2))", "    else null", "}"]),
    p("Observa-se o `\"id = ?\"` seguido de `arrayOf(id.toString())` logo abaixo: esse é o \"bind parameter\". O SQLite substitui o `?` pelo valor de forma segura, nunca interpretando o conteúdo como parte do comando SQL. É dessa maneira que todo o projeto se protege contra SQL Injection."),
  ]),
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
      children: [...capa, ...sumario, ...intro, ...corpo],
    },
  ],
});

Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync("../componentes-android-kotlin.docx", buf);
  console.log("ok: componentes-android-kotlin.docx");
});
