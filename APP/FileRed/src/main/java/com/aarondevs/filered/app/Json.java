package com.aarondevs.filered.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private final String texto;
    private int posicion;

    private Json(String texto) {
        this.texto = texto;
    }

    public static Object leer(String texto) {
        if (texto == null || texto.isBlank()) return null;
        Json lector = new Json(texto.trim());
        Object valor = lector.valor();
        lector.espacios();
        return valor;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> objeto(String texto) {
        Object valor = leer(texto);
        return valor instanceof Map<?, ?> mapa ? (Map<String, Object>) mapa : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> lista(String texto) {
        Object valor = leer(texto);
        return valor instanceof List<?> l ? (List<Object>) l : new ArrayList<>();
    }

    public static String escribir(Object valor) {
        StringBuilder salida = new StringBuilder();
        escribir(valor, salida);
        return salida.toString();
    }

    private static void escribir(Object valor, StringBuilder salida) {
        if (valor == null) {
            salida.append("null");
        } else if (valor instanceof String cadena) {
            salida.append('"');
            for (char c : cadena.toCharArray()) {
                switch (c) {
                    case '"' -> salida.append("\\\"");
                    case '\\' -> salida.append("\\\\");
                    case '\n' -> salida.append("\\n");
                    case '\r' -> salida.append("\\r");
                    case '\t' -> salida.append("\\t");
                    default -> {
                        if (c < 0x20) salida.append(String.format("\\u%04x", (int) c));
                        else salida.append(c);
                    }
                }
            }
            salida.append('"');
        } else if (valor instanceof Number || valor instanceof Boolean) {
            salida.append(valor);
        } else if (valor instanceof Map<?, ?> mapa) {
            salida.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                if (!primero) salida.append(',');
                primero = false;
                escribir(String.valueOf(entrada.getKey()), salida);
                salida.append(':');
                escribir(entrada.getValue(), salida);
            }
            salida.append('}');
        } else if (valor instanceof Iterable<?> lista) {
            salida.append('[');
            boolean primero = true;
            for (Object elemento : lista) {
                if (!primero) salida.append(',');
                primero = false;
                escribir(elemento, salida);
            }
            salida.append(']');
        } else {
            escribir(valor.toString(), salida);
        }
    }

    public static String texto(Map<String, Object> mapa, String clave) {
        Object valor = mapa == null ? null : mapa.get(clave);
        return valor == null ? null : valor.toString();
    }

    public static long numero(Map<String, Object> mapa, String clave) {
        Object valor = mapa == null ? null : mapa.get(clave);
        if (valor instanceof Number n) return n.longValue();
        try {
            return valor == null ? 0 : Long.parseLong(valor.toString());
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private void espacios() {
        while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) posicion++;
    }

    private Object valor() {
        espacios();
        if (posicion >= texto.length()) throw new IllegalArgumentException("JSON incompleto");
        char c = texto.charAt(posicion);
        if (c == '{') return objetoInterno();
        if (c == '[') return listaInterna();
        if (c == '"') return cadena();
        if (texto.startsWith("true", posicion)) {
            posicion += 4;
            return Boolean.TRUE;
        }
        if (texto.startsWith("false", posicion)) {
            posicion += 5;
            return Boolean.FALSE;
        }
        if (texto.startsWith("null", posicion)) {
            posicion += 4;
            return null;
        }
        return numeroInterno();
    }

    private Map<String, Object> objetoInterno() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        posicion++;
        espacios();
        if (texto.charAt(posicion) == '}') {
            posicion++;
            return mapa;
        }
        while (true) {
            espacios();
            String clave = cadena();
            espacios();
            posicion++;
            mapa.put(clave, valor());
            espacios();
            char c = texto.charAt(posicion++);
            if (c == '}') return mapa;
        }
    }

    private List<Object> listaInterna() {
        List<Object> lista = new ArrayList<>();
        posicion++;
        espacios();
        if (texto.charAt(posicion) == ']') {
            posicion++;
            return lista;
        }
        while (true) {
            lista.add(valor());
            espacios();
            char c = texto.charAt(posicion++);
            if (c == ']') return lista;
        }
    }

    private String cadena() {
        StringBuilder salida = new StringBuilder();
        posicion++;
        while (posicion < texto.length()) {
            char c = texto.charAt(posicion++);
            if (c == '"') return salida.toString();
            if (c != '\\') {
                salida.append(c);
                continue;
            }
            char e = texto.charAt(posicion++);
            switch (e) {
                case 'n' -> salida.append('\n');
                case 'r' -> salida.append('\r');
                case 't' -> salida.append('\t');
                case 'b' -> salida.append('\b');
                case 'f' -> salida.append('\f');
                case 'u' -> {
                    salida.append((char) Integer.parseInt(texto.substring(posicion, posicion + 4), 16));
                    posicion += 4;
                }
                default -> salida.append(e);
            }
        }
        throw new IllegalArgumentException("Cadena sin cerrar");
    }

    private Number numeroInterno() {
        int inicio = posicion;
        while (posicion < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(posicion)) >= 0) posicion++;
        String numero = texto.substring(inicio, posicion);
        if (numero.isEmpty()) throw new IllegalArgumentException("JSON no válido");
        if (numero.contains(".") || numero.contains("e") || numero.contains("E")) return Double.parseDouble(numero);
        return Long.parseLong(numero);
    }
}
