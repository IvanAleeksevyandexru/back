package ru.gosuslugi.pgu.fs.delirium.model.declaration;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.gosuslugi.pgu.dto.ApplicantRole;

@Data
@AllArgsConstructor
public class DeliriumApplicantDto {

    /**
     * Идентификатор участника заявления
     */
    private long oid;

    /**
     * Роль участника заявления
     */
    private ApplicantRole role;

}
