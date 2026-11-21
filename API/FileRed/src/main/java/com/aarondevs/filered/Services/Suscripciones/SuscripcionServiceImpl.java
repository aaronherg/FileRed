package com.aarondevs.filered.Services.Suscripciones;

import com.aarondevs.filered.DAO.Administracion.PlanDataSet;
import com.aarondevs.filered.DAO.Suscripciones.SuscripcionDataSet;
import com.aarondevs.filered.Models.Administracion.Plan;
import com.aarondevs.filered.Models.Suscripciones.DetalleSuscripcion;
import com.aarondevs.filered.Models.Suscripciones.Suscripcion;
import com.aarondevs.filered.Parameters.Suscripciones.SuscripcionAsignarParameters;
import com.aarondevs.filered.Services.Administracion.PlanService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuscripcionServiceImpl implements SuscripcionService {
    private final SuscripcionDataSet datos;
    private final PlanDataSet planes;
    private final PagoSuscripcionService pagos;

    public SuscripcionServiceImpl(SuscripcionDataSet datos, PlanDataSet planes, PagoSuscripcionService pagos) {
        this.datos = datos;
        this.planes = planes;
        this.pagos = pagos;
    }

    @Override
    @Transactional
    public Suscripcion asignar(SuscripcionAsignarParameters parametro, String actor, Long usuarioAdministracionId) {
        if (parametro == null || parametro.getClienteId() == null || parametro.getPlanId() == null) {
            throw new IllegalArgumentException("clienteId y planId son requeridos");
        }
        String tipoCobro = parametro.getTipoCobro() == null ? PREPAGO : parametro.getTipoCobro().trim().toUpperCase();
        if (!Set.of(PREPAGO, POSTPAGO).contains(tipoCobro)) throw new IllegalArgumentException("tipoCobro debe ser PREPAGO o POSTPAGO");
        Suscripcion suscripcion = datos.findByClienteId(parametro.getClienteId());
        if (suscripcion == null) throw new IllegalArgumentException("El cliente no tiene suscripción registrada");
        Plan plan = planes.findById(parametro.getPlanId());
        if (plan == null || !"Activo".equals(plan.getEstado())) throw new IllegalArgumentException("El plan no está disponible");

        boolean adicionales = POSTPAGO.equals(tipoCobro) && Boolean.TRUE.equals(parametro.getPermitirAdicionales());
        long usado = datos.espacioUsadoBytes(parametro.getClienteId());
        if (!adicionales && plan.getEspacioGb() != null && usado > SuscripcionService.limiteBytes(plan.getEspacioGb())) {
            throw new IllegalArgumentException("El cliente ya usa más espacio del que incluye el plan " + plan.getNombre());
        }

        datos.cerrarVigente(parametro.getClienteId(), "Reemplazado", actor);
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = PlanService.calcularFechaFin(inicio, plan.getModalidad());
        Long detalleId = datos.createDetalle(suscripcion.getSuscripcionId(), plan.getPlanId(), inicio, fin, tipoCobro,
                adicionales, POSTPAGO.equals(tipoCobro), null, actor);
        if (plan.getPrecio().compareTo(BigDecimal.ZERO) > 0) {
            Long pagoId = datos.createPago(detalleId, plan.getPrecio(), POSTPAGO.equals(tipoCobro) ? fin : inicio, actor);
            if (parametro.getMontoPagado() != null && parametro.getMontoPagado().compareTo(BigDecimal.ZERO) > 0) {
                pagos.registrar(pagoId, parametro.getMontoPagado(), parametro.getMetodoPago(), parametro.getReferenciaPago(),
                        "Pago registrado al asignar el plan", actor, usuarioAdministracionId);
            }
        }
        return datos.findByClienteId(parametro.getClienteId());
    }

    @Override
    public int cancelar(Long clienteId, String actor) {
        if (clienteId == null) throw new IllegalArgumentException("clienteId es requerido");
        return datos.cerrarVigente(clienteId, "Cancelado", actor);
    }

    @Override
    public Suscripcion cliente(Long clienteId) {
        return datos.findByClienteId(clienteId);
    }

    @Override
    public Object pages(Integer page, Integer size, String buscar) {
        return datos.findAllPages(page, size, buscar);
    }

    @Override
    public DetalleSuscripcion vigente(Long clienteId) {
        return datos.findVigente(clienteId);
    }

    @Override
    public Object historial(Long clienteId) {
        return datos.findDetalles(clienteId);
    }

    @Override
    public long espacioUsado(Long clienteId) {
        return datos.espacioUsadoBytes(clienteId);
    }

    @Override
    public Map<String, Object> consumo(Long clienteId) {
        DetalleSuscripcion vigente = datos.findVigente(clienteId);
        long usado = datos.espacioUsadoBytes(clienteId);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("vigente", vigente);
        resultado.put("espacioUsadoBytes", usado);
        resultado.put("espacioIlimitado", vigente != null && vigente.getEspacioGb() == null);
        resultado.put("espacioLimiteBytes", vigente == null || vigente.getEspacioGb() == null ? 0 : SuscripcionService.limiteBytes(vigente.getEspacioGb()));
        resultado.put("historial", datos.findDetalles(clienteId));
        return resultado;
    }

    @Override
    @Transactional
    public void renovarCadena(DetalleSuscripcion vencido, LocalDateTime limite) {
        DetalleSuscripcion anterior = vencido;
        for (int i = 0; i < 240 && anterior != null && !anterior.getFechaFin().toLocalDateTime().isAfter(limite); i++) {
            if (datos.existsRenovacion(anterior.getDetalleSuscripcionId())) return;
            cobrarAdicionales(anterior);
            Plan plan = planes.findById(anterior.getPlanId());
            if (plan == null) return;
            LocalDateTime inicio = anterior.getFechaFin().toLocalDateTime();
            LocalDateTime fin = PlanService.calcularFechaFin(inicio, plan.getModalidad());
            Long siguiente = datos.createDetalle(anterior.getSuscripcionId(), plan.getPlanId(), inicio, fin, POSTPAGO,
                    Boolean.TRUE.equals(anterior.getPermitirAdicionales()), true, anterior.getDetalleSuscripcionId(), "RENOVACION");
            if (plan.getPrecio().compareTo(BigDecimal.ZERO) > 0) datos.createPago(siguiente, plan.getPrecio(), fin, "RENOVACION");
            anterior = datos.findDetalleById(siguiente);
        }
    }

    @Override
    public List<DetalleSuscripcion> pendientesRenovacion(LocalDateTime limite) {
        return datos.findPendientesRenovacion(limite);
    }

    private void cobrarAdicionales(DetalleSuscripcion detalle) {
        if (!Boolean.TRUE.equals(detalle.getPermitirAdicionales()) || detalle.getEspacioGb() == null || detalle.getPrecioGbAdicional() == null
                || detalle.getPrecioGbAdicional().compareTo(BigDecimal.ZERO) <= 0) return;
        Long clienteId = clienteDeSuscripcion(detalle);
        if (clienteId == null) return;
        BigDecimal usadoGb = BigDecimal.valueOf(datos.espacioUsadoBytes(clienteId)).divide(BYTES_POR_GB, 4, RoundingMode.HALF_UP);
        BigDecimal exceso = usadoGb.subtract(detalle.getEspacioGb());
        if (exceso.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal gb = exceso.setScale(0, RoundingMode.CEILING);
        datos.registrarAdicionales(detalle.getDetalleSuscripcionId(), gb,
                gb.multiply(detalle.getPrecioGbAdicional()).setScale(2, RoundingMode.HALF_UP));
    }

    private Long clienteDeSuscripcion(DetalleSuscripcion detalle) {
        return pagos.clienteDeSuscripcion(detalle.getSuscripcionId());
    }
}
