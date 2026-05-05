<script setup>
defineProps({
  label: { type: String, required: true },
  modelValue: { type: [String, Number], default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  wrapperClass: { type: String, default: '' },
  inputClass: { type: String, default: '' },
  labelClass: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])
</script>

<template>
  <label class="grid gap-1.5" :class="wrapperClass">
    <span class="form-label" :class="labelClass">{{ label }}</span>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      class="form-input"
      :class="[inputClass, error ? 'border-danger bg-red-50/60 focus:border-danger focus:ring-red-100' : '']"
      :disabled="disabled"
      @input="emit('update:modelValue', $event.target.value)"
    />
    <span v-if="error" class="text-xs font-medium text-danger">{{ error }}</span>
    <span v-else-if="hint" class="text-xs text-neutral-500">{{ hint }}</span>
  </label>
</template>
