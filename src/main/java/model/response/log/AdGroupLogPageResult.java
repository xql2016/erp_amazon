package model.response.log;

import lombok.Data;
import model.response.PageResult;

import java.util.List;

@Data
public class AdGroupLogPageResult extends PageResult {

    private List<AdGroupLog> data;
}
