package com.aarondevs.filered.DAO.Seguridad;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PermisoJerarquiaClienteDAO extends PermisoJerarquiaDAO {
    public PermisoJerarquiaClienteDAO(JdbcTemplate jdbc) {
        super(jdbc, "Clientes", "Cliente");
    }
}
