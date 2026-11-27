package com.aarondevs.filered.DAO.Seguridad;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PermisoJerarquiaAdministracionDAO extends PermisoJerarquiaDAO {
    public PermisoJerarquiaAdministracionDAO(JdbcTemplate jdbc) {
        super(jdbc, "Administracion", "Administracion");
    }
}
