<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  type: { type: String, default: 'terms' },
})

const emit = defineEmits(['close', 'accept', 'agree'])

const scroller = ref(null)
const reachedBottom = ref(false)

const modalCopy = computed(() => {
  if (props.type === 'privacy') {
    return {
      title: '개인정보 수집 및 이용 동의',
      sections: [
        ['수집 항목', '이메일, 닉네임, 비밀번호, 구독 등록 정보, 결제 캘린더 이용 기록을 수집합니다.'],
        ['이용 목적', '회원 식별, 서비스 제공, 구독 관리 기능 운영, 고객 문의 대응, 서비스 품질 개선에 활용합니다.'],
        ['보관 기간', '회원 탈퇴 또는 개인정보 삭제 요청 시 지체 없이 파기합니다. 법령상 보관이 필요한 경우 해당 기간 동안만 별도 보관합니다.'],
        ['이용자 권리', '이용자는 언제든지 개인정보 조회, 수정, 삭제를 요청할 수 있으며, 동의 철회를 통해 계정 삭제를 요청할 수 있습니다.'],
        ['안전 조치', '계정 정보 보호를 위해 접근 제한, 권한 관리, 저장 정보 분리 등 기본적인 보호 조치를 적용합니다.'],
      ],
    }
  }

  return {
    title: '이용약관 동의',
    sections: [
      ['서비스 내용', 'Subees는 구독 등록, 결제 일정 확인, 결제 캘린더 조회, 커뮤니티 이용 기능을 제공합니다.'],
      ['이용자 책임', '이용자는 정확한 계정 정보를 입력해야 하며, 타인의 권리를 침해하거나 서비스 운영을 방해하는 행위를 해서는 안 됩니다.'],
      ['계정 관리', '아이디와 비밀번호 관리 책임은 이용자에게 있으며, 제3자에게 계정을 양도하거나 공유할 수 없습니다.'],
      ['콘텐츠 이용', '커뮤니티에 작성한 게시글은 서비스 내 노출될 수 있으며, 운영 정책에 따라 제한 또는 삭제될 수 있습니다.'],
      ['서비스 변경', '서비스 운영상 필요한 경우 기능, 화면, 운영 정책은 사전 안내 후 변경될 수 있습니다.'],
      ['약관 동의 철회', '회원 탈퇴 시 서비스 이용 계약은 종료되며, 관련 법령에 따라 필요한 정보만 별도로 보관됩니다.'],
    ],
  }
})

const handleScroll = () => {
  const element = scroller.value
  if (!element) return
  const threshold = 16
  reachedBottom.value = element.scrollTop + element.clientHeight >= element.scrollHeight - threshold
}

const close = () => emit('close')
const accept = () => {
  if (!reachedBottom.value) return
  emit('accept', props.type)
  emit('agree', props.type)
}

watch(
  () => props.open,
  async (value) => {
    if (!value) return
    reachedBottom.value = false
    await nextTick()
    if (scroller.value) {
      scroller.value.scrollTop = 0
      handleScroll()
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-[100] flex items-center justify-center bg-[rgba(28,22,11,0.44)] px-4 py-6 backdrop-blur-sm" @click.self="close">
      <section class="flex h-[min(78vh,760px)] w-full max-w-[720px] flex-col overflow-hidden rounded-[30px] border border-[rgba(46,34,10,0.08)] bg-[rgba(255,253,249,0.98)] shadow-floating">
        <header class="flex items-center justify-between gap-4 border-b border-[rgba(46,34,10,0.08)] px-6 py-5">
          <div>
            <p class="text-[11px] font-extrabold uppercase tracking-[0.14em] text-neutral-300">Agreement</p>
            <h2 class="mt-2 text-[24px] font-bold tracking-[-0.03em] text-neutral-900">{{ modalCopy.title }}</h2>
          </div>
          <button type="button" class="tertiary-button !min-h-10 !px-3" @click="close">닫기</button>
        </header>

        <div ref="scroller" class="min-h-0 flex-1 overflow-y-auto px-6 py-5" @scroll="handleScroll">
          <article class="rounded-[22px] border border-[rgba(46,34,10,0.08)] bg-brand-50 px-5 py-5">
            <div class="space-y-5">
              <section
                v-for="(section, index) in modalCopy.sections"
                :key="section[0]"
                class="pb-5"
                :class="index !== modalCopy.sections.length - 1 ? 'border-b border-[rgba(46,34,10,0.08)]' : 'pb-0'"
              >
                <h3 class="text-sm font-bold text-neutral-900">{{ section[0] }}</h3>
                <p class="mt-2 text-sm leading-7 text-neutral-600">{{ section[1] }}</p>
              </section>
            </div>
          </article>
        </div>

        <footer class="flex flex-col gap-3 border-t border-[rgba(46,34,10,0.08)] px-6 py-5">
          <p class="text-xs text-neutral-500">내용을 끝까지 확인하면 동의 버튼이 활성화됩니다.</p>
          <div class="flex items-center justify-end gap-2.5">
            <button type="button" class="secondary-button !min-h-11 !px-4" @click="close">취소</button>
            <button type="button" class="primary-button !min-h-11 !px-4" :class="!reachedBottom ? 'cursor-not-allowed opacity-50' : ''" :disabled="!reachedBottom" @click="accept">동의하기</button>
          </div>
        </footer>
      </section>
    </div>
  </Teleport>
</template>
