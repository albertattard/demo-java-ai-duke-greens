document.addEventListener("DOMContentLoaded", () => {
    const accessCode = document.querySelector("#access-code");
    const toggle = document.querySelector("#toggle-access-code");

    if (accessCode === null || toggle === null) {
        return;
    }

    toggle.addEventListener("click", () => {
        const accessCodeIsVisible = accessCode.type === "text";
        accessCode.type = accessCodeIsVisible ? "password" : "text";
        toggle.setAttribute("aria-pressed", String(!accessCodeIsVisible));
        toggle.setAttribute("aria-label", accessCodeIsVisible ? "Reveal entered code" : "Hide entered code");
        toggle.textContent = accessCodeIsVisible ? "Show" : "Hide";
    });
});
