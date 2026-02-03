set -euo pipefail

export PORT="${PORT:-8080}"

COMPOSE_CMD="docker compose"
if ! docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD="docker-compose"
fi

echo "1) Subindo stack (api + postgres)..."
${COMPOSE_CMD} up -d --build

echo "2) Logs iniciais (api):"
${COMPOSE_CMD} logs --tail=50 api

echo ""
echo "Acesse:"
echo "  API:    http://localhost:${PORT}"
echo "  Health: http://localhost:${PORT}/actuator/health"
echo ""
echo "Para acompanhar logs:"
echo "  ${COMPOSE_CMD} logs -f api"
