import i18n from "./index";

const BCP47_BY_LANGUAGE = { en: "en-US", pt: "pt-BR", es: "es-ES" };

export function dateLocale() {
  return BCP47_BY_LANGUAGE[i18n.language] ?? "en-US";
}
