import { describe, test, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../App.vue'
import { vi } from 'vitest'

vi.spyOn(console, 'error').mockImplementation(() => {})
vi.spyOn(console, 'warn').mockImplementation(() => {})

describe('App.vue', () => {
  test('приложение рендерится', () => {
    const wrapper = mount(App)
    expect(wrapper.exists()).toBe(true)
  })
})
