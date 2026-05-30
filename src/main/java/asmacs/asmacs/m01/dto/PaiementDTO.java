package asmacs.asmacs.m01.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementDTO {

    @NotBlank(message = "L'opérateur est obligatoire")
    private String operateur; // MTN_MOMO | ORANGE_MONEY

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String numeroTelephone;

    @NotBlank(message = "La référence de transaction est obligatoire")
    private String referenceTransaction;

    private Double  montant;
    private Long    candidatureId;
}