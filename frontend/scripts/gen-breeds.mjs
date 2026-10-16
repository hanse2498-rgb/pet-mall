// 生成宠物档案：封面 SVG + 字段/推荐 SQL（介绍/生活习性/外观颜色/饲养建议 + 手工推荐）
import { mkdirSync, writeFileSync } from 'node:fs'
import path from 'node:path'

const IMG_OUT = path.resolve('frontend/public/breeds')
const PATCH_OUT = path.resolve('backend/src/main/resources/db/patch_pet_breed_detail.sql')
const SEED_OUT = path.resolve('backend/src/main/resources/db/seed_pet_breed_detail.sql')
mkdirSync(IMG_OUT, { recursive: true })

// 每只：habits 生活习性 / appearance 外观与颜色 / care 饲养建议，rec 为 3 条推荐 [type, productId]
const breeds = [
  { id: 3001, type: 1, name: '英国短毛猫', en: 'British Shorthair', color: '#9aa7b5',
    intro: '圆脸厚毛、性情温和的"蓝胖子"，适合新手与家庭饲养。',
    habits: '性格沉稳安静，独立性强但亲人；日常运动量中等，喜欢安静地趴在主人身边。对环境适应力好，较少因搬家换环境而应激。',
    appearance: '体型圆胖结实，四肢粗短；被毛短而浓密、触感如绒；最经典为蓝灰色，另有银渐层、金渐层、乳色、三花等花色；眼睛大而圆，多为铜色或金绿色。',
    care: '易发胖，需控制喂食量与热量；短毛也要每周梳毛 2-3 次减少浮毛；注意心脏（心肌病）与牙齿健康，建议定期体检。',
    rec: [[1, 1001], [1, 1101], [2, 1102]] },
  { id: 3002, type: 1, name: '美国短毛猫', en: 'American Shorthair', color: '#b9c2c8',
    intro: '健壮活泼的虎斑猫，运动量大、体质好。',
    habits: '精力充沛、爱攀爬跳跃，喜欢互动游戏与追逐玩具；性格独立又友善，能自得其乐。',
    appearance: '肌肉发达、体态匀称；被毛短厚、质地偏硬；以银虎斑最经典，另有棕虎斑、纯白、黑白、三花等；眼睛多为金色或绿色。',
    care: '提供猫爬架与逗猫玩具消耗精力；易有肥胖倾向，注意运动与定量喂养；体质较好的品种，定期疫苗驱虫即可。',
    rec: [[1, 1001], [1, 1101], [2, 1004]] },
  { id: 3003, type: 1, name: '布偶猫', en: 'Ragdoll', color: '#e8d9c6',
    intro: '蓝眼睛、性格粘人的"仙女猫"，抱起来软乎乎。',
    habits: '性格极其温顺粘人，被抱起时会放松身体；喜欢跟随主人，适合室内饲养，社交需求高。',
    appearance: '体型大、被毛半长而蓬松；标志性蓝色眼睛；重点色（脸/耳/尾/四肢较深）为主，分双色、手套色、重点色等。',
    care: '需每天梳毛防打结；黏人不宜长时间独处；注意心脏（HCM）与肾脏健康，建议定期筛查。',
    rec: [[1, 1001], [1, 1101], [2, 1108]] },
  { id: 3004, type: 1, name: '暹罗猫', en: 'Siamese', color: '#d8c3a5',
    intro: '挖煤脸、话多粘人，重点色鼻祖。',
    habits: '聪明好动、极度粘人，喜欢"说话"（叫声响亮）；需要主人大量陪伴与互动，不适合长期独处。',
    appearance: '身型修长纤细、四肢细长；短毛紧贴；脸、耳、尾、四肢呈深色重点色（挖煤脸），眼睛为亮蓝色。',
    care: '多陪伴、多互动游戏；注意保暖（短毛怕冷）；留意牙齿与呼吸道问题，定期体检。',
    rec: [[1, 1001], [1, 1101], [2, 1103]] },
  { id: 3005, type: 1, name: '缅因猫', en: 'Maine Coon', color: '#a9743f',
    intro: '体型最大的家猫，像"温柔的巨人"。',
    habits: '性格温和友善，能与狗、孩子相处；喜欢跟随与互动，也爱玩水。活动空间需求较大。',
    appearance: '体格魁梧、尾巴蓬松似浣熊；有耳尖毛与颈部毛领；毛色丰富（棕虎斑、银、黑、白等）；眼睛大、多为绿色或金色。',
    care: '需要较大活动空间与结实的猫爬架；长毛需经常梳理；关注心脏（HCM）与髋关节健康。',
    rec: [[1, 1001], [1, 1101], [2, 1203]] },
  { id: 3006, type: 1, name: '波斯猫', en: 'Persian', color: '#efe7dd',
    intro: '长毛贵族，脸扁毛长，气质高雅。',
    habits: '性格安静慵懒，活动量小，喜欢独处休息；对环境变化较敏感，需安静稳定。',
    appearance: '被毛长而华丽、毛量丰厚；面部扁平、鼻梁短；毛色繁多（白、银、金、蓝、黑等）；眼睛大而圆。',
    care: '需每天梳毛防打结；扁脸易有泪痕与呼吸道问题，需每日清洁眼周、用浅口食盆；注意饮食与肾脏健康。',
    rec: [[1, 1001], [1, 1101], [2, 1109]] },
  { id: 3007, type: 1, name: '异国短毛猫（加菲）', en: 'Exotic Shorthair', color: '#e3a35c',
    intro: '短毛版波斯，圆脸扁嘴、憨态可掬。',
    habits: '性格温顺安静、亲人但不闹腾；活动量不大，喜欢待在主人身边，适合公寓饲养。',
    appearance: '圆脸扁鼻、表情"委屈"；短毛蓬松易打理；毛色与波斯类似（白、金、蓝、红等）；眼睛大而圆。',
    care: '短毛但需定期梳理；注意泪痕清洁与呼吸畅通，食盆用浅口；控制体重，定期体检。',
    rec: [[1, 1001], [1, 1101], [2, 1107]] },
  { id: 3008, type: 1, name: '中华狸花猫', en: 'Dragon Li', color: '#8a6b4a',
    intro: '本土田园猫，虎斑纹、独立强壮、好养活。',
    habits: '独立机敏、捕猎能力强、好奇心重；精力旺盛，喜欢攀爬探索，对陌生环境适应力好。',
    appearance: '体态匀称结实、肌肉利落；被毛短而贴身；典型黑褐虎斑纹（额"王"字、四肢环纹）；眼睛黄绿色。',
    care: '体质强健、抗病力较好；保证运动与磨爪需求即可；注意定期驱虫疫苗，控制体重。',
    rec: [[1, 1001], [1, 1101], [2, 1004]] },
  { id: 3009, type: 1, name: '橘猫', en: 'Orange Tabby', color: '#e8933f',
    intro: '"十个橘猫九个胖"，亲和能吃的大暖猫。',
    habits: '性格亲人和善、胆子偏大，食欲旺盛；爱粘人也爱撒娇，与小孩相处友好。',
    appearance: '毛色为橘黄虎斑（深色条纹），底色由浅橘到深橘不等；眼睛多为金色；体型多偏壮实。',
    care: '极易发胖，务必控制热量、定量喂食并增加运动；易有口腔与泌尿问题，注意饮水量。',
    rec: [[1, 1001], [1, 1101], [2, 1102]] },
  { id: 3010, type: 1, name: '斯芬克斯无毛猫', en: 'Sphynx', color: '#e6c3b3',
    intro: '无毛、皮肤皱褶，性格热情像小狗。',
    habits: '性格热情外向、极度粘人，喜欢依偎取暖；活动量大、好奇爱玩，几乎不会"高冷"。',
    appearance: '看似无毛实则皮肤覆有细绒；皮肤多皱褶、体温偏高；毛色体现在皮肤色（白、灰、花斑等）；耳朵大、眼睛大。',
    care: '需频繁清洁皮肤与耳道（易出油积垢）；不抗寒，天冷要穿衣保暖；注意心脏与皮肤健康。',
    rec: [[1, 1001], [1, 1101], [2, 1201]] },
  { id: 4001, type: 2, name: '金毛寻回犬', en: 'Golden Retriever', color: '#e0b25e',
    intro: '温柔耐心、适合家庭的"暖男犬"。',
    habits: '性格稳重友善、服从性高，喜欢陪伴与户外活动；对孩子和其他宠物都友好，乐于学习取悦主人。',
    appearance: '体型健壮匀称；被毛长而浓密、呈波浪状，色彩由奶油色到金黄色；尾巴蓬松，耳朵自然下垂。',
    care: '每天保证 1-2 小时运动；长毛需经常梳理、换毛季更频繁；注意髋关节与皮肤健康。',
    rec: [[1, 1002], [1, 1104], [2, 1111]] },
  { id: 4002, type: 2, name: '拉布拉多', en: 'Labrador Retriever', color: '#d8c38a',
    intro: '贪吃爱玩、聪明易训，导盲犬首选。',
    habits: '精力旺盛、爱玩水与衔取，学习能力强、非常听话；喜欢与人互动，需足量运动消耗精力。',
    appearance: '体格结实、胸宽；被毛短而密实、防水；毛色为黑、黄、巧克力三色；尾巴粗壮似"水獭尾"。',
    care: '极易发胖，务必控制食量；每天足量运动；注意髋关节与耳道清洁。',
    rec: [[1, 1002], [1, 1104], [2, 1112]] },
  { id: 4003, type: 2, name: '柯基', en: 'Welsh Corgi', color: '#e09a4e',
    intro: '小短腿大屁股，活泼爱叫的小型牧羊犬。',
    habits: '活泼机警、精力充沛、警惕性高爱叫；聪明好学，喜欢参与活动，需要适度运动与训练。',
    appearance: '身长腿短、耳朵直立；被毛中长、双层；常见橙白、黑三色、红白；眼睛圆亮，表情机灵。',
    care: '注意脊椎与关节：避免频繁上下楼梯和跳跃；控制体重；提供磨牙与互动玩具。',
    rec: [[1, 1002], [1, 1104], [2, 1304]] },
  { id: 4004, type: 2, name: '泰迪（贵宾犬）', en: 'Poodle', color: '#b07a4a',
    intro: '卷毛、少掉毛、聪明，城市饲养热门。',
    habits: '智商极高、易训练，粘人爱撒娇；精力好、喜欢互动，独处久了易分离焦虑吠叫。',
    appearance: '被毛卷曲浓密、几乎不掉毛；体型从玩具到标准不等；毛色有棕、白、黑、灰、香槟等。',
    care: '需定期修剪造型与梳毛（防打结）；多陪伴、训练缓解焦虑；注意泪痕与牙齿清洁。',
    rec: [[1, 1002], [1, 1104], [2, 1202]] },
  { id: 4005, type: 2, name: '哈士奇', en: 'Siberian Husky', color: '#9aa7b5',
    intro: '"二哈"：精力爆表、爱拆家的雪橇犬。',
    habits: '精力极其旺盛、好奇心强、爱挖爱拆；性格独立、喜群居，运动量与陪伴需求都很高。',
    appearance: '体格结实、四肢有力；被毛中长、双层厚实；毛色有黑白、灰白、红白等；眼睛常见蓝或褐，可有"异瞳"。',
    care: '每天大量运动（1-2 小时以上）否则易拆家；耐寒不耐热，注意防暑；换毛季掉毛量大需勤梳。',
    rec: [[1, 1002], [1, 1104], [2, 1111]] },
  { id: 4006, type: 2, name: '萨摩耶', en: 'Samoyed', color: '#eceff2',
    intro: '白色微笑天使，毛量惊人。',
    habits: '性格开朗友善、爱笑、喜欢与人亲近；精力充沛、爱户外活动，几乎不具攻击性。',
    appearance: '全身被毛纯白或奶白、蓬松厚实，有"微笑"表情与卷曲尾巴；体型中大型、结实匀称。',
    care: '掉毛量极大，需每天梳理；保证运动量；耐寒不耐热，夏季注意防暑降温。',
    rec: [[1, 1002], [1, 1104], [2, 1302]] },
  { id: 4007, type: 2, name: '柴犬', en: 'Shiba Inu', color: '#d99a5b',
    intro: '表情包担当，独立、爱干净、有点小倔强。',
    habits: '性格独立、领地意识与警戒心强、爱干净；不喜过度亲昵，有自己的小脾气，训练需耐心。',
    appearance: '体型中小、紧凑结实；耳朵直立、尾巴卷曲；毛色有赤（橘棕）、黑、胡麻等，常见"白口罩"；眼睛三角上扬。',
    care: '出门务必牵绳（警惕性强易爆冲）；掉毛量中等需定期梳毛；注意关节与皮肤健康。',
    rec: [[1, 1002], [1, 1104], [2, 1308]] },
  { id: 4008, type: 2, name: '边境牧羊犬', en: 'Border Collie', color: '#4a4f57',
    intro: '智商第一的工作犬，学习能力惊人。',
    habits: '聪明专注、精力旺盛、学习力极强，喜欢有挑战的任务与奔跑；精力无处消耗易出现行为问题。',
    appearance: '体型中等、匀称灵活；被毛中长、可黑白色或棕色等；眼睛多为深色，表情机敏。',
    care: '每天大量运动 + 训练/益智游戏；不适合长时间独处；注意关节与视力健康。',
    rec: [[1, 1002], [1, 1104], [2, 1304]] },
  { id: 4009, type: 2, name: '博美', en: 'Pomeranian', color: '#e8a94e',
    intro: '毛球小狮子，机警爱叫的小型犬。',
    habits: '性格活泼机警、警惕性高爱叫；亲人粘人，喜欢在主人身边，需从小做社会化训练。',
    appearance: '体型小巧、被毛蓬松呈"毛球"状；毛色有橙、棕、白、黑等；耳朵小直立，尾巴蓬松上翘。',
    care: '易吠叫，需训练与社会化；注意气管（提拉勿用项圈）与牙齿、泪痕清洁；定期修剪被毛。',
    rec: [[1, 1002], [1, 1104], [2, 1105]] },
  { id: 4010, type: 2, name: '比熊', en: 'Bichon Frise', color: '#eef1f4',
    intro: '棉花糖炸毛，性格温和、掉毛少。',
    habits: '性格开朗活泼、温和亲人，爱撒娇也爱玩；掉毛少、体味小，适合公寓饲养。',
    appearance: '被毛纯白、卷曲蓬松如棉花糖；体型小巧、圆润；眼睛与鼻子黑色，表情甜美。',
    care: '需定期修剪造型与每日梳毛；注意泪痕清洁（白毛易显）；控制体重、定期洗护。',
    rec: [[1, 1002], [1, 1104], [2, 1204]] },
]

const esc = (s) => String(s).replace(/'/g, "''")

// 结构化字段（体型/毛长/性格/原产地/寿命/难度）——供 init.sql 种子使用
const META = {
  3001: ['中大型', '短毛', '温顺安静', '英国', '12-17 年', 1],
  3002: ['中大型', '短毛', '活泼机敏', '美国', '15-20 年', 1],
  3003: ['大型', '长毛', '温顺粘人', '美国', '12-15 年', 2],
  3004: ['中型', '短毛', '话多粘人', '泰国', '12-20 年', 2],
  3005: ['超大型', '长毛', '温和友善', '美国', '10-15 年', 2],
  3006: ['中型', '长毛', '安静慵懒', '伊朗', '12-17 年', 3],
  3007: ['中型', '短毛', '温顺安静', '美国', '12-15 年', 2],
  3008: ['中型', '短毛', '独立机敏', '中国', '15-20 年', 1],
  3009: ['中大型', '短毛', '亲和贪吃', '各地', '15-20 年', 1],
  3010: ['中型', '无毛', '活泼亲人', '加拿大', '9-15 年', 3],
  4001: ['大型', '长毛', '温顺友善', '英国', '10-12 年', 2],
  4002: ['大型', '短毛', '活泼忠诚', '加拿大', '10-12 年', 2],
  4003: ['中小型', '中长毛', '活泼机警', '英国', '12-15 年', 2],
  4004: ['小型', '卷毛', '聪明粘人', '德国', '12-18 年', 2],
  4005: ['中大型', '中长毛', '活泼顽皮', '俄罗斯', '12-14 年', 3],
  4006: ['中大型', '长毛', '友好亲人', '俄罗斯', '12-14 年', 2],
  4007: ['中小型', '短毛', '独立机警', '日本', '12-15 年', 2],
  4008: ['中型', '中长毛', '聪明专注', '英国', '12-15 年', 3],
  4009: ['小型', '长毛', '活泼机警', '德国', '12-16 年', 2],
  4010: ['小型', '卷毛', '温和活泼', '法国', '14-15 年', 2],
}

function face(b) {
  const c = b.color
  if (b.type === 1) {
    return `
  <g>
    <path d="M196 210 L210 110 L286 178 Z" fill="${c}"/>
    <path d="M404 210 L390 110 L314 178 Z" fill="${c}"/>
    <path d="M212 194 L222 138 L272 182 Z" fill="#f7d9d9" opacity=".7"/>
    <path d="M388 194 L378 138 L328 182 Z" fill="#f7d9d9" opacity=".7"/>
    <ellipse cx="300" cy="300" rx="150" ry="140" fill="${c}"/>
    <ellipse cx="248" cy="286" rx="20" ry="26" fill="#fff"/>
    <ellipse cx="352" cy="286" rx="20" ry="26" fill="#fff"/>
    <circle cx="248" cy="290" r="10" fill="#2b2b2b"/>
    <circle cx="352" cy="290" r="10" fill="#2b2b2b"/>
    <path d="M300 336 l16 12 -16 12 -16 -12 Z" fill="#e5847f"/>
    <path d="M300 360 v20 m0 0 q-16 16 -34 6 M300 380 q16 16 34 6" fill="none" stroke="#8a6b4a" stroke-width="6" stroke-linecap="round"/>
    <path d="M150 320 h60 M150 344 h60 M450 320 h-60 M450 344 h-60" stroke="#fff" stroke-width="5" stroke-linecap="round" opacity=".85"/>
  </g>`
  }
  return `
  <g>
    <ellipse cx="176" cy="300" rx="46" ry="96" fill="${c}"/>
    <ellipse cx="424" cy="300" rx="46" ry="96" fill="${c}"/>
    <ellipse cx="300" cy="300" rx="150" ry="140" fill="${c}"/>
    <ellipse cx="256" cy="282" rx="19" ry="24" fill="#fff"/>
    <ellipse cx="344" cy="282" rx="19" ry="24" fill="#fff"/>
    <circle cx="256" cy="286" r="10" fill="#2b2b2b"/>
    <circle cx="344" cy="286" r="10" fill="#2b2b2b"/>
    <ellipse cx="300" cy="352" rx="66" ry="48" fill="#fff" opacity=".9"/>
    <ellipse cx="300" cy="330" rx="22" ry="15" fill="#2b2b2b"/>
    <path d="M300 345 v18 m0 0 q-18 16 -38 6 M300 363 q18 16 38 6" fill="none" stroke="#2b2b2b" stroke-width="6" stroke-linecap="round"/>
  </g>`
}

function svg(b) {
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#f7f4f0"/>
      <stop offset="1" stop-color="#ebe4da"/>
    </linearGradient>
  </defs>
  <rect width="600" height="600" fill="url(#bg)"/>
  <circle cx="300" cy="300" r="210" fill="#fff" opacity=".55"/>
  ${face(b)}
  <text x="300" y="520" text-anchor="middle" font-size="30" font-family="'PingFang SC','Microsoft YaHei',sans-serif" font-weight="700" fill="#3a3f45">${esc(b.name)}</text>
  <text x="300" y="552" text-anchor="middle" font-size="16" font-family="Segoe UI, sans-serif" fill="#9aa1ab">${esc(b.en)}</text>
</svg>
`
}

for (const b of breeds) {
  writeFileSync(path.join(IMG_OUT, `${b.id}.svg`), svg(b))
}

// ---------- SQL ----------
const recDdl = `CREATE TABLE IF NOT EXISTS \`tb_breed_recommend\` (
  \`id\`         BIGINT  NOT NULL COMMENT '主键',
  \`breed_id\`   BIGINT  NOT NULL COMMENT '品种id',
  \`product_id\` BIGINT  NOT NULL COMMENT '商品id',
  \`type\`       TINYINT NOT NULL DEFAULT 1 COMMENT '1主粮 2其他',
  \`sort\`       INT     NOT NULL DEFAULT 0,
  PRIMARY KEY (\`id\`),
  KEY \`idx_breed\` (\`breed_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品种推荐商品';`

const updates = breeds
  .map(
    (b) =>
      `UPDATE \`tb_pet_breed\` SET \`habits\`='${esc(b.habits)}', \`appearance\`='${esc(b.appearance)}', \`care_tips\`='${esc(b.care)}', \`update_time\`=NOW() WHERE \`id\`=${b.id};`,
  )
  .join('\n')

const recRows = breeds
  .flatMap((b) => b.rec.map((r, i) => `(${b.id * 10 + i + 1}, ${b.id}, ${r[1]}, ${r[0]}, ${i + 1})`))
  .join(',\n')

const recInsert = `INSERT INTO \`tb_breed_recommend\` (\`id\`, \`breed_id\`, \`product_id\`, \`type\`, \`sort\`) VALUES
${recRows};`

const patch = `-- 宠物档案二期：新增字段 + 品种推荐（在已有库执行）
ALTER TABLE \`tb_pet_breed\`
  ADD COLUMN \`habits\`     TEXT NULL COMMENT '生活习性',
  ADD COLUMN \`appearance\` TEXT NULL COMMENT '外观与颜色',
  ADD COLUMN \`care_tips\`  TEXT NULL COMMENT '饲养建议';

${recDdl}

${updates}

DELETE FROM \`tb_breed_recommend\`;
${recInsert}
`
writeFileSync(PATCH_OUT, patch)

const seed = `-- 宠物档案二期种子（供 init.sql 追加）
${recDdl}

${updates}

DELETE FROM \`tb_breed_recommend\`;
${recInsert}
`
writeFileSync(SEED_OUT, seed)

// ---------- init.sql 用的完整块（CREATE + INSERT + 推荐） ----------
const breedDdl = `CREATE TABLE IF NOT EXISTS \`tb_pet_breed\` (
  \`id\`         BIGINT       NOT NULL COMMENT '主键',
  \`type\`       TINYINT      NOT NULL COMMENT '1猫 2狗',
  \`name\`       VARCHAR(64)  NOT NULL COMMENT '品种名',
  \`en_name\`    VARCHAR(64)  DEFAULT NULL COMMENT '英文名',
  \`cover_img\`  VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  \`intro\`      VARCHAR(255) DEFAULT NULL COMMENT '一句话简介',
  \`description\` TEXT        DEFAULT NULL COMMENT '详细介绍',
  \`habits\`     TEXT         DEFAULT NULL COMMENT '生活习性',
  \`appearance\` TEXT         DEFAULT NULL COMMENT '外观与颜色',
  \`care_tips\`  TEXT         DEFAULT NULL COMMENT '饲养建议',
  \`size\`       VARCHAR(32)  DEFAULT NULL COMMENT '体型',
  \`hair\`       VARCHAR(32)  DEFAULT NULL COMMENT '毛长',
  \`character_trait\` VARCHAR(64) DEFAULT NULL COMMENT '性格',
  \`origin\`     VARCHAR(64)  DEFAULT NULL COMMENT '原产地',
  \`life_span\`  VARCHAR(32)  DEFAULT NULL COMMENT '寿命',
  \`difficulty\` TINYINT      NOT NULL DEFAULT 1 COMMENT '饲养难度1-3',
  \`sort\`       INT          NOT NULL DEFAULT 0,
  \`status\`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
  \`create_time\` DATETIME    NOT NULL,
  \`update_time\` DATETIME    DEFAULT NULL,
  \`deleted\`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (\`id\`),
  KEY \`idx_type\` (\`type\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物品种图鉴';`

const breedRows = breeds
  .map((b) => {
    const m = META[b.id] || ['', '', '', '', '', 1]
    return `(${b.id}, ${b.type}, '${esc(b.name)}', '${esc(b.en)}', '/breeds/${b.id}.svg', '${esc(b.intro)}', '', '${esc(
      b.habits,
    )}', '${esc(b.appearance)}', '${esc(b.care)}', '${esc(m[0])}', '${esc(m[1])}', '${esc(m[2])}', '${esc(
      m[3],
    )}', '${esc(m[4])}', ${m[5]}, ${b.id % 100}, 1, NOW())`
  })
  .join(',\n')

// 说明：品种的结构化字段（体型/毛长/性格/原产地/寿命）由上层 gen 脚本的 BREED_META 决定，
// 这里从 breeds 扩展数据读取（见文件顶部 extra 字段）。为保持简洁，直接内联。
const initBlock = `-- ---------- 宠物档案：犬猫品种图鉴 ----------

${breedDdl}

INSERT INTO \`tb_pet_breed\`
  (\`id\`, \`type\`, \`name\`, \`en_name\`, \`cover_img\`, \`intro\`, \`description\`, \`habits\`, \`appearance\`, \`care_tips\`,
   \`size\`, \`hair\`, \`character_trait\`, \`origin\`, \`life_span\`, \`difficulty\`, \`sort\`, \`status\`, \`create_time\`) VALUES
${breedRows};

${recDdl}

${recInsert}
`
writeFileSync(path.resolve('backend/src/main/resources/db/init_pet_breed.sql'), initBlock)

console.log('breeds:', breeds.length, '| recommends:', breeds.length * 3)
console.log('IMG  ->', IMG_OUT)
console.log('PATCH->', PATCH_OUT)
console.log('INIT -> backend/src/main/resources/db/init_pet_breed.sql')
