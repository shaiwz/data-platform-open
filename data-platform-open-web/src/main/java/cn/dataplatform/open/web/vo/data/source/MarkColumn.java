package cn.dataplatform.open.web.vo.data.source;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MarkColumn {

    @NotEmpty
    private String columnName;

    /**
     * @see cn.dataplatform.open.common.enums.MaskType
     */
    @NotEmpty
    private String maskType;

}
