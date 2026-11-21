package com.aarondevs.filered.Services.General;

import java.util.Map;

public interface ResumenService {
    Map<String, Object> administracion();

    Map<String, Object> cliente(Long clienteId);
}
