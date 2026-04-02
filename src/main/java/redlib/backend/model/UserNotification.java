package redlib.backend.model;

import java.util.Date;
import lombok.Data;

@Data
public class UserNotification {
    private Long id;
    private Integer userId;
    private String category;
    private String title;
    private String body;
    private Boolean readFlag;
    private Date createdAt;
}
