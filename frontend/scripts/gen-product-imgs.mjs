// 生成商品示意图（SVG 产品照风格），输出到 frontend/public/products/
import { mkdirSync, writeFileSync } from 'node:fs'
import path from 'node:path'

const OUT = path.resolve('frontend/public/products')
mkdirSync(OUT, { recursive: true })

const BRAND = {
  霍曼: { accent: '#5B8DEF', name: 'HOMERUN' },
  小佩: { accent: '#22A987', name: 'PETKIT' },
  华元: { accent: '#E8A33D', name: 'HUAYUAN' },
  麦富迪: { accent: '#E05A3D', name: 'MYFOODIE' },
}

const products = `
1001	全价鸡肉配方猫粮 2kg	主粮	霍曼	1	1
1002	深海鱼成犬粮 10kg	主粮	小佩	1	2
1003	冻干鸡肉零食 100g	零食	华元	1	1
1004	瓦楞纸猫抓板	玩具	麦富迪	0	1
1005	宠物低敏沐浴露 500ml	清洁护理	霍曼	0	0
1006	关节养护营养膏	医疗保健	小佩	0	0
1007	智能定时喂食器 3L	智能设备	华元	0	0
1008	鸡肉冻干零食 100g（临期）	零食	麦富迪	1	0
1101	三文鱼猫主食罐头 170g	零食	霍曼	1	1
1102	猫条零食 12支装	零食	小佩	1	1
1103	冻干鹌鹑零食 40g	零食	华元	1	1
1104	鸡肉牛油果成犬粮 2kg	主粮	麦富迪	1	2
1105	狗咬胶磨牙棒 大号	零食	霍曼	1	2
1106	牛肉罐头 370g	零食	小佩	1	2
1107	豆腐猫砂 6L	清洁护理	华元	0	1
1108	逗猫棒套装 三件	玩具	麦富迪	0	1
1109	猫粮碗防滑增高	医疗保健	霍曼	0	1
1110	漏食玩具球	玩具	小佩	0	2
1111	伸缩牵引绳 3m	玩具	华元	0	2
1112	狗狗窝柔软坐垫	玩具	麦富迪	0	2
1201	宠物连帽卫衣 猫狗通用	服饰	霍曼	0	0
1202	宠物雨衣 四脚款	服饰	小佩	0	0
1203	宠物外出双肩包 透气猫包	出行	华元	0	1
1204	车载宠物坐垫 防滑垫	出行	麦富迪	0	2
1301	宠物芭蕾风连衣裙	服饰	霍曼	0	1
1302	狗狗冲锋衣 防泼水	服饰	小佩	0	2
1303	宠物推车 可折叠	出行	华元	0	0
1304	宠物飞盘套装	玩具	麦富迪	0	2
1305	宠物指甲剪套装	清洁护理	霍曼	0	0
1306	体内外驱虫滴剂 3支	医疗保健	小佩	0	0
1307	智能宠物饮水机 2L	智能设备	华元	0	1
1308	藏食嗅闻垫	玩具	麦富迪	0	0
`.trim().split('\n').map((l) => {
  const [id, name, cat, brand, saleType, petType] = l.split('\t')
  return { id, name, cat, brand, saleType, petType }
})

const shadow = (cx = 300, cy = 425, rx = 130) =>
  `<ellipse cx="${cx}" cy="${cy}" rx="${rx}" ry="16" fill="rgba(90,80,70,.16)"/>`

const label = (name, brand) => {
  const short = name.length > 9 ? name.slice(0, 9) + '…' : name
  const b = BRAND[brand]
  return `
  <g>
    <rect x="208" y="268" width="184" height="26" rx="13" fill="${b.accent}"/>
    <text x="300" y="286" text-anchor="middle" font-size="14" font-family="Segoe UI, sans-serif" font-weight="600" fill="#fff" letter-spacing="1">${b.name}</text>
  </g>
  <text x="300" y="520" text-anchor="middle" font-size="22" font-family="'PingFang SC','Microsoft YaHei',sans-serif" font-weight="600" fill="#3a3f45">${short}</text>
  <text x="300" y="548" text-anchor="middle" font-size="14" font-family="'PingFang SC','Microsoft YaHei',sans-serif" fill="#9aa1ab">${b.name} · 自营正品</text>`
}

// ---------- 品类插画 ----------
const draw = {
  foodBag(a) {
    return `
  <g>
    ${shadow()}
    <path d="M208 246 l92 -44 l92 44 l-11 176 a24 24 0 0 1 -162 0 Z" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <path d="M208 246 l92 -44 l92 44 l-92 34 Z" fill="#f3efe9"/>
    <circle cx="252" cy="338" r="10" fill="${a}" opacity=".9"/>
    <circle cx="352" cy="368" r="11" fill="#e7c78a"/>
    <circle cx="300" cy="392" r="9" fill="#d9a35f"/>
    <circle cx="272" cy="424" r="10" fill="#e7c78a"/>
  </g>`
  },
  treatPouch(a) {
    return `
  <g>
    ${shadow()}
    <path d="M222 240 a78 24 0 0 0 156 0 l-26 176 a20 20 0 0 1 -104 0 Z" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <path d="M222 240 a78 24 0 0 1 156 0 l-22 14 a78 24 0 0 0 -112 0 Z" fill="#f3efe9"/>
    <circle cx="252" cy="330" r="12" fill="${a}" opacity=".85"/>
    <circle cx="318" cy="362" r="14" fill="#e7c78a"/>
    <circle cx="276" cy="408" r="12" fill="#d9a35f"/>
  </g>`
  },
  can(a) {
    return `
  <g>
    ${shadow()}
    <rect x="232" y="300" width="136" height="118" rx="18" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="222" y="292" width="156" height="26" rx="10" fill="${a}"/>
    <circle cx="260" cy="344" r="8" fill="${a}" opacity=".6"/>
    <circle cx="340" cy="392" r="9" fill="#e7c78a"/>
  </g>`
  },
  scratchBoard(a) {
    return `
  <g>
    ${shadow(300, 420, 150)}
    <rect x="150" y="250" width="300" height="150" rx="16" fill="#c8b391"/>
    <path d="M150 280 h300 M150 310 h300 M150 340 h300" stroke="#a98d6c" stroke-width="3" opacity=".55"/>
    <path d="M170 280 q0 20 -14 20 M318 310 q0 26 -16 26 M262 340 q0 22 -12 22" stroke="#8a7357" stroke-width="6" fill="none" stroke-linecap="round"/>
    <rect x="150" y="250" width="300" height="150" rx="16" fill="none" stroke="#b39b7d" stroke-width="4"/>
  </g>`
  },
  bottle(a) {
    return `
  <g>
    ${shadow()}
    <path d="M262 250 h76 v26 a18 18 0 0 1 18 18 v118 a26 26 0 0 1 -26 26 h-60 a26 26 0 0 1 -26 -26 v-118 a18 18 0 0 1 18 -18 Z" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="258" y="300" width="84" height="34" rx="8" fill="${a}" opacity=".85"/>
    <path d="M300 222 v28" stroke="#b9b2aa" stroke-width="8" stroke-linecap="round"/>
    <path d="M340 268 a10 10 0 0 1 -10 10 l-60 0 a10 10 0 0 1 -10 -10 l22 -18 a10 10 0 0 1 36 0 Z" fill="#e8e2da" stroke="#b9b2aa" stroke-width="3"/>
  </g>`
  },
  medTube(a) {
    return `
  <g>
    ${shadow()}
    <rect x="236" y="300" width="128" height="120" rx="20" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <path d="M286 300 v-34 l28 -10 v38" fill="${a}"/>
    <rect x="236" y="386" width="128" height="34" rx="12" fill="${a}" opacity=".85"/>
    <circle cx="262" cy="348" r="7" fill="${a}" opacity=".5"/>
    <circle cx="338" cy="330" r="8" fill="#e7c78a"/>
  </g>`
  },
  device(a) {
    return `
  <g>
    ${shadow()}
    <rect x="236" y="266" width="128" height="150" rx="26" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="246" y="284" width="108" height="30" rx="10" fill="${a}" opacity=".75"/>
    <circle cx="278" cy="356" r="16" fill="#e8f5f1"/>
    <circle cx="322" cy="356" r="16" fill="#e8f5f1"/>
    <path d="M236 392 a64 16 0 0 0 128 0 Z" fill="#f3efe9"/>
  </g>`
  },
  garment(a) {
    return `
  <g>
    ${shadow()}
    <path d="M220 260 h160 l26 60 -46 8 -6 -70 -24 60 h-60 l-24 -60 -6 70 -46 -8 Z" fill="#ffffff" stroke="#d8d2cb" stroke-width="3" stroke-linejoin="round"/>
    <path d="M220 260 h42 m78 0 h40" stroke="#d8d2cb" stroke-width="3"/>
    <circle cx="262" cy="352" r="7" fill="${a}" opacity=".8"/>
    <circle cx="338" cy="352" r="7" fill="${a}" opacity=".8"/>
  </g>`
  },
  carrier(a) {
    return `
  <g>
    ${shadow()}
    <path d="M236 268 h128 v110 a24 24 0 0 1 -24 24 h-80 a24 24 0 0 1 -24 -24 Z" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="252" y="258" width="96" height="24" rx="12" fill="${a}"/>
    <rect x="270" y="300" width="60" height="58" rx="14" fill="#eef2f6" stroke="#d8d2cb" stroke-width="3"/>
    <path d="M262 268 v40 m76 0 v-40" stroke="#d8d2cb" stroke-width="6"/>
  </g>`
  },
  teaser(a) {
    return `
  <g>
    ${shadow(300, 300, 100)}
    <path d="M300 150 v150" stroke="#c9bfae" stroke-width="10" stroke-linecap="round"/>
    <circle cx="300" cy="330" r="34" fill="${a}"/>
    <path d="M268 300 q28 -30 64 0 q-28 30 -64 0 Z" fill="#e05a3d"/>
    <path d="M300 174 l30 -34 M308 166 l12 -40 M296 184 l-24 -40" stroke="#e8e2da" stroke-width="6" stroke-linecap="round"/>
  </g>`
  },
  ball(a) {
    return `
  <g>
    ${shadow()}
    <circle cx="300" cy="330" r="72" fill="${a}" opacity=".9"/>
    <circle cx="300" cy="330" r="72" fill="none" stroke="#ffffff" stroke-width="4" opacity=".6"/>
    <circle cx="278" cy="306" r="10" fill="#ffffff" opacity=".5"/>
    <circle cx="300" cy="282" r="9" fill="#fff" opacity=".35"/>
    <circle cx="300" cy="330" r="14" fill="#fff" opacity=".4"/>
  </g>`
  },
  leash(a) {
    return `
  <g>
    ${shadow(300, 360, 120)}
    <path d="M300 220 v120" stroke="#9aa1ab" stroke-width="16" stroke-linecap="round"/>
    <circle cx="300" cy="210" r="26" fill="none" stroke="${a}" stroke-width="14"/>
    <circle cx="300" cy="398" r="24" fill="${a}"/>
    <path d="M240 330 h120" stroke="#b9b2aa" stroke-width="10" stroke-linecap="round"/>
  </g>`
  },
  petBed(a) {
    return `
  <g>
    ${shadow(300, 428, 160)}
    <path d="M180 300 a120 90 0 0 240 0 l-20 -48 a110 80 0 0 0 -200 0 Z" fill="#f6efe8"/>
    <path d="M180 300 a120 90 0 0 240 0" fill="#ffd9c7" opacity=".6"/>
    <ellipse cx="300" cy="328" rx="86" ry="46" fill="#fff7f2"/>
  </g>`
  },
  bucket(a) {
    return `
  <g>
    ${shadow()}
    <path d="M300 240 l-18 40 v96 a40 26 0 0 0 36 0 v-96 Z" fill="none" stroke="#c9bfae" stroke-width="9"/>
    <path d="M258 300 h84" stroke="#c9bfae" stroke-width="22"/>
    <rect x="240" y="320" width="120" height="96" rx="14" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="240" y="320" width="120" height="86" fill="#f3efe9"/>
    <rect x="252" y="344" width="96" height="22" rx="6" fill="${a}" opacity=".8"/>
  </g>`
  },
  frisbee(a) {
    return `
  <g>
    ${shadow()}
    <ellipse cx="300" cy="330" rx="96" ry="30" fill="${a}"/>
    <ellipse cx="300" cy="322" rx="96" ry="30" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <ellipse cx="300" cy="322" rx="52" ry="14" fill="${a}" opacity=".35"/>
  </g>`
  },
  sniffMat(a) {
    return `
  <g>
    ${shadow()}
    <rect x="180" y="280" width="240" height="140" rx="24" fill="${a}" opacity=".9"/>
    <g fill="#ffffff" opacity=".7">
      <rect x="210" y="305" width="34" height="34" rx="8"/><rect x="270" y="305" width="34" height="34" rx="8"/><rect x="330" y="305" width="34" height="34" rx="8"/>
      <rect x="240" y="356" width="34" height="34" rx="8"/><rect x="300" y="356" width="34" height="34" rx="8"/><rect x="360" y="356" width="34" height="34" rx="8"/>
    </g>
  </g>`
  },
  bowl(a) {
    return `
  <g>
    ${shadow()}
    <path d="M240 300 v40 a60 26 0 0 0 120 0 v-40 Z" fill="#f3efe9"/>
    <rect x="240" y="282" width="120" height="26" rx="13" fill="${a}" opacity=".85"/>
    <rect x="220" y="180" width="160" height="110" rx="24" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <circle cx="300" cy="236" r="14" fill="#e8f5f1"/>
  </g>`
  },
  bone(a) {
    return `
  <g>
    ${shadow()}
    <rect x="212" y="306" width="176" height="58" rx="26" fill="#f3e3c6"/>
    <circle cx="212" cy="306" r="26" fill="#f3e3c6"/>
    <circle cx="212" cy="364" r="26" fill="#f3e3c6"/>
    <circle cx="388" cy="306" r="26" fill="#f3e3c6"/>
    <circle cx="388" cy="364" r="26" fill="#f3e3c6"/>
    <path d="M212 306 a26 26 0 0 1 40 0 m24 0 a26 26 0 0 1 48 0 m24 0 a26 26 0 0 1 -48 0" stroke="#e3cba0" stroke-width="6" fill="none"/>
  </g>`
  },
  catLitter(a) {
    return draw.bucket(a)
  },
  dropper(a) {
    return `
  <g>
    ${shadow()}
    <rect x="268" y="250" width="64" height="120" rx="16" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="268" y="302" width="64" height="26" rx="8" fill="${a}" opacity=".85"/>
    <path d="M300 250 v-26" stroke="#9aa1ab" stroke-width="8" stroke-linecap="round"/>
    <path d="M272 414 q28 26 56 0 l8 40 h-72 Z" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
  </g>`
  },
  stroller(a) {
    return `
  <g>
    ${shadow()}
    <rect x="170" y="250" width="150" height="96" rx="18" fill="#ffffff" stroke="#d8d2cb" stroke-width="3"/>
    <rect x="188" y="268" width="88" height="60" rx="12" fill="#eef2f6" stroke="#d8d2cb" stroke-width="3"/>
    <path d="M300 270 h56 v150" fill="none" stroke="#c9bfae" stroke-width="10"/>
    <circle cx="330" cy="450" r="34" fill="#8b8680"/>
    <circle cx="214" cy="450" r="26" fill="#8b8680"/>
    <path d="M196 292 h-30" stroke="${a}" stroke-width="12" stroke-linecap="round"/>
  </g>`
  },
  carSeat(a) {
    return `
  <g>
    ${shadow()}
    <rect x="150" y="300" width="300" height="110" rx="26" fill="#f6efe8"/>
    <rect x="150" y="300" width="300" height="60" rx="26" fill="${a}" opacity=".25"/>
    <path d="M180 340 l40 -26 h160 l40 26" stroke="#ffffff" stroke-width="10" fill="none"/>
  </g>`
  },
}

function variantFor(p) {
  const { cat, id, name } = p
  if (id === '1004') return draw.scratchBoard(p.brandColor)
  if (id === '1108') return draw.teaser(p.brandColor)
  if (id === '1110') return draw.ball(p.brandColor)
  if (id === '1111') return draw.leash(p.brandColor)
  if (id === '1112') return draw.petBed(p.brandColor)
  if (id === '1304') return draw.frisbee(p.brandColor)
  if (id === '1308') return draw.sniffMat(p.brandColor)
  if (id === '1105') return draw.bone(p.brandColor)
  if (id === '1107') return draw.bucket(p.brandColor)
  if (id === '1204') return draw.carSeat(p.brandColor)
  if (id === '1303') return draw.stroller(p.brandColor)
  if (name.includes('驱虫')) return draw.dropper(p.brandColor)
  if (name.includes('营养膏')) return draw.medTube(p.brandColor)
  if (name.includes('猫粮碗')) return draw.bowl(p.brandColor)
  if (cat === '主粮') return draw.foodBag(p.brandColor)
  if (cat === '零食') return draw.treatPouch(p.brandColor)
  if (cat === '清洁护理') return draw.bottle(p.brandColor)
  if (cat === '医疗保健') return draw.medTube(p.brandColor)
  if (cat === '智能设备') return draw.device(p.brandColor)
  if (cat === '服饰') return draw.garment(p.brandColor)
  if (cat === '出行') return draw.carrier(p.brandColor)
  if (cat === '玩具') return draw.ball(p.brandColor)
  return draw.treatPouch(p.brandColor)
}

for (const p of products) {
  const brandColor = (BRAND[p.brand] || { accent: '#5B8DEF' }).accent
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#f7f4f0"/>
      <stop offset="1" stop-color="#ebe5dc"/>
    </linearGradient>
  </defs>
  <rect width="600" height="600" fill="url(#bg)"/>
  ${variantFor({ ...p, brandColor })}
  ${label(p.name, p.brand)}
</svg>
`
  writeFileSync(path.join(OUT, `${p.id}.svg`), svg)
  console.log(`generated ${p.id}.svg  ${p.name}`)
}

// 通用占位图
const ph = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs><linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="#f7f4f0"/><stop offset="1" stop-color="#ebe5dc"/>
  </linearGradient></defs>
  <rect width="600" height="600" fill="url(#bg)"/>
  <path d="M300 220 l64 64 a44 44 0 0 1 -62 62 l-2 -2 -2 2 a44 44 0 0 1 -62 -62 Z" fill="#d8d2cb"/>
  <circle cx="300" cy="400" r="54" fill="#d8d2cb"/>
  <text x="300" y="520" text-anchor="middle" font-size="24" font-family="'PingFang SC','Microsoft YaHei',sans-serif" font-weight="600" fill="#9aa1ab">宠物商城</text>
</svg>
`
writeFileSync(path.join(OUT, 'placeholder.svg'), ph)
console.log('generated placeholder.svg')
console.log('DONE', products.length, 'products')