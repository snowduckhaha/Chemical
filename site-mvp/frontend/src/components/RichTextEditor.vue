<template>
  <div class="rich-editor">
    <div class="toolbar" role="toolbar" aria-label="正文编辑工具">
      <button type="button" title="段落" @mousedown.prevent="formatBlock('p')">正文</button>
      <button type="button" title="二级标题" @mousedown.prevent="formatBlock('h2')">标题</button>
      <button type="button" title="加粗" @mousedown.prevent="command('bold')"><strong>B</strong></button>
      <button type="button" title="斜体" @mousedown.prevent="command('italic')"><em>I</em></button>
      <button type="button" title="项目列表" @mousedown.prevent="command('insertUnorderedList')">• 列表</button>
      <button type="button" title="编号列表" @mousedown.prevent="command('insertOrderedList')">1. 列表</button>
      <button type="button" title="插入链接" @mousedown.prevent="insertLink">链接</button>
      <span class="divider" aria-hidden="true"></span>
      <button type="button" title="插入 3 列 3 行表格" @mousedown.prevent="insertTable">插入表格</button>
      <button type="button" title="在当前行后新增一行" :disabled="!activeCell" @mousedown.prevent="addRow">+ 行</button>
      <button type="button" title="在当前列后新增一列" :disabled="!activeCell" @mousedown.prevent="addColumn">+ 列</button>
      <button type="button" title="删除当前行" :disabled="!activeCell" @mousedown.prevent="removeRow">− 行</button>
      <button type="button" title="删除当前列" :disabled="!activeCell" @mousedown.prevent="removeColumn">− 列</button>
    </div>
    <div
      ref="editor"
      class="editor-surface"
      contenteditable="true"
      role="textbox"
      aria-multiline="true"
      :data-placeholder="placeholder"
      @input="emitValue"
      @keyup="updateActiveCell"
      @mouseup="updateActiveCell"
      @focus="updateActiveCell"
      @paste="paste"
    ></div>
    <p class="hint">可直接在单元格中输入内容；将光标放入表格后可增删行、列。</p>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from "vue";

const props = withDefaults(defineProps<{ modelValue: string; placeholder?: string }>(), {
  placeholder: "请输入正文…"
});
const emit = defineEmits<{ "update:modelValue": [value: string] }>();
const editor = ref<HTMLDivElement>();
const activeCell = ref(false);

const hasHtml = (value: string) => /<\/?[a-z][^>]*>/i.test(value);
const escapeHtml = (value: string) => value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
const editableHtml = (value: string) => {
  if (!value.trim() || hasHtml(value)) return value;
  return value.split(/\r?\n\s*\r?\n/).map((paragraph) =>
    `<p>${escapeHtml(paragraph).replace(/\r?\n/g, "<br>")}</p>`
  ).join("");
};

const selectedCell = () => {
  const node = window.getSelection()?.anchorNode;
  const element = node?.nodeType === Node.ELEMENT_NODE ? node as Element : node?.parentElement;
  return element?.closest("td, th") as HTMLTableCellElement | null;
};
const updateActiveCell = () => { activeCell.value = Boolean(selectedCell()); };
const emitValue = () => { emit("update:modelValue", editor.value?.innerHTML || ""); updateActiveCell(); };
const focus = () => editor.value?.focus();

const command = (name: string, value?: string) => {
  focus();
  document.execCommand(name, false, value);
  emitValue();
};
const formatBlock = (tag: string) => command("formatBlock", tag);
const insertLink = () => {
  const address = window.prompt("请输入链接地址（例如 https://example.com）：", "");
  if (!address) return;
  const href = /^(https?:|mailto:|\/|#)/i.test(address) ? address : `https://${address}`;
  command("createLink", href);
};
const insertTable = () => {
  const header = ["项目", "参数", "说明"].map((text) => `<th>${text}</th>`).join("");
  const rows = Array.from({ length: 3 }, () => "<tr><td>内容</td><td>内容</td><td>内容</td></tr>").join("");
  command("insertHTML", `<table><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table><p><br></p>`);
};
const currentRow = () => selectedCell()?.parentElement as HTMLTableRowElement | null;
const currentTable = () => selectedCell()?.closest("table") as HTMLTableElement | null;
const addRow = () => {
  const row = currentRow();
  if (!row) return;
  const next = row.cloneNode(false) as HTMLTableRowElement;
  Array.from(row.cells).forEach((cell) => { const copy = document.createElement(cell.tagName.toLowerCase()); copy.innerHTML = "<br>"; next.append(copy); });
  row.after(next); emitValue();
};
const addColumn = () => {
  const cell = selectedCell(); const table = currentTable();
  if (!cell || !table) return;
  const column = cell.cellIndex + 1;
  table.querySelectorAll("tr").forEach((row) => {
    const tag = row.parentElement?.tagName === "THEAD" ? "th" : "td";
    const next = document.createElement(tag); next.innerHTML = "<br>";
    row.insertBefore(next, row.children[column] || null);
  });
  emitValue();
};
const removeRow = () => {
  const row = currentRow(); const table = currentTable();
  if (!row || !table) return;
  row.remove();
  if (!table.querySelector("tr")) table.remove();
  emitValue();
};
const removeColumn = () => {
  const cell = selectedCell(); const table = currentTable();
  if (!cell || !table) return;
  const column = cell.cellIndex;
  table.querySelectorAll("tr").forEach((row) => row.children[column]?.remove());
  table.querySelectorAll("tr").forEach((row) => { if (!row.children.length) row.remove(); });
  if (!table.querySelector("tr")) table.remove();
  emitValue();
};
const paste = (event: ClipboardEvent) => {
  const html = event.clipboardData?.getData("text/html");
  if (!html) return;
  event.preventDefault();
  const fragment = document.createElement("template");
  fragment.innerHTML = html;
  fragment.content.querySelectorAll("script, style, iframe, object, embed").forEach((node) => node.remove());
  fragment.content.querySelectorAll<HTMLElement>("*").forEach((node) => {
    [...node.attributes].forEach((attribute) => {
      if (attribute.name.startsWith("on") || (attribute.name === "href" && /^javascript:/i.test(attribute.value))) node.removeAttribute(attribute.name);
    });
  });
  command("insertHTML", fragment.innerHTML);
};

watch(() => props.modelValue, (value) => {
  const next = editableHtml(value || "");
  if (editor.value && editor.value.innerHTML !== next) editor.value.innerHTML = next;
  if (next !== value) emit("update:modelValue", next);
}, { immediate: true });
onMounted(() => nextTick(() => {
  if (editor.value) editor.value.innerHTML = editableHtml(props.modelValue || "");
}));
</script>

<style scoped>
.rich-editor{display:grid;gap:7px}.toolbar{display:flex;flex-wrap:wrap;gap:6px;padding:8px;border:1px solid #cbd5e1;border-radius:8px 8px 0 0;background:#f8fafc}.toolbar button{min-height:30px;border:1px solid #cbd5e1;border-radius:5px;padding:4px 8px;background:#fff;color:#334155;font:inherit;font-size:.78rem;cursor:pointer}.toolbar button:hover:not(:disabled){border-color:#60a5fa;color:#1d4ed8}.toolbar button:disabled{cursor:not-allowed;opacity:.45}.divider{width:1px;margin:2px 2px;background:#cbd5e1}.editor-surface{min-height:260px;overflow:auto;border:1px solid #cbd5e1;border-radius:0 0 8px 8px;padding:12px;background:#fff;color:#1e293b;font-size:.88rem;font-weight:400;line-height:1.65;outline:none}.editor-surface:focus{border-color:#60a5fa;box-shadow:0 0 0 3px rgb(96 165 250 / 15%)}.editor-surface:empty::before{color:#94a3b8;content:attr(data-placeholder);pointer-events:none}.editor-surface :deep(h2){margin:1em 0 .55em;font-size:1.35em}.editor-surface :deep(p){margin:.55em 0}.editor-surface :deep(table){width:100%;margin:1em 0;border-collapse:collapse}.editor-surface :deep(th),.editor-surface :deep(td){min-width:86px;border:1px solid #94a3b8;padding:7px 9px;vertical-align:top}.editor-surface :deep(th){background:#eaf2ff;font-weight:700}.hint{margin:0;color:#64748b;font-size:.75rem;font-weight:400;line-height:1.45}@media(max-width:650px){.toolbar{gap:4px}.toolbar button{padding:4px 6px}.editor-surface :deep(table){display:block;overflow-x:auto;white-space:nowrap}}
</style>
