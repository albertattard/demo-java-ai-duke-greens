document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-character-count]").forEach((counter) => {
        const input = document.getElementById(counter.dataset.characterCountTarget);
        const error = document.getElementById(counter.dataset.characterCountErrorTarget);
        const form = input?.form;

        if (input === null || input.maxLength < 0 || error === null || form === null) {
            return;
        }

        const update = () => {
            const excess = input.value.length - input.maxLength;
            counter.textContent = `${input.value.length.toLocaleString("en")} of ${input.maxLength.toLocaleString("en")} characters${excess > 0 ? ` — ${excess.toLocaleString("en")} character${excess === 1 ? "" : "s"} too long` : ""}`;
            if (excess > 0) {
                input.setAttribute("aria-invalid", "true");
            } else {
                input.removeAttribute("aria-invalid");
                error.hidden = true;
                error.textContent = "";
            }
        };

        input.addEventListener("input", update);
        form.addEventListener("submit", (event) => {
            const excess = input.value.length - input.maxLength;
            if (excess <= 0) {
                return;
            }

            event.preventDefault();
            input.setAttribute("aria-invalid", "true");
            error.textContent = `Your message is ${excess.toLocaleString("en")} character${excess === 1 ? "" : "s"} too long. Shorten it before sending.`;
            error.hidden = false;
            input.focus();
        });
        update();
    });

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    document.querySelectorAll("[data-dictation]").forEach((controls) => {
        const input = document.getElementById(controls.dataset.dictationTarget);
        const start = controls.querySelector("[data-dictation-start]");
        const cancel = controls.querySelector("[data-dictation-cancel]");
        const status = document.getElementById(controls.dataset.dictationStatusId);

        if (input === null || start === null || cancel === null || status === null) {
            return;
        }

        if (SpeechRecognition === undefined) {
            start.hidden = true;
            status.textContent = "Dictation is unavailable in this browser. You can still type your request.";
            status.hidden = false;
            return;
        }

        let recognition;
        let originalText = "";
        let cancelled = false;
        let receivedTranscript = false;

        const setIdle = (message) => {
            start.disabled = false;
            start.textContent = "Start dictation";
            start.setAttribute("aria-pressed", "false");
            cancel.hidden = true;
            status.textContent = message;
            status.hidden = false;
        };

        const setListening = () => {
            start.textContent = "Stop dictation";
            start.setAttribute("aria-pressed", "true");
            cancel.hidden = false;
            status.textContent = "Listening. Speak your request, then stop dictation when you are finished.";
            status.hidden = false;
        };

        start.addEventListener("click", () => {
            if (recognition !== undefined) {
                recognition.stop();
                return;
            }

            originalText = input.value;
            cancelled = false;
            receivedTranscript = false;
            recognition = new SpeechRecognition();
            recognition.continuous = false;
            recognition.interimResults = false;
            recognition.lang = document.documentElement.lang;

            recognition.onresult = (event) => {
                const transcript = Array.from(event.results)
                    .filter((result) => result.isFinal)
                    .map((result) => result[0].transcript)
                    .join(" ")
                    .trim();

                if (transcript !== "") {
                    input.value = transcript;
                    input.dispatchEvent(new Event("input", { bubbles: true }));
                    receivedTranscript = true;
                }
            };

            recognition.onerror = (event) => {
                input.value = originalText;
                input.dispatchEvent(new Event("input", { bubbles: true }));
                const message = event.error === "not-allowed" || event.error === "service-not-allowed"
                    ? "Microphone access was not allowed. Check your browser permission and try again."
                    : "Dictation didn’t work. You can still type your request.";
                setIdle(message);
                recognition = undefined;
            };

            recognition.onend = () => {
                if (recognition === undefined) {
                    return;
                }

                recognition = undefined;
                if (cancelled) {
                    input.value = originalText;
                    input.dispatchEvent(new Event("input", { bubbles: true }));
                    setIdle("Dictation cancelled. Your typed request is unchanged.");
                } else if (receivedTranscript) {
                    setIdle("Dictation complete. Review or amend the text before submitting.");
                } else {
                    setIdle("No speech was transcribed. Your typed request is unchanged.");
                }
            };

            setListening();
            try {
                recognition.start();
            } catch (error) {
                input.value = originalText;
                recognition = undefined;
                setIdle("We couldn’t start dictation. Your typed request is unchanged.");
            }
        });

        cancel.addEventListener("click", () => {
            if (recognition === undefined) {
                return;
            }

            cancelled = true;
            recognition.abort();
        });
    });
});
