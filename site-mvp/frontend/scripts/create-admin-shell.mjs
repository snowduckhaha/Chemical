import { readFile, writeFile } from "node:fs/promises";
import { resolve } from "node:path";

const dist = resolve("dist");
const source = resolve(dist, "index.html");
const target = resolve(dist, "admin-shell.html");
const html = await readFile(source, "utf8");
const adminShell = html.replace(
  "<head>",
  '<head><meta name="robots" content="noindex,nofollow">'
);

if (adminShell === html) {
  throw new Error("Unable to add noindex metadata to admin shell");
}

await writeFile(target, adminShell, "utf8");
