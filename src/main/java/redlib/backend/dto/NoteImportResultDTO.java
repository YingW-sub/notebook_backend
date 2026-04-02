package redlib.backend.dto;

import lombok.Data;

@Data
public class NoteImportResultDTO {
    private String title;
    private String plainText;
    private String sourceType;
}
