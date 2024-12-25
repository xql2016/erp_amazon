package model.response.keyPlace;

import lombok.Data;
import model.response.PageResult;

import java.util.List;

@Data
public class KeyPlacePageResult extends PageResult {

    private List<KeyPlace> data;
}
