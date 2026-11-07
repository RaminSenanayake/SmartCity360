"use strict";

const number = (value) =>
  new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 }).format(value);
const palette = [
  "#588f77",
  "#93b699",
  "#bfd1ac",
  "#dabd86",
  "#96afbd",
  "#b19ea8",
  "#7fa49a",
  "#b4b99c",
];

document.querySelectorAll("[data-today]").forEach((element) => {
  element.textContent = new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date());
});
document
  .querySelectorAll("[data-auto-submit]")
  .forEach((select) =>
    select.addEventListener("change", () => select.form.requestSubmit()),
  );
document.querySelectorAll('input[name="portal"]').forEach((input) => {
  input.addEventListener("change", () => {
    if (!input.checked) return;
    document.querySelector("#username").placeholder =
      input.value === "staff" ? "e.g. local_staff.1" : "e.g. resident.1";
  });
});

document
  .querySelector("[data-password-toggle]")
  ?.addEventListener("click", (event) => {
    const input = document.querySelector("#password");
    const show = input.type === "password";
    input.type = show ? "text" : "password";
    event.currentTarget.textContent = show ? "Hide" : "Show";
    event.currentTarget.setAttribute(
      "aria-label",
      show ? "Hide password" : "Show password",
    );
  });
async function getJson(url, signal) {
  const response = await fetch(url, {
    headers: { Accept: "application/json" },
    signal,
  });
  if (response.redirected || response.status === 401)
    throw new Error("Your session has ended. Sign in again to continue.");
  if (response.status === 403)
    throw new Error("This reading is available to authenticated staff only.");
  if (
    !response.ok ||
    !response.headers.get("content-type")?.includes("application/json")
  )
    throw new Error("We couldn't load these readings. Please try again.");
  return response.json();
}

const chart = document.querySelector("[data-energy-chart]");
if (chart) {
  let data = [],
    style = "bar",
    request;
  const canvas = chart.querySelector(".chart-canvas");
  const status = chart.querySelector(".chart-status");
  const level = chart.querySelector("[data-chart-level]");
  const refresh = chart.querySelector("[data-chart-retry]");
  const table = chart.querySelector("[data-chart-table]");
  if (chart.dataset.city) level.value = "DISTRICT";
  try {
    const savedLevel = sessionStorage.getItem("smartcity.chart.level");
    if ([...level.options].some((option) => option.value === savedLevel))
      level.value = savedLevel;
    const savedStyle = sessionStorage.getItem("smartcity.chart.style");
    if (["bar", "share"].includes(savedStyle)) style = savedStyle;
  } catch {
    /* Storage is optional. */
  }
  chart.querySelectorAll("[data-chart-style]").forEach((tab) => {
    tab.classList.toggle("selected", tab.dataset.chartStyle === style);
    tab.setAttribute("aria-pressed", String(tab.dataset.chartStyle === style));
  });
  let selectedKey = null;
  let hoveredKey = null;
  const inspector = chart.querySelector(".chart-inspector");
  const tooltip = document.createElement("div");
  tooltip.className = "chart-tooltip";
  tooltip.id = "energy-reading-tooltip";
  tooltip.setAttribute("role", "tooltip");
  tooltip.hidden = true;
  const readingKey = (point) => String(point.id);
  const readingValue = (point) => Number(point.usage ?? point.value);
  const totalUsage = () =>
    data.reduce((sum, point) => sum + Number(point.usage), 0);
  const readingShare = (point) =>
    totalUsage() ? (readingValue(point) / totalUsage()) * 100 : 0;
  function describe(point) {
    return `${number(readingValue(point))} usage units; ${number(readingShare(point))}% of total`;
  }
  function updateHighlights() {
    const active = hoveredKey ?? selectedKey;
    canvas.querySelectorAll("[data-reading-key]").forEach((element) => {
      const key = element.dataset.readingKey;
      element.classList.toggle("is-highlighted", key === active);
      element.classList.toggle("is-selected", key === selectedKey);
      element.classList.toggle("is-dimmed", active !== null && key !== active);
      element.setAttribute("aria-pressed", String(key === selectedKey));
    });
    [...table.children].forEach((row, index) => {
      row.classList.toggle(
        "is-selected",
        row.dataset.readingKey === selectedKey ||
          (selectedKey === "other" && index >= 8),
      );
    });
  }
  function showTooltip(element, point, event) {
    tooltip.replaceChildren();
    const title = document.createElement("strong");
    title.textContent = point.name;
    const detail = document.createElement("span");
    detail.textContent = describe(point);
    tooltip.append(title, detail);
    tooltip.hidden = false;
    const bounds = canvas.getBoundingClientRect();
    const mark = element.getBoundingClientRect();
    const x = event?.clientX ?? mark.left + mark.width / 2;
    const y = event?.clientY ?? mark.top;
    tooltip.style.left =
      Math.max(
        10,
        Math.min(x - bounds.left + 12, bounds.width - tooltip.offsetWidth - 10),
      ) + "px";
    tooltip.style.top =
      Math.max(
        4,
        Math.min(
          y - bounds.top - tooltip.offsetHeight - 12,
          bounds.height - tooltip.offsetHeight - 4,
        ),
      ) + "px";
  }
  function selectReading(point) {
    selectedKey = selectedKey === readingKey(point) ? null : readingKey(point);
    inspector.hidden = selectedKey === null;
    if (selectedKey !== null) {
      inspector.querySelector("[data-reading-name]").textContent = point.name;
      inspector.querySelector("[data-reading-value]").textContent =
        describe(point);
    }
    updateHighlights();
  }
  function bindReading(element, point) {
    element.classList.add("chart-reading-target");
    element.dataset.readingKey = readingKey(point);
    element.setAttribute("role", "button");
    element.setAttribute("tabindex", "0");
    element.setAttribute(
      "aria-label",
      `${point.name}: ${describe(point)}. Select reading.`,
    );
    element.setAttribute(
      "aria-pressed",
      String(selectedKey === readingKey(point)),
    );
    element.setAttribute("aria-describedby", tooltip.id);
    element.querySelector("title")?.remove();
    element.addEventListener("pointerenter", (event) => {
      hoveredKey = readingKey(point);
      updateHighlights();
      showTooltip(element, point, event);
    });
    element.addEventListener("pointermove", (event) =>
      showTooltip(element, point, event),
    );
    element.addEventListener("pointerleave", () => {
      hoveredKey = null;
      tooltip.hidden = true;
      updateHighlights();
    });
    element.addEventListener("focus", () => {
      hoveredKey = readingKey(point);
      updateHighlights();
      showTooltip(element, point);
    });
    element.addEventListener("blur", () => {
      hoveredKey = null;
      tooltip.hidden = true;
      updateHighlights();
    });
    element.addEventListener("click", () => selectReading(point));
    element.addEventListener("keydown", (event) => {
      if (event.key === "Escape") {
        selectedKey = null;
        inspector.hidden = true;
        tooltip.hidden = true;
        updateHighlights();
      } else if (event.key === "Enter" || event.key === " ") {
        if (element.tagName.toLowerCase() !== "button") {
          event.preventDefault();
          selectReading(point);
        }
      } else if (
        ["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown"].includes(event.key)
      ) {
        event.preventDefault();
        const container = element.closest("svg,.share-legend");
        const targets = [
          ...container.querySelectorAll(".chart-reading-target"),
        ];
        const step = ["ArrowLeft", "ArrowUp"].includes(event.key) ? -1 : 1;
        targets[
          (targets.indexOf(element) + step + targets.length) % targets.length
        ].focus();
      }
    });
  }
  chart.querySelector("[data-clear-reading]").addEventListener("click", () => {
    const key = selectedKey;
    selectedKey = null;
    inspector.hidden = true;
    updateHighlights();
    canvas
      .querySelector(`[data-reading-key="${CSS.escape(key || "")}"]`)
      ?.focus();
  });
  function render() {
    tooltip.hidden = true;
    hoveredKey = null;
    canvas.replaceChildren();
    table.replaceChildren();
    data.forEach((point) => {
      const row = document.createElement("tr");
      row.dataset.readingKey = readingKey(point);
      [point.name, number(point.usage)].forEach((value) => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.append(cell);
      });
      table.append(row);
    });
    if (!data.length) {
      chart.querySelector(".chart-limit").textContent = "";
      inspector.hidden = true;
      status.hidden = false;
      status.textContent = "No readings available in this view.";
      canvas.hidden = true;
      return;
    }
    status.hidden = true;
    canvas.hidden = false;
    const visible = data.slice(0, 8);
    chart.querySelector(".chart-limit").textContent =
      data.length > 8
        ? "Chart shows the 8 highest readings. Full list below."
        : "";
    if (style === "share") drawShare(visible);
    else drawBars(visible);
    canvas.append(tooltip);
    updateHighlights();
  }
  function svgElement(tag, attributes = {}, text) {
    const element = document.createElementNS("http://www.w3.org/2000/svg", tag);
    Object.entries(attributes).forEach(([key, value]) =>
      element.setAttribute(key, value),
    );
    if (text !== undefined) element.textContent = text;
    return element;
  }
  function drawBars(points) {
    if (window.innerWidth < 760) {
      drawMobileBars(points);
      return;
    }
    const svg = svgElement("svg", {
      viewBox: "0 0 760 300",
      role: "group",
      "aria-label": "Current energy usage by " + level.value.toLowerCase(),
    });
    svg.append(
      svgElement(
        "title",
        {},
        "Current usage comparison. Exact values are available in the table below.",
      ),
    );
    const max =
      Math.max(...points.map((point) => Number(point.usage)), 1) * 1.18;
    const left = 65,
      width = 675,
      top = 20,
      height = 205,
      step = width / points.length;
    for (let i = 0; i <= 4; i++) {
      const y = top + height - (height * i) / 4;
      svg.append(
        svgElement("line", {
          x1: left,
          x2: left + width,
          y1: y,
          y2: y,
          stroke: "#edf1e8",
          "stroke-dasharray": i === 0 ? "none" : "3 4",
        }),
      );
      svg.append(
        svgElement(
          "text",
          { x: left - 12, y: y + 4, "text-anchor": "end" },
          number((max * i) / 4),
        ),
      );
    }
    points.forEach((point, i) => {
      const barWidth = Math.min(50, step * 0.48),
        barHeight = (Number(point.usage) / max) * height;
      const x = left + step * i + (step - barWidth) / 2;
      const bar = svgElement("rect", {
        x,
        y: top + height - barHeight,
        width: barWidth,
        height: Math.max(barHeight, 1),
        fill: i === 0 ? "#4b856c" : "#a5c4a3",
        rx: 5,
        tabindex: 0,
        role: "img",
        "aria-label": point.name + ": " + number(point.usage),
      });
      bar.append(
        svgElement("title", {}, point.name + ": " + number(point.usage)),
      );
      bar.classList.add("chart-mark");
      bindReading(bar, point);
      svg.append(bar);
      svg.append(
        svgElement(
          "text",
          {
            x: x + barWidth / 2,
            y: top + height - barHeight - 9,
            "text-anchor": "middle",
            class: "chart-value",
          },
          number(point.usage),
        ),
      );
      const label =
        point.name.length > 15 ? point.name.slice(0, 14) + "..." : point.name;
      svg.append(
        svgElement(
          "text",
          {
            x: left + step * (i + 0.5),
            y: top + height + 26,
            "text-anchor": "middle",
          },
          label,
        ),
      );
    });
    canvas.append(svg);
  }
  function drawMobileBars(points) {
    const svg = svgElement("svg", {
      viewBox: `0 0 340 ${points.length * 48 + 14}`,
      role: "group",
      "aria-label": "Current usage by zone",
    });
    const max = Math.max(...points.map((point) => Number(point.usage)), 1);
    points.forEach((point, i) => {
      const group = svgElement("g", { class: "chart-mark chart-mobile-point" });
      const y = 10 + i * 48;
      group.append(
        svgElement("rect", {
          x: 0,
          y: y - 5,
          width: 340,
          height: 40,
          fill: "transparent",
        }),
      );
      const label =
        point.name.length > 26 ? point.name.slice(0, 25) + "..." : point.name;
      group.append(svgElement("text", { x: 4, y: y + 10 }, label));
      group.append(
        svgElement(
          "text",
          { x: 336, y: y + 10, "text-anchor": "end", class: "chart-value" },
          number(point.usage),
        ),
      );
      group.append(
        svgElement("rect", {
          x: 4,
          y: y + 19,
          width: 332,
          height: 9,
          rx: 4,
          fill: "#f0f4ec",
        }),
      );
      const bar = svgElement("rect", {
        x: 4,
        y: y + 19,
        width: Math.max((Number(point.usage) / max) * 332, 1),
        height: 9,
        rx: 4,
        fill: i === 0 ? "#4b856c" : "#a5c4a3",
        "data-value-bar": "",
      });
      bar.append(
        svgElement("title", {}, point.name + ": " + number(point.usage)),
      );
      group.append(bar);
      bindReading(group, point);
      svg.append(group);
    });
    canvas.append(svg);
  }
  function drawShare(points) {
    const total = totalUsage();
    const segments = points.map((point, i) => ({
      ...point,
      color: palette[i],
    }));
    if (data.length > 8)
      segments.push({
        id: "other",
        name: "Other zones",
        color: "#d2dcc8",
        usage: data
          .slice(8)
          .reduce((sum, point) => sum + Number(point.usage), 0),
      });
    const layout = document.createElement("div");
    layout.className = "share-layout";
    const donut = document.createElement("div");
    donut.className = "share-donut";
    const svg = svgElement("svg", {
      viewBox: "0 0 220 220",
      class: "share-svg",
      role: "group",
      "aria-label": "Share of total usage by zone",
    });
    const circumference = 2 * Math.PI * 87;
    svg.append(
      svgElement("circle", {
        cx: 110,
        cy: 110,
        r: 87,
        fill: "none",
        stroke: "#edf1e8",
        "stroke-width": 30,
      }),
    );
    let offset = 0;
    segments.forEach((segment) => {
      const length = total
        ? (readingValue(segment) / total) * circumference
        : 0;
      if (length > 0) {
        const slice = svgElement("circle", {
          cx: 110,
          cy: 110,
          r: 87,
          class: "chart-mark share-slice",
          stroke: segment.color,
          "stroke-width": 30,
          "stroke-dasharray": `${length} ${circumference - length}`,
          "stroke-dashoffset": -offset,
          transform: "rotate(-90 110 110)",
        });
        bindReading(slice, segment);
        svg.append(slice);
      }
      offset += length;
    });
    donut.append(svg);
    const center = document.createElement("div");
    center.className = "share-center";
    const centerText = document.createElement("div"),
      totalLabel = document.createElement("strong"),
      caption = document.createElement("span");
    totalLabel.textContent = number(total);
    caption.textContent = "Total usage";
    centerText.append(totalLabel, caption);
    center.append(centerText);
    donut.append(center);
    const legend = document.createElement("div");
    legend.className = "share-legend";
    segments.forEach((segment) => {
      const row = document.createElement("button");
      row.type = "button";
      row.className = "chart-legend-button";
      const swatch = document.createElement("span"),
        name = document.createElement("span"),
        percent = document.createElement("strong");
      swatch.className = "legend-swatch";
      swatch.style.background = segment.color;
      name.textContent = segment.name;
      percent.textContent = number(readingShare(segment)) + "%";
      row.append(swatch, name, percent);
      bindReading(row, segment);
      legend.append(row);
    });
    layout.append(donut, legend);
    canvas.append(layout);
  }
  async function load() {
    request?.abort();
    request = new AbortController();
    const current = request;
    refresh.disabled = true;
    refresh.setAttribute("aria-busy", "true");
    selectedKey = null;
    hoveredKey = null;
    inspector.hidden = true;
    tooltip.hidden = true;
    canvas.hidden = true;
    status.hidden = false;
    status.textContent = "Loading energy readings...";
    const query = new URLSearchParams({ level: level.value });
    if (chart.dataset.city) query.set("cityId", chart.dataset.city);
    try {
      data = await getJson("/api/usage?" + query, current.signal);
      render();
    } catch (error) {
      if (error.name !== "AbortError") {
        data = [];
        table.replaceChildren();
        status.textContent = error.message;
      }
    } finally {
      if (current === request) {
        refresh.disabled = false;
        refresh.removeAttribute("aria-busy");
      }
    }
  }
  level.addEventListener("change", () => {
    try {
      sessionStorage.setItem("smartcity.chart.level", level.value);
    } catch {
      /* Optional. */
    }
    load();
  });
  refresh.addEventListener("click", load);
  chart.querySelectorAll("[data-chart-style]").forEach((button) =>
    button.addEventListener("click", () => {
      style = button.dataset.chartStyle;
      if (style === "bar" && selectedKey === "other") {
        selectedKey = null;
        inspector.hidden = true;
      }
      try {
        sessionStorage.setItem("smartcity.chart.style", style);
      } catch {
        /* Optional. */
      }
      chart.querySelectorAll("[data-chart-style]").forEach((tab) => {
        tab.classList.toggle("selected", tab === button);
        tab.setAttribute("aria-pressed", String(tab === button));
      });
      if (!refresh.disabled) render();
    }),
  );
  const viewport = window.matchMedia("(max-width:759px)");
  viewport.addEventListener("change", () => {
    if (!refresh.disabled && data.length) render();
  });
  load();
}

const meter = document.querySelector("[data-meter-panel]");
if (meter) {
  const button = meter.querySelector("[data-read-meter]");
  button.addEventListener("click", async () => {
    const id = meter.querySelector("select").value,
      message = meter.querySelector(".meter-message"),
      result = meter.querySelector(".meter-result");
    result.hidden = true;
    if (!id) {
      message.textContent = "Select a building first.";
      meter.querySelector("select").focus();
      return;
    }
    button.disabled = true;
    message.textContent = "Reading building meter...";
    try {
      const reading = await getJson("/staff/meters/" + encodeURIComponent(id));
      meter.querySelector("[data-meter-usage]").textContent = number(
        reading.powerUsage,
      );
      meter.querySelector("[data-meter-power]").textContent = reading.powerOn
        ? "On"
        : "Off";
      meter.querySelector("[data-meter-time]").textContent = new Date(
        reading.readAt,
      ).toLocaleString();
      message.textContent = "Latest available building reading.";
      result.hidden = false;
    } catch (error) {
      message.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });
}
