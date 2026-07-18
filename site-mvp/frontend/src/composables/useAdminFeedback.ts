import { readonly, ref } from "vue";

type FeedbackType = "success" | "error" | "info";

const message = ref("");
const type = ref<FeedbackType>("info");
let timer: ReturnType<typeof setTimeout> | undefined;

const show = (nextMessage: string, nextType: FeedbackType = "info") => {
  message.value = nextMessage;
  type.value = nextType;
  if (timer) clearTimeout(timer);
  timer = setTimeout(() => {
    message.value = "";
  }, 5000);
};

export const useAdminFeedback = () => ({
  message: readonly(message),
  type: readonly(type),
  success: (nextMessage: string) => show(nextMessage, "success"),
  error: (nextMessage: string) => show(nextMessage, "error"),
  info: (nextMessage: string) => show(nextMessage, "info"),
  clear: () => {
    if (timer) clearTimeout(timer);
    message.value = "";
  }
});
