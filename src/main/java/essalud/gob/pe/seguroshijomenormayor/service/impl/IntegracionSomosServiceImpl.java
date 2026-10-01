package essalud.gob.pe.seguroshijomenormayor.service.impl;

import essalud.gob.pe.seguroshijomenormayor.dto.request.GenerarTockenReq;
import essalud.gob.pe.seguroshijomenormayor.dto.response.DatosUsuarioResponse;
import essalud.gob.pe.seguroshijomenormayor.dto.response.GenerarTokenResponse;
import essalud.gob.pe.seguroshijomenormayor.persistence.entity.TockenSomosEntity;

import essalud.gob.pe.seguroshijomenormayor.persistence.repository.CsamPersonRepository;
import essalud.gob.pe.seguroshijomenormayor.persistence.repository.IntegracionTokenRepository;
import essalud.gob.pe.seguroshijomenormayor.service.IntegracionSomosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class IntegracionSomosServiceImpl implements IntegracionSomosService {
    private final IntegracionTokenRepository repository;
    private final CsamPersonRepository csamPersonRepository;

    private static final String URL_APLICACION =
            "https://appsqa.essalud.gob.pe/segurocomplementario-masvida/acceso/";
    @Override
    @Transactional

    public GenerarTokenResponse generarToken(GenerarTockenReq request) {

        String token = generarTokenAleatorio();

        TockenSomosEntity entidad = new TockenSomosEntity();
        if(request.tipoDocumento().equals("01")){
            entidad.setTokenHash(hash(token));
            entidad.setTipoDocumento(request.tipoDocumento());
            entidad.setNumeroDocumento(request.numeroDocumento());
            entidad.setCodigoPlanilla(request.codigoPlanilla());
            entidad.setRegimenLaboral(request.regimenLaboral());
            entidad.setCodigoRed(request.codigoRed());
            entidad.setCorreo(request.correo());
            entidad.setFechaExpiracion(
                    LocalDateTime.now().plusMinutes(5)
            );
            entidad.setUsado(false);

            repository.save(entidad);

            return new GenerarTokenResponse(
                    token,
                    URL_APLICACION + token
            );


        }
        var personaOpt = csamPersonRepository
                .findFirstByCodEdocumentPersonaAndNumDocumentPersona(
                        request.tipoDocumento(),
                        request.numeroDocumento()
                );

        if (personaOpt.isEmpty()) {
            String numeroSinCeros =
                    quitarCerosIniciales(request.numeroDocumento());

            if (!numeroSinCeros.equals(request.numeroDocumento())) {
                personaOpt = csamPersonRepository
                        .findFirstByCodEdocumentPersonaAndNumDocumentPersona(
                                request.tipoDocumento(),
                                numeroSinCeros
                        );
                if(personaOpt.isPresent()){
                    entidad.setTokenHash(hash(token));
                    entidad.setTipoDocumento(request.tipoDocumento());
                    entidad.setNumeroDocumento(numeroSinCeros);
                    entidad.setCodigoPlanilla(request.codigoPlanilla());
                    entidad.setRegimenLaboral(request.regimenLaboral());
                    entidad.setCodigoRed(request.codigoRed());
                    entidad.setCorreo(request.correo());
                    entidad.setFechaExpiracion(
                            LocalDateTime.now().plusMinutes(5)
                    );
                    entidad.setUsado(false);

                    repository.save(entidad);

                    return new GenerarTokenResponse(
                            token,
                            URL_APLICACION + token
                    );

                }
                throw  new RuntimeException("No se encontró la persona");


            }
        }
        entidad.setTokenHash(hash(token));
        entidad.setTipoDocumento(request.tipoDocumento());
        entidad.setNumeroDocumento(request.numeroDocumento());
        entidad.setCodigoPlanilla(request.codigoPlanilla());
        entidad.setRegimenLaboral(request.regimenLaboral());
        entidad.setCodigoRed(request.codigoRed());
        entidad.setCorreo(request.correo());
        entidad.setFechaExpiracion(
                LocalDateTime.now().plusMinutes(5)
        );
        entidad.setUsado(false);

        repository.save(entidad);

        return new GenerarTokenResponse(
                token,
                URL_APLICACION + token
        );



    }
    private String quitarCerosIniciales(String numero) {
        if (numero == null || numero.isBlank()) {
            return numero;
        }

        String resultado = numero.replaceFirst("^0+(?!$)", "");
        return resultado;
    }
    @Override
    @Transactional

    public DatosUsuarioResponse validarToken(String token) {
        String tokenHash = hash(token);

        TockenSomosEntity entidad = repository
                .findByTokenHashAndUsadoFalse(tokenHash)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Token inválido o ya utilizado"
                        )
                );

        if (LocalDateTime.now()
                .isAfter(entidad.getFechaExpiracion())) {

            throw new RuntimeException("Token expirado");
        }

        entidad.setUsado(true);
        repository.save(entidad);

        return new DatosUsuarioResponse(
                entidad.getTipoDocumento(),
                entidad.getNumeroDocumento(),
                entidad.getCodigoPlanilla(),
                entidad.getRegimenLaboral(),
                entidad.getCodigoRed(),
                entidad.getCorreo()

        );
    }

    private String generarTokenAleatorio() {
        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] resultado = digest.digest(
                    valor.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(resultado);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "No se pudo generar el hash del token", e
            );
        }
    }
}
