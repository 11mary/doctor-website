
document.addEventListener("DOMContentLoaded", function () {

    // NAVIGATION
    const siteHeader = document.getElementById("siteHeader");
    const menuToggle = document.getElementById("menuToggle");
    const mainNav = document.getElementById("mainNav");

    function updateHeader() {
        if (siteHeader) {
            siteHeader.classList.toggle("scrolled", window.scrollY > 30);
        }
    }

    updateHeader();
    window.addEventListener("scroll", updateHeader, { passive: true });

    if (menuToggle && mainNav) {
        menuToggle.addEventListener("click", function () {
            const isOpen = mainNav.classList.toggle("active");
            menuToggle.setAttribute("aria-expanded", String(isOpen));

            const icon = menuToggle.querySelector("i");
            if (icon) {
                icon.classList.toggle("bi-list", !isOpen);
                icon.classList.toggle("bi-x-lg", isOpen);
            }
        });

        mainNav.querySelectorAll('a[href^="#"]').forEach(function (link) {
            link.addEventListener("click", function () {
                mainNav.classList.remove("active");
                menuToggle.setAttribute("aria-expanded", "false");

                const icon = menuToggle.querySelector("i");
                if (icon) {
                    icon.classList.add("bi-list");
                    icon.classList.remove("bi-x-lg");
                }
            });
        });
    }

    // REVEAL ANIMATIONS
    const revealElements = document.querySelectorAll(".reveal");

    if ("IntersectionObserver" in window) {
        const revealObserver = new IntersectionObserver(function (entries, observer) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add("visible");
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });

        revealElements.forEach(function (element) {
            revealObserver.observe(element);
        });
    } else {
        revealElements.forEach(function (element) {
            element.classList.add("visible");
        });
    }

    // COUNTERS
    const counters = document.querySelectorAll(".counter[data-target]");

    function animateCounter(counter) {
        if (counter.dataset.animated === "true") return;

        counter.dataset.animated = "true";
        const target = Number(counter.dataset.target || 0);
        const startTime = performance.now();

        function updateCounter(now) {
            const progress = Math.min((now - startTime) / 1200, 1);
            counter.textContent = String(Math.floor(target * progress));

            if (progress < 1) {
                requestAnimationFrame(updateCounter);
            } else {
                counter.textContent = String(target);
            }
        }

        requestAnimationFrame(updateCounter);
    }

    if ("IntersectionObserver" in window) {
        const counterObserver = new IntersectionObserver(function (entries, observer) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    animateCounter(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.5 });

        counters.forEach(function (counter) {
            counterObserver.observe(counter);
        });
    } else {
        counters.forEach(animateCounter);
    }

    // FAQ
    document.querySelectorAll(".faq-question").forEach(function (button) {
        button.setAttribute("aria-expanded", "false");

        button.addEventListener("click", function () {
            const item = button.closest(".faq-item");
            if (!item) return;

            const wasActive = item.classList.contains("active");

            document.querySelectorAll(".faq-item.active").forEach(function (activeItem) {
                activeItem.classList.remove("active");

                const activeButton = activeItem.querySelector(".faq-question");
                if (activeButton) {
                    activeButton.setAttribute("aria-expanded", "false");
                }
            });

            if (!wasActive) {
                item.classList.add("active");
                button.setAttribute("aria-expanded", "true");
            }
        });
    });

    // APPOINTMENT BOOKING
    const appointmentForm = document.getElementById("appointmentForm");
    const dateInput = document.getElementById("date");
    const timeSelect = document.getElementById("appointmentTime");
    const formSuccess = document.getElementById("formSuccess");
    const formError = document.getElementById("formError");

    function getLocalDateString() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, "0");
        const day = String(now.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    }

    function formatTime(time) {
        const parts = time.split(":");
        const hours = Number(parts[0]);
        const minutes = Number(parts[1]);
        const suffix = hours >= 12 ? "PM" : "AM";
        const displayHour = hours % 12 || 12;

        return `${displayHour}:${String(minutes).padStart(2, "0")} ${suffix}`;
    }

    function showError(message) {
        if (formError) {
            formError.textContent = message;
            formError.hidden = false;
        } else {
            alert(message);
        }
    }

    function clearMessages() {
        if (formError) {
            formError.textContent = "";
            formError.hidden = true;
        }

        if (formSuccess) {
            formSuccess.hidden = true;
            formSuccess.classList.remove("show");
        }
    }

    // LOAD SLOTS FROM THE BACKEND
    let slotRequestNumber = 0;

    async function loadAvailableSlots() {
        if (!dateInput || !timeSelect) return;

        const selectedDate = dateInput.value;
        const requestNumber = ++slotRequestNumber;

        timeSelect.disabled = true;
        timeSelect.replaceChildren();

        if (!selectedDate) {
            timeSelect.add(new Option("Select a date first", ""));
            return;
        }

        timeSelect.add(new Option("Loading available slots...", ""));

        try {
            const response = await fetch(
                "/api/appointments/available-slots?date=" +
                encodeURIComponent(selectedDate),
                {
                    method: "GET",
                    headers: { "Accept": "application/json" },
                    cache: "no-store"
                }
            );

            if (!response.ok) {
                throw new Error("Server returned HTTP " + response.status);
            }

            const slots = await response.json();

            // Ignore an older response if the user selected another date.
            if (requestNumber !== slotRequestNumber) return;

            timeSelect.replaceChildren();

            if (!Array.isArray(slots) || slots.length === 0) {
                timeSelect.add(new Option("No slots available on this date", ""));
                return;
            }

            timeSelect.add(new Option("Choose a time", ""));

            slots.forEach(function (slot) {
                timeSelect.add(new Option(formatTime(slot), slot));
            });

            timeSelect.disabled = false;

        } catch (error) {
            if (requestNumber !== slotRequestNumber) return;

            console.error("Failed to load appointment slots:", error);
            timeSelect.replaceChildren();
            timeSelect.add(new Option("Could not load slots — retry", ""));

            showError(
                "Unable to load appointment times. Please refresh the page and try again."
            );
        }
    }

    if (dateInput && timeSelect) {
        dateInput.min = getLocalDateString();

        // Clear the old selection and fetch slots whenever the date changes.
        dateInput.addEventListener("change", function () {
            clearMessages();
            loadAvailableSlots();
        });

        // Also support browsers that update the date through the input event.
        dateInput.addEventListener("input", function () {
            clearMessages();

            if (dateInput.value) {
                loadAvailableSlots();
            }
        });
    }

    // SUBMIT BOOKING
    if (appointmentForm) {
        appointmentForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            clearMessages();

            if (!appointmentForm.reportValidity()) return;

            if (!dateInput || !dateInput.value ||
                !timeSelect || !timeSelect.value) {
                showError("Please select a date and an available time slot.");
                return;
            }

            const submitButton = appointmentForm.querySelector('button[type="submit"]');
            const originalButtonHTML = submitButton ? submitButton.innerHTML : "";

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "Booking...";
            }

            const nameInput = document.getElementById("name");
            const phoneInput = document.getElementById("phone");
            const emailInput = document.getElementById("email");
            const serviceInput = document.getElementById("service");
            const messageInput = document.getElementById("message");

            const appointmentData = {
                patientName: nameInput.value.trim(),
                phone: phoneInput.value.trim(),
                email: emailInput.value.trim(),
                service: serviceInput.value,
                appointmentDate: dateInput.value,
                appointmentTime: timeSelect.value,
                message: messageInput ? messageInput.value.trim() : ""
            };

            try {
                const response = await fetch("/api/appointments", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify(appointmentData)
                });

                if (response.status === 409) {
                    showError(
                        "That time slot has just been booked. Please choose another time."
                    );
                    await loadAvailableSlots();
                    return;
                }

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(errorText || "Booking failed.");
                }

                appointmentForm.reset();
                dateInput.min = getLocalDateString();

                // Invalidate any pending slot request and reset the dropdown.
                slotRequestNumber++;
                timeSelect.replaceChildren();
                timeSelect.add(new Option("Select a date first", ""));
                timeSelect.disabled = true;

                if (formSuccess) {
                    formSuccess.hidden = false;
                    formSuccess.classList.add("show");
                    formSuccess.scrollIntoView({
                        behavior: "smooth",
                        block: "nearest"
                    });
                }

            } catch (error) {
                console.error("Appointment booking failed:", error);
                showError(
                    "We couldn't complete your booking. Please try again."
                );
            } finally {
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.innerHTML = originalButtonHTML;
                }
            }
        });
    }

    // FOOTER YEAR
    const yearElement = document.getElementById("currentYear");

    if (yearElement) {
        yearElement.textContent = String(new Date().getFullYear());
    }

    // SMOOTH INTERNAL LINKS
    document.querySelectorAll('a[href^="#"]').forEach(function (link) {
        link.addEventListener("click", function (event) {
            const targetId = link.getAttribute("href");
            if (!targetId || targetId === "#") return;

            const target = document.querySelector(targetId);

            if (target) {
                event.preventDefault();
                target.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        });
    });

});
