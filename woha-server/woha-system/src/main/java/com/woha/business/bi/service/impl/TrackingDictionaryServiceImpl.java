package com.woha.business.bi.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.woha.business.bi.domain.TrackingChangeRecord;
import com.woha.business.bi.domain.TrackingCommonParam;
import com.woha.business.bi.domain.TrackingEvent;
import com.woha.business.bi.domain.TrackingEventParam;
import com.woha.business.bi.domain.TrackingEventQuery;
import com.woha.business.bi.domain.TrackingReviewRequest;
import com.woha.business.bi.mapper.TrackingDictionaryMapper;
import com.woha.business.bi.service.ITrackingDictionaryService;
import com.woha.business.common.exception.ServiceException;

/**
 * 埋点字典服务实现。
 *
 * @author luowenshen
 */
@Service
public class TrackingDictionaryServiceImpl implements ITrackingDictionaryService
{
    /** 启用状态。 */
    private static final String STATUS_ENABLED = "0";

    /** 已废弃状态。 */
    private static final String STATUS_DEPRECATED = "2";

    /** 待审批状态。 */
    private static final String APPROVAL_PENDING = "0";

    /** 审批通过状态。 */
    private static final String APPROVAL_APPROVED = "1";

    /** 审批驳回状态。 */
    private static final String APPROVAL_REJECTED = "2";

    /** 新事件登记变更类型。 */
    private static final String CHANGE_REGISTER = "REGISTER";

    /** 事件修改变更类型。 */
    private static final String CHANGE_UPDATE = "UPDATE";

    /** 事件状态变更类型。 */
    private static final String CHANGE_STATUS = "STATUS";

    /** 默认字典版本。 */
    private static final String DEFAULT_VERSION = "1.2";

    /** 默认事件优先级。 */
    private static final String DEFAULT_PRIORITY = "P2";

    /** 事件和参数命名规则，仅允许小写字母、数字及下划线。 */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");

    /** 允许的事件状态集合。 */
    private static final Set<String> EVENT_STATUSES = Set.of("0", "1", "2");

    /** 允许的优先级集合。 */
    private static final Set<String> PRIORITIES = Set.of("P0", "P1", "P2", "--");

    /** 允许的上报端集合。 */
    private static final Set<String> TERMINALS = Set.of("CLIENT", "SERVER", "BOTH");

    /** 允许的参数类型集合。 */
    private static final Set<String> PARAM_TYPES = Set.of("string", "integer", "long", "decimal", "boolean", "json");

    /** 埋点字典数据访问层。 */
    private final TrackingDictionaryMapper dictionaryMapper;

    public TrackingDictionaryServiceImpl(TrackingDictionaryMapper dictionaryMapper)
    {
        this.dictionaryMapper = dictionaryMapper;
    }

    @Override
    public List<TrackingEvent> selectEventList(TrackingEventQuery query)
    {
        return dictionaryMapper.selectEventList(query == null ? new TrackingEventQuery() : query);
    }

    @Override
    public TrackingEvent selectEventById(Long eventId)
    {
        TrackingEvent event = requireEvent(eventId);
        event.setEventParams(dictionaryMapper.selectEventParams(eventId));
        return event;
    }

    @Override
    public List<String> selectModules()
    {
        return dictionaryMapper.selectModules();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertEvent(TrackingEvent event, String operator)
    {
        normalizeEvent(event);
        validateEvent(event);
        ensureUniqueEventName(event.getEventName(), null);
        event.setStatus(STATUS_ENABLED);
        event.setCreateBy(operator);
        int rows = dictionaryMapper.insertEvent(event);
        saveEventParams(event);
        insertApprovedRecord(event, CHANGE_REGISTER, "登记新事件", operator);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitEventChange(TrackingEvent event, String operator)
    {
        if (event == null || event.getEventId() == null)
        {
            throw new ServiceException("事件编号不能为空");
        }
        TrackingEvent current = requireEvent(event.getEventId());
        if (STATUS_DEPRECATED.equals(current.getStatus()))
        {
            throw new ServiceException("已废弃事件不能再次修改");
        }
        normalizeEvent(event);
        validateEvent(event);
        ensureUniqueEventName(event.getEventName(), event.getEventId());
        if (isBlank(event.getChangeReason()))
        {
            throw new ServiceException("变更原因不能为空");
        }
        if (dictionaryMapper.countPendingChange(event.getEventId()) > 0)
        {
            throw new ServiceException("该事件已有待审批变更，请先完成审批");
        }
        TrackingChangeRecord record = new TrackingChangeRecord();
        record.setEventId(event.getEventId());
        record.setChangeType(CHANGE_UPDATE);
        record.setChangeReason(event.getChangeReason().trim());
        record.setSnapshotJson(JSON.toJSONString(event));
        record.setApprovalStatus(APPROVAL_PENDING);
        record.setCreateBy(operator);
        return dictionaryMapper.insertChangeRecord(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEventStatus(Long eventId, String status, String operator)
    {
        TrackingEvent event = requireEvent(eventId);
        validateStatus(status);
        if (STATUS_DEPRECATED.equals(event.getStatus()) && !STATUS_DEPRECATED.equals(status))
        {
            throw new ServiceException("已废弃事件不能恢复");
        }
        if (status.equals(event.getStatus()))
        {
            return 0;
        }
        event.setStatus(status);
        event.setUpdateBy(operator);
        int rows = dictionaryMapper.updateEventStatus(eventId, status, operator);
        event.setEventParams(dictionaryMapper.selectEventParams(eventId));
        insertApprovedRecord(event, CHANGE_STATUS, "调整事件状态", operator);
        return rows;
    }

    @Override
    public List<TrackingCommonParam> selectCommonParamList(TrackingCommonParam param)
    {
        return dictionaryMapper.selectCommonParamList(param == null ? new TrackingCommonParam() : param);
    }

    @Override
    public int insertCommonParam(TrackingCommonParam param, String operator)
    {
        normalizeCommonParam(param);
        validateCommonParam(param);
        ensureUniqueCommonParam(param.getParamName(), null);
        param.setStatus(STATUS_ENABLED);
        param.setCreateBy(operator);
        return dictionaryMapper.insertCommonParam(param);
    }

    @Override
    public int updateCommonParam(TrackingCommonParam param, String operator)
    {
        if (param == null || param.getCommonParamId() == null)
        {
            throw new ServiceException("公共参数编号不能为空");
        }
        TrackingCommonParam current = dictionaryMapper.selectCommonParamById(param.getCommonParamId());
        if (current == null)
        {
            throw new ServiceException("公共参数不存在");
        }
        if (STATUS_DEPRECATED.equals(current.getStatus()))
        {
            throw new ServiceException("已废弃公共参数不能再次修改");
        }
        normalizeCommonParam(param);
        validateCommonParam(param);
        validateStatus(param.getStatus());
        ensureUniqueCommonParam(param.getParamName(), param.getCommonParamId());
        param.setUpdateBy(operator);
        return dictionaryMapper.updateCommonParam(param);
    }

    @Override
    public int updateCommonParamStatus(Long commonParamId, String status, String operator)
    {
        TrackingCommonParam current = dictionaryMapper.selectCommonParamById(commonParamId);
        if (current == null)
        {
            throw new ServiceException("公共参数不存在");
        }
        validateStatus(status);
        if (STATUS_DEPRECATED.equals(current.getStatus()) && !STATUS_DEPRECATED.equals(status))
        {
            throw new ServiceException("已废弃公共参数不能恢复");
        }
        return dictionaryMapper.updateCommonParamStatus(commonParamId, status, operator);
    }

    @Override
    public List<TrackingChangeRecord> selectChangeList(TrackingChangeRecord record)
    {
        return dictionaryMapper.selectChangeList(record == null ? new TrackingChangeRecord() : record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reviewChange(Long changeId, TrackingReviewRequest request, String operator)
    {
        TrackingChangeRecord record = dictionaryMapper.selectChangeById(changeId);
        if (record == null)
        {
            throw new ServiceException("变更审批记录不存在");
        }
        if (!APPROVAL_PENDING.equals(record.getApprovalStatus()))
        {
            throw new ServiceException("该变更已经完成审批");
        }
        if (request == null || (!APPROVAL_APPROVED.equals(request.getApprovalStatus())
            && !APPROVAL_REJECTED.equals(request.getApprovalStatus())))
        {
            throw new ServiceException("审批结果不正确");
        }
        if (APPROVAL_REJECTED.equals(request.getApprovalStatus()) && isBlank(request.getApprovalRemark()))
        {
            throw new ServiceException("驳回时必须填写审批意见");
        }
        if (APPROVAL_APPROVED.equals(request.getApprovalStatus()))
        {
            TrackingEvent event = JSON.parseObject(record.getSnapshotJson(), TrackingEvent.class);
            event.setUpdateBy(operator);
            dictionaryMapper.updateEvent(event);
            dictionaryMapper.deleteEventParams(event.getEventId());
            saveEventParams(event);
        }
        return dictionaryMapper.reviewChange(changeId, request.getApprovalStatus(),
            request.getApprovalRemark(), operator);
    }

    /** 查询事件，不存在时抛出业务异常。 */
    private TrackingEvent requireEvent(Long eventId)
    {
        if (eventId == null)
        {
            throw new ServiceException("事件编号不能为空");
        }
        TrackingEvent event = dictionaryMapper.selectEventById(eventId);
        if (event == null)
        {
            throw new ServiceException("埋点事件不存在");
        }
        return event;
    }

    /** 规范化事件输入，补齐默认值。 */
    private void normalizeEvent(TrackingEvent event)
    {
        if (event == null)
        {
            throw new ServiceException("事件信息不能为空");
        }
        event.setModuleName(trim(event.getModuleName()));
        event.setEventLabel(trim(event.getEventLabel()));
        event.setEventName(trim(event.getEventName()));
        event.setTriggerTiming(trim(event.getTriggerTiming()));
        event.setMetricCodes(trim(event.getMetricCodes()));
        event.setPriority(isBlank(event.getPriority()) ? DEFAULT_PRIORITY : event.getPriority().trim());
        event.setVersionNo(isBlank(event.getVersionNo()) ? DEFAULT_VERSION : event.getVersionNo().trim());
        event.setStatus(isBlank(event.getStatus()) ? STATUS_ENABLED : event.getStatus().trim());
        event.setEventParams(event.getEventParams() == null ? Collections.emptyList() : event.getEventParams());
    }

    /** 校验事件登记内容。 */
    private void validateEvent(TrackingEvent event)
    {
        if (isBlank(event.getModuleName()) || isBlank(event.getEventLabel())
            || isBlank(event.getEventName()) || isBlank(event.getReportTerminal())
            || isBlank(event.getTriggerTiming()))
        {
            throw new ServiceException("模块、事件标签、事件名、上报端和触发时机不能为空");
        }
        if (!NAME_PATTERN.matcher(event.getEventName()).matches())
        {
            throw new ServiceException("事件名必须以小写字母开头，仅允许小写字母、数字和下划线，禁止缩写与前后缀空格");
        }
        if (!PRIORITIES.contains(event.getPriority()))
        {
            throw new ServiceException("事件优先级不正确");
        }
        if (!TERMINALS.contains(event.getReportTerminal()))
        {
            throw new ServiceException("事件上报端不正确");
        }
        validateStatus(event.getStatus());
        validateParams(event.getEventParams());
    }

    /** 校验事件参数名称、类型和重复项。 */
    private void validateParams(List<TrackingEventParam> params)
    {
        Set<String> names = new HashSet<>();
        for (TrackingEventParam param : params)
        {
            param.setParamName(trim(param.getParamName()));
            param.setDescription(trim(param.getDescription()));
            param.setRequiredFlag(isBlank(param.getRequiredFlag()) ? "0" : param.getRequiredFlag());
            param.setCommonFlag(isBlank(param.getCommonFlag()) ? "0" : param.getCommonFlag());
            if (isBlank(param.getParamName()) || !NAME_PATTERN.matcher(param.getParamName()).matches())
            {
                throw new ServiceException("事件参数名必须为小写下划线命名");
            }
            if (!PARAM_TYPES.contains(param.getParamType()))
            {
                throw new ServiceException("事件参数类型不正确");
            }
            if (dictionaryMapper.countCommonParamName(param.getParamName(), null) > 0)
            {
                throw new ServiceException("公共参数由 SDK 自动注入，事件不得重复登记：" + param.getParamName());
            }
            if (!names.add(param.getParamName()))
            {
                throw new ServiceException("事件参数不能重复：" + param.getParamName());
            }
        }
    }

    /** 保存事件参数快照。 */
    private void saveEventParams(TrackingEvent event)
    {
        List<TrackingEventParam> params = event.getEventParams() == null
            ? Collections.emptyList() : event.getEventParams();
        for (int index = 0; index < params.size(); index++)
        {
            TrackingEventParam param = params.get(index);
            param.setEventId(event.getEventId());
            param.setSortOrder(index + 1);
            dictionaryMapper.insertEventParam(param);
        }
    }

    /** 新增已通过的操作留痕记录。 */
    private void insertApprovedRecord(TrackingEvent event, String changeType, String reason, String operator)
    {
        TrackingChangeRecord record = new TrackingChangeRecord();
        record.setEventId(event.getEventId());
        record.setChangeType(changeType);
        record.setChangeReason(reason);
        record.setSnapshotJson(JSON.toJSONString(event));
        record.setApprovalStatus(APPROVAL_APPROVED);
        record.setCreateBy(operator);
        record.setApproveBy(operator);
        record.setApprovalRemark("系统即时生效");
        dictionaryMapper.insertChangeRecord(record);
    }

    /** 规范化公共参数输入。 */
    private void normalizeCommonParam(TrackingCommonParam param)
    {
        if (param == null)
        {
            throw new ServiceException("公共参数信息不能为空");
        }
        param.setParamName(trim(param.getParamName()));
        param.setDescription(trim(param.getDescription()));
        param.setVersionNo(isBlank(param.getVersionNo()) ? DEFAULT_VERSION : param.getVersionNo().trim());
        param.setStatus(isBlank(param.getStatus()) ? STATUS_ENABLED : param.getStatus().trim());
    }

    /** 校验公共参数。 */
    private void validateCommonParam(TrackingCommonParam param)
    {
        if (isBlank(param.getParamName()) || !NAME_PATTERN.matcher(param.getParamName()).matches())
        {
            throw new ServiceException("公共参数名必须为小写下划线命名");
        }
        if (!PARAM_TYPES.contains(param.getParamType()))
        {
            throw new ServiceException("公共参数类型不正确");
        }
        if (isBlank(param.getDescription()))
        {
            throw new ServiceException("公共参数说明不能为空");
        }
    }

    /** 校验事件名称唯一性。 */
    private void ensureUniqueEventName(String eventName, Long eventId)
    {
        if (dictionaryMapper.countEventName(eventName, eventId) > 0)
        {
            throw new ServiceException("事件名已登记，不能重复使用");
        }
    }

    /** 校验公共参数名称唯一性。 */
    private void ensureUniqueCommonParam(String paramName, Long commonParamId)
    {
        if (dictionaryMapper.countCommonParamName(paramName, commonParamId) > 0)
        {
            throw new ServiceException("公共参数名已存在");
        }
    }

    /** 校验状态取值。 */
    private void validateStatus(String status)
    {
        if (!EVENT_STATUSES.contains(status))
        {
            throw new ServiceException("状态取值不正确");
        }
    }

    /** 去除字符串前后空格。 */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /** 判断字符串是否为空。 */
    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
