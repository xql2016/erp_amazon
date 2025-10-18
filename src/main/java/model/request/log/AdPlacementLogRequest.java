package model.request.log;

import lombok.Data;
import model.request.PageRequest;

import java.util.List;

@Data
public class AdPlacementLogRequest extends PageRequest {

    private String type; // keywords(关键词),targets(自动),targets(asin),targets(类目)

    private List<Long> campaign_id; // 广告组id,关键词广告+自动广告

    private List<Long> ad_group_id; // 广告组id,关键词广告+自动广告

    private List<Long> keyword_id; // 关键词广告有

    private List<Long> target_id; // 自动广告有,asin有,类目有

    private String key_text; // 自动广告有-[{"type":"queryBroadRelMatches"}],asin-[{"type":"asinSameAs","value":"B0DGD5G4HJ"}],类目-[{"type":"asinCategorySameAs","value":"1486784031"}]

    private String report_date;

    private String log_type; // api_log

    private Long profile_id; // hub_id

    private List<String> operate_object; // keyword(关键词),auto_target(自动),product_target(asin),product_target(类目)

    private List<String> operate_type; // bid代表改竞价

    private int show_child; // 1代表展示下一级内容,0代表不展示
}
