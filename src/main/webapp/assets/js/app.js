(function () {
    const loginTab = document.getElementById("loginTab");
    const signupTab = document.getElementById("signupTab");
    const loginForm = document.getElementById("loginForm");
    const signupForm = document.getElementById("signupForm");
    const authStatus = document.getElementById("authStatus");

    function getApiBase() {
        const parts = window.location.pathname.split("/").filter(Boolean);
        const context = parts.length > 0 ? parts[0] : "";
        return context ? (window.location.origin + "/" + context) : window.location.origin;
    }

    function setStatus(message, isError) {
        authStatus.textContent = message;
        authStatus.className = "status " + (isError ? "error" : "ok");
    }

    function switchTab(showLogin) {
        loginTab.classList.toggle("active", showLogin);
        signupTab.classList.toggle("active", !showLogin);
        loginForm.style.display = showLogin ? "grid" : "none";
        signupForm.style.display = showLogin ? "none" : "grid";
        authStatus.className = "status";
        authStatus.textContent = "";
    }

    loginTab.addEventListener("click", function () {
        switchTab(true);
    });

    signupTab.addEventListener("click", function () {
        switchTab(false);
    });

    signupForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const name = document.getElementById("signupName").value.trim();
        const phone = document.getElementById("signupPhone").value.trim();
        const licenseNumber = document.getElementById("signupLicense").value.trim();
        const email = document.getElementById("signupEmail").value.trim().toLowerCase();
        const password = document.getElementById("signupPassword").value;

        if (!name || !phone || !licenseNumber || !email || !password) {
            setStatus("All signup fields are required.", true);
            return;
        }

        try {
            const response = await fetch(getApiBase() + "/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: name,
                    phone: phone,
                    licenseNumber: licenseNumber,
                    email: email,
                    password: password
                })
            });
            const payload = await response.json();
            if (!response.ok) {
                setStatus(payload.error || "Signup failed.", true);
                return;
            }

            setStatus("Signup successful. Please login.", false);
            signupForm.reset();
            switchTab(true);
        } catch (_err) {
            setStatus("Unable to reach server. Check backend deployment.", true);
        }
    });

    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value.trim().toLowerCase();
        const password = document.getElementById("loginPassword").value;

        try {
            const response = await fetch(getApiBase() + "/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: email, password: password })
            });
            const payload = await response.json();
            if (!response.ok) {
                setStatus(payload.error || "Invalid credentials.", true);
                return;
            }

            localStorage.setItem("lmd_session", JSON.stringify({
                role: payload.role,
                email: payload.email,
                name: payload.name,
                customerId: payload.customerId
            }));
            window.location.href = payload.role === "admin" ? "admin.html" : "user.html";
        } catch (_err) {
            setStatus("Unable to reach server. Check backend deployment.", true);
        }
    });
})();
