<template>
  <div class="app-container">
    <div class="table-toolbar"><span class="section-title">外管礼包</span><el-button type="primary" @click="newPack">新增礼包</el-button></div>
    <el-table :data="packs" border style="margin-top:16px"><el-table-column prop="giftName" label="礼包名称" min-width="130"/><el-table-column prop="goldAmount" label="金币" width="100"/><el-table-column prop="receiveLimit" label="每人限领" width="100"/><el-table-column label="内容" min-width="220"><template #default="{row}">{{ itemText(row.items) }}</template></el-table-column><el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="80"><template #default="{row}"><el-button link type="primary" @click="editPack(row)">编辑</el-button></template></el-table-column></el-table>
    <el-dialog v-model="visible" :title="form.id ? '编辑礼包' : '新增礼包'" width="650px"><el-form label-width="100px"><el-form-item label="礼包名称"><el-input v-model="form.giftName"/></el-form-item><el-form-item label="金币"><el-input-number v-model="form.goldAmount" :min="0"/></el-form-item><el-form-item label="每人限领"><el-input-number v-model="form.receiveLimit" :min="1"/></el-form-item><el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio label="0">启用</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item><el-divider>礼包内容</el-divider><div v-for="(item,index) in form.items" :key="index" class="item"><el-select v-model="item.itemType"><el-option label="装扮" value="PROP"/><el-option label="VIP卡" value="VIP"/></el-select><el-input v-if="item.itemType === 'PROP'" v-model.number="item.propId" placeholder="装扮ID"/><el-select v-else v-model="item.vipLevel"><el-option label="VIP1" :value="1000"/><el-option label="VIP2" :value="2000"/><el-option label="VIP3" :value="3000"/></el-select><el-input-number v-model="item.dayNum" :min="1"/><el-button link type="danger" @click="form.items.splice(index,1)">删除</el-button></div><el-button link type="primary" @click="form.items.push({itemType:'PROP',propId:null,dayNum:30,quantity:1})">新增内容</el-button></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="savePack">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ElMessage } from 'element-plus'
import { listExternalGiftPacks, saveExternalGiftPack } from '@/api/external/manager'
const packs = ref([]); const visible = ref(false); const form = reactive({})
const propTypeText = { 1: '头像框', 2: '座驾', 4: '进房特效', 6: '聊天气泡', 7: '房间背景', 11: '资料卡' }
const itemText = items => (items || []).map(item => item.itemType === 'VIP' ? `VIP${item.vipLevel / 1000} ${item.dayNum}天` : `${propTypeText[item.propType] || '装扮'}${item.propId} ${item.dayNum}天`).join('、')
function resetForm() { Object.assign(form, { id: null, giftName: '', goldAmount: 0, receiveLimit: 1, status: '0', items: [{ itemType: 'PROP', propId: null, dayNum: 30, quantity: 1 }] }) }
function newPack() { resetForm(); visible.value = true }
function editPack(row) { Object.assign(form, JSON.parse(JSON.stringify(row))); visible.value = true }
async function load() { const response = await listExternalGiftPacks(); packs.value = response.data }
async function savePack() { const data = { ...form, items: form.items.map(item => ({ ...item, quantity: 1 })) }; await saveExternalGiftPack(data); ElMessage.success('保存成功'); visible.value = false; await load() }
load()
</script>
<style scoped>.table-toolbar{display:flex;justify-content:space-between;align-items:center}.section-title{font-size:16px;font-weight:600}.item{display:flex;gap:8px;margin-bottom:8px;align-items:center}.item>:nth-child(1){flex:0 0 110px}.item>:nth-child(2){flex:1 1 auto;min-width:0}.item>:nth-child(3){flex:0 0 120px}.item :deep(.el-button){flex:0 0 64px;min-width:64px;margin-left:0;white-space:nowrap}</style>
