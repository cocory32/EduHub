#!/usr/bin/env bash
# .claude/hooks/protect-files.sh — PreToolUse(Edit|Write): 보호 파일 수정 차단
# 막을 때는 "이유 + 대신 할 일"을 stderr로 쓰고 exit 2.
# JSON을 읽을 도구(jq, 없으면 node)가 없으면 검사할 수 없으므로 통과시키지 않고 막는다 (그냥 두면 보호가 조용히 꺼진다).

block() { echo "$1" >&2; exit 2; }

INPUT=$(cat)
if command -v jq >/dev/null 2>&1; then
  FILE=$(printf '%s' "$INPUT" | jq -r '.tool_input.file_path // empty') \
    || block "훅 입력(JSON)을 읽지 못해 보호 훅이 검사할 수 없습니다."
elif command -v node >/dev/null 2>&1; then
  # jq가 없는 PC용 대체 경로
  FILE=$(printf '%s' "$INPUT" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{process.stdout.write(String(JSON.parse(s).tool_input?.file_path??""))}catch{process.exit(3)}})') \
    || block "훅 입력(JSON)을 읽지 못해 보호 훅이 검사할 수 없습니다."
else
  block "jq가 없어 보호 훅이 검사할 수 없습니다. 사람이 jq를 설치하세요 (Windows: winget install jqlang.jq)."
fi

FILE="${FILE//\\//}"   # Windows 경로의 \ 를 / 로 (Git Bash에서 실행돼도 경로는 \ 로 들어온다)

case "$FILE" in
  *application-prod.yml)
    block "운영 설정은 사람이 PR로만 수정합니다. 개발 설정은 application-dev.yml을 수정하세요." ;;
  *.env.example) ;;
  */.env|*/.env.*|.env|.env.*)
    block "비밀 파일은 수정하지 않습니다. 필요한 변수 이름은 .env.example에 추가하세요." ;;
  */api-client/src/generated/*)
    block "자동 생성 코드입니다. 'npm run generate:api'로 다시 생성하세요." ;;
esac

if [[ "$FILE" == */db/migration/V*.sql && -f "$FILE" ]]; then
  block "기존 Flyway 마이그레이션은 수정할 수 없습니다. 다음 버전 번호로 새 파일을 추가하세요."
fi
exit 0
