(function () {
    const sessionRaw = localStorage.getItem("lmd_session");
    if (!sessionRaw) {
        window.location.href = "index.html";
        return;
    }

    const session = JSON.parse(sessionRaw);
    if (session.role !== "admin") {
        window.location.href = session.role === "user" ? "user.html" : "index.html";
        return;
    }

    function getApiBase() {
        const parts = window.location.pathname.split("/").filter(Boolean);
        const context = parts.length > 0 ? parts[0] : "";
        return context ? (window.location.origin + "/" + context) : window.location.origin;
    }

    function setStatus(message, isError) {
        const el = document.getElementById("adminStatus");
        el.textContent = message;
        el.className = "status " + (isError ? "error" : "ok");
    }

    function addOperation(customerId, action, result) {
        const tbody = document.getElementById("opsBody");
        const tr = document.createElement("tr");
        tr.innerHTML =
            "<td>" + new Date().toLocaleString() + "</td>" +
            "<td>" + customerId + "</td>" +
            "<td>" + action + "</td>" +
            "<td>" + result + "</td>";
        tbody.prepend(tr);
    }

    document.getElementById("blockForm").addEventListener("submit", async function (event) {
        event.preventDefault();
        const customerId = Number(document.getElementById("customerId").value);
        const blocked = document.getElementById("blockAction").value === "true";

        if (!customerId) {
            setStatus("Customer ID is required.", true);
            return;
        }

        try {
            const res = await fetch(getApiBase() + "/admin/customers/block", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ customerId: customerId, blocked: blocked })
            });
            const data = await res.json();
            if (!res.ok) {
                setStatus(data.error || "Operation failed.", true);
                addOperation(customerId, blocked ? "BLOCK" : "UNBLOCK", "FAILED");
                return;
            }

            const actionText = blocked ? "blocked" : "unblocked";
            setStatus("Customer " + data.customerId + " " + actionText + " successfully.", false);
            addOperation(customerId, blocked ? "BLOCK" : "UNBLOCK", "SUCCESS");
        } catch (_err) {
            setStatus("Unable to reach server.", true);
            addOperation(customerId, blocked ? "BLOCK" : "UNBLOCK", "FAILED");
        }
    });

    document.getElementById("logoutBtn").addEventListener("click", function () {
        localStorage.removeItem("lmd_session");
        window.location.href = "index.html";
    });
})();
