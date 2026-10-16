-- 宠物档案：犬猫品种图鉴
CREATE TABLE IF NOT EXISTS `tb_pet_breed` (
  `id`         BIGINT       NOT NULL COMMENT '主键',
  `type`       TINYINT      NOT NULL COMMENT '1猫 2狗',
  `name`       VARCHAR(64)  NOT NULL COMMENT '品种名',
  `en_name`    VARCHAR(64)  DEFAULT NULL COMMENT '英文名',
  `cover_img`  VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `intro`      VARCHAR(255) DEFAULT NULL COMMENT '一句话简介',
  `description` TEXT        DEFAULT NULL COMMENT '详细介绍',
  `size`       VARCHAR(32)  DEFAULT NULL COMMENT '体型',
  `hair`       VARCHAR(32)  DEFAULT NULL COMMENT '毛长',
  `character_trait` VARCHAR(64) DEFAULT NULL COMMENT '性格',
  `origin`     VARCHAR(64)  DEFAULT NULL COMMENT '原产地',
  `life_span`  VARCHAR(32)  DEFAULT NULL COMMENT '寿命',
  `difficulty` TINYINT      NOT NULL DEFAULT 1 COMMENT '饲养难度1-3',
  `sort`       INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
  `create_time` DATETIME    NOT NULL,
  `update_time` DATETIME    DEFAULT NULL,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物品种图鉴';

INSERT INTO `tb_pet_breed`
  (`id`, `type`, `name`, `en_name`, `cover_img`, `intro`, `description`, `size`, `hair`, `character_trait`, `origin`, `life_span`, `difficulty`, `sort`, `status`, `create_time`) VALUES
(3001, 1, '英国短毛猫', 'British Shorthair', '/breeds/3001.svg', '圆脸厚毛、性情温和的"蓝胖子"，适合新手与家庭饲养。', '英国短毛猫体型圆胖结实，被毛短而浓密，触感如绒。性格沉稳、亲人但不过分粘人，对环境适应力强。日常需注意控制体重与定期梳毛。', '中大型', '短毛', '温顺安静', '英国', '12-17 年', 1, 1, 1, NOW()),
(3002, 1, '美国短毛猫', 'American Shorthair', '/breeds/3002.svg', '健壮活泼的虎斑猫，运动量大、体质好。', '美国短毛猫肌肉发达、适应力强，经典的银虎斑纹辨识度高。精力充沛，喜欢攀爬与互动游戏，适合有活动空间的家庭。', '中大型', '短毛', '活泼机敏', '美国', '15-20 年', 1, 2, 1, NOW()),
(3003, 1, '布偶猫', 'Ragdoll', '/breeds/3003.svg', '蓝眼睛、性格粘人的"仙女猫"，抱起来软乎乎。', '布偶猫体型大、被毛半长，标志性的蓝色眼睛与重点色花纹。性格极其温顺，被抱起时会放松，需坚持梳毛并注意心脏健康。', '大型', '长毛', '温顺粘人', '美国', '12-15 年', 2, 3, 1, NOW()),
(3004, 1, '暹罗猫', 'Siamese', '/breeds/3004.svg', '挖煤脸、话多粘人，重点色鼻祖。', '暹罗猫身型修长、四肢纤细，脸、耳、尾呈深色重点色。聪明好动、叫声响亮，非常依赖主人陪伴。', '中型', '短毛', '话多粘人', '泰国', '12-20 年', 2, 4, 1, NOW()),
(3005, 1, '缅因猫', 'Maine Coon', '/breeds/3005.svg', '体型最大的家猫，像"温柔的巨人"。', '缅因猫体格魁梧、尾巴蓬松，有耳尖毛与毛领。性格温和，能与狗和孩子相处，需较大空间与充足梳毛。', '超大型', '长毛', '温和友善', '美国', '10-15 年', 2, 5, 1, NOW()),
(3006, 1, '波斯猫', 'Persian', '/breeds/3006.svg', '长毛贵族，脸扁毛长，气质高雅。', '波斯猫被毛长而华丽，面部扁平。性格安静，需要每天梳毛并注意眼部与呼吸道护理。', '中型', '长毛', '安静慵懒', '伊朗', '12-17 年', 3, 6, 1, NOW()),
(3007, 1, '异国短毛猫（加菲）', 'Exotic Shorthair', '/breeds/3007.svg', '短毛版波斯，圆脸扁嘴、憨态可掬。', '加菲猫继承了波斯的圆脸与安静性格，但被毛好打理。需注意泪痕与呼吸问题，饮食器具建议用浅口。', '中型', '短毛', '温顺安静', '美国', '12-15 年', 2, 7, 1, NOW()),
(3008, 1, '中华狸花猫', 'Dragon Li', '/breeds/3008.svg', '本土田园猫，虎斑纹、独立强壮、好养活。', '狸花猫肌肉利落、反应敏捷，毛色为黑褐虎斑。体质强健、捕猎能力强，是最好养的本土品种之一。', '中型', '短毛', '独立机敏', '中国', '15-20 年', 1, 8, 1, NOW()),
(3009, 1, '橘猫', 'Orange Tabby', '/breeds/3009.svg', '"十个橘猫九个胖"，亲和能吃的大暖猫。', '橘猫是毛色称呼而非单一品种，多为狸花或美短底子。性格亲人和善、食欲旺盛，注意控制热量避免肥胖。', '中大型', '短毛', '亲和贪吃', '各地', '15-20 年', 1, 9, 1, NOW()),
(3010, 1, '斯芬克斯无毛猫', 'Sphynx', '/breeds/3010.svg', '无毛、皮肤皱褶，性格热情像小狗。', '无毛猫并非完全无毛，皮肤有细绒。体温高、需频繁清洁皮肤与耳道，并注意保暖，性格极度亲人。', '中型', '无毛', '活泼亲人', '加拿大', '9-15 年', 3, 10, 1, NOW()),
(4001, 2, '金毛寻回犬', 'Golden Retriever', '/breeds/4001.svg', '温柔耐心、适合家庭的"暖男犬"。', '金毛性格稳重、服从性高，对孩子友好，适合新手家庭。需每天运动并定期梳毛，注意髋关节健康。', '大型', '长毛', '温顺友善', '英国', '10-12 年', 2, 1, 1, NOW()),
(4002, 2, '拉布拉多', 'Labrador Retriever', '/breeds/4002.svg', '贪吃爱玩、聪明易训，导盲犬首选。', '拉布拉多精力旺盛、学习能力强，是常见的工作犬。容易发胖，需控制饮食并保证足量运动。', '大型', '短毛', '活泼忠诚', '加拿大', '10-12 年', 2, 2, 1, NOW()),
(4003, 2, '柯基', 'Welsh Corgi', '/breeds/4003.svg', '小短腿大屁股，活泼爱叫的小型牧羊犬。', '柯基腿短身长、精力充沛，爱叫且聪明。需注意脊椎与关节，避免频繁上下楼梯和跳跃。', '中小型', '中长毛', '活泼机警', '英国', '12-15 年', 2, 3, 1, NOW()),
(4004, 2, '泰迪（贵宾犬）', 'Poodle', '/breeds/4004.svg', '卷毛、少掉毛、聪明，城市饲养热门。', '贵宾犬智商极高、几乎不掉毛，需定期修剪造型。粘人爱撒娇，容易因分离焦虑而吠叫。', '小型', '卷毛', '聪明粘人', '德国', '12-18 年', 2, 4, 1, NOW()),
(4005, 2, '哈士奇', 'Siberian Husky', '/breeds/4005.svg', '"二哈"：精力爆表、爱拆家的雪橇犬。', '哈士奇耐寒、爱运动、好奇心强，需要大量运动与陪伴，否则容易拆家。适合有时间的活跃主人。', '中大型', '中长毛', '活泼顽皮', '俄罗斯', '12-14 年', 3, 5, 1, NOW()),
(4006, 2, '萨摩耶', 'Samoyed', '/breeds/4006.svg', '白色微笑天使，毛量惊人。', '萨摩耶性格开朗、爱笑，被毛浓密需频繁梳理。适合喜欢户外活动、能接受大量掉毛的家庭。', '中大型', '长毛', '友好亲人', '俄罗斯', '12-14 年', 2, 6, 1, NOW()),
(4007, 2, '柴犬', 'Shiba Inu', '/breeds/4007.svg', '表情包担当，独立、爱干净、有点小倔强。', '柴犬性格独立、领地意识强，爱干净且体味小。训练需耐心，遛弯务必牵绳以防爆冲追击。', '中小型', '短毛', '独立机警', '日本', '12-15 年', 2, 7, 1, NOW()),
(4008, 2, '边境牧羊犬', 'Border Collie', '/breeds/4008.svg', '智商第一的工作犬，学习能力惊人。', '边牧聪明、精力旺盛，需要大量运动与训练，适合能投入时间的铲屎官。运动不足易出现行为问题。', '中型', '中长毛', '聪明专注', '英国', '12-15 年', 3, 8, 1, NOW()),
(4009, 2, '博美', 'Pomeranian', '/breeds/4009.svg', '毛球小狮子，机警爱叫的小型犬。', '博美体型小巧、被毛蓬松，警惕性高、爱叫。需从小进行社会化训练，并注意气管与牙齿健康。', '小型', '长毛', '活泼机警', '德国', '12-16 年', 2, 9, 1, NOW()),
(4010, 2, '比熊', 'Bichon Frise', '/breeds/4010.svg', '棉花糖炸毛，性格温和、掉毛少。', '比熊犬性格开朗、亲人，毛色纯白且掉毛少，需定期修剪与清洁泪痕，适合公寓饲养。', '小型', '卷毛', '温和活泼', '法国', '14-15 年', 2, 10, 1, NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `intro`=VALUES(`intro`), `description`=VALUES(`description`), `cover_img`=VALUES(`cover_img`), `update_time`=NOW();
