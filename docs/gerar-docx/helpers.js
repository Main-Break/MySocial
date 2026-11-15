const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Table, TableRow, TableCell, WidthType, BorderStyle, Header, Footer,
  PageNumber, LevelFormat, ShadingType, VerticalAlign,
} = require("docx");

// Configuração de numeração (bullets e listas numeradas) compartilhada pelos
// dois documentos. Precisa ser passada em "numbering" ao criar o Document.
const NUMBERING_CONFIG = {
  config: [
    {
      reference: "bullets",
      levels: [
        {
          level: 0,
          format: LevelFormat.BULLET,
          text: "•",
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 540, hanging: 260 } } },
        },
      ],
    },
    {
      reference: "passos",
      levels: [
        {
          level: 0,
          format: LevelFormat.DECIMAL,
          text: "%1.",
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 540, hanging: 260 } } },
        },
      ],
    },
  ],
};

const CM = 566.929; // twips por centímetro
const FONT = "Times New Roman";
const CODE_FONT = "Consolas";
const SIZE = 24; // 12pt
const SIZE_SMALL = 20; // 10pt (código, notas)
const LINE_15 = 360; // espaçamento 1,5 (ABNT)
const LINE_1 = 240; // espaçamento simples (código)
const FIRST_LINE_INDENT = 709; // 1,25cm, recuo de primeira linha (ABNT)

// Markdown simplificado dentro do texto: `codigo` vira fonte monoespaçada,
// **negrito** vira negrito. O resto é texto normal, tudo em Times New Roman 12.
function parseInline(text, baseSize = SIZE, forceBold = false) {
  const regex = /(`[^`]+`|\*\*[^*]+\*\*)/g;
  const parts = text.split(regex).filter((p) => p !== undefined && p !== "");
  return parts.map((part) => {
    if (part.startsWith("`") && part.endsWith("`")) {
      return new TextRun({ text: part.slice(1, -1), font: CODE_FONT, size: baseSize - 2, bold: forceBold });
    }
    if (part.startsWith("**") && part.endsWith("**")) {
      return new TextRun({ text: part.slice(2, -2), bold: true, font: FONT, size: baseSize });
    }
    return new TextRun({ text: part, font: FONT, size: baseSize, bold: forceBold });
  });
}

// Título de seção primária: numerado, caixa alta, negrito (padrão ABNT).
function h1(numero, titulo) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 360, after: 200, line: LINE_15 },
    keepNext: true,
    children: [
      new TextRun({ text: `${numero} ${titulo.toUpperCase()}`, bold: true, font: FONT, size: SIZE + 2, color: "000000" }),
    ],
  });
}

// Título de seção secundária (ex: nome de um pacote ou componente): negrito, sem caixa alta.
function h2(titulo, mono = false) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 240, after: 120, line: LINE_15 },
    keepNext: true,
    children: [
      new TextRun({ text: titulo, bold: true, font: mono ? CODE_FONT : FONT, size: SIZE, color: "000000" }),
    ],
  });
}

// Subtítulo pequeno em itálico (ex: "androidx.compose.runtime"), logo abaixo de um h2.
function subtitulo(texto) {
  return new Paragraph({
    spacing: { after: 120, line: LINE_15 },
    keepNext: true,
    children: [new TextRun({ text: texto, italics: true, font: FONT, size: SIZE - 2, color: "555555" })],
  });
}

// Parágrafo de texto corrido: justificado, recuo de primeira linha, espaçamento 1,5.
function p(text) {
  return new Paragraph({
    alignment: AlignmentType.JUSTIFIED,
    spacing: { after: 160, line: LINE_15 },
    indent: { firstLine: FIRST_LINE_INDENT },
    children: parseInline(text),
  });
}

// Item de lista com marcador.
function bullet(text) {
  return new Paragraph({
    alignment: AlignmentType.JUSTIFIED,
    spacing: { after: 80, line: LINE_15 },
    numbering: { reference: "bullets", level: 0 },
    children: parseInline(text),
  });
}

// Item de lista numerada.
function step(text) {
  return new Paragraph({
    alignment: AlignmentType.JUSTIFIED,
    spacing: { after: 80, line: LINE_15 },
    numbering: { reference: "passos", level: 0 },
    children: parseInline(text),
  });
}

// Bloco de código: fonte monoespaçada, fundo cinza claro, borda fina, espaçamento simples.
function code(lines) {
  return lines.map(
    (line, i) =>
      new Paragraph({
        spacing: { line: LINE_1, after: 0 },
        shading: { type: ShadingType.CLEAR, fill: "F2F2F2" },
        border: {
          top: i === 0 ? { style: BorderStyle.SINGLE, size: 4, color: "999999" } : undefined,
          bottom: i === lines.length - 1 ? { style: BorderStyle.SINGLE, size: 4, color: "999999" } : undefined,
          left: { style: BorderStyle.SINGLE, size: 4, color: "999999" },
          right: { style: BorderStyle.SINGLE, size: 4, color: "999999" },
        },
        children: [new TextRun({ text: line.length ? line : " ", font: CODE_FONT, size: SIZE_SMALL })],
      })
  );
}

// Caixa de destaque (texto corrido com borda e fundo, tipo "nota").
function caixa(text) {
  return new Paragraph({
    alignment: AlignmentType.JUSTIFIED,
    spacing: { before: 120, after: 160, line: LINE_15 },
    shading: { type: ShadingType.CLEAR, fill: "F2F2F2" },
    border: {
      top: { style: BorderStyle.SINGLE, size: 4, color: "999999" },
      bottom: { style: BorderStyle.SINGLE, size: 4, color: "999999" },
      left: { style: BorderStyle.SINGLE, size: 4, color: "999999" },
      right: { style: BorderStyle.SINGLE, size: 4, color: "999999" },
    },
    indent: { left: 200, right: 200 },
    children: parseInline(text, SIZE - 2),
  });
}

const TABLE_WIDTH = 9072; // ~16cm, largura útil da página A4 com margens ABNT

function cell(text, { header = false, width } = {}) {
  return new TableCell({
    width: { size: width, type: WidthType.DXA },
    shading: header ? { type: ShadingType.CLEAR, fill: "E0E0E0" } : undefined,
    verticalAlign: VerticalAlign.CENTER,
    margins: { top: 60, bottom: 60, left: 100, right: 100 },
    children: [
      new Paragraph({
        spacing: { line: LINE_1 },
        children: parseInline(text, SIZE - 2, header),
      }),
    ],
  });
}

// Tabela ABNT simples: cabeçalho sombreado em negrito, bordas finas.
function table(headers, rows, widths) {
  const colWidths = widths || headers.map(() => Math.floor(TABLE_WIDTH / headers.length));
  return new Table({
    width: { size: TABLE_WIDTH, type: WidthType.DXA },
    columnWidths: colWidths,
    borders: {
      top: { style: BorderStyle.SINGLE, size: 4, color: "666666" },
      bottom: { style: BorderStyle.SINGLE, size: 4, color: "666666" },
      left: { style: BorderStyle.SINGLE, size: 4, color: "666666" },
      right: { style: BorderStyle.SINGLE, size: 4, color: "666666" },
      insideHorizontal: { style: BorderStyle.SINGLE, size: 2, color: "999999" },
      insideVertical: { style: BorderStyle.SINGLE, size: 2, color: "999999" },
    },
    rows: [
      new TableRow({
        children: headers.map((h, i) => cell(h, { header: true, width: colWidths[i] })),
      }),
      ...rows.map(
        (row) => new TableRow({ children: row.map((v, i) => cell(v, { width: colWidths[i] })) })
      ),
    ],
  });
}

function spacer() {
  return new Paragraph({ spacing: { after: 40 } });
}

module.exports = {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Header, Footer, PageNumber,
  CM, FONT, CODE_FONT, SIZE, SIZE_SMALL, LINE_15, FIRST_LINE_INDENT,
  h1, h2, subtitulo, p, bullet, step, code, caixa, table, spacer,
  NUMBERING_CONFIG,
};
