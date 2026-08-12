import { computed, toValue, type MaybeRefOrGetter } from "vue";
import { useHead } from "@unhead/vue";
import type { JsonLdNode } from "../lib/jsonLd";

/**
 * Emits reactive JSON-LD script blocks through Unhead so they are present in
 * both SSG output and client hydration. Escape "<" to prevent "</script>"
 * injection from entity-controlled text.
 */
export function useJsonLd(schemas: MaybeRefOrGetter<JsonLdNode[]>) {
  useHead(computed(() => ({
    script: toValue(schemas).map((schema, index) => ({
      key: `jsonld-${index}`,
      type: "application/ld+json",
      innerHTML: JSON.stringify(schema).replace(/</g, "\\u003c")
    }))
  })));
}
