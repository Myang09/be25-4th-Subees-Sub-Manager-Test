const normalize = (value = '') =>
  String(value)
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '')
    .replace(/[+._-]/g, '')

const defineMap = (entries) => {
  const map = {}
  for (const [keys, path] of entries) {
    for (const key of keys) {
      const normalized = normalize(key)
      if (normalized) map[normalized] = path
    }
  }
  return map
}

const buildStemMap = (paths) =>
  defineMap(
    paths.map((path) => {
      const fileName = path.split('/').pop() || ''
      const stem = fileName.replace(/\.[^.]+$/, '')
      return [[stem], path]
    })
  )

const isExplicitPath = (value = '') => {
  const trimmed = String(value).trim()
  return trimmed.startsWith('/') || trimmed.startsWith('./') || /^https?:\/\//i.test(trimmed)
}

const CATEGORY_MAP = defineMap([
  [['ott', 'OTT', '오티티'], '/image/Category/ott.png'],
  [['music', '음악', '뮤직'], '/image/Category/music.png'],
  [['ai', 'AI'], '/image/Category/ai.png'],
  [['cloud', 'Cloud', '클라우드'], '/image/Category/cloud.png'],
  [['etc', 'other', 'others', '기타', '그외', '그 외'], '/image/Category/other.png'],
])

const SERVICE_ALIASES = [
  [['넷플릭스', 'Netflix', 'Netflix Premium', 'NetflixPremium'], '/image/ott/netflix.png'],
  [['티빙', 'Tving'], '/image/ott/tving.png'],
  [['왓챠', 'Watcha'], '/image/ott/Watcha.png'],
  [['웨이브', 'Wavve', 'Wave'], '/image/ott/wave.png'],
  [['디즈니+', '디즈니플러스', 'Disney+', 'Disney Plus', 'DisneyPlus'], '/image/ott/disney.png'],
  [['애플TV+', '애플 TV+', 'Apple TV+', 'Apple TV Plus', 'AppleTV+', 'AppleTVPlus', 'AppleTv'], '/image/ott/apple_tv.png'],
  [['쿠팡플레이', 'Coupang Play', 'CoupangPlay'], '/image/ott/coupangPlay.png'],
  [['쿠팡TV', '쿠팡 티비', 'Coupang TV', 'CoupangTV'], '/image/ott/coupang_tv.png'],
  [['라프텔', 'Laftel'], '/image/ott/laftel.png'],

  [['스포티파이', 'Spotify', 'Sportify', 'Spotify Premium', 'SpotifyPremium', 'Sportify Premium', 'SportifyPremium'], '/image/music/Spotify.png'],
  [['멜론', 'Melon', 'Melon Family', 'MelonFamily'], '/image/music/melon.png'],
  [['지니', 'Genie'], '/image/music/genie.png'],
  [['바이브', 'Vibe', 'Vive'], '/image/music/vive.png'],
  [['애플뮤직', 'Apple Music', 'AppleMusic'], '/image/music/applemusic.png'],
  [['플로', 'FLO'], '/image/music/flo.png'],
  [['유튜브 뮤직', '유튜브뮤직', 'YouTube Music', 'Youtube Music', 'YouTubeMusic', 'YoutubeMusic'], '/image/music/youtube_music.png'],

  [['ChatGPT', 'Chat GPT', 'ChatGPT Plus', 'ChatGPTPlus', 'ChatGpt'], '/image/ai/chatgpt.png'],
  [['Gemini', 'Gemini Advanced', 'GeminiAdvanced'], '/image/ai/gemini.png'],
  [['Claude', 'Claude Pro', 'ClaudePro'], '/image/ai/claude.png'],

  [['어도비', 'Adobe', 'Adobe CC', 'Adobe Creative Cloud', 'AdobeCreativeCloud'], '/image/another/adobe.png'],
  [['카카오 톡서랍', '카카오톡서랍', '톡서랍', 'surrap'], '/image/another/surrap.png'],
  [['구글 드라이브', '구글드라이브', 'Google Drive', 'GoogleDrive'], '/image/another/google_drive.png'],
  [['iCloud+', 'iCloud Plus', 'iCloudPlus', 'ICLOUD+', 'ICLOUDPLUS'], '/image/another/icloud.png'],
  [['이모티콘 플러스', '이모티콘플러스', '카카오 이모티콘+', '카카오이모티콘+', '카카오 이모티콘 플러스', '카카오이모티콘플러스', 'iemo', '이모티콘+', '카카오이모티콘'], '/image/another/iemo.png'],
  [['배민클럽', 'Baemin Club', 'BaeminClub'], '/image/another/baeminclub.png'],
  [['인텔리제이', 'IntelliJ', 'IntelliJ IDEA', 'IntelliJIDEA'], '/image/another/intellij.png'],
  [['컬리', '컬리멤버스', 'Kurly', 'Kurly Membership', 'KurlyMembership'], '/image/another/kurly.png'],
  [['마이크로소프트 오피스', '오피스 365', '오피스365', 'Microsoft Office', 'MS Office', 'MSOffice', 'Microsoft 365', 'Microsoft365', 'Microsoft 365 Personal', 'Microsoft365Personal', 'MS 365', 'MS365'], '/image/another/ms_office.png'],
  [['노션', 'Notion'], '/image/another/notion.png'],
  [['네이버멤버십', '네이버 멤버십', '네이버멤버쉽', '네이버맵버십', '네이버맵버쉽', '네이버플러스', '네이버 플러스', '네이버플러스멤버십', '네이버 플러스 멤버십', 'Npay+', 'N Pay+', 'Npay Plus', 'NpayPlus', '네이버페이 플러스', '네이버페이플러스'], '/image/another/npay+.png'],
  [['쿠팡와우', 'Coupang Wow', 'CoupangWow', 'wow'], '/image/another/wow.png'],
  [['유튜브 프리미엄', '유튜브프리미엄', 'YouTube Premium', 'Youtube Premium', 'YouTubePremium', 'YoutubePremium'], '/image/another/youtube_premium.png'],
]

const SERVICE_PUBLIC_IMAGE_PATHS = [
  '/image/ai/chatgpt.png',
  '/image/ai/claude.png',
  '/image/ai/gemini.png',
  '/image/another/adobe.png',
  '/image/another/baeminclub.png',
  '/image/another/google_drive.png',
  '/image/another/icloud.png',
  '/image/another/iemo.png',
  '/image/another/intellij.png',
  '/image/another/kurly.png',
  '/image/another/ms_office.png',
  '/image/another/notion.png',
  '/image/another/npay+.png',
  '/image/another/surrap.png',
  '/image/another/wow.png',
  '/image/another/youtube_premium.png',
  '/image/music/Spotify.png',
  '/image/music/applemusic.png',
  '/image/music/flo.png',
  '/image/music/genie.png',
  '/image/music/melon.png',
  '/image/music/vive.png',
  '/image/music/youtube_music.png',
  '/image/ott/Watcha.png',
  '/image/ott/apple_tv.png',
  '/image/ott/coupangPlay.png',
  '/image/ott/coupang_tv.png',
  '/image/ott/disney.png',
  '/image/ott/laftel.png',
  '/image/ott/netflix.png',
  '/image/ott/tving.png',
  '/image/ott/wave.png',
]

const SERVICE_MAP = defineMap(SERVICE_ALIASES)
const SERVICE_FILE_STEM_MAP = buildStemMap(SERVICE_PUBLIC_IMAGE_PATHS)

const CARD_MAP = defineMap([
  [['현대카드', 'Hyundai Card', 'HyundaiCard'], '/image/card/hun.png'],
  [['KB국민카드', 'KB 국민카드', '국민카드', 'KB Card', 'KB', 'KBCard'], '/image/card/kb.png'],
  [['롯데카드', 'Lotte Card', 'LotteCard'], '/image/card/lot.png'],
  [['NH농협카드', 'NH 농협카드', '농협카드', 'NH', 'NHCard'], '/image/card/nh.png'],
  [['삼성카드', 'Samsung Card', 'SamsungCard'], '/image/card/sam.png'],
  [['신한카드', 'Shinhan Card', 'ShinhanCard'], '/image/card/sin.png'],
  [['우리카드', 'Woori Card', 'WooriCard', 'Uri'], '/image/card/u.png'],
  [['하나카드', '하나 카드', 'Hana Card', 'HanaCard'], null],
  [['토스카드', '토스뱅크 카드', '토스뱅크카드', 'Toss Card', 'TossBank Card', 'TossBankCard'], null],
])

const CORE_MAP = defineMap([
  [['registration', '구독 등록', '구독등록', '구독 추가', '구독추가'], '/image/core_images/registration.png'],
  [['alarm', '알림', '결제일 알림', '결제알림'], '/image/core_images/alarm.png'],
  [['calendar', 'calender', '결제 캘린더', '캘린더'], '/image/core_images/calender.png'],
  [['analyze', 'analysis', '지출 분석', '지출분석', '소비 분석', '대시보드', '카테고리 분류', '카테고리분류', '카테고리 분석', '카테고리분석'], '/image/core_images/analyze.png'],
  [['reduction', '절감', '절감 제안', '절감제안', '절감 포인트', '절감포인트', '절약 제안', '절약제안'], '/image/core_images/reduction.png'],
  [['inventory', 'list', '구독목록', '구독 목록', 'faq', '전체 기능', '커뮤니티', 'FAQ'], '/image/core_images/inventory.png'],
])

const MENU_MAP = defineMap([
  [['대시보드'], '/image/core_images/analyze.png'],
  [['구독목록'], '/image/core_images/inventory.png'],
  [['구독추가'], '/image/core_images/registration.png'],
  [['결제 캘린더'], '/image/core_images/calender.png'],
  [['지출분석'], '/image/core_images/analyze.png'],
  [['커뮤니티'], '/image/core_images/inventory.png'],
  [['FAQ'], '/image/core_images/inventory.png'],
  [['마이페이지'], '/image/core_images/registration.png'],
])

const getFrom = (map, value) => {
  const normalized = normalize(value)
  if (!normalized) return null
  return map[normalized] || null
}

const getServiceImage = (value) => {
  if (isExplicitPath(value)) return String(value).trim()
  return getFrom(SERVICE_MAP, value) || getFrom(SERVICE_FILE_STEM_MAP, value) || null
}

export const resolveAssetImage = (type, value) => {
  if (!value && !type) return null
  if (type === 'category') return getFrom(CATEGORY_MAP, value)
  if (type === 'service') return getServiceImage(value)
  if (type === 'card') return getFrom(CARD_MAP, value)
  if (type === 'core') return getFrom(CORE_MAP, value)
  if (type === 'menu') return getFrom(MENU_MAP, value)
  return null
}

export const categoryImage = (value) => getFrom(CATEGORY_MAP, value)
export const serviceImage = (value) => getServiceImage(value)
export const cardImage = (value) => getFrom(CARD_MAP, value)
export const coreImage = (value) => getFrom(CORE_MAP, value)
export const menuImage = (value) => getFrom(MENU_MAP, value)

export const cardBadge = (value) => {
  const key = normalize(value)
  if (!key) return null
  if (key.includes('토스')) return { text: 'toss', bg: '#0064FF', color: '#FFFFFF' }
  if (key.includes('하나')) return { text: 'hana', bg: '#009178', color: '#FFFFFF' }
  if (key.includes('카카오')) return { text: 'k', bg: '#FFDD00', color: '#2B2B2B' }
  return null
}

export const assetCatalog = {
  categories: Object.values(CATEGORY_MAP),
  services: [...new Set([...Object.values(SERVICE_MAP), ...Object.values(SERVICE_FILE_STEM_MAP)])],
  cards: Object.values(CARD_MAP),
  core: Object.values(CORE_MAP),
}