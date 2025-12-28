import { describe, test, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ExpensesList from '../components/ExpensesList.vue'

describe('ExpensesList.vue', () => {
    test('показывает сообщение, если список пуст', () => {
        const wrapper = mount(ExpensesList, {
          props: { expenses: [] }
        })
      
        expect(wrapper.text()).toContain('Нет расходов')
      })
      
})
