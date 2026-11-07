/* Progressive enhancements: the same protected server forms also work without JavaScript. */
(() => {
  "use strict";
  const reducedMotion = matchMedia("(prefers-reduced-motion: reduce)");
  const draftBaselines = new WeakMap();
  let mutationInProgress = false;

  const toastRegion = document.createElement("div");
  toastRegion.className = "toast-region";
  document.body.append(toastRegion);
  let toastTimeout;
  function notify(message, error = false, announce = true) {
    clearTimeout(toastTimeout);
    const toast = document.createElement("div");
    toast.className = "toast" + (error ? " toast-error" : "");
    const text = document.createElement("span");
    if (announce) text.setAttribute("role", error ? "alert" : "status");
    text.textContent = message;
    const close = document.createElement("button");
    close.type = "button";
    close.textContent = "\u00d7";
    close.setAttribute("aria-label", "Dismiss notification");
    close.addEventListener("click", () => toast.remove());
    toast.append(text, close);
    toastRegion.replaceChildren(toast);
    if (!error) toastTimeout = setTimeout(() => toast.remove(), 7000);
  }

  const confirmDialog = document.createElement("dialog");
  confirmDialog.className = "confirm-dialog";
  confirmDialog.setAttribute("aria-labelledby", "confirm-title");
  confirmDialog.setAttribute("aria-describedby", "confirm-description");
  confirmDialog.innerHTML =
    '<form method="dialog"><div class="section-kicker">PLEASE CONFIRM</div><h2 id="confirm-title">Continue with this action?</h2><p id="confirm-description"></p><div class="form-actions"><button class="button" value="cancel" autofocus>Cancel</button><button class="button primary" value="confirm">Continue</button></div></form>';
  document.body.append(confirmDialog);
  function confirmAction(message) {
    return new Promise((resolve) => {
      confirmDialog.querySelector("#confirm-description").textContent = message;
      confirmDialog.returnValue = "cancel";
      confirmDialog.addEventListener(
        "close",
        () => resolve(confirmDialog.returnValue === "confirm"),
        { once: true },
      );
      confirmDialog.showModal();
    });
  }

  function signature(form) {
    return JSON.stringify(
      [...new FormData(form)].filter(([name]) => name !== "_csrf"),
    );
  }
  function rememberDraft(form) {
    draftBaselines.set(form, signature(form));
  }
  function isDirty(form) {
    return (
      draftBaselines.has(form) && signature(form) !== draftBaselines.get(form)
    );
  }
  document.querySelectorAll("[data-draft-guard]").forEach(rememberDraft);
  window.addEventListener("beforeunload", (event) => {
    if (
      mutationInProgress ||
      [...document.querySelectorAll("[data-draft-guard]")].some(isDirty)
    ) {
      event.preventDefault();
      event.returnValue = "";
    }
  });

  function updateCounts() {
    document.querySelectorAll("[data-character-count]").forEach((counter) => {
      const field = document.getElementById(counter.dataset.characterCount);
      if (field)
        counter.textContent = `${field.value.length} / ${field.maxLength}`;
    });
  }
  function filterList(name) {
    const container = document.querySelector(
      `[data-filter-container="${name}"]`,
    );
    if (!container) return;
    const query =
      document
        .querySelector(`[data-list-search="${name}"]`)
        ?.value.trim()
        .toLocaleLowerCase() || "";
    const status =
      document.querySelector(`[data-list-status="${name}"]`)?.value || "all";
    const items = [...container.querySelectorAll("[data-filter-item]")];
    let visible = 0;
    for (const item of items) {
      // Avoid matching unselected severity options inside staff forms.
      const text = [
        ...item.querySelectorAll(
          "h2,h3,.table-id,.rule-block,.muted,.severity",
        ),
      ]
        .map((element) => element.textContent)
        .join(" ")
        .toLocaleLowerCase();
      item.hidden = !(
        text.includes(query) &&
        (status === "all" || item.dataset.status === status)
      );
      if (!item.hidden) visible++;
    }
    const count = document.querySelector(`[data-filter-count="${name}"]`);
    if (count) count.textContent = `${visible} of ${items.length} shown`;
    const empty = document.querySelector(`[data-filter-empty="${name}"]`);
    if (empty) empty.hidden = visible > 0 || items.length === 0;
    const clear = document.querySelector(`[data-clear-filters="${name}"]`);
    if (clear) clear.disabled = !query && status === "all";
  }
  function refreshFilters() {
    ["complaints", "policies", "zones"].forEach(filterList);
  }
  document.querySelectorAll("[data-filter-toolbar]").forEach((toolbar) => {
    toolbar.hidden = false;
  });
  document.addEventListener("input", (event) => {
    if (event.target.matches("[data-list-search]"))
      filterList(event.target.dataset.listSearch);
    updateCounts();
  });
  document.addEventListener("change", (event) => {
    if (event.target.matches("[data-list-status]"))
      filterList(event.target.dataset.listStatus);
  });
  refreshFilters();
  updateCounts();

  function clearFormFeedback(form) {
    const feedback = form.querySelector(".form-feedback");
    if (feedback) {
      feedback.textContent = "";
      feedback.setAttribute("role", "status");
    }
  }
  function clearPolicyForm(form) {
    if (!form) return;
    clearFormFeedback(form);
    for (const name of ["id", "name", "rule"])
      form.elements.namedItem(name).value = "";
    document.querySelector("[data-policy-heading]").textContent =
      "Create a policy";
    rememberDraft(form);
  }
  document.addEventListener("click", async (event) => {
    const clear = event.target.closest("[data-clear-filters]");
    if (clear) {
      const name = clear.dataset.clearFilters;
      const search = document.querySelector(`[data-list-search="${name}"]`);
      if (search) {
        search.value = "";
        search.focus();
      }
      const status = document.querySelector(`[data-list-status="${name}"]`);
      if (status) status.value = "all";
      filterList(name);
    }
    const edit = event.target.closest("[data-edit-policy]");
    const reset = event.target.closest("[data-policy-reset]");
    if (!edit && !reset) return;
    const form = document.querySelector("[data-policy-form]");
    if (!form || mutationInProgress) return;
    if (
      isDirty(form) &&
      !(await confirmAction("Discard your unsaved policy changes?"))
    )
      return;
    clearFormFeedback(form);
    toastRegion.replaceChildren();
    if (edit) {
      form.elements.namedItem("id").value = edit.dataset.id;
      form.elements.namedItem("name").value = edit.dataset.name;
      form.elements.namedItem("rule").value = edit.dataset.rule;
      document.querySelector("[data-policy-heading]").textContent =
        "Edit policy";
      rememberDraft(form);
    } else clearPolicyForm(form);
    form.scrollIntoView({
      behavior: reducedMotion.matches ? "instant" : "smooth",
      block: "center",
    });
    form.elements.namedItem("name").focus({ preventScroll: true });
  });

  document.addEventListener("submit", async (event) => {
    const form = event.target;
    if (!form.matches("[data-async-form]")) return;
    event.preventDefault();
    if (mutationInProgress) {
      notify("Please wait for the current update to finish.");
      return;
    }
    if (form.dataset.confirm && !(await confirmAction(form.dataset.confirm)))
      return;
    if (mutationInProgress) return;
    const payload = new URLSearchParams(new FormData(form));
    const action = form.getAttribute("action");
    const regionNames = form.dataset.updateRegions
      .split(",")
      .map((value) => value.trim());
    const anchor = form.closest("[data-item-id]");
    const anchorId = anchor?.dataset.itemId;
    const anchorTop = anchor?.getBoundingClientRect().top;
    const previousFocus = document.activeElement;
    const focusedId = previousFocus?.id;
    let focusAfterUpdate = previousFocus;
    const controls = [
      ...form.querySelectorAll("button,input,select,textarea"),
    ].map((element) => ({ element, disabled: element.disabled }));
    mutationInProgress = true;
    form.setAttribute("aria-busy", "true");
    controls.forEach(({ element }) => {
      element.disabled = true;
    });
    form
      .querySelector('button:not([type="button"])')
      ?.classList.add("is-loading");
    let feedback = form.querySelector(".form-feedback");
    if (!feedback) {
      feedback = document.createElement("p");
      feedback.className = "form-feedback";
      form.append(feedback);
    }
    feedback.setAttribute("role", "status");
    feedback.textContent = "Saving changes...";
    try {
      const response = await fetch(form.action, {
        method: "POST",
        body: payload,
        headers: { Accept: "text/html" },
        credentials: "same-origin",
      }).catch(() => {
        throw new Error(
          "Connection interrupted. Your input has been kept. Check your connection and try again.",
        );
      });
      if (new URL(response.url).pathname.endsWith("/sign-in"))
        throw new Error(
          "Your session has ended. Sign in again to continue; your input is still here.",
        );
      if (response.status === 403)
        throw new Error(
          "This request could not be authorized. Refresh the page and sign in again if needed.",
        );
      if (
        !response.ok ||
        !response.headers.get("content-type")?.includes("text/html")
      )
        throw new Error(
          "We could not save this change. Your input is still here; please try again.",
        );
      const documentUpdate = new DOMParser().parseFromString(
        await response.text(),
        "text/html",
      );
      const validationError = documentUpdate.querySelector(".messages .error");
      if (validationError) throw new Error(validationError.textContent.trim());
      // Validate all regions before replacing any of them, including a redirected error page.
      const updates = regionNames.map((name) => {
        const selector = `[data-refresh-region="${CSS.escape(name)}"]`;
        const current = document.querySelector(selector);
        const replacement = documentUpdate.querySelector(selector);
        if (!current || !replacement)
          throw new Error(
            "The page could not be updated. Refresh to see the latest changes.",
          );
        return { current, replacement };
      });
      updates.forEach(({ current, replacement }) =>
        current.replaceWith(document.importNode(replacement, true)),
      );
      document
        .querySelectorAll(".messages .alert")
        .forEach((alert) => alert.remove());
      if (form.hasAttribute("data-reset-on-success")) {
        if (form.matches("[data-policy-form]")) clearPolicyForm(form);
        else {
          form.reset();
          if (form.elements.namedItem("text"))
            form.elements.namedItem("text").value = "";
        }
      }
      const deleted = action.match(/\/policies\/(\d+)\/delete$/);
      const policyForm = document.querySelector("[data-policy-form]");
      if (deleted && policyForm?.elements.namedItem("id").value === deleted[1])
        clearPolicyForm(policyForm);
      refreshFilters();
      updateCounts();
      const newAnchor = anchorId
        ? document.querySelector(`[data-item-id="${CSS.escape(anchorId)}"]`)
        : null;
      if (newAnchor && !newAnchor.hidden) {
        window.scrollBy({
          top: newAnchor.getBoundingClientRect().top - anchorTop,
          behavior: "instant",
        });
        newAnchor.classList.add("just-updated");
      }
      const replacementFocus = focusedId && document.getElementById(focusedId);
      if (replacementFocus && !replacementFocus.closest("[hidden]")) {
        focusAfterUpdate = replacementFocus;
      } else if (newAnchor && !newAnchor.hidden) {
        focusAfterUpdate = newAnchor.querySelector(
          'button:not([disabled]):not([type="button"]),select:not([disabled])',
        );
      } else if (!form.isConnected) {
        focusAfterUpdate = document.querySelector("[data-list-search]");
      }
      const preview = document.querySelector(
        '[data-refresh-region="policy-preview"] .alert',
      );
      const message =
        documentUpdate
          .querySelector(".messages .success")
          ?.textContent.trim() ||
        (regionNames.includes("policy-preview") && preview
          ? preview.textContent.trim()
          : "Changes saved.");
      feedback.textContent = "";
      notify(message);
    } catch (error) {
      feedback.setAttribute("role", "alert");
      feedback.textContent = error.message;
      notify(error.message, true, false);
    } finally {
      controls.forEach(({ element, disabled }) => {
        element.disabled = disabled;
      });
      form.removeAttribute("aria-busy");
      form.querySelector(".is-loading")?.classList.remove("is-loading");
      if (form.hasAttribute("data-draft-guard") && !feedback.textContent)
        rememberDraft(form);
      mutationInProgress = false;
      if (
        focusAfterUpdate?.isConnected &&
        !focusAfterUpdate.closest("[hidden]") &&
        (document.activeElement === document.body ||
          document.activeElement === previousFocus)
      ) {
        focusAfterUpdate.focus({ preventScroll: true });
      }
    }
  });

  const sidebar = document.getElementById("sidebar");
  const menu = document.querySelector(".menu-toggle");
  if (sidebar && menu) {
    const mobile = matchMedia("(max-width:760px)");
    const backdrop = document.createElement("button");
    backdrop.type = "button";
    backdrop.className = "nav-backdrop";
    backdrop.setAttribute("aria-label", "Close navigation");
    backdrop.tabIndex = -1;
    backdrop.hidden = true;
    document.body.append(backdrop);
    const close = document.createElement("button");
    close.type = "button";
    close.className = "nav-close";
    close.textContent = "\u00d7";
    close.setAttribute("aria-label", "Close navigation");
    sidebar.prepend(close);
    function setNavigation(open, restoreFocus = true) {
      open = mobile.matches && open;
      document.body.classList.toggle("nav-open", open);
      menu.setAttribute("aria-expanded", String(open));
      backdrop.hidden = !open;
      sidebar.inert = mobile.matches && !open;
      document.querySelector(".app-shell").inert = open;
      if (open) close.focus();
      else if (restoreFocus && mobile.matches) menu.focus();
    }
    menu.addEventListener("click", () =>
      setNavigation(!document.body.classList.contains("nav-open")),
    );
    close.addEventListener("click", () => setNavigation(false));
    backdrop.addEventListener("click", () => setNavigation(false));
    sidebar.addEventListener("click", (event) => {
      if (event.target.closest("a") && mobile.matches)
        setNavigation(false, false);
    });
    document.addEventListener("keydown", (event) => {
      if (!mobile.matches || !document.body.classList.contains("nav-open"))
        return;
      if (event.key === "Escape") {
        setNavigation(false);
        return;
      }
      if (event.key !== "Tab") return;
      const focusable = [
        ...sidebar.querySelectorAll(
          'a[href],button:not([disabled]),input:not([type="hidden"])',
        ),
      ].filter((element) => element.getClientRects().length);
      const first = focusable[0],
        last = focusable.at(-1);
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    });
    mobile.addEventListener("change", () => setNavigation(false, false));
    setNavigation(false, false);
  }
})();
