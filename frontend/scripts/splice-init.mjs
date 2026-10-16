import { readFileSync, writeFileSync } from 'node:fs'

const initPath = 'backend/src/main/resources/db/init.sql'
let s = readFileSync(initPath, 'utf8')
const marker = '-- 宠物档案'
const idx = s.indexOf(marker)
if (idx < 0) {
  console.error('marker not found')
  process.exit(1)
}
const prefix = s.slice(0, idx).replace(/\s+$/, '')
const block = readFileSync('backend/src/main/resources/db/init_pet_breed.sql', 'utf8')
writeFileSync(initPath, prefix + '\n\n' + block)
console.log('spliced init.sql: kept', prefix.length, 'chars, appended', block.length, 'chars')
