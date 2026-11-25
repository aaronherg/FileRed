package com.aarondevs.filered.Services.Administracion;

import com.aarondevs.filered.Models.Administracion.ConfiguracionNotificacion;
import com.aarondevs.filered.Parameters.Administracion.ServicioSmtpParameters;
import com.aarondevs.filered.Parameters.Administracion.ServicioWhatsappParameters;

public interface ServicioNotificacionService {
    ConfiguracionNotificacion obtener();

    ConfiguracionNotificacion guardarSmtp(ServicioSmtpParameters parametro, String actor);

    void probarSmtp(String destino);

    ConfiguracionNotificacion guardarWhatsapp(ServicioWhatsappParameters parametro, String actor);

    void probarWhatsapp(String destino);
}
