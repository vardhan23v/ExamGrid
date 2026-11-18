document.addEventListener("DOMContentLoaded", () => {
    const filter = document.getElementById("tableFilter");
    const table = document.getElementById("dataTable");
    if (!filter || !table) {
        return;
    }
    filter.addEventListener("input", () => {
        const text = filter.value.toLowerCase();
        table.querySelectorAll("tbody tr").forEach(row => {
            row.style.display = row.textContent.toLowerCase().includes(text) ? "" : "none";
        });
    });
});
