<template>
  <div class="tracking-page">
    <header class="page-toolbar">
      <div class="page-title">
        <div>
          <h1>埋点字典平台</h1>
          <p>治理 / 埋点字典 M5</p>
        </div>
        <el-tag effect="plain">初始字典 v1.2</el-tag>
      </div>
      <el-button
        v-if="activeView === 'events'"
        v-hasPermi="['bi:tracking:add']"
        type="primary"
        :icon="Plus"
        @click="openEventDialog()"
      >登记新事件</el-button>
      <el-button
        v-else-if="activeView === 'params'"
        v-hasPermi="['bi:tracking:param']"
        type="primary"
        :icon="Plus"
        @click="openCommonParamDialog()"
      >新增公共参数</el-button>
    </header>

    <nav class="view-tabs" aria-label="埋点字典工作区">
      <button :class="{ active: activeView === 'events' }" @click="activeView = 'events'">事件字典</button>
      <button :class="{ active: activeView === 'params' }" @click="switchView('params')">公共参数</button>
      <button :class="{ active: activeView === 'changes' }" @click="switchView('changes')">
        变更审批
        <span v-if="pendingCount">{{ pendingCount }}</span>
      </button>
    </nav>

    <template v-if="activeView === 'events'">
      <section class="filter-band">
        <strong>事件字典列表</strong>
        <div class="filter-controls">
          <el-select v-model="eventQuery.moduleName" clearable placeholder="模块：全部" @change="handleEventQuery">
            <el-option v-for="item in modules" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="eventQuery.reportTerminal" clearable placeholder="端：全部" @change="handleEventQuery">
            <el-option v-for="item in terminalOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="eventQuery.status" clearable placeholder="状态：全部" @change="handleEventQuery">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input
            v-model="eventQuery.keyword"
            clearable
            placeholder="事件名 / 关键词"
            :prefix-icon="Search"
            @keyup.enter="handleEventQuery"
            @clear="handleEventQuery"
          />
          <el-tooltip content="刷新事件字典" placement="top">
            <el-button :icon="Refresh" circle @click="loadEvents" />
          </el-tooltip>
        </div>
      </section>

      <section class="table-band" v-loading="eventLoading">
        <el-table :data="eventRows" class="dictionary-table" row-key="eventId">
          <el-table-column prop="moduleName" label="模块" width="94" />
          <el-table-column prop="priority" label="优先级" width="84" align="center">
            <template #default="scope">
              <span class="priority-tag" :class="`priority-${scope.row.priority.replace('--', 'none')}`">{{ scope.row.priority }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="eventLabel" label="事件标签" min-width="132" show-overflow-tooltip />
          <el-table-column prop="eventName" label="事件名" min-width="220" show-overflow-tooltip>
            <template #default="scope"><code>{{ scope.row.eventName }}</code></template>
          </el-table-column>
          <el-table-column label="上报端" width="94">
            <template #default="scope">{{ terminalLabel(scope.row.reportTerminal) }}</template>
          </el-table-column>
          <el-table-column prop="triggerTiming" label="触发时机" min-width="180" show-overflow-tooltip />
          <el-table-column prop="metricCodes" label="引用指标" min-width="145">
            <template #default="scope">{{ scope.row.metricCodes || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="94" align="center">
            <template #default="scope">
              <span class="status-tag" :class="`status-${scope.row.status}`">{{ statusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="right" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
              <el-dropdown
                v-if="scope.row.status !== '2'"
                v-hasPermi="['bi:tracking:edit']"
                trigger="click"
                @command="command => handleEventCommand(command, scope.row)"
              >
                <el-button link type="primary">更多<el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit" :icon="Edit">修改提审</el-dropdown-item>
                    <el-dropdown-item :command="scope.row.status === '0' ? 'disable' : 'enable'">
                      {{ scope.row.status === '0' ? '停用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="deprecated" divided>标记废弃</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="eventTotal > 0"
          :total="eventTotal"
          v-model:page="eventQuery.pageNum"
          v-model:limit="eventQuery.pageSize"
          @pagination="loadEvents"
        />
      </section>

      <section class="rules-band">
        <h2>登记规则</h2>
        <div class="rule-grid">
          <p><b>命名</b>模块_动作[_状态]，全部小写下划线；曝光 <code>_exposure</code>、成功 <code>_success</code>、失败 <code>_failed</code>、完成 <code>_complete</code>。</p>
          <p><b>参数</b>公共参数由 SDK 自动注入，事件不得重复自带；事件变更通过审批后生效，废弃事件永久保留。</p>
          <p><b>指标</b>事件应建立引用指标，详情中展示关联编号，便于数据口径反向追踪。</p>
        </div>
      </section>
    </template>

    <template v-else-if="activeView === 'params'">
      <section class="filter-band">
        <strong>公共参数表 <small>v1.2 新增 8 项</small></strong>
        <div class="filter-controls compact-controls">
          <el-select v-model="paramQuery.status" clearable placeholder="状态：全部" @change="handleParamQuery">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input
            v-model="paramQuery.paramName"
            clearable
            placeholder="参数名 / 说明"
            :prefix-icon="Search"
            @keyup.enter="handleParamQuery"
            @clear="handleParamQuery"
          />
          <el-tooltip content="刷新公共参数" placement="top">
            <el-button :icon="Refresh" circle @click="loadCommonParams" />
          </el-tooltip>
        </div>
      </section>
      <section class="table-band" v-loading="paramLoading">
        <el-table :data="paramRows" row-key="commonParamId">
          <el-table-column prop="paramName" label="参数名" min-width="180">
            <template #default="scope"><code>{{ scope.row.paramName }}</code></template>
          </el-table-column>
          <el-table-column prop="paramType" label="数据类型" width="120" />
          <el-table-column prop="description" label="业务说明" min-width="360" />
          <el-table-column prop="versionNo" label="启用版本" width="110" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <span class="status-tag" :class="`status-${scope.row.status}`">{{ statusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" align="right">
            <template #default="scope">
              <el-button
                v-if="scope.row.status !== '2'"
                v-hasPermi="['bi:tracking:param']"
                link
                type="primary"
                @click="openCommonParamDialog(scope.row)"
              >编辑</el-button>
              <el-button
                v-if="scope.row.status !== '2'"
                v-hasPermi="['bi:tracking:param']"
                link
                type="primary"
                @click="changeCommonParamStatus(scope.row, scope.row.status === '0' ? '1' : '0')"
              >{{ scope.row.status === '0' ? '停用' : '启用' }}</el-button>
              <el-button
                v-if="scope.row.status !== '2'"
                v-hasPermi="['bi:tracking:param']"
                link
                type="danger"
                @click="changeCommonParamStatus(scope.row, '2')"
              >废弃</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="paramTotal > 0"
          :total="paramTotal"
          v-model:page="paramQuery.pageNum"
          v-model:limit="paramQuery.pageSize"
          @pagination="loadCommonParams"
        />
      </section>
    </template>

    <template v-else>
      <section class="filter-band">
        <strong>事件变更审批</strong>
        <div class="filter-controls compact-controls">
          <el-select v-model="changeQuery.approvalStatus" clearable placeholder="审批状态：全部" @change="handleChangeQuery">
            <el-option v-for="item in approvalOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input
            v-model="changeQuery.eventName"
            clearable
            placeholder="事件名"
            :prefix-icon="Search"
            @keyup.enter="handleChangeQuery"
            @clear="handleChangeQuery"
          />
          <el-tooltip content="刷新审批列表" placement="top">
            <el-button :icon="Refresh" circle @click="loadChanges" />
          </el-tooltip>
        </div>
      </section>
      <section class="table-band" v-loading="changeLoading">
        <el-table :data="changeRows" row-key="changeId">
          <el-table-column prop="eventName" label="事件名" min-width="210">
            <template #default="scope"><code>{{ scope.row.eventName }}</code></template>
          </el-table-column>
          <el-table-column label="变更类型" width="110">
            <template #default="scope">{{ changeTypeLabel(scope.row.changeType) }}</template>
          </el-table-column>
          <el-table-column prop="changeReason" label="变更原因" min-width="260" show-overflow-tooltip />
          <el-table-column prop="createBy" label="提交人" width="110" />
          <el-table-column prop="createTime" label="提交时间" width="168" />
          <el-table-column label="审批状态" width="110" align="center">
            <template #default="scope">
              <span class="approval-tag" :class="`approval-${scope.row.approvalStatus}`">{{ approvalLabel(scope.row.approvalStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="approveBy" label="审批人" width="110">
            <template #default="scope">{{ scope.row.approveBy || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="right">
            <template #default="scope">
              <el-button link type="primary" @click="openChangeDetail(scope.row)">查看</el-button>
              <el-button
                v-if="scope.row.approvalStatus === '0'"
                v-hasPermi="['bi:tracking:review']"
                link
                type="primary"
                @click="openReviewDialog(scope.row)"
              >审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="changeTotal > 0"
          :total="changeTotal"
          v-model:page="changeQuery.pageNum"
          v-model:limit="changeQuery.pageSize"
          @pagination="loadChanges"
        />
      </section>
    </template>

    <el-dialog v-model="eventDialogOpen" :title="eventForm.eventId ? '修改事件并提审' : '登记新事件'" width="920px" append-to-body destroy-on-close>
      <el-form ref="eventFormRef" :model="eventForm" :rules="eventRules" label-position="top">
        <div class="form-grid">
          <el-form-item label="业务模块" prop="moduleName">
            <el-input v-model="eventForm.moduleName" maxlength="32" placeholder="例如：账号" />
          </el-form-item>
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="eventForm.priority">
              <el-option v-for="item in priorityOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="事件标签" prop="eventLabel">
            <el-input v-model="eventForm.eventLabel" maxlength="64" placeholder="例如：注册成功" />
          </el-form-item>
          <el-form-item label="事件名" prop="eventName">
            <el-input v-model="eventForm.eventName" maxlength="128" placeholder="account_register_success" />
          </el-form-item>
          <el-form-item label="上报端" prop="reportTerminal">
            <el-radio-group v-model="eventForm.reportTerminal" class="segmented-control">
              <el-radio-button v-for="item in terminalOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="字典版本" prop="versionNo">
            <el-input v-model="eventForm.versionNo" maxlength="16" />
          </el-form-item>
          <el-form-item class="span-two" label="触发时机" prop="triggerTiming">
            <el-input v-model="eventForm.triggerTiming" maxlength="255" placeholder="描述事件何时上报" />
          </el-form-item>
          <el-form-item class="span-two" label="引用指标">
            <el-input v-model="eventForm.metricCodes" maxlength="255" placeholder="例如：U1 U8，多个编号使用空格分隔" />
          </el-form-item>
          <el-form-item v-if="eventForm.eventId" class="span-two" label="变更原因" prop="changeReason">
            <el-input v-model="eventForm.changeReason" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
        </div>

        <div class="param-editor-heading">
          <div><strong>事件私有参数</strong><span>公共参数由 SDK 自动注入，无需重复登记</span></div>
          <el-tooltip content="添加事件参数" placement="top">
            <el-button :icon="Plus" circle @click="addEventParam" />
          </el-tooltip>
        </div>
        <el-table :data="eventForm.eventParams" empty-text="当前事件没有私有参数" class="param-editor">
          <el-table-column label="参数名" min-width="170">
            <template #default="scope"><el-input v-model="scope.row.paramName" placeholder="game_type" /></template>
          </el-table-column>
          <el-table-column label="类型" width="126">
            <template #default="scope">
              <el-select v-model="scope.row.paramType">
                <el-option v-for="item in paramTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="必填" width="84" align="center">
            <template #default="scope"><el-switch v-model="scope.row.requiredFlag" active-value="1" inactive-value="0" /></template>
          </el-table-column>
          <el-table-column label="参数说明" min-width="240">
            <template #default="scope"><el-input v-model="scope.row.description" maxlength="255" /></template>
          </el-table-column>
          <el-table-column width="56" align="center">
            <template #default="scope">
              <el-tooltip content="移除参数" placement="top">
                <el-button :icon="Delete" circle text type="danger" @click="eventForm.eventParams.splice(scope.$index, 1)" />
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="eventDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="eventSubmitting" @click="submitEventForm">
          {{ eventForm.eventId ? '提交审批' : '确认登记' }}
        </el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailOpen" title="事件详情" size="620px">
      <template v-if="detailEvent.eventId">
        <div class="detail-title">
          <span>{{ detailEvent.eventLabel }}</span>
          <code>{{ detailEvent.eventName }}</code>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模块">{{ detailEvent.moduleName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ detailEvent.priority }}</el-descriptions-item>
          <el-descriptions-item label="上报端">{{ terminalLabel(detailEvent.reportTerminal) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(detailEvent.status) }}</el-descriptions-item>
          <el-descriptions-item label="字典版本">v{{ detailEvent.versionNo }}</el-descriptions-item>
          <el-descriptions-item label="引用指标">{{ detailEvent.metricCodes || '—' }}</el-descriptions-item>
          <el-descriptions-item label="触发时机" :span="2">{{ detailEvent.triggerTiming }}</el-descriptions-item>
          <el-descriptions-item label="补充说明" :span="2">{{ detailEvent.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
        <h3 class="drawer-heading">事件私有参数</h3>
        <el-table :data="detailEvent.eventParams || []" empty-text="无私有参数">
          <el-table-column prop="paramName" label="参数名" min-width="150">
            <template #default="scope"><code>{{ scope.row.paramName }}</code></template>
          </el-table-column>
          <el-table-column prop="paramType" label="类型" width="90" />
          <el-table-column label="必填" width="70" align="center">
            <template #default="scope">{{ scope.row.requiredFlag === '1' ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="180" />
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="commonParamDialogOpen" :title="commonParamForm.commonParamId ? '编辑公共参数' : '新增公共参数'" width="560px" append-to-body>
      <el-form ref="commonParamFormRef" :model="commonParamForm" :rules="commonParamRules" label-position="top">
        <div class="form-grid">
          <el-form-item label="参数名" prop="paramName">
            <el-input v-model="commonParamForm.paramName" maxlength="64" placeholder="attribution_channel" />
          </el-form-item>
          <el-form-item label="数据类型" prop="paramType">
            <el-select v-model="commonParamForm.paramType">
              <el-option v-for="item in paramTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用版本" prop="versionNo">
            <el-input v-model="commonParamForm.versionNo" maxlength="16" />
          </el-form-item>
          <el-form-item v-if="commonParamForm.commonParamId" label="状态">
            <el-select v-model="commonParamForm.status">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item class="span-two" label="业务说明" prop="description">
            <el-input v-model="commonParamForm.description" type="textarea" :rows="3" maxlength="255" show-word-limit />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="commonParamDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="paramSubmitting" @click="submitCommonParam">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogOpen" title="审批事件变更" width="620px" append-to-body>
      <div class="change-summary">
        <code>{{ reviewTarget.eventName }}</code>
        <p>{{ reviewTarget.changeReason }}</p>
      </div>
      <el-form label-position="top">
        <el-form-item label="审批结果">
          <el-radio-group v-model="reviewForm.approvalStatus" class="segmented-control">
            <el-radio-button v-for="item in reviewOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" :required="reviewForm.approvalStatus === '2'">
          <el-input v-model="reviewForm.approvalRemark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">确认审批</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="changeDetailOpen" title="变更快照" size="620px">
      <template v-if="changeSnapshot.eventName">
        <div class="detail-title"><span>{{ changeSnapshot.eventLabel }}</span><code>{{ changeSnapshot.eventName }}</code></div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模块">{{ changeSnapshot.moduleName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ changeSnapshot.priority }}</el-descriptions-item>
          <el-descriptions-item label="上报端">{{ terminalLabel(changeSnapshot.reportTerminal) }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ changeSnapshot.versionNo }}</el-descriptions-item>
          <el-descriptions-item label="触发时机" :span="2">{{ changeSnapshot.triggerTiming }}</el-descriptions-item>
          <el-descriptions-item label="引用指标" :span="2">{{ changeSnapshot.metricCodes || '—' }}</el-descriptions-item>
        </el-descriptions>
        <h3 class="drawer-heading">变更后参数</h3>
        <el-table :data="changeSnapshot.eventParams || []" empty-text="无私有参数">
          <el-table-column prop="paramName" label="参数名" min-width="150" />
          <el-table-column prop="paramType" label="类型" width="90" />
          <el-table-column prop="description" label="说明" min-width="200" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="TrackingDictionary">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { ArrowDown, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  addTrackingCommonParam,
  addTrackingEvent,
  getTrackingEvent,
  getTrackingModules,
  listTrackingChanges,
  listTrackingCommonParams,
  listTrackingEvents,
  reviewTrackingChange,
  submitTrackingEventChange,
  updateTrackingCommonParam,
  updateTrackingCommonParamStatus,
  updateTrackingEventStatus
} from '@/api/bi/trackingDictionary'

const { proxy } = getCurrentInstance()

// 上报端固定选项
const terminalOptions = [
  { label: '客户端', value: 'CLIENT' },
  { label: '服务端', value: 'SERVER' },
  { label: '双端', value: 'BOTH' }
]
// 字典状态固定选项
const statusOptions = [
  { label: '启用', value: '0' },
  { label: '停用', value: '1' },
  { label: '已废弃', value: '2' }
]
// 审批状态固定选项
const approvalOptions = [
  { label: '待审批', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已驳回', value: '2' }
]
// 审批操作固定选项
const reviewOptions = [
  { label: '通过', value: '1' },
  { label: '驳回', value: '2' }
]
// 事件优先级固定选项
const priorityOptions = ['P0', 'P1', 'P2', '--']
// 埋点参数数据类型固定选项
const paramTypeOptions = ['string', 'integer', 'long', 'decimal', 'boolean', 'json']
// 事件名和参数名的小写下划线校验规则
const namePattern = /^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$/

const activeView = ref('events')
const modules = ref([])
const eventLoading = ref(false)
const eventRows = ref([])
const eventTotal = ref(0)
const eventQuery = reactive({ pageNum: 1, pageSize: 10, moduleName: '', reportTerminal: '', status: '', keyword: '' })

const paramLoading = ref(false)
const paramRows = ref([])
const paramTotal = ref(0)
const paramQuery = reactive({ pageNum: 1, pageSize: 10, paramName: '', status: '' })

const changeLoading = ref(false)
const changeRows = ref([])
const changeTotal = ref(0)
const pendingCount = ref(0)
const changeQuery = reactive({ pageNum: 1, pageSize: 10, eventName: '', approvalStatus: '' })

const eventDialogOpen = ref(false)
const eventSubmitting = ref(false)
const eventFormRef = ref()
const eventForm = reactive(createEmptyEvent())
const eventRules = {
  moduleName: [{ required: true, message: '业务模块不能为空', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  eventLabel: [{ required: true, message: '事件标签不能为空', trigger: 'blur' }],
  eventName: [
    { required: true, message: '事件名不能为空', trigger: 'blur' },
    { pattern: namePattern, message: '仅允许小写字母、数字和下划线，且必须以小写字母开头', trigger: 'blur' }
  ],
  reportTerminal: [{ required: true, message: '请选择上报端', trigger: 'change' }],
  versionNo: [{ required: true, message: '字典版本不能为空', trigger: 'blur' }],
  triggerTiming: [{ required: true, message: '触发时机不能为空', trigger: 'blur' }],
  changeReason: [{ required: true, message: '变更原因不能为空', trigger: 'blur' }]
}

const detailOpen = ref(false)
const detailEvent = reactive({})

const commonParamDialogOpen = ref(false)
const paramSubmitting = ref(false)
const commonParamFormRef = ref()
const commonParamForm = reactive(createEmptyCommonParam())
const commonParamRules = {
  paramName: [
    { required: true, message: '参数名不能为空', trigger: 'blur' },
    { pattern: namePattern, message: '参数名必须为小写下划线命名', trigger: 'blur' }
  ],
  paramType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  versionNo: [{ required: true, message: '启用版本不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '业务说明不能为空', trigger: 'blur' }]
}

const reviewDialogOpen = ref(false)
const reviewSubmitting = ref(false)
const reviewTarget = reactive({})
const reviewForm = reactive({ approvalStatus: '1', approvalRemark: '' })
const changeDetailOpen = ref(false)
const changeSnapshot = reactive({})

onMounted(async () => {
  await Promise.all([loadModules(), loadEvents(), loadPendingCount()])
})

async function loadModules() {
  const response = await getTrackingModules()
  modules.value = response.data || []
}

async function loadEvents() {
  eventLoading.value = true
  try {
    const response = await listTrackingEvents(eventQuery)
    eventRows.value = response.rows || []
    eventTotal.value = response.total || 0
  } finally {
    eventLoading.value = false
  }
}

async function loadCommonParams() {
  paramLoading.value = true
  try {
    const response = await listTrackingCommonParams(paramQuery)
    paramRows.value = response.rows || []
    paramTotal.value = response.total || 0
  } finally {
    paramLoading.value = false
  }
}

async function loadChanges() {
  changeLoading.value = true
  try {
    const response = await listTrackingChanges(changeQuery)
    changeRows.value = response.rows || []
    changeTotal.value = response.total || 0
  } finally {
    changeLoading.value = false
  }
}

async function loadPendingCount() {
  const response = await listTrackingChanges({ pageNum: 1, pageSize: 1, approvalStatus: '0' })
  pendingCount.value = Number(response.total || 0)
}

function switchView(view) {
  activeView.value = view
  if (view === 'params' && !paramRows.value.length) loadCommonParams()
  if (view === 'changes') loadChanges()
}

function handleEventQuery() {
  eventQuery.pageNum = 1
  loadEvents()
}

function handleParamQuery() {
  paramQuery.pageNum = 1
  loadCommonParams()
}

function handleChangeQuery() {
  changeQuery.pageNum = 1
  loadChanges()
}

function createEmptyEvent() {
  return {
    eventId: undefined,
    moduleName: '',
    priority: 'P0',
    eventLabel: '',
    eventName: '',
    reportTerminal: 'SERVER',
    triggerTiming: '',
    metricCodes: '',
    versionNo: '1.2',
    status: '0',
    changeReason: '',
    remark: '',
    eventParams: []
  }
}

async function openEventDialog(row) {
  Object.assign(eventForm, createEmptyEvent())
  eventFormRef.value?.clearValidate()
  if (row?.eventId) {
    const response = await getTrackingEvent(row.eventId)
    Object.assign(eventForm, response.data || {}, { changeReason: '' })
    eventForm.eventParams = (response.data?.eventParams || []).map(item => ({ ...item }))
  }
  eventDialogOpen.value = true
}

function addEventParam() {
  eventForm.eventParams.push({ paramName: '', paramType: 'string', requiredFlag: '0', commonFlag: '0', description: '' })
}

async function submitEventForm() {
  const valid = await eventFormRef.value.validate().catch(() => false)
  if (!valid || !validateEventParams()) return
  eventSubmitting.value = true
  try {
    if (eventForm.eventId) {
      await submitTrackingEventChange(eventForm)
      proxy.$modal.msgSuccess('修改已提交审批')
      await loadPendingCount()
    } else {
      await addTrackingEvent(eventForm)
      proxy.$modal.msgSuccess('事件登记成功')
      await loadModules()
    }
    eventDialogOpen.value = false
    await loadEvents()
  } finally {
    eventSubmitting.value = false
  }
}

function validateEventParams() {
  const names = new Set()
  for (const item of eventForm.eventParams) {
    if (!namePattern.test(item.paramName || '')) {
      proxy.$modal.msgError('事件参数名必须为小写下划线命名')
      return false
    }
    if (names.has(item.paramName)) {
      proxy.$modal.msgError(`事件参数不能重复：${item.paramName}`)
      return false
    }
    names.add(item.paramName)
  }
  return true
}

async function openDetail(row) {
  const response = await getTrackingEvent(row.eventId)
  Object.assign(detailEvent, response.data || {})
  detailOpen.value = true
}

function handleEventCommand(command, row) {
  if (command === 'edit') {
    openEventDialog(row)
    return
  }
  const status = command === 'enable' ? '0' : command === 'disable' ? '1' : '2'
  const action = status === '0' ? '启用' : status === '1' ? '停用' : '标记为已废弃'
  proxy.$modal.confirm(`确认将事件 ${row.eventName} ${action}吗？${status === '2' ? '废弃后不可恢复。' : ''}`)
    .then(() => updateTrackingEventStatus(row.eventId, status))
    .then(() => {
      proxy.$modal.msgSuccess(`${action}成功`)
      loadEvents()
      loadPendingCount()
    })
    .catch(() => {})
}

function createEmptyCommonParam() {
  return { commonParamId: undefined, paramName: '', paramType: 'string', description: '', versionNo: '1.2', status: '0' }
}

function openCommonParamDialog(row) {
  Object.assign(commonParamForm, createEmptyCommonParam(), row || {})
  commonParamFormRef.value?.clearValidate()
  commonParamDialogOpen.value = true
}

async function submitCommonParam() {
  const valid = await commonParamFormRef.value.validate().catch(() => false)
  if (!valid) return
  paramSubmitting.value = true
  try {
    if (commonParamForm.commonParamId) {
      await updateTrackingCommonParam(commonParamForm)
      proxy.$modal.msgSuccess('公共参数更新成功')
    } else {
      await addTrackingCommonParam(commonParamForm)
      proxy.$modal.msgSuccess('公共参数新增成功')
    }
    commonParamDialogOpen.value = false
    await loadCommonParams()
  } finally {
    paramSubmitting.value = false
  }
}

function changeCommonParamStatus(row, status) {
  const action = status === '0' ? '启用' : status === '1' ? '停用' : '废弃'
  proxy.$modal.confirm(`确认${action}公共参数 ${row.paramName} 吗？${status === '2' ? '废弃后不可恢复。' : ''}`)
    .then(() => updateTrackingCommonParamStatus(row.commonParamId, status))
    .then(() => {
      proxy.$modal.msgSuccess(`${action}成功`)
      loadCommonParams()
    })
    .catch(() => {})
}

function openReviewDialog(row) {
  Object.assign(reviewTarget, row)
  reviewForm.approvalStatus = '1'
  reviewForm.approvalRemark = ''
  reviewDialogOpen.value = true
}

async function submitReview() {
  if (reviewForm.approvalStatus === '2' && !reviewForm.approvalRemark.trim()) {
    proxy.$modal.msgError('驳回时必须填写审批意见')
    return
  }
  reviewSubmitting.value = true
  try {
    await reviewTrackingChange(reviewTarget.changeId, reviewForm)
    proxy.$modal.msgSuccess(reviewForm.approvalStatus === '1' ? '审批通过，变更已生效' : '变更已驳回')
    reviewDialogOpen.value = false
    await Promise.all([loadChanges(), loadPendingCount(), loadEvents()])
  } finally {
    reviewSubmitting.value = false
  }
}

function openChangeDetail(row) {
  try {
    Object.assign(changeSnapshot, JSON.parse(row.snapshotJson || '{}'))
    changeDetailOpen.value = true
  } catch {
    proxy.$modal.msgError('变更快照格式不正确')
  }
}

function terminalLabel(value) {
  return terminalOptions.find(item => item.value === value)?.label || value || '—'
}

function statusLabel(value) {
  return statusOptions.find(item => item.value === value)?.label || '未知'
}

function approvalLabel(value) {
  return approvalOptions.find(item => item.value === value)?.label || '未知'
}

function changeTypeLabel(value) {
  return { REGISTER: '事件登记', UPDATE: '事件修改', STATUS: '状态调整' }[value] || value
}
</script>

<style scoped lang="scss">
.tracking-page { min-height: calc(100vh - 84px); padding: 18px; background: #f2f4f7; color: #252b38; }
.page-toolbar { min-height: 72px; padding: 0 18px; display: flex; align-items: center; justify-content: space-between; gap: 20px; background: #fff; border: 1px solid #e4e8ef; border-radius: 6px 6px 0 0; }
.page-title { min-width: 0; display: flex; align-items: center; gap: 18px; }
.page-title h1 { margin: 0; font-size: 19px; line-height: 1.3; letter-spacing: 0; }
.page-title p { margin: 3px 0 0; color: #8993a4; font-size: 12px; }
.view-tabs { height: 46px; padding: 0 18px; display: flex; align-items: flex-end; gap: 26px; background: #fff; border: solid #e4e8ef; border-width: 0 1px 1px; }
.view-tabs button { position: relative; height: 46px; padding: 0 2px; color: #667085; background: transparent; border: 0; border-bottom: 2px solid transparent; cursor: pointer; font-size: 14px; }
.view-tabs button.active { color: #3478dc; border-bottom-color: #3478dc; font-weight: 600; }
.view-tabs button span { position: absolute; top: 7px; right: -18px; min-width: 18px; height: 18px; padding: 0 5px; color: #fff; background: #e45166; border-radius: 9px; font-size: 11px; line-height: 18px; }
.filter-band { min-height: 66px; margin-top: 14px; padding: 12px 16px; display: flex; align-items: center; justify-content: space-between; gap: 18px; background: #fff; border: 1px solid #e4e8ef; border-radius: 6px 6px 0 0; }
.filter-band > strong { white-space: nowrap; font-size: 15px; }
.filter-band > strong small { margin-left: 8px; color: #8a94a6; font-size: 12px; font-weight: 400; }
.filter-controls { display: flex; align-items: center; justify-content: flex-end; gap: 8px; }
.filter-controls :deep(.el-select) { width: 136px; }
.filter-controls :deep(.el-input) { width: 210px; }
.compact-controls :deep(.el-select) { width: 154px; }
.compact-controls :deep(.el-input) { width: 250px; }
.table-band { padding: 0 14px 12px; background: #fff; border: solid #e4e8ef; border-width: 0 1px 1px; border-radius: 0 0 6px 6px; }
.dictionary-table code, .table-band code, .detail-title code { color: #24324a; background: transparent; font-family: Consolas, monospace; font-size: 13px; }
.priority-tag, .status-tag, .approval-tag { display: inline-block; min-width: 42px; padding: 4px 8px; border-radius: 3px; font-size: 12px; line-height: 1; }
.priority-P0 { color: #dd4962; background: #ffedf0; }
.priority-P1 { color: #c7781d; background: #fff2df; }
.priority-P2 { color: #3679c9; background: #eaf3ff; }
.priority-none { color: #7257c7; background: #f0ecff; }
.status-0 { color: #168158; background: #e7f7ef; }
.status-1 { color: #8b651d; background: #fff5df; }
.status-2 { color: #cb445a; background: #fff0f2; }
.approval-0 { color: #9b6a18; background: #fff4dc; }
.approval-1 { color: #168158; background: #e7f7ef; }
.approval-2 { color: #cb445a; background: #fff0f2; }
.rules-band { min-height: 148px; margin-top: 14px; padding: 18px 20px; background: #fff; border: 1px solid #e4e8ef; border-radius: 6px; }
.rules-band h2 { margin: 0 0 12px; font-size: 14px; letter-spacing: 0; }
.rule-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 28px; }
.rule-grid p { margin: 0; color: #8490a3; font-size: 12px; line-height: 1.8; }
.rule-grid b { margin-right: 8px; color: #364152; }
.rule-grid code { color: #67748a; font-family: Consolas, monospace; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.span-two { grid-column: 1 / -1; }
.form-grid :deep(.el-select), .segmented-control { width: 100%; }
.segmented-control :deep(.el-radio-button) { flex: 1; }
.segmented-control :deep(.el-radio-button__inner) { width: 100%; }
.param-editor-heading { min-height: 48px; display: flex; align-items: center; justify-content: space-between; gap: 16px; border-top: 1px solid #edf0f4; }
.param-editor-heading div { display: flex; align-items: baseline; gap: 10px; }
.param-editor-heading span { color: #8a94a6; font-size: 12px; }
.param-editor :deep(.el-input__wrapper), .param-editor :deep(.el-select__wrapper) { box-shadow: 0 0 0 1px #e3e7ee inset; }
.detail-title { margin-bottom: 20px; display: flex; flex-direction: column; gap: 6px; }
.detail-title span { font-size: 19px; font-weight: 650; }
.detail-title code { color: #477dcc; }
.drawer-heading { margin: 24px 0 10px; font-size: 15px; letter-spacing: 0; }
.change-summary { margin-bottom: 18px; padding: 14px 16px; background: #f6f8fb; border-left: 3px solid #4d83d4; }
.change-summary code { color: #274a7d; font-family: Consolas, monospace; }
.change-summary p { margin: 8px 0 0; color: #667085; font-size: 13px; line-height: 1.6; }

@media (max-width: 1100px) {
  .filter-band { align-items: flex-start; flex-direction: column; }
  .filter-controls { width: 100%; justify-content: flex-start; flex-wrap: wrap; }
  .rule-grid { grid-template-columns: 1fr; gap: 8px; }
}

@media (max-width: 680px) {
  .tracking-page { padding: 10px; }
  .page-toolbar { align-items: flex-start; padding: 14px; flex-direction: column; }
  .page-toolbar > .el-button { width: 100%; }
  .page-title { align-items: flex-start; flex-direction: column; gap: 8px; }
  .view-tabs { gap: 18px; overflow-x: auto; }
  .filter-controls :deep(.el-select), .filter-controls :deep(.el-input) { width: calc(50% - 4px); }
  .form-grid { grid-template-columns: 1fr; }
  .span-two { grid-column: auto; }
}
</style>
