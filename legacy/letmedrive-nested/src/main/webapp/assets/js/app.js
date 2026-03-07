(function () {
    const ADMIN_EMAIL = "admin@letmedrive.com";
    const ADMIN_PASSWORD = "Admin@123";

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

    function readUsers() {
        const raw = localStorage.getItem("lmd_users");
        return raw ? JSON.parse(raw) : [];
    }

    function writeUsers(users) {
        localStorage.setItem("lmd_users", JSON.stringify(users));
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

        if (email === ADMIN_EMAIL) {
            setStatus("This email is reserved for admin.", true);
            return;
        }

        const users = readUsers();
        const exists = users.some(function (u) { return u.email === email; });
        if (exists) {
            setStatus("User already exists. Please log in.", true);
            return;
        }

        try {
            const response = await fetch(getApiBase() + "/customers", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: name, phone: phone, licenseNumber: licenseNumber })
            });
            const payload = await response.json();
            if (!response.ok) {
                setStatus(payload.error || "Signup failed.", true);
                return;
            }

            users.push({
                customerId: payload.customerId,
                name: name,
                email: email,
                password: password,
                phone: phone,
                licenseNumber: licenseNumber,
                role: "user"
            });
            writeUsers(users);
            setStatus("Signup successful. Please login.", false);
            signupForm.reset();
        } catch (_err) {
            setStatus("Unable to reach server. Check backend deployment.", true);
        }
    });

    loginForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value.trim().toLowerCase();
        const password = document.getElementById("loginPassword").value;

        if (email === ADMIN_EMAIL && password === ADMIN_PASSWORD) {
            localStorage.setItem("lmd_session", JSON.stringify({ role: "admin", email: email }));
            window.location.href = "admin.html";
            return;
        }

        const users = readUsers();
        const user = users.find(function (u) { return u.email === email && u.password === password; });
        if (!user) {
            setStatus("Invalid credentials.", true);
            return;
        }

        localStorage.setItem("lmd_session", JSON.stringify({
            role: "user",
            email: user.email,
            name: user.name,
            customerId: user.customerId
        }));
        window.location.href = "user.html";
    });
})();
