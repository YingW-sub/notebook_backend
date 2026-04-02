package redlib.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReorderNotesDTO {
    private List<Long> orderedIds;
}
