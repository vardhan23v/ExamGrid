function formatDate(isoDate) {
    const parts = isoDate.split("-");
    return parts[2] + "-" + parts[1] + "-" + parts[0];
}

function formatTime(time) {
    return time.substring(0, 5);
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function seatCard(seat) {
    return `
        <div class="col-md-6">
            <div class="card seat-result-card p-3 h-100">
                <div class="d-flex justify-content-between">
                    <div>
                        <div class="fw-bold">${escapeHtml(seat.subjectCode)}</div>
                        <div class="small text-muted">${escapeHtml(seat.subjectName)}</div>
                    </div>
                    <span class="badge bg-secondary align-self-start">${formatDate(seat.examDate)}</span>
                </div>
                <div class="seat-number mt-2">${escapeHtml(seat.seatNumber)}</div>
                <div class="small">
                    Hall <strong>${escapeHtml(seat.hallName)}</strong>, ${escapeHtml(seat.block)}<br>
                    Row ${seat.row}, Bench ${seat.column} &middot; ${formatTime(seat.startTime)} to ${formatTime(seat.endTime)}
                </div>
            </div>
        </div>`;
}

async function loadSeats(usn, messageBox, resultBox) {
    messageBox.innerHTML = '<div class="text-muted">Searching...</div>';
    resultBox.innerHTML = "";
    try {
        const response = await fetch("/api/seats/" + encodeURIComponent(usn));
        const data = await response.json();
        if (!response.ok) {
            messageBox.innerHTML = '<div class="alert alert-warning">' + escapeHtml(data.message) + '</div>';
            return;
        }
        if (data.length === 0) {
            messageBox.innerHTML = '<div class="alert alert-info">Seats have not been allotted for ' + escapeHtml(usn) + ' yet. Please check again later.</div>';
            return;
        }
        messageBox.innerHTML = '<div class="alert alert-success py-2">' + escapeHtml(data[0].studentName)
            + ' (' + escapeHtml(data[0].usn) + ') has ' + data.length + ' allotted exam seat(s).</div>';
        resultBox.innerHTML = data.map(seatCard).join("");
    } catch (error) {
        messageBox.innerHTML = '<div class="alert alert-danger">Could not reach the server. Please try again.</div>';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("lookupForm");
    const input = document.getElementById("usnInput");
    const messageBox = document.getElementById("lookupMessage");
    const resultBox = document.getElementById("seatResults");

    if (form) {
        form.addEventListener("submit", event => {
            event.preventDefault();
            loadSeats(input.value.trim().toUpperCase(), messageBox, resultBox);
        });
    }

    const ownSeat = document.getElementById("ownSeat");
    if (ownSeat) {
        loadSeats(ownSeat.dataset.usn, messageBox, resultBox);
    }
});
