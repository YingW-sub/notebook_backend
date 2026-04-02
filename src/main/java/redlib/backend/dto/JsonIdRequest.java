package redlib.backend.dto;

import lombok.Data;

/** 前端以 JSON 对象传主键，避免 @RequestBody Long 与 form-urlencoded 不兼容 */
@Data
public class JsonIdRequest {
    private Long id;
}
