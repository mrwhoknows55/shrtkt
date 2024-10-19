// Add floating particles
const particlesContainer = document.createElement("div");
particlesContainer.className = "particles";
document.body.appendChild(particlesContainer);

for (let i = 0; i < 20; i++) {
  const particle = document.createElement("div");
  particle.className = "particle";
  particle.style.width = Math.random() * 4 + "px";
  particle.style.height = particle.style.width;
  particle.style.left = Math.random() * 100 + "%";
  particle.style.top = Math.random() * 100 + "%";
  particle.style.animation = `float ${Math.random() * 6 + 3}s ease-in-out infinite`;
  particle.style.animationDelay = `-${Math.random() * 5}s`;
  particlesContainer.appendChild(particle);
}
