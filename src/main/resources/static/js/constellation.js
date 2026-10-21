/* ============================================================
   Constelación: procedural brain-shaped particle field.
   Outlined triangles, chromatic palette, soft drift.
   No images, no gradients — pure canvas.
   ============================================================ */

(() => {
    "use strict";

    const canvas = document.getElementById("constellation");
    if (!canvas) return;

    const ctx = canvas.getContext("2d", { alpha: true });
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    const COLORS = [
        "#8052ff", "#8052ff", "#8052ff",
        "#a06bff", "#4f7cff",
        "#ffb829",
        "#15846e", "#38d6c4",
        "#ff5f9e"
    ];

    const BRAIN_COUNT = 1150;
    const AMBIENT_COUNT = 170;

    let width = 0;
    let height = 0;
    let dpr = 1;
    let radius = 0;
    let brain = [];
    let ambient = [];
    let frame = 0;
    let running = false;
    let start = 0;
    let visible = true;
    let pointer = { x: 0, y: 0 };
    let pointerTarget = { x: 0, y: 0 };

    const random = (min, max) => min + Math.random() * (max - min);
    const pick = (list) => list[(Math.random() * list.length) | 0];

    /* --------------------------------------------------------
       Silhouette: union of lobes minus the central groove.
       Coordinates are normalized to [-1, 1].
       -------------------------------------------------------- */

    function inEllipse(x, y, cx, cy, rx, ry) {
        const dx = (x - cx) / rx;
        const dy = (y - cy) / ry;
        return dx * dx + dy * dy <= 1;
    }

    function insideBrain(x, y) {
        if (Math.abs(x) > 0.88 || Math.abs(y) > 0.9) return false;

        const leftLobe = inEllipse(x, y, -0.33, -0.04, 0.54, 0.46);
        const rightLobe = inEllipse(x, y, 0.33, -0.04, 0.54, 0.46);
        const crown = inEllipse(x, y, 0, -0.16, 0.46, 0.42);
        const lower = inEllipse(x, y, 0, 0.34, 0.36, 0.24);
        const stem = inEllipse(x, y, 0, 0.55, 0.11, 0.13);

        if (!(leftLobe || rightLobe || crown || lower || stem)) return false;

        // Central groove between the hemispheres.
        if (Math.abs(x) < 0.035 && y < 0.28) return false;

        // Soft notches at the sides, so the silhouette is not a perfect blob.
        if (Math.abs(y - 0.18) < 0.05 && Math.abs(x) > 0.82) return false;

        return true;
    }

    /* --------------------------------------------------------
       Particles
       -------------------------------------------------------- */

    function makeBrainParticle() {
        for (let attempt = 0; attempt < 200; attempt++) {
            const x = random(-0.92, 0.92);
            const y = random(-0.9, 0.9);
            if (!insideBrain(x, y)) continue;

            return {
                x,
                y,
                size: random(1.6, 4.6),
                color: pick(COLORS),
                rot: random(0, Math.PI * 2),
                spin: random(-0.35, 0.35),
                alpha: random(0.28, 0.95),
                phase: random(0, Math.PI * 2),
                swayX: random(1.5, 4.5),
                swayY: random(1.5, 4.5),
                depth: random(0.4, 1)
            };
        }
        return null;
    }

    function makeAmbientParticle() {
        const x = random(-1.12, 1.12);
        const y = random(-1.12, 1.12);
        if (insideBrain(x, y)) return makeAmbientParticle();

        return {
            x,
            y,
            size: random(1.2, 3),
            color: pick(COLORS),
            rot: random(0, Math.PI * 2),
            spin: random(-0.25, 0.25),
            alpha: random(0.08, 0.32),
            phase: random(0, Math.PI * 2),
            swayX: random(2, 6),
            swayY: random(2, 6),
            vx: random(-0.00012, 0.00012),
            vy: random(-0.00009, 0.00009),
            depth: random(0.2, 0.6)
        };
    }

    function build() {
        brain = [];
        while (brain.length < BRAIN_COUNT) {
            const particle = makeBrainParticle();
            if (particle) brain.push(particle);
        }

        ambient = [];
        while (ambient.length < AMBIENT_COUNT) {
            ambient.push(makeAmbientParticle());
        }
    }

    /* --------------------------------------------------------
       Sizing
       -------------------------------------------------------- */

    function resize() {
        const rect = canvas.getBoundingClientRect();
        dpr = Math.min(window.devicePixelRatio || 1, 2);
        width = Math.max(1, rect.width);
        height = Math.max(1, rect.height);
        radius = Math.min(width, height) * 0.47;

        canvas.width = Math.round(width * dpr);
        canvas.height = Math.round(height * dpr);
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    /* --------------------------------------------------------
       Drawing
       -------------------------------------------------------- */

    function triangle(x, y, size, rotation, color, alpha) {
        ctx.globalAlpha = alpha;
        ctx.strokeStyle = color;
        ctx.beginPath();
        for (let i = 0; i < 3; i++) {
            const angle = rotation + i * (Math.PI * 2 / 3) - Math.PI / 2;
            const px = x + Math.cos(angle) * size;
            const py = y + Math.sin(angle) * size;
            if (i === 0) ctx.moveTo(px, py);
            else ctx.lineTo(px, py);
        }
        ctx.closePath();
        ctx.stroke();
    }

    function draw(time) {
        ctx.clearRect(0, 0, width, height);
        ctx.lineWidth = 1.1;
        ctx.lineCap = "round";

        const cx = width / 2 + pointer.x;
        const cy = height / 2 + pointer.y;

        for (let i = 0; i < brain.length; i++) {
            const p = brain[i];
            const swayX = Math.sin(time * 0.00035 + p.phase) * p.swayX;
            const swayY = Math.cos(time * 0.00028 + p.phase * 1.3) * p.swayY;

            const x = cx + p.x * radius + swayX;
            const y = cy + p.y * radius + swayY;
            const rotation = p.rot + time * 0.00012 * p.spin;

            triangle(x, y, p.size, rotation, p.color, p.alpha);
        }

        for (let i = 0; i < ambient.length; i++) {
            const p = ambient[i];

            if (!reduceMotion) {
                p.x += p.vx;
                p.y += p.vy;
                if (p.x > 1.14) p.x = -1.14;
                if (p.x < -1.14) p.x = 1.14;
                if (p.y > 1.14) p.y = -1.14;
                if (p.y < -1.14) p.y = 1.14;
            }

            const swayX = Math.sin(time * 0.00025 + p.phase) * p.swayX;
            const swayY = Math.cos(time * 0.00022 + p.phase) * p.swayY;

            const x = cx + p.x * radius + swayX;
            const y = cy + p.y * radius + swayY;
            const rotation = p.rot + time * 0.0001 * p.spin;

            triangle(x, y, p.size, rotation, p.color, p.alpha);
        }

        ctx.globalAlpha = 1;
    }

    /* --------------------------------------------------------
       Loop
       -------------------------------------------------------- */

    function tick(now) {
        if (!running) return;
        if (!start) start = now;

        pointer.x += (pointerTarget.x - pointer.x) * 0.04;
        pointer.y += (pointerTarget.y - pointer.y) * 0.04;

        draw(now - start);
        frame = requestAnimationFrame(tick);
    }

    function play() {
        if (reduceMotion || running) return;
        running = true;
        frame = requestAnimationFrame(tick);
    }

    function pause() {
        running = false;
        if (frame) cancelAnimationFrame(frame);
        frame = 0;
    }

    function updateRunningState() {
        if (visible && !document.hidden) play();
        else pause();
    }

    /* --------------------------------------------------------
       Events
       -------------------------------------------------------- */

    window.addEventListener("resize", () => {
        resize();
        if (reduceMotion) draw(0);
    }, { passive: true });

    document.addEventListener("visibilitychange", updateRunningState);

    if ("IntersectionObserver" in window) {
        const observer = new IntersectionObserver((entries) => {
            visible = entries.some((entry) => entry.isIntersecting);
            updateRunningState();
        }, { threshold: 0 });
        observer.observe(canvas);
    }

    if (!reduceMotion && window.matchMedia("(pointer: fine)").matches) {
        window.addEventListener("pointermove", (event) => {
            const rect = canvas.getBoundingClientRect();
            const nx = (event.clientX - rect.left) / rect.width - 0.5;
            const ny = (event.clientY - rect.top) / rect.height - 0.5;
            pointerTarget.x = Math.max(-1, Math.min(1, nx)) * 10;
            pointerTarget.y = Math.max(-1, Math.min(1, ny)) * 10;
        }, { passive: true });
    }

    /* --------------------------------------------------------
       Init
       -------------------------------------------------------- */

    resize();
    build();

    // Paint the first frame immediately: the field must be visible even if
    // requestAnimationFrame is throttled (hidden tab, reduced motion, etc.).
    draw(0);

    if (!reduceMotion) {
        updateRunningState();
    }
})();
