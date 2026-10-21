-- 批次 1 埋点字典同步脚本
-- 在 BI 后台主库执行；依赖 sql/bi_base.sql 已创建的三张埋点字典表
-- 按 event_name 和 (event_id, param_name) 幂等同步，可重复执行

INSERT INTO bi_tracking_event
    (module_name, priority, event_label, event_name, report_terminal, trigger_timing,
     metric_codes, version_no, status, create_by, create_time, remark)
VALUES
    ('启动', 'P0', '应用启动', 'app_launch', 'CLIENT', '冷启动或热启动回到前台', 'U2 U3 U4 U5 U6 U7', '1.2', '0', 'admin', SYSDATE(), '客户端生命周期事件'),
    ('账号', 'P0', '注册成功', 'account_register_success', 'SERVER', '注册校验通过', 'U1 U8', '1.2', '0', 'admin', SYSDATE(), '服务端注册结果为准'),
    ('账号', 'P0', '登录成功', 'account_login_success', 'SERVER', '登录校验通过', NULL, '1.2', '0', 'admin', SYSDATE(), '服务端登录结果为准'),
    ('房间', 'P0', '进房', 'room_enter', 'CLIENT', '用户进入房间', 'P3', '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P0', '离房', 'room_exit', 'CLIENT', '用户离开房间', 'P4', '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('礼物', 'P0', '送礼成功', 'gift_send_success', 'SERVER', '送礼扣费成功', 'R11 R12 R13', '1.2', '0', 'admin', SYSDATE(), '服务端结算结果为准'),
    ('礼物', 'P0', '卡券发放成功', 'coupon_receive_success', 'SERVER', '卡券进入背包', NULL, '1.2', '0', 'admin', SYSDATE(), '活动或补偿发放结果'),
    ('充值', 'P0', '充值成功', 'recharge_success', 'SERVER', '支付回调成功', 'R1 R2 R3 R4 R5 R6 R7 R8', '1.2', '0', 'admin', SYSDATE(), '服务端支付结果为准'),
    ('充值', 'P0', '币商派发成功', 'agent_distribute_success', 'SERVER', '代理派发金币成功', 'R22', '1.2', '0', 'admin', SYSDATE(), '服务端资金派发结果'),
    ('提现', 'P0', '提现提交', 'withdraw_submit', 'CLIENT', '用户提交提现申请', 'R18 R19', '1.2', '0', 'admin', SYSDATE(), '客户端提交意图'),
    ('提现', 'P0', '提现成功', 'withdraw_success', 'SERVER', '提现到账', 'R18 R19', '1.2', '0', 'admin', SYSDATE(), '服务端到账结果'),
    ('提现', 'P0', '提现失败', 'withdraw_failed', 'SERVER', '提现驳回或失败', 'R18 R19', '1.2', '0', 'admin', SYSDATE(), '服务端失败结果'),
    ('公会', 'P0', '入会成功', 'guild_join_success', 'SERVER', '申请审核通过或签约生效', NULL, '1.2', '0', 'admin', SYSDATE(), '服务端审核结果'),
    ('玩法', 'P0', '玩法投注', 'game_bet', 'SERVER', '投注扣费成功', 'P11 P14', '1.2', '0', 'admin', SYSDATE(), '使用 game_type 区分玩法'),
    ('玩法', 'P0', '玩法结算', 'game_settle', 'SERVER', '开奖结算完成', 'P12 P13', '1.2', '0', 'admin', SYSDATE(), '服务端结算结果'),
    ('玩法', 'P0', '补偿发放成功', 'compensation_grant_success', 'SERVER', '补偿券批量发放成功', NULL, '1.2', '0', 'admin', SYSDATE(), '砸蛋补偿链路'),
    ('玩法', 'P0', '补偿触达曝光', 'compensation_popup_exposure', 'CLIENT', '补偿到账弹窗展示', NULL, '1.2', '0', 'admin', SYSDATE(), '砸蛋补偿链路'),
    ('玩法', 'P0', '补偿砸核销成功', 'compensation_use_success', 'SERVER', '补偿券核销并发放奖励', NULL, '1.2', '0', 'admin', SYSDATE(), '砸蛋补偿链路'),
    ('秘密花园', 'P0', '订阅成功', 'secret_garden_subscribe_success', 'SERVER', '订阅扣费成功', NULL, '1.2', '0', 'admin', SYSDATE(), '秘密花园付费主链路'),
    ('秘密花园', 'P0', '自动续订成功', 'secret_garden_subscribe_renew_success', 'SERVER', '到期自动扣费成功', NULL, '1.2', '0', 'admin', SYSDATE(), '秘密花园付费主链路'),
    ('秘密花园', 'P0', '单条购买成功', 'secret_garden_single_buy_success', 'SERVER', '单条内容扣费成功', NULL, '1.2', '0', 'admin', SYSDATE(), '秘密花园付费主链路'),
    ('秘密花园', 'P0', '花园送礼成功', 'secret_garden_gift_send_success', 'SERVER', '内容页打赏送礼成功', NULL, '1.2', '0', 'admin', SYSDATE(), '秘密花园付费主链路'),
    ('邀请', 'P0', '邀请绑定成功', 'invite_bind_success', 'SERVER', '新用户成功绑定邀请码', 'U13 U14 U15 U16', '1.2', '0', 'admin', SYSDATE(), '邀请关系建立事件')
ON DUPLICATE KEY UPDATE
    module_name = VALUES(module_name), priority = VALUES(priority), event_label = VALUES(event_label),
    report_terminal = VALUES(report_terminal), trigger_timing = VALUES(trigger_timing),
    metric_codes = VALUES(metric_codes), version_no = VALUES(version_no), status = VALUES(status),
    update_by = 'admin', update_time = SYSDATE(), remark = VALUES(remark);

-- 补齐埋点规划中尚未登记的客户端事件，供客户端 SDK 校验与字典发布使用。
INSERT INTO bi_tracking_event
    (module_name, priority, event_label, event_name, report_terminal, trigger_timing,
     metric_codes, version_no, status, create_by, create_time, remark)
VALUES
    ('账号', 'P1', '注册页曝光', 'account_register_exposure', 'CLIENT', '进入登录或注册页', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端页面曝光事件'),
    ('账号', 'P2', '退出登录', 'account_logout_complete', 'CLIENT', '设置页退出登录完成', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('账号', 'P2', '访问他人主页', 'profile_visit', 'CLIENT', '打开他人主页', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('账号', 'P2', '资料卡曝光', 'profilecard_exposure', 'CLIENT', '资料卡实际展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('账号', 'P1', '实名提交', 'realperson_submit', 'CLIENT', '提交实名审核', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('账号', 'P2', '举报提交', 'report_submit', 'CLIENT', '提交举报', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('房间', 'P1', '房间列表曝光', 'room_list_exposure', 'CLIENT', '首页房间列表展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('房间', 'P2', '申请上麦', 'room_seat_apply', 'CLIENT', '点击空麦位或申请上麦', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P2', '开麦', 'room_mic_open', 'CLIENT', '麦克风开启', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P2', '闭麦', 'room_mic_close', 'CLIENT', '麦克风关闭', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P1', '公屏发言', 'room_publicscreen_send', 'CLIENT', '公屏消息发送', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P2', '分享房间', 'room_share_click', 'CLIENT', '点击房间分享', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P2', '房间最小化', 'room_minimize_click', 'CLIENT', '收起房间悬浮窗', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P2', '搜索点击', 'search_click', 'CLIENT', '点击搜索结果', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('礼物', 'P1', '礼物面板曝光', 'gift_panel_exposure', 'CLIENT', '打开礼物面板', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('礼物', 'P2', '背包曝光', 'backpack_exposure', 'CLIENT', '打开背包', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('礼物', 'P2', '礼物墙曝光', 'giftwall_exposure', 'CLIENT', '查看礼物墙', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('充值', 'P1', '充值面板曝光', 'recharge_panel_exposure', 'CLIENT', '打开充值面板', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('充值', 'P1', '充值下单', 'recharge_submit', 'CLIENT', '提交充值订单', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('VIP', 'P2', 'VIP页曝光', 'vip_page_exposure', 'CLIENT', '进入VIP页', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('VIP', 'P2', '贵族页曝光', 'noble_page_exposure', 'CLIENT', '进入贵族页', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('公会', 'P2', '公会页曝光', 'guild_page_exposure', 'CLIENT', '进入公会页面', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('公会', 'P1', '入会申请提交', 'guild_apply_submit', 'CLIENT', '提交入会申请', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('消息', 'P1', '私信发送完成', 'dm_send_complete', 'CLIENT', '私信发送成功', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('消息', 'P2', '会话页曝光', 'dm_page_exposure', 'CLIENT', '打开私信会话', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('动态', 'P1', '动态发布提交', 'feed_publish_submit', 'CLIENT', '点击发布动态', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('动态', 'P2', '动态曝光', 'feed_exposure', 'CLIENT', '动态卡片曝光', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('动态', 'P2', '动态点赞', 'feed_like_click', 'CLIENT', '点击动态点赞', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('动态', 'P2', '动态评论提交', 'feed_comment_submit', 'CLIENT', '提交动态评论', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('消息', 'P1', '推送点击', 'push_click', 'CLIENT', '打开推送', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('房间', 'P2', '宝箱面板曝光', 'roombox_exposure', 'CLIENT', '打开宝箱面板', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('榜单', 'P2', '榜单页曝光', 'rank_page_exposure', 'CLIENT', '打开榜单', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('玩法', 'P1', '玩法曝光', 'game_exposure', 'CLIENT', '玩法入口或面板展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('玩法', 'P1', '进入玩法', 'game_enter', 'CLIENT', '进入玩法页面', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('玩法', 'P2', '退出玩法', 'game_exit', 'CLIENT', '离开玩法页面', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('玩法', 'P1', '概率公示页曝光', 'game_odds_exposure', 'CLIENT', '打开概率公示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('玩法', 'P1', '补偿触达点击', 'compensation_popup_click', 'CLIENT', '点击补偿触达弹窗', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('运营', 'P2', 'Banner曝光', 'banner_exposure', 'CLIENT', 'Banner进入可视区', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('运营', 'P2', 'Banner点击', 'banner_click', 'CLIENT', '点击Banner', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('运营', 'P2', '活动弹窗曝光', 'actpopup_exposure', 'CLIENT', '活动弹窗展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('运营', 'P2', '活动弹窗点击', 'actpopup_click', 'CLIENT', '点击活动弹窗', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('运营', 'P1', '人气主播弹窗曝光', 'anchorpopup_exposure', 'CLIENT', '主播弹窗展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('运营', 'P1', '人气主播弹窗点击', 'anchorpopup_click', 'CLIENT', '点击主播弹窗', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('运营', 'P2', '更新弹窗曝光', 'appupdate_popup_exposure', 'CLIENT', '更新弹窗展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('运营', 'P2', '更新确认点击', 'appupdate_confirm_click', 'CLIENT', '点击立即更新', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('秘密花园', 'P1', '入口曝光', 'secret_garden_entry_exposure', 'CLIENT', '我的页入口展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('秘密花园', 'P1', '图文遮罩曝光', 'secret_garden_image_blur_exposure', 'CLIENT', '图文遮罩展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('秘密花园', 'P1', '试看完成', 'secret_garden_video_trial_complete', 'CLIENT', '试看结束触发付费弹窗', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('秘密花园', 'P1', '付费弹窗曝光', 'secret_garden_pay_popup_exposure', 'CLIENT', '付费弹窗展示', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端曝光事件'),
    ('秘密花园', 'P1', '订阅点击', 'secret_garden_subscribe_click', 'CLIENT', '点击订阅', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('秘密花园', 'P1', '单条购买点击', 'secret_garden_single_buy_click', 'CLIENT', '点击单条购买', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件'),
    ('秘密花园', 'P2', '评论发布', 'secret_garden_comment_send', 'CLIENT', '提交评论', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端提交事件'),
    ('邀请', 'P2', '邀请分享点击', 'invite_share_click', 'CLIENT', '点击邀请分享入口', NULL, '1.2', '0', 'admin', SYSDATE(), '客户端行为事件')
ON DUPLICATE KEY UPDATE
    module_name = VALUES(module_name), priority = VALUES(priority), event_label = VALUES(event_label),
    report_terminal = VALUES(report_terminal), trigger_timing = VALUES(trigger_timing),
    metric_codes = VALUES(metric_codes), version_no = VALUES(version_no), status = VALUES(status),
    update_by = 'admin', update_time = SYSDATE(), remark = VALUES(remark);

-- 同步批次 1 私有参数；room_id、room_type、scene 等公共参数以 common_flag=1 登记。
INSERT INTO bi_tracking_event_param
    (event_id, param_name, param_type, required_flag, common_flag, description, sort_order)
SELECT event.event_id, source.param_name, source.param_type, source.required_flag,
       source.common_flag, source.description, source.sort_order
FROM bi_tracking_event event
INNER JOIN (
    SELECT 'app_launch' event_name, 'launch_type' param_name, 'string' param_type, '1' required_flag, '0' common_flag, '启动类型，cold 或 hot' description, 1 sort_order
    UNION ALL SELECT 'account_register_success', 'register_channel', 'string', '1', '0', '注册渠道', 1
    UNION ALL SELECT 'account_register_success', 'register_country', 'string', '1', '0', '注册国家', 2
    UNION ALL SELECT 'account_login_success', 'login_type', 'string', '1', '0', '登录方式', 1
    UNION ALL SELECT 'room_enter', 'room_id', 'long', '1', '1', '进入的房间编号', 1
    UNION ALL SELECT 'room_enter', 'room_type', 'string', '0', '1', '进入的房间类型', 2
    UNION ALL SELECT 'room_enter', 'source', 'string', '1', '0', '进入来源', 3
    UNION ALL SELECT 'room_exit', 'room_id', 'long', '1', '1', '离开的房间编号', 1
    UNION ALL SELECT 'room_exit', 'stay_duration', 'long', '1', '0', '停留时长，单位秒', 2
    UNION ALL SELECT 'gift_send_success', 'gift_id', 'integer', '1', '0', '礼物编号', 1
    UNION ALL SELECT 'gift_send_success', 'gift_value', 'long', '1', '0', '礼物价值', 2
    UNION ALL SELECT 'gift_send_success', 'receiver_id', 'long', '1', '0', '收礼用户编号', 3
    UNION ALL SELECT 'gift_send_success', 'combo_count', 'integer', '1', '0', '连击数量', 4
    UNION ALL SELECT 'gift_send_success', 'scene', 'string', '1', '1', '送礼场景', 5
    UNION ALL SELECT 'gift_send_success', 'gift_source', 'string', '1', '0', '礼物来源', 6
    UNION ALL SELECT 'gift_send_success', 'is_locked_released', 'integer', '1', '0', '是否释放锁定礼物', 7
    UNION ALL SELECT 'coupon_receive_success', 'coupon_type', 'string', '1', '0', '卡券类型', 1
    UNION ALL SELECT 'coupon_receive_success', 'coupon_source', 'string', '1', '0', '卡券来源', 2
    UNION ALL SELECT 'coupon_receive_success', 'count', 'integer', '1', '0', '发放数量', 3
    UNION ALL SELECT 'coupon_receive_success', 'expire_time', 'long', '1', '0', '过期时间戳', 4
    UNION ALL SELECT 'recharge_success', 'product_id', 'string', '1', '0', '充值商品编号', 1
    UNION ALL SELECT 'recharge_success', 'pay_channel', 'string', '1', '0', '支付渠道', 2
    UNION ALL SELECT 'recharge_success', 'amount', 'decimal', '1', '0', '支付金额', 3
    UNION ALL SELECT 'recharge_success', 'coin_amount', 'long', '1', '0', '到账金币数量', 4
    UNION ALL SELECT 'agent_distribute_success', 'agent_id', 'long', '1', '0', '币商用户编号', 1
    UNION ALL SELECT 'agent_distribute_success', 'target_id', 'long', '1', '0', '收款用户编号', 2
    UNION ALL SELECT 'agent_distribute_success', 'coin_amount', 'long', '1', '0', '派发金币数量', 3
    UNION ALL SELECT 'withdraw_submit', 'amount', 'decimal', '1', '0', '提现金额', 1
    UNION ALL SELECT 'withdraw_submit', 'currency', 'string', '1', '0', '提现币种', 2
    UNION ALL SELECT 'withdraw_submit', 'withdraw_type', 'string', '1', '0', '提现类型', 3
    UNION ALL SELECT 'withdraw_success', 'amount', 'decimal', '1', '0', '到账金额', 1
    UNION ALL SELECT 'withdraw_success', 'currency', 'string', '1', '0', '提现币种', 2
    UNION ALL SELECT 'withdraw_success', 'withdraw_type', 'string', '1', '0', '提现类型', 3
    UNION ALL SELECT 'withdraw_failed', 'fail_reason', 'string', '1', '0', '失败原因', 1
    UNION ALL SELECT 'withdraw_failed', 'withdraw_type', 'string', '1', '0', '提现类型', 2
    UNION ALL SELECT 'guild_join_success', 'guild_id', 'long', '1', '0', '公会编号', 1
    UNION ALL SELECT 'guild_join_success', 'profit_ratio', 'decimal', '1', '0', '分成比例', 2
    UNION ALL SELECT 'game_bet', 'game_type', 'string', '1', '0', '玩法类型标识', 1
    UNION ALL SELECT 'game_bet', 'bet_amount', 'long', '1', '0', '投注金额', 2
    UNION ALL SELECT 'game_bet', 'round_id', 'string', '1', '0', '局次编号', 3
    UNION ALL SELECT 'game_bet', 'pool_id', 'string', '1', '0', '奖池类型', 4
    UNION ALL SELECT 'game_bet', 'batch', 'integer', '1', '0', '投注批次', 5
    UNION ALL SELECT 'game_bet', 'is_coupon', 'integer', '1', '0', '是否使用卡券', 6
    UNION ALL SELECT 'game_bet', 'coupon_source', 'string', '0', '0', '卡券来源', 7
    UNION ALL SELECT 'game_bet', 'user_tag', 'string', '1', '0', '用户标签', 8
    UNION ALL SELECT 'game_settle', 'game_type', 'string', '1', '0', '玩法类型标识', 1
    UNION ALL SELECT 'game_settle', 'bet_amount', 'long', '1', '0', '投注金额', 2
    UNION ALL SELECT 'game_settle', 'payout', 'long', '1', '0', '派奖金额', 3
    UNION ALL SELECT 'game_settle', 'round_id', 'string', '1', '0', '局次编号', 4
    UNION ALL SELECT 'game_settle', 'pool_id', 'string', '1', '0', '奖池类型', 5
    UNION ALL SELECT 'game_settle', 'tier', 'string', '1', '0', '奖励档位', 6
    UNION ALL SELECT 'game_settle', 'is_pity', 'integer', '1', '0', '是否触发保底', 7
    UNION ALL SELECT 'game_settle', 'pity_type', 'string', '0', '0', '保底类型', 8
    UNION ALL SELECT 'game_settle', 'user_tag', 'string', '1', '0', '用户标签', 9
    UNION ALL SELECT 'game_settle', 'is_compensation', 'integer', '1', '0', '是否补偿结算', 10
    UNION ALL SELECT 'compensation_grant_success', 'game_type', 'string', '1', '0', '玩法类型', 1
    UNION ALL SELECT 'compensation_grant_success', 'coupon_count', 'integer', '1', '0', '补偿券数量', 2
    UNION ALL SELECT 'compensation_grant_success', 'loss_amount', 'long', '1', '0', '亏损金额', 3
    UNION ALL SELECT 'compensation_grant_success', 'user_tag', 'string', '1', '0', '用户标签', 4
    UNION ALL SELECT 'compensation_grant_success', 'trigger_type', 'string', '1', '0', '触发类型', 5
    UNION ALL SELECT 'compensation_popup_exposure', 'trigger_type', 'string', '1', '0', '触发类型', 1
    UNION ALL SELECT 'compensation_popup_exposure', 'available_count', 'integer', '1', '0', '可用券数量', 2
    UNION ALL SELECT 'compensation_use_success', 'game_type', 'string', '1', '0', '玩法类型', 1
    UNION ALL SELECT 'compensation_use_success', 'round_id', 'string', '1', '0', '局次编号', 2
    UNION ALL SELECT 'compensation_use_success', 'tier', 'string', '1', '0', '奖励档位', 3
    UNION ALL SELECT 'compensation_use_success', 'gift_value', 'long', '1', '0', '礼物价值', 4
    UNION ALL SELECT 'compensation_use_success', 'real_cost', 'long', '1', '0', '实际成本', 5
    UNION ALL SELECT 'compensation_use_success', 'nth_time', 'integer', '1', '0', '第几次核销', 6
    UNION ALL SELECT 'secret_garden_subscribe_success', 'creator_id', 'long', '1', '0', '创作者编号', 1
    UNION ALL SELECT 'secret_garden_subscribe_success', 'price', 'decimal', '1', '0', '订阅价格', 2
    UNION ALL SELECT 'secret_garden_subscribe_success', 'is_renew', 'integer', '1', '0', '是否续订', 3
    UNION ALL SELECT 'secret_garden_subscribe_renew_success', 'creator_id', 'long', '1', '0', '创作者编号', 1
    UNION ALL SELECT 'secret_garden_subscribe_renew_success', 'price', 'decimal', '1', '0', '续订价格', 2
    UNION ALL SELECT 'secret_garden_single_buy_success', 'content_id', 'long', '1', '0', '内容编号', 1
    UNION ALL SELECT 'secret_garden_single_buy_success', 'content_type', 'string', '1', '0', '内容类型', 2
    UNION ALL SELECT 'secret_garden_single_buy_success', 'price', 'decimal', '1', '0', '购买价格', 3
    UNION ALL SELECT 'secret_garden_gift_send_success', 'gift_id', 'integer', '1', '0', '礼物编号', 1
    UNION ALL SELECT 'secret_garden_gift_send_success', 'gift_value', 'long', '1', '0', '礼物价值', 2
    UNION ALL SELECT 'secret_garden_gift_send_success', 'creator_id', 'long', '1', '0', '创作者编号', 3
    UNION ALL SELECT 'invite_bind_success', 'invite_code', 'string', '1', '0', '邀请码', 1
    UNION ALL SELECT 'invite_bind_success', 'inviter_id', 'long', '1', '0', '邀请人编号', 2
) source ON source.event_name = event.event_name
ON DUPLICATE KEY UPDATE
    param_type = VALUES(param_type), required_flag = VALUES(required_flag),
    common_flag = VALUES(common_flag), description = VALUES(description), sort_order = VALUES(sort_order);

-- 同步 SDK 自动注入公共参数；已有同名记录仅更新字段定义。
INSERT INTO bi_tracking_common_param
    (param_name, param_type, description, version_no, status, create_by, create_time)
VALUES
    ('client_version', 'string', '客户端版本号', '1.2', '0', 'admin', SYSDATE()),
    ('os', 'string', '客户端操作系统', '1.2', '0', 'admin', SYSDATE()),
    ('network_type', 'string', '网络类型', '1.2', '0', 'admin', SYSDATE()),
    ('device_model', 'string', '设备型号', '1.2', '0', 'admin', SYSDATE()),
    ('app_language', 'string', '应用语言', '1.2', '0', 'admin', SYSDATE()),
    ('room_id', 'long', '当前房间编号，无房间上下文时为空', '1.2', '0', 'admin', SYSDATE()),
    ('room_type', 'string', '当前房间类型', '1.2', '0', 'admin', SYSDATE()),
    ('family_id', 'long', '用户所属公会编号，未加入时为空', '1.2', '0', 'admin', SYSDATE()),
    ('country', 'string', '事件国家或地区编码', '1.2', '0', 'admin', SYSDATE()),
    ('currency', 'string', '结算币种编码', '1.2', '0', 'admin', SYSDATE()),
    ('scene', 'string', '事件发生的业务场景', '1.2', '0', 'admin', SYSDATE()),
    ('ab_group', 'string', '用户命中的实验分组', '1.2', '0', 'admin', SYSDATE()),
    ('attribution_channel', 'string', '用户归因渠道', '1.2', '0', 'admin', SYSDATE()),
    ('app_package', 'string', '应用包名或发行标识', '1.2', '0', 'admin', SYSDATE()),
    ('region', 'string', '运营大区标识，由服务端注入', '1.2', '0', 'admin', SYSDATE()),
    ('timezone', 'string', '设备 IANA 时区', '1.2', '0', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
    param_type = VALUES(param_type), description = VALUES(description), version_no = VALUES(version_no),
    status = VALUES(status), update_by = 'admin', update_time = SYSDATE();

-- 记录批次 1 已审批的事件登记，供 App 版本发布核对读取；按事件和变更原因幂等执行。
INSERT INTO bi_tracking_change_record
    (event_id, change_type, change_reason, snapshot_json, approval_status,
     create_by, create_time, approve_by, approve_time, approval_remark)
SELECT event.event_id,
       'REGISTER',
       '批次1埋点字典同步',
       JSON_OBJECT(
           'eventId', event.event_id,
           'moduleName', event.module_name,
           'priority', event.priority,
           'eventLabel', event.event_label,
           'eventName', event.event_name,
           'reportTerminal', event.report_terminal,
           'triggerTiming', event.trigger_timing,
           'metricCodes', event.metric_codes,
           'versionNo', event.version_no,
           'status', event.status,
           'eventParams', COALESCE(
               (SELECT JSON_ARRAYAGG(JSON_OBJECT(
                   'paramName', param.param_name,
                   'paramType', param.param_type,
                   'requiredFlag', param.required_flag,
                   'commonFlag', param.common_flag,
                   'description', param.description,
                   'sortOrder', param.sort_order))
                FROM bi_tracking_event_param param
                WHERE param.event_id = event.event_id),
               JSON_ARRAY())),
       '1',
       'admin',
       SYSDATE(),
       'admin',
       SYSDATE(),
       '批次1埋点字典同步已审批'
FROM bi_tracking_event event
WHERE event.version_no = '1.2'
  AND event.priority = 'P0'
  AND NOT EXISTS (
      SELECT 1
      FROM bi_tracking_change_record change_record
      WHERE change_record.event_id = event.event_id
        AND change_record.change_type = 'REGISTER'
        AND change_record.change_reason = '批次1埋点字典同步'
  );
