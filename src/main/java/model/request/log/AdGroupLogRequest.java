package model.request.log;

import lombok.Data;
import model.request.PageRequest;

import java.util.List;

@Data
public class AdGroupLogRequest extends PageRequest {

    private String type; // campaigns 代表广告组

    private List<Long> campaign_id; // 广告组id

    private String report_date;

    private String log_type; // api_log

    private Long profile_id; // hub_id

    private List<String> operate_object; // campaign

    private List<String> operate_type; // bid代表改竞价

    private int show_child; // 1代表展示下一级内容,0代表不展示
}
