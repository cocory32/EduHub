#!/usr/bin/env bash
# .claude/hooks/guard-bash.sh — PreToolUse(Bash): 위험 명령 차단 (최선의 노력용 안전망)
# 막을 때는 "이유 + 대신 할 일"을 stderr로 쓰고 exit 2.
# JSON을 읽을 도구(jq, 없으면 node)가 없으면 검사할 수 없으므로 통과시키지 않고 막는다 (그냥 두면 보호가 조용히 꺼진다).
# 다른 형태의 명령(예: git push origin HEAD:main)은 빠져나갈 수 있다.
# GitHub main 브랜치 보호와 settings.json의 deny 규칙을 함께 쓴다.

block() { echo "$1" >&2; exit 2; }

INPUT=$(cat)
if command -v jq >/dev/null 2>&1; then
  CMD=$(printf '%s' "$INPUT" | jq -r '.tool_input.command // empty') \
    || block "훅 입력(JSON)을 읽지 못해 보호 훅이 검사할 수 없습니다."
elif command -v node >/dev/null 2>&1; then
  # jq가 없는 PC용 대체 경로
  CMD=$(printf '%s' "$INPUT" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{process.stdout.write(String(JSON.parse(s).tool_input?.command??""))}catch{process.exit(3)}})') \
    || block "훅 입력(JSON)을 읽지 못해 보호 훅이 검사할 수 없습니다."
else
  block "jq가 없어 보호 훅이 검사할 수 없습니다. 사람이 jq를 설치하세요 (Windows: winget install jqlang.jq)."
fi

case "$CMD" in
  *eduhub-prod*|*prod-db*)
    block "운영 환경 접속 명령은 실행할 수 없습니다. 개발 DB(localhost)나 테스트 DB(eduhub_test)를 사용하세요." ;;
  *flywayClean*)
    block "flywayClean은 DB를 모두 지웁니다. 개발 DB 초기화가 필요하면 사람에게 요청하세요." ;;
  *"push --force"*|*"push -f"*)
    block "강제 푸시는 금지입니다. 새 커밋으로 수정하세요." ;;
  *"push origin main"*)
    block "main에 직접 푸시하지 않습니다. 작업 브랜치를 푸시하고 PR을 만드세요." ;;
esac
exit 0
