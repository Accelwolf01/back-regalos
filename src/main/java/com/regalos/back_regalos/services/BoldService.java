package com.regalos.back_regalos.services;

import com.regalos.back_regalos.repositories.StoreSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class BoldService {

    private final StoreSettingRepository settingRepository;

    /**
     * Genera el hash de integridad SHA-256 requerido por Bold.
     * Estructura: {Identificador}{Monto}{Divisa}{LlaveSecreta}
     */
    public String generateIntegritySignature(String orderId, Long amount, String currency) {
        String secretKey = settingRepository.findByConfigKey("bold_secret_key")
                .map(s -> s.getConfigValue())
                .orElse("");

        // La llave secreta (Integridad) es obligatoria para que Bold valide el pago
        String rawString = orderId + amount + currency + secretKey;
        return sha256(rawString);
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            // Importante: Bold espera el hash en minúsculas
            return hexString.toString().toLowerCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Error al generar hash SHA-256", ex);
        }
    }
}
