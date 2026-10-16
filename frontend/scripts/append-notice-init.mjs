import { readFileSync, writeFileSync } from 'node:fs'
const target = 'backend/src/main/resources/db/init.sql'
const block = readFileSync('backend/src/main/resources/db/patch_notice.sql', 'utf8')
let s = readFileSync(target, 'utf8').replace(/\s+$/, '')
if (s.includes('tb_notice')) {
  console.log('init.sql already has tb_notice, skip')
} else {
  writeFileSync(target, s + '\n\n-- ---------- 站内通知 ----------\n\n' + block)
  console.log('appended tb_notice to init.sql')
}
