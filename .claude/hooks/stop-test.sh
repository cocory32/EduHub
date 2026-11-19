#!/usr/bin/env bash
# .claude/hooks/stop-test.sh — Stop: 백엔드 코드가 바뀌었으면 테스트를 돌리고, 실패하면 Claude가 계속 고치게 한다.
# 막는 목적의 보호 훅이 아니므로, 입력을 읽을 도구가 없거나 판단할 수 없으면 그냥 exit 0으로 넘어간다.

INPUT=$(cat)

# JSON 도구: jq, 없으면 node (jq가 없는 PC용 대체 경로)
if command -v jq >/dev/null 2>&1; then
  ACTIVE=$(printf '%s' "$INPUT" | jq -r '.stop_hook_active // false' 2>/dev/null)
  to_json() { jq -n --arg r "$1" '{decision: "block", reason: $r}'; }
elif command -v node >/dev/null 2>&1; then
  ACTIVE=$(printf '%s' "$INPUT" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{process.stdout.write(String(JSON.parse(s).stop_hook_active===true))}catch{process.stdout.write("false")}})' 2>/dev/null)
  to_json() { R="$1" node -e 'process.stdout.write(JSON.stringify({decision:"block",reason:process.env.R}))'; }
else
  exit 0
fi

# Stop 훅 때문에 이미 한 번 더 진행 중이면 반복하지 않는다 (무한 반복 방지)
[ "$ACTIVE" = "true" ] && exit 0

cd "$CLAUDE_PROJECT_DIR" 2>/dev/null || exit 0
[ -z "$(git status --porcelain -- backend/src 2>/dev/null)" ] && exit 0

if ! OUT=$(cd backend && ./gradlew test -q 2>&1); then
  # -q 출력에는 실패한 테스트 이름이 없다. 요약·컴파일 오류 줄은 출력에서,
  # 실패한 테스트 이름과 메시지는 JUnit 결과 XML에서 골라 20줄만 넘긴다 (토큰 절약).
  FAILS=$(
    {
      printf '%s\n' "$OUT" | grep -E 'tests completed|FAILED|Exception|error:'
      if printf '%s' "$OUT" | grep -q 'tests completed'; then
        grep -h -B1 '<failure' backend/build/test-results/test/*.xml 2>/dev/null | awk '
          /<testcase/ { match($0, /name="[^"]*"/); n = substr($0, RSTART + 6, RLENGTH - 7)
                        match($0, /classname="[^"]*"/); c = substr($0, RSTART + 11, RLENGTH - 12); sub(/.*\./, "", c) }
          /<failure/  { match($0, /message="[^"]*"/); m = substr($0, RSTART + 9, RLENGTH - 10)
                        gsub(/&#13;|&#10;/, " ", m); gsub(/&quot;/, "\"", m); gsub(/&lt;/, "<", m); gsub(/&gt;/, ">", m); gsub(/&amp;/, "\\&", m)
                        gsub(/ +/, " ", m); print c " > " n ": " substr(m, 1, 200) }'
      fi
    } | head -20
  )
  to_json "백엔드 테스트가 실패했습니다. 원인을 고친 뒤 마무리하세요.
$FAILS"
fi
exit 0
