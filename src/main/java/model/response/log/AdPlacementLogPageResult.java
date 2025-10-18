package model.response.log;

import lombok.Data;
import model.response.PageResult;

import java.util.List;

@Data
public class AdPlacementLogPageResult extends PageResult {

    private List<AdPlacementLog> data;
}
