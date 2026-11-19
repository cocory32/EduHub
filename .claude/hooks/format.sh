#!/usr/bin/env bash
# PostToolUse(Edit|Write): Claude가 수정한 파일 하나를 포맷한다.
# 포맷 도구가 없거나 실패해도 Claude 작업이 멈추지 않게 항상 exit 0.

input=$(cat)

if command -v jq >/dev/null 2>&1; then
  file=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty' 2>/dev/null)
elif command -v node >/dev/null 2>&1; then
  # jq가 없는 PC용 대체 경로
  file=$(printf '%s' "$input" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{process.stdout.write(JSON.parse(s).tool_input?.file_path??"")}catch{}})' 2>/dev/null)
fi

[ -z "$file" ] && exit 0

root="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"

case "$file" in
  *.java)
    (cd "$root/backend" && ./gradlew spotlessApply -q) >/dev/null 2>&1
    ;;
  *.ts|*.tsx)
    prettier="$root/frontend/node_modules/.bin/prettier"
    if [ -x "$prettier" ]; then
      (cd "$root/frontend" && "$prettier" --write "$file") >/dev/null 2>&1
    fi
    ;;
esac

exit 0
