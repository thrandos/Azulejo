function handleResize() {
    const container = document.getElementById('app-container');
    if (!container) return;

    const designWidth = 1920;
    const designHeight = 1080;

    const scaleX = window.innerWidth / designWidth;
    const scaleY = window.innerHeight / designHeight;
    const scale = Math.min(scaleX, scaleY);

    container.style.transform = `scale(${scale})`;
}

// Initialize on load
handleResize();

// Update on window resize
window.addEventListener('resize', handleResize);
