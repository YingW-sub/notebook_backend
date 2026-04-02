package redlib.backend.vo;

import java.util.Date;
import lombok.Data;

@Data
public class UserNotificationVO {
    private Long id;
    private String category;
    private String title;
    private String body;
    private Boolean read;
    private Date createdAt;
}
