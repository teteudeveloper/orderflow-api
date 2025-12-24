set -euo pipefail

APP_NAME="orderflow-api"
PORT="${PORT:-8080}"

echo "1) Buildando imagem Docker..."
docker build -t "${APP_NAME}:latest" .

echo "2) Parando container anterior (se existir)..."
docker rm -f "${APP_NAME}" >/dev/null 2>&1 || true

echo "3) Subindo container..."
docker run -d \
  --name "${APP_NAME}" \
  -p "${PORT}:8080" \
  "${APP_NAME}:latest"

echo "4) Logs iniciais:"
docker logs -n 50 "${APP_NAME}"

echo ""
echo "Acesse:"
echo "  API:    http://localhost:${PORT}"
echo "  Health: http://localhost:${PORT}/actuator/health"
echo ""
echo "Para acompanhar logs:"
echo "  docker logs -f ${APP_NAME}"