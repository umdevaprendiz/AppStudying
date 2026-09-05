(function () {
  try {
    var theme = localStorage.getItem("theme");
    if (theme === "dark" || theme === "light") {
      document.documentElement.setAttribute("data-theme", theme);
    }
  } catch {
    // localStorage indisponível (aba anônima, etc.) — só mantém o tema padrão.
  }
})();
