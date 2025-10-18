package model.response.log;

import lombok.Data;

@Data
public class AdPlacementLog {

    private String created_at; // 2025-10-16 11:47:49

    private String log_type; // api_log

    private String method; // 更新

    private String code; // SUCCESS

    private String campaignId;

    private String adGroupId;

    private String keywordId;

    private String targetId;

    private String portfolio_id;

    private String old_logs;

    private String new_logs;
}
