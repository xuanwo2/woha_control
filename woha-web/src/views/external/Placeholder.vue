<template><div class="placeholder"><h2>{{ title }}</h2><p>该功能暂未开放。</p></div></template>
<script setup>
const titles = { '/external/guild': '公会管理', '/external/guild-flow': '公会流水', '/external/gift': '礼包赠送', '/external/chat-no': '靓号', '/external/account-ban': '账号封禁', '/external/export': '数据导出' }
const route = useRoute()
const title = computed(() => titles[route.path] || '功能建设中')
</script>
<style scoped>.placeholder{margin:20px;padding:24px;background:#fff;border:1px solid #e4e7ed}.placeholder h2{font-size:18px;color:#303133}.placeholder p{color:#909399}</style>
