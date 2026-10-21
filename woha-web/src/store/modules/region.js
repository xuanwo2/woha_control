import { defineStore } from 'pinia'
import cache from '@/plugins/cache'

/** 区域选择缓存键。 */
const REGION_CACHE_KEY = 'bi-region-id'

/** Region 枚举默认区域：东南亚。 */
const DEFAULT_REGION_ID = 2

/** 与 com.yamoo.ichat.common.constant.Region 保持一致的区域选项。 */
export const REGION_OPTIONS = Object.freeze([
  { value: 2, label: '东南亚' },
  { value: 3, label: '华语' },
  { value: 4, label: '中东' },
  { value: 5, label: '印度' },
  { value: 6, label: '越南' }
])

/** 读取并校验本地缓存区域。 */
function resolveCachedRegionId() {
  const cachedRegionId = Number(cache.local.get(REGION_CACHE_KEY))
  return REGION_OPTIONS.some(item => item.value === cachedRegionId)
    ? cachedRegionId : DEFAULT_REGION_ID
}

const useRegionStore = defineStore('region', {
  state: () => ({
    /** 当前看板区域编号。 */
    regionId: resolveCachedRegionId()
  }),
  actions: {
    /** 切换当前看板区域并持久化。 */
    setRegionId(regionId) {
      const actualRegionId = Number(regionId)
      if (!REGION_OPTIONS.some(item => item.value === actualRegionId)) return
      this.regionId = actualRegionId
      cache.local.set(REGION_CACHE_KEY, String(actualRegionId))
    }
  }
})

export default useRegionStore
