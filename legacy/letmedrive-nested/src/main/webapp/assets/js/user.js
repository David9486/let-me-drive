(function () {
    const sessionRaw = localStorage.getItem("lmd_session");
    if (!sessionRaw) {
        window.location.href = "index.html";
        return;
    }

    const session = JSON.parse(sessionRaw);
    if (session.role !== "user") {
        window.location.href = session.role === "admin" ? "admin.html" : "index.html";
        return;
    }

    function getApiBase() {
        const parts = window.location.pathname.split("/").filter(Boolean);
        const context = parts.length > 0 ? parts[0] : "";
        return context ? (window.location.origin + "/" + context) : window.location.origin;
    }

    function rentalsKey() {
        return "lmd_rentals_" + (session.customerId || "unknown");
    }

    function readRentals() {
        const raw = localStorage.getItem(rentalsKey());
        return raw ? JSON.parse(raw) : [];
    }

    function writeRentals(records) {
        localStorage.setItem(rentalsKey(), JSON.stringify(records));
    }

    function renderRentals() {
        const tbody = document.getElementById("myRentalsBody");
        const records = readRentals().sort(function (a, b) { return b.rentalId - a.rentalId; });
        tbody.innerHTML = "";

        if (!records.length) {
            const tr = document.createElement("tr");
            tr.innerHTML = '<td colspan="6">No rentals yet. Book a car to see history.</td>';
            tbody.appendChild(tr);
            return;
        }

        records.forEach(function (r) {
            const tr = document.createElement("tr");
            tr.innerHTML =
                "<td>" + r.rentalId + "</td>" +
                "<td>" + r.carId + "</td>" +
                "<td>" + r.startTime.replace("T", " ") + "</td>" +
                "<td>" + r.expectedReturnTime.replace("T", " ") + "</td>" +
                "<td>" + r.status + "</td>" +
                "<td>INR " + r.totalAmount + "</td>";
            tbody.appendChild(tr);
        });
    }

    function fmtDateTimeLocal(value) {
        return value ? value + ":00" : "";
    }

    function setStatus(id, message, isError) {
        const el = document.getElementById(id);
        el.textContent = message;
        el.className = "status " + (isError ? "error" : "ok");
    }

    function carImageByBrand(brand) {
        const map = {
            Audi: "assets/images/cars/audi_A6.jpg",
            Mercedes: "assets/images/cars/mercedes_e_class.jpg",
            BMW: "assets/images/cars/bmw_x7_.jpg"
        };
        return map[brand] || "assets/images/cars/audi_A6.jpg";
    }

    async function loadCars(event) {
        if (event) {
            event.preventDefault();
        }

        const seats = Number(document.getElementById("seats").value);
        const startInput = document.getElementById("startTime").value;
        const endInput = document.getElementById("endTime").value;

        if (!seats || !startInput || !endInput) {
            setStatus("availabilityStatus", "Please fill all search fields.", true);
            return;
        }

        const start = fmtDateTimeLocal(startInput);
        const end = fmtDateTimeLocal(endInput);
        const grid = document.getElementById("carsGrid");
        grid.innerHTML = "";

        try {
            const res = await fetch(getApiBase() + "/cars/available?seats=" + encodeURIComponent(seats) + "&start=" + encodeURIComponent(start) + "&end=" + encodeURIComponent(end));
            const data = await res.json();
            if (!res.ok) {
                setStatus("availabilityStatus", data.error || "Failed to fetch cars.", true);
                return;
            }

            if (!data.length) {
                setStatus("availabilityStatus", "No cars available for the selected time window.", true);
                return;
            }

            setStatus("availabilityStatus", data.length + " premium car(s) found.", false);

            data.forEach(function (car, idx) {
                const card = document.createElement("article");
                card.className = "card";
                card.style.animationDelay = (idx * 90) + "ms";
                card.innerHTML =
                    '<img class="car-image" src="' + carImageByBrand(car.brand) + '" alt="' + car.carName + '">' +
                    '<h3 class="car-title">' + car.carName + '</h3>' +
                    '<p class="car-meta">Brand: ' + car.brand + ' | Seats: ' + car.seatCount + '</p>' +
                    '<p class="car-meta">Rate: INR ' + car.pricePerHour + '/hr | Late fee: INR ' + car.lateFeePerHour + '/hr</p>' +
                    '<button class="btn btn-primary" type="button" data-car-id="' + car.id + '">Book This Car</button>';
                grid.appendChild(card);
            });

            grid.querySelectorAll("button[data-car-id]").forEach(function (btn) {
                btn.addEventListener("click", function () {
                    createRental(Number(btn.getAttribute("data-car-id")), start, end);
                });
            });
        } catch (_err) {
            setStatus("availabilityStatus", "Unable to reach server.", true);
        }
    }

    async function createRental(carId, start, end) {
        if (!session.customerId) {
            setStatus("availabilityStatus", "Missing customer ID in session. Re-login required.", true);
            return;
        }

        try {
            const res = await fetch(getApiBase() + "/rentals", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    customerId: session.customerId,
                    carId: carId,
                    startTime: start,
                    expectedReturnTime: end
                })
            });
            const data = await res.json();
            if (!res.ok) {
                setStatus("availabilityStatus", data.error || "Rental failed.", true);
                return;
            }

            const records = readRentals();
            records.push({
                rentalId: data.rentalId,
                carId: carId,
                startTime: start,
                expectedReturnTime: end,
                status: data.status,
                totalAmount: data.totalAmount,
                fineAmount: data.fineAmount
            });
            writeRentals(records);
            renderRentals();

            setStatus("availabilityStatus", "Rental booked. Rental ID: " + data.rentalId + ", Total: INR " + data.totalAmount, false);
        } catch (_err) {
            setStatus("availabilityStatus", "Unable to reach server for rental booking.", true);
        }
    }

    async function returnRental(event) {
        event.preventDefault();
        const rentalId = Number(document.getElementById("rentalId").value);
        const actualReturnInput = document.getElementById("actualReturnTime").value;

        if (!rentalId || !actualReturnInput) {
            setStatus("returnStatus", "Rental ID and return time are required.", true);
            return;
        }

        try {
            const res = await fetch(getApiBase() + "/rentals/return", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    rentalId: rentalId,
                    actualReturnTime: fmtDateTimeLocal(actualReturnInput)
                })
            });
            const data = await res.json();
            if (!res.ok) {
                setStatus("returnStatus", data.error || "Return failed.", true);
                return;
            }

            const records = readRentals();
            const idx = records.findIndex(function (r) { return r.rentalId === rentalId; });
            if (idx >= 0) {
                records[idx].status = data.status;
                records[idx].fineAmount = data.fineAmount;
                records[idx].totalAmount = data.totalAmount;
                records[idx].actualReturnTime = fmtDateTimeLocal(actualReturnInput);
            } else {
                records.push({
                    rentalId: rentalId,
                    carId: "-",
                    startTime: "-",
                    expectedReturnTime: "-",
                    status: data.status,
                    totalAmount: data.totalAmount,
                    fineAmount: data.fineAmount,
                    actualReturnTime: fmtDateTimeLocal(actualReturnInput)
                });
            }
            writeRentals(records);
            renderRentals();

            setStatus("returnStatus", "Returned. Fine: INR " + data.fineAmount + ", Total: INR " + data.totalAmount, false);
        } catch (_err) {
            setStatus("returnStatus", "Unable to reach server for return operation.", true);
        }
    }

    document.getElementById("availabilityForm").addEventListener("submit", loadCars);
    document.getElementById("returnForm").addEventListener("submit", returnRental);
    document.getElementById("logoutBtn").addEventListener("click", function () {
        localStorage.removeItem("lmd_session");
        window.location.href = "index.html";
    });

    renderRentals();
})();


