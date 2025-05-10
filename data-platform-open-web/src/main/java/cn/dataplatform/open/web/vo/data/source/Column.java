package cn.dataplatform.open.web.vo.data.source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {

    private String columnLabel;
    private String columnTypeName;

}