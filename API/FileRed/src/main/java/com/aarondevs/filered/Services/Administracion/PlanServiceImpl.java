package com.aarondevs.filered.Services.Administracion;

import com.aarondevs.filered.DAO.Administracion.PlanDataSet;
import com.aarondevs.filered.Parameters.Administracion.PlanActualizarParameters;
import com.aarondevs.filered.Parameters.Administracion.PlanAgregarParameters;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PlanServiceImpl implements PlanService {
    private final PlanDataSet datos;

    public PlanServiceImpl(PlanDataSet datos) {
        this.datos = datos;
    }

    @Override
    public Object crear(PlanAgregarParameters parametro, String actor) {
        validar(parametro);
        return datos.create(parametro, actor);
    }

    @Override
    public Object actualizar(PlanActualizarParameters parametro, String actor) {
        if (parametro == null || parametro.getPlanUuid() == null || parametro.getPlanUuid().isBlank()) {
            throw new IllegalArgumentException("planUuid es requerido");
        }
        validar(parametro);
        if (parametro.getEstado() != null && !Set.of("Activo", "Inactivo").contains(parametro.getEstado())) {
            throw new IllegalArgumentException("estado debe ser Activo o Inactivo");
        }
        return datos.update(parametro, actor);
    }

    @Override
    public Object id(Long id) {
        return datos.findById(id);
    }

    @Override
    public Object all() {
        return datos.findAll();
    }

    @Override
    public Object publicos() {
        return datos.findPublicos();
    }

    @Override
    public boolean eliminar(Long id, String actor) {
        if (id != null && datos.enUso(id)) throw new IllegalArgumentException("El plan tiene suscripciones vigentes");
        return datos.delete(id, actor);
    }

    private void validar(PlanAgregarParameters parametro) {
        if (parametro == null || parametro.getMonedaId() == null || parametro.getNombre() == null || parametro.getNombre().isBlank()
                || parametro.getModalidad() == null
                || parametro.getPrecio() == null) {
            throw new IllegalArgumentException("monedaId, nombre, modalidad y precio son requeridos");
        }
        if (!MODALIDADES_MESES.containsKey(parametro.getModalidad())) {
            throw new IllegalArgumentException("modalidad debe ser Mensual, Bimestral, Trimestral, Semestral o Anual");
        }
        if (parametro.getEspacioGb() != null && parametro.getEspacioGb().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El espacio debe ser mayor que cero");
        if (parametro.getTamanoMaximoArchivoMb() != null && parametro.getTamanoMaximoArchivoMb() <= 0) throw new IllegalArgumentException("El tamaño máximo por archivo debe ser mayor que cero");
        if (parametro.getPrecio().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        if (parametro.getPrecioGbAdicional() == null) parametro.setPrecioGbAdicional(BigDecimal.ZERO);
        if (parametro.getEspacioGb() == null) parametro.setPrecioGbAdicional(BigDecimal.ZERO);
        if (parametro.getPrecioGbAdicional().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio por GB adicional no puede ser negativo");
        }
        if (!datos.existsMoneda(parametro.getMonedaId())) throw new IllegalArgumentException("monedaId no es válido");
    }
}
