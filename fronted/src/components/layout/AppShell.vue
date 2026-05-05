<script setup>
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'

defineProps({
  title: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="min-h-screen">
    <div class="app-frame min-h-screen items-start">
      <AppSidebar />

      <div class="shell-main flex min-h-screen flex-col">
        <AppHeader v-if="title !== 'hidden'" />
        <main class="page-stack flex-1 pb-6">
          <slot />
        </main>
        <AppFooter />
      </div>
    </div>
  </div>
</template>
