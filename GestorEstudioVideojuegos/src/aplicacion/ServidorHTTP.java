package aplicacion;

import modelo.Videojuego;
import estructuras.Arista;
import estructuras.RutaMinima;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServidorHTTP {
    private static Catalogo catalogo = new Catalogo();
    private static HttpServer servidor;
    private static final int PUERTO = 8080;

    public static void main(String[] args) throws IOException {
        cargarDatosEjemplo();
        iniciarServidor();
    }

    private static void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);


        servidor.createContext("/", new ManejadorIndex());
        servidor.createContext("/registrar", new ManejadorRegistrar());
        servidor.createContext("/buscar", new ManejadorBuscar());
        servidor.createContext("/modificar", new ManejadorModificar());
        servidor.createContext("/eliminar", new ManejadorEliminar());
        servidor.createContext("/relacionar", new ManejadorRelacionar());
        servidor.createContext("/verRelaciones", new ManejadorVerRelaciones());
        servidor.createContext("/ruta", new ManejadorRuta());
        servidor.createContext("/explorar", new ManejadorExplorar());
        servidor.createContext("/estadisticas", new ManejadorEstadisticas());
        servidor.createContext("/styles.css", new ManejadorCSS());

        servidor.setExecutor(null);
        servidor.start();

        System.out.println("==================================================");
        System.out.println("   🎮 SISTEMA DE RECOMENDACIÓN DE VIDEOJUEGOS");
        System.out.println("==================================================");
        System.out.println("   Servidor iniciado en: http://localhost:" + PUERTO);
        System.out.println("   Abre tu navegador y comienza a usar la aplicación");
        System.out.println("==================================================");
    }

    private static void cargarDatosEjemplo() {
        registrarSilencioso(new Videojuego(101, "The Witcher 3", "RPG", 2015, 9.3));
        registrarSilencioso(new Videojuego(102, "Elden Ring", "RPG", 2022, 9.5));
        registrarSilencioso(new Videojuego(103, "Dark Souls III", "RPG", 2016, 9.0));
        registrarSilencioso(new Videojuego(104, "Hollow Knight", "Metroidvania", 2017, 9.1));
        registrarSilencioso(new Videojuego(105, "Celeste", "Plataformas", 2018, 8.8));
        registrarSilencioso(new Videojuego(106, "Hades", "Roguelike", 2020, 9.0));
        registrarSilencioso(new Videojuego(107, "Stardew Valley", "Simulacion", 2016, 8.9));
        registrarSilencioso(new Videojuego(108, "Cyberpunk 2077", "RPG", 2020, 8.1));

        catalogo.relacionarVideojuegos(101, 102, 1.0);
        catalogo.relacionarVideojuegos(102, 103, 0.8);
        catalogo.relacionarVideojuegos(101, 103, 1.5);
        catalogo.relacionarVideojuegos(103, 104, 2.0);
        catalogo.relacionarVideojuegos(104, 105, 1.2);
        catalogo.relacionarVideojuegos(105, 106, 1.8);
        catalogo.relacionarVideojuegos(104, 106, 1.5);
        catalogo.relacionarVideojuegos(105, 107, 2.5);
        catalogo.relacionarVideojuegos(108, 101, 1.0);
        catalogo.relacionarVideojuegos(108, 102, 1.3);
    }

    private static void registrarSilencioso(Videojuego videojuego) {
        if (catalogo.buscarVideojuego(videojuego.getCodigo()) == null) {
            catalogo.registrarVideojuego(videojuego);
        }
    }

    // MANEJADORES
    static class ManejadorIndex implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generarHTML(exchange);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class ManejadorCSS implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String css = generarCSS();
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            byte[] response = css.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class ManejadorRegistrar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                redirect(exchange, "/");
                return;
            }

            Map<String, String> params = parsearCuerpo(exchange);
            String mensaje;
            String tipo;

            try {
                int codigo = Integer.parseInt(params.get("codigo"));
                String titulo = params.get("titulo");
                String genero = params.get("genero");
                int anio = Integer.parseInt(params.get("anio"));
                double calificacion = Double.parseDouble(params.get("calificacion"));
                if (anio < 0) {
                    mensaje = "❌ El año no puede ser negativo (recibido: " + anio + ")";
                    tipo = "error";
                    redirectConMensaje(exchange, "/", mensaje, tipo);
                    return;
                }
                if (calificacion < 1.0 || calificacion > 10.0) {
                    mensaje = "❌ La calificación debe estar entre 1.0 y 10.0 (recibido: " + calificacion + ")";
                    tipo = "error";
                    redirectConMensaje(exchange, "/", mensaje, tipo);
                    return;
                }
                Videojuego nuevo = new Videojuego(codigo, titulo, genero, anio, calificacion);
                boolean success = catalogo.registrarVideojuego(nuevo);

                if (success) {
                    mensaje = "✅ Videojuego registrado exitosamente: " + titulo;
                    tipo = "success";
                } else {
                    mensaje = "❌ Ya existe un videojuego con el código " + codigo;
                    tipo = "error";
                }
            } catch (Exception e) {
                mensaje = "❌ Error: " + e.getMessage();
                tipo = "error";
            }

            redirectConMensaje(exchange, "/", mensaje, tipo);
        }
    }

    static class ManejadorBuscar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parsearQuery(query);
            int codigo = Integer.parseInt(params.getOrDefault("codigo", "0"));

            Videojuego v = catalogo.buscarVideojuego(codigo);
            String html;

            if (v != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("<div class='resultado-busqueda success'>");
                sb.append("<h3>✅ Videojuego Encontrado</h3>");
                sb.append("<p><strong>Código:</strong> ").append(v.getCodigo()).append("</p>");
                sb.append("<p><strong>Título:</strong> ").append(v.getTitulo()).append("</p>");
                sb.append("<p><strong>Género:</strong> ").append(v.getGenero()).append("</p>");
                sb.append("<p><strong>Año:</strong> ").append(v.getAnio()).append("</p>");
                sb.append("<p><strong>Calificación:</strong> ").append(v.getCalificacion()).append("</p>");

                List<Arista> vecinos = catalogo.vecinosDe(codigo);
                if (!vecinos.isEmpty()) {
                    sb.append("<h4>📌 Relaciones de afinidad:</h4><ul>");
                    for (Arista a : vecinos) {
                        Videojuego destino = catalogo.buscarVideojuego(a.getDestino());
                        sb.append("<li>").append(destino.getTitulo());
                        sb.append(" [afinidad: ").append(String.format("%.2f", a.getPeso())).append("]</li>");
                    }
                    sb.append("</ul>");
                } else {
                    sb.append("<p>No tiene relaciones de afinidad</p>");
                }
                sb.append("</div>");
                html = generarHTMLConResultado(sb.toString());
            } else {
                html = generarHTMLConResultado("<div class='resultado-busqueda error'><h3>❌ No se encontró el videojuego</h3></div>");
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class ManejadorModificar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                redirect(exchange, "/");
                return;
            }

            Map<String, String> params = parsearCuerpo(exchange);
            try {
                int codigo = Integer.parseInt(params.get("codigo"));
                String titulo = params.get("titulo");
                String genero = params.get("genero");
                int anio = Integer.parseInt(params.get("anio"));
                double calificacion = Double.parseDouble(params.get("calificacion"));
                if (anio < 0) {
                    redirectConMensaje(exchange, "/",
                            "❌ El año no puede ser negativo (recibido: " + anio + ")",
                            "error");
                    return;
                }
                if (calificacion < 1.0 || calificacion > 10.0) {
                    redirectConMensaje(exchange, "/",
                            "❌ La calificación debe estar entre 1.0 y 10.0 (recibido: " + calificacion + ")",
                            "error");
                    return;
                }
                boolean success = catalogo.modificarVideojuego(codigo, titulo, genero, anio, calificacion);

                if (success) {
                    redirectConMensaje(exchange, "/", "✅ Videojuego modificado exitosamente", "success");
                } else {
                    redirectConMensaje(exchange, "/", "❌ No se encontró el videojuego", "error");
                }
            } catch (Exception e) {
                redirectConMensaje(exchange, "/", "❌ Error: " + e.getMessage(), "error");
            }
        }
    }

    static class ManejadorEliminar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parsearQuery(query);
            int codigo = Integer.parseInt(params.getOrDefault("codigo", "0"));

            Videojuego v = catalogo.buscarVideojuego(codigo);
            boolean success = catalogo.eliminarVideojuego(codigo);

            if (success && v != null) {
                redirectConMensaje(exchange, "/", "✅ Videojuego eliminado: " + v.getTitulo(), "success");
            } else {
                redirectConMensaje(exchange, "/", "❌ No se encontró el videojuego", "error");
            }
        }
    }

    static class ManejadorRelacionar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                redirect(exchange, "/");
                return;
            }

            Map<String, String> params = parsearCuerpo(exchange);
            try {
                int origen = Integer.parseInt(params.get("origen"));
                int destino = Integer.parseInt(params.get("destino"));
                double afinidad = Double.parseDouble(params.get("afinidad"));

                boolean success = catalogo.relacionarVideojuegos(origen, destino, afinidad);

                if (success) {
                    Videojuego v1 = catalogo.buscarVideojuego(origen);
                    Videojuego v2 = catalogo.buscarVideojuego(destino);
                    redirectConMensaje(exchange, "/", "✅ Relación establecida: " + v1.getTitulo() + " ↔ " + v2.getTitulo(), "success");
                } else {
                    redirectConMensaje(exchange, "/", "❌ No se pudo establecer la relación", "error");
                }
            } catch (Exception e) {
                redirectConMensaje(exchange, "/", "❌ Error: " + e.getMessage(), "error");
            }
        }
    }

    static class ManejadorVerRelaciones implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='resultado-relaciones'>");
            sb.append("<h3>📋 Lista de Relaciones de Afinidad</h3>");

            List<Videojuego> videojuegos = catalogo.listarInorden();
            boolean hayRelaciones = false;

            for (Videojuego v : videojuegos) {
                List<Arista> vecinos = catalogo.vecinosDe(v.getCodigo());
                if (!vecinos.isEmpty()) {
                    hayRelaciones = true;
                    sb.append("<div class='relacion-item'>");
                    sb.append("<strong>🎮 ").append(v.getTitulo()).append("</strong>");
                    sb.append("<ul>");
                    for (Arista a : vecinos) {
                        Videojuego destino = catalogo.buscarVideojuego(a.getDestino());
                        sb.append("<li>→ ").append(destino.getTitulo());
                        sb.append(" [afinidad: ").append(String.format("%.2f", a.getPeso())).append("]</li>");
                    }
                    sb.append("</ul></div>");
                }
            }

            if (!hayRelaciones) {
                sb.append("<p>No hay relaciones de afinidad registradas.</p>");
            }
            sb.append("</div>");

            String html = generarHTMLConResultado(sb.toString());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class ManejadorRuta implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parsearQuery(query);
            int origen = Integer.parseInt(params.getOrDefault("origen", "0"));
            int destino = Integer.parseInt(params.getOrDefault("destino", "0"));

            RutaMinima ruta = catalogo.rutaRecomendacion(origen, destino);
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='resultado-ruta'>");

            if (ruta.existeCamino()) {
                sb.append("<h3>🎯 Cadena de Recomendación Sugerida</h3>");
                sb.append("<div class='ruta-camino'>");
                List<Integer> camino = ruta.getCamino();
                for (int i = 0; i < camino.size(); i++) {
                    Videojuego juego = catalogo.buscarVideojuego(camino.get(i));
                    sb.append("<div class='paso-ruta'>");
                    sb.append("  ▶ ").append(juego.getTitulo());
                    sb.append(" (").append(juego.getGenero()).append(")");
                    if (i < camino.size() - 1) {
                        double afinidad = obtenerAfinidad(camino.get(i), camino.get(i + 1));
                        sb.append("<span class='flecha'> ↓ [afinidad: ").append(String.format("%.2f", afinidad)).append("]</span>");
                    }
                    sb.append("</div>");
                }
                sb.append("</div>");
                sb.append("<div class='costo-total'>📊 Afinidad acumulada: ").append(String.format("%.2f", ruta.getCostoTotal())).append("</div>");
            } else {
                sb.append("<h3>❌ No existe una cadena de recomendación</h3>");
            }
            sb.append("</div>");

            String html = generarHTMLConResultado(sb.toString());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private double obtenerAfinidad(int origen, int destino) {
            for (Arista a : catalogo.vecinosDe(origen)) {
                if (a.getDestino() == destino) return a.getPeso();
            }
            return 0;
        }
    }

    static class ManejadorExplorar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parsearQuery(query);
            int origen = Integer.parseInt(params.getOrDefault("origen", "0"));

            List<Integer> alcanzados = catalogo.explorarRelacionados(origen);
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='resultado-exploracion'>");

            Videojuego inicio = catalogo.buscarVideojuego(origen);
            sb.append("<h3>🔍 Explorando desde: ").append(inicio.getTitulo()).append("</h3>");

            if (alcanzados.size() <= 1) {
                sb.append("<p>❌ Este videojuego no tiene relaciones de afinidad.</p>");
            } else {
                sb.append("<p>📋 ").append(alcanzados.size() - 1).append(" videojuego(s) alcanzable(s):</p>");
                sb.append("<ul>");
                for (int i = 1; i < alcanzados.size(); i++) {
                    Videojuego juego = catalogo.buscarVideojuego(alcanzados.get(i));
                    sb.append("<li>");
                    sb.append(juego.getTitulo());
                    sb.append(" (").append(juego.getGenero()).append(", ").append(juego.getAnio()).append(")");
                    sb.append(" [⭐ ").append(String.format("%.1f", juego.getCalificacion())).append("]");
                    sb.append("</li>");
                }
                sb.append("</ul>");
                sb.append("<p class='info-bfs'>✅ BFS completado - ").append(alcanzados.size() - 1).append(" nodos alcanzados</p>");
            }
            sb.append("</div>");

            String html = generarHTMLConResultado(sb.toString());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class ManejadorEstadisticas implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int total = catalogo.cantidadVideojuegos();
            int conRelaciones = 0;
            double sumaCalif = 0;
            List<Videojuego> videojuegos = catalogo.listarInorden();

            for (Videojuego v : videojuegos) {
                sumaCalif += v.getCalificacion();
                if (!catalogo.vecinosDe(v.getCodigo()).isEmpty()) {
                    conRelaciones++;
                }
            }

            double promedio = total > 0 ? sumaCalif / total : 0;
            int sinRelaciones = total - conRelaciones;

            StringBuilder sb = new StringBuilder();
            sb.append("<div class='resultado-estadisticas'>");
            sb.append("<h3>📊 Estadísticas del Catálogo</h3>");
            sb.append("<div class='stats-grid'>");
            sb.append("<div class='stat-box'><span class='stat-num'>").append(total).append("</span><span class='stat-label'>Total Videojuegos</span></div>");
            sb.append("<div class='stat-box'><span class='stat-num'>").append(conRelaciones).append("</span><span class='stat-label'>Con Relaciones</span></div>");
            sb.append("<div class='stat-box'><span class='stat-num'>").append(sinRelaciones).append("</span><span class='stat-label'>Sin Relaciones</span></div>");
            sb.append("<div class='stat-box'><span class='stat-num'>").append(String.format("%.2f", promedio)).append("</span><span class='stat-label'>Calificación Promedio</span></div>");
            sb.append("</div></div>");

            String html = generarHTMLConResultado(sb.toString());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    // ============== UTILIDADES ==============

    private static Map<String, String> parsearQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        try {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                    params.put(key, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return params;
    }

    private static Map<String, String> parsearCuerpo(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder cuerpo = new StringBuilder();
        String linea;
        while ((linea = br.readLine()) != null) {
            cuerpo.append(linea);
        }
        return parsearQuery(cuerpo.toString());
    }

    private static void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", path);
        exchange.sendResponseHeaders(302, -1);
    }

    private static void redirectConMensaje(HttpExchange exchange, String path, String mensaje, String tipo) throws IOException {
        try {
            String encoded = URLEncoder.encode(mensaje, StandardCharsets.UTF_8.name());
            String location = path + "?mensaje=" + encoded + "&tipo=" + tipo;
            exchange.getResponseHeaders().set("Location", location);
            exchange.sendResponseHeaders(302, -1);
        } catch (Exception e) {
            // Si falla la codificación, redirigir sin mensaje
            redirect(exchange, path);
        }
    }

    private static String generarHTML(HttpExchange exchange) {
        String mensaje = "";
        String tipo = "";

        // Leer parámetros de la URL
        String query = exchange.getRequestURI().getQuery();
        if (query != null && !query.isEmpty()) {
            Map<String, String> params = parsearQuery(query);
            mensaje = params.getOrDefault("mensaje", "");
            tipo = params.getOrDefault("tipo", "");

            // Decodificar el mensaje si viene codificado
            try {
                if (!mensaje.isEmpty()) {
                    mensaje = URLDecoder.decode(mensaje, StandardCharsets.UTF_8.name());
                }
            } catch (Exception e) {
                // Si falla la decodificación, usar el mensaje tal cual
            }
        }

        return generarHTMLCompleto("", mensaje, tipo);
    }

    private static String generarHTMLConResultado(String contenidoResultado) {
        return generarHTMLCompleto(contenidoResultado, "", "");
    }
    private static String generarHTMLCompleto(String resultado, String mensaje, String tipo) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>🎮 Sistema de Recomendación de Videojuegos</title>\n");
        html.append("    <link rel='stylesheet' href='/styles.css'>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class='container'>\n");

        // Header
        html.append("        <header class='header'>\n");
        html.append("            <div class='header-content'>\n");
        html.append("                <h1>🎮 Sistema de Recomendación de Videojuegos</h1>\n");
        html.append("                <p>Catálogo y Red de Afinidad</p>\n");
        html.append("            </div>\n");
        html.append("        </header>\n");

        // Mensaje flash
        if (mensaje != null && !mensaje.isEmpty()) {
            String clase = "success".equals(tipo) ? "success" : "error";
            html.append("        <div class='flash-message ").append(clase).append("'>\n");
            html.append("            ").append(mensaje).append("\n");
            html.append("        </div>\n");
        }

        // Resultado
        if (resultado != null && !resultado.isEmpty()) {
            html.append("        <div class='resultado-container'>\n");
            html.append("            ").append(resultado).append("\n");
            html.append("        </div>\n");
        }

        // ============================================================
        // ← NUEVO: TABLA DEL CATÁLOGO EN LA PARTE SUPERIOR
        // ============================================================
        html.append("        <div class='panel catalogo-principal'>\n");
        html.append("            <h2>📋 Catálogo de Videojuegos</h2>\n");
        html.append("            <div class='table-container'>\n");
        html.append("                <table>\n");
        html.append("                    <thead>\n");
        html.append("                        <tr><th>Código</th><th>Título</th><th>Género</th><th>Año</th><th>Calificación</th></tr>\n");
        html.append("                    </thead>\n");
        html.append("                    <tbody>\n");

        // Obtener la lista de videojuegos del catálogo
        List<Videojuego> videojuegos = catalogo.listarInorden();
        if (videojuegos.isEmpty()) {
            html.append("                        <tr><td colspan='5' style='text-align:center;'>No hay videojuegos registrados</td></tr>\n");
        } else {
            for (Videojuego v : videojuegos) {
                html.append("                            <tr>\n");
                html.append("                                <td>").append(v.getCodigo()).append("</td>\n");
                html.append("                                <td>").append(v.getTitulo()).append("</td>\n");
                html.append("                                <td>").append(v.getGenero()).append("</td>\n");
                html.append("                                <td>").append(v.getAnio()).append("</td>\n");
                html.append("                                <td>").append(String.format("%.1f", v.getCalificacion())).append("</td>\n");
                html.append("                            </tr>\n");
            }
        }

        html.append("                    </tbody>\n");
        html.append("                </table>\n");
        html.append("            </div>\n");
        html.append("            <div class='catalogo-info'>\n");
        html.append("                <span>📊 Total: ").append(catalogo.cantidadVideojuegos()).append(" videojuegos</span>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        // ============================================================

        // Contenido principal (paneles de gestión)
        html.append("        <div class='main-content'>\n");

        // Panel de Gestión
        html.append("            <div class='panel'>\n");
        html.append("                <h2>📋 Gestión de Videojuegos</h2>\n");
        html.append("                <form action='/registrar' method='POST' class='form-grid'>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Código:</label>\n");
        html.append("                        <input type='number' name='codigo' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Título:</label>\n");
        html.append("                        <input type='text' name='titulo' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Género:</label>\n");
        html.append("                        <input type='text' name='genero' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Año:</label>\n");
        html.append("                        <input type='number' name='anio' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Calificación (1-10):</label>\n");
        html.append("                        <input type='number' step='0.1' name='calificacion' min='1' max='10' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group full-width'>\n");
        html.append("                        <button type='submit' class='btn btn-success'>➕ Registrar</button>\n");
        html.append("                    </div>\n");
        html.append("                </form>\n");

        // Búsqueda
        html.append("                <form action='/buscar' method='GET' class='busqueda-rapida'>\n");
        html.append("                    <label>Buscar por código:</label>\n");
        html.append("                    <input type='number' name='codigo' placeholder='Código' required>\n");
        html.append("                    <button type='submit' class='btn btn-info'>🔍 Buscar</button>\n");
        html.append("                </form>\n");

        // Modificar
        html.append("                <form action='/modificar' method='POST' class='form-grid'>\n");
        html.append("                    <h3>✏️ Modificar Videojuego</h3>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Código a modificar:</label>\n");
        html.append("                        <input type='number' name='codigo' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Nuevo Título:</label>\n");
        html.append("                        <input type='text' name='titulo' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Nuevo Género:</label>\n");
        html.append("                        <input type='text' name='genero' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Nuevo Año:</label>\n");
        html.append("                        <input type='number' name='anio' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Nueva Calificación (1-10):</label>\n");
        html.append("                        <input type='number' step='0.1' name='calificacion' min='1' max='10' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group full-width'>\n");
        html.append("                        <button type='submit' class='btn btn-warning'>✏️ Modificar</button>\n");
        html.append("                    </div>\n");
        html.append("                </form>\n");

        // Eliminar
        html.append("                <form action='/eliminar' method='GET' class='eliminar-form'>\n");
        html.append("                    <h3>🗑️ Eliminar Videojuego</h3>\n");
        html.append("                    <div class='form-group inline'>\n");
        html.append("                        <label>Código a eliminar:</label>\n");
        html.append("                        <input type='number' name='codigo' required>\n");
        html.append("                        <button type='submit' class='btn btn-danger' onclick='return confirm(\"¿Está seguro?\")'>🗑️ Eliminar</button>\n");
        html.append("                    </div>\n");
        html.append("                </form>\n");
        html.append("            </div>\n");

        // Panel de Relaciones
        html.append("            <div class='panel'>\n");
        html.append("                <h2>🔗 Gestión de Relaciones de Afinidad</h2>\n");
        html.append("                <form action='/relacionar' method='POST' class='form-grid'>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Videojuego 1:</label>\n");
        html.append("                        <select name='origen'>\n");
        for (Videojuego v : catalogo.listarInorden()) {
            html.append("                            <option value='").append(v.getCodigo()).append("'>");
            html.append(v.getCodigo()).append(" - ").append(v.getTitulo());
            html.append("</option>\n");
        }
        html.append("                        </select>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Videojuego 2:</label>\n");
        html.append("                        <select name='destino'>\n");
        for (Videojuego v : catalogo.listarInorden()) {
            html.append("                            <option value='").append(v.getCodigo()).append("'>");
            html.append(v.getCodigo()).append(" - ").append(v.getTitulo());
            html.append("</option>\n");
        }
        html.append("                        </select>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Afinidad (menor = más parecidos):</label>\n");
        html.append("                        <input type='number' step='0.1' name='afinidad' value='1.0' required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group full-width'>\n");
        html.append("                        <button type='submit' class='btn btn-primary'>🔗 Establecer Relación</button>\n");
        html.append("                    </div>\n");
        html.append("                </form>\n");
        html.append("                <div class='btn-group'>\n");
        html.append("                    <a href='/verRelaciones' class='btn btn-info'>📋 Ver Todas las Relaciones</a>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");

        // Panel de Recomendación
        html.append("            <div class='panel'>\n");
        html.append("                <h2>🎯 Ruta de Recomendación (Dijkstra)</h2>\n");
        html.append("                <form action='/ruta' method='GET' class='form-grid'>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Videojuego de origen:</label>\n");
        html.append("                        <select name='origen'>\n");
        for (Videojuego v : catalogo.listarInorden()) {
            html.append("                            <option value='").append(v.getCodigo()).append("'>");
            html.append(v.getCodigo()).append(" - ").append(v.getTitulo());
            html.append("</option>\n");
        }
        html.append("                        </select>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Videojuego destino:</label>\n");
        html.append("                        <select name='destino'>\n");
        for (Videojuego v : catalogo.listarInorden()) {
            html.append("                            <option value='").append(v.getCodigo()).append("'>");
            html.append(v.getCodigo()).append(" - ").append(v.getTitulo());
            html.append("</option>\n");
        }
        html.append("                        </select>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group full-width'>\n");
        html.append("                        <button type='submit' class='btn btn-success'>🛤️ Calcular Mejor Ruta</button>\n");
        html.append("                    </div>\n");
        html.append("                </form>\n");
        html.append("            </div>\n");

        // Panel de Exploración
        html.append("            <div class='panel'>\n");
        html.append("                <h2>🔍 Explorar Videojuegos Relacionados (BFS)</h2>\n");
        html.append("                <form action='/explorar' method='GET' class='form-grid'>\n");
        html.append("                    <div class='form-group'>\n");
        html.append("                        <label>Videojuego de partida:</label>\n");
        html.append("                        <select name='origen'>\n");
        for (Videojuego v : catalogo.listarInorden()) {
            html.append("                            <option value='").append(v.getCodigo()).append("'>");
            html.append(v.getCodigo()).append(" - ").append(v.getTitulo());
            html.append("</option>\n");
        }
        html.append("                        </select>\n");
        html.append("                    </div>\n");
        html.append("                    <div class='form-group full-width'>\n");
        html.append("                        <button type='submit' class='btn btn-primary'>🔍 Explorar Relaciones</button>\n");
        html.append("                    </div>\n");
        html.append("                </form>\n");
        html.append("            </div>\n");

        // Panel de Estadísticas
        html.append("            <div class='panel'>\n");
        html.append("                <h2>📊 Estadísticas del Catálogo</h2>\n");
        html.append("                <div class='btn-group'>\n");
        html.append("                    <a href='/estadisticas' class='btn btn-info'>📊 Mostrar Estadísticas</a>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");

        // La tabla del catálogo ya no va aquí abajo, se movió arriba
        // Así que eliminamos la sección que estaba antes

        html.append("        </div>\n");

        // Footer
        html.append("        <footer class='footer'>\n");
        html.append("            <p>💡 Sistema de Recomendación de Videojuegos | Estructura de Datos II</p>\n");
        html.append("        </footer>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private static String generarCSS() {
        StringBuilder css = new StringBuilder();
        css.append("/* Reset y base */\n");
        css.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n\n");
        css.append("body {\n");
        css.append("    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
        css.append("    background: #f0f4f8;\n");
        css.append("    color: #2d3748;\n");
        css.append("    padding: 20px;\n");
        css.append("}\n\n");
        css.append(".container { max-width: 1200px; margin: 0 auto; }\n\n");

        // Header
        css.append(".header {\n");
        css.append("    background: linear-gradient(135deg, #1e90ff, #0066cc);\n");
        css.append("    color: white;\n");
        css.append("    padding: 25px 30px;\n");
        css.append("    border-radius: 12px;\n");
        css.append("    margin-bottom: 25px;\n");
        css.append("    box-shadow: 0 4px 15px rgba(30, 144, 255, 0.3);\n");
        css.append("}\n\n");
        css.append(".header-content h1 { font-size: 28px; font-weight: 700; }\n");
        css.append(".header-content p { font-size: 14px; opacity: 0.9; margin-top: 5px; }\n\n");

        // Flash messages
        css.append(".flash-message {\n");
        css.append("    padding: 15px 20px;\n");
        css.append("    border-radius: 8px;\n");
        css.append("    margin-bottom: 20px;\n");
        css.append("    font-weight: 500;\n");
        css.append("    border-left: 4px solid;\n");
        css.append("}\n\n");
        css.append(".flash-message.success {\n");
        css.append("    background: #f0fff4;\n");
        css.append("    border-color: #38a169;\n");
        css.append("    color: #2f855a;\n");
        css.append("}\n\n");
        css.append(".flash-message.error {\n");
        css.append("    background: #fff5f5;\n");
        css.append("    border-color: #e53e3e;\n");
        css.append("    color: #c53030;\n");
        css.append("}\n\n");

        // Paneles
        css.append(".panel {\n");
        css.append("    background: white;\n");
        css.append("    border-radius: 12px;\n");
        css.append("    padding: 25px;\n");
        css.append("    margin-bottom: 25px;\n");
        css.append("    box-shadow: 0 2px 10px rgba(0,0,0,0.08);\n");
        css.append("}\n\n");
        css.append(".panel h2 {\n");
        css.append("    font-size: 20px;\n");
        css.append("    color: #2d3748;\n");
        css.append("    margin-bottom: 20px;\n");
        css.append("    padding-bottom: 12px;\n");
        css.append("    border-bottom: 2px solid #e2e8f0;\n");
        css.append("}\n\n");
        css.append(".panel h3 {\n");
        css.append("    font-size: 16px;\n");
        css.append("    color: #2d3748;\n");
        css.append("    margin: 15px 0 10px 0;\n");
        css.append("}\n\n");

        // Formularios
        css.append(".form-grid {\n");
        css.append("    display: grid;\n");
        css.append("    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n");
        css.append("    gap: 15px;\n");
        css.append("    margin-bottom: 20px;\n");
        css.append("}\n\n");
        css.append(".form-group { display: flex; flex-direction: column; }\n");
        css.append(".form-group.full-width { grid-column: 1 / -1; }\n");
        css.append(".form-group.inline { flex-direction: row; align-items: center; gap: 10px; }\n\n");
        css.append(".form-group label {\n");
        css.append("    font-size: 13px;\n");
        css.append("    font-weight: 600;\n");
        css.append("    color: #4a5568;\n");
        css.append("    margin-bottom: 5px;\n");
        css.append("}\n\n");
        css.append(".form-group input, .form-group select {\n");
        css.append("    padding: 10px 12px;\n");
        css.append("    border: 2px solid #e2e8f0;\n");
        css.append("    border-radius: 8px;\n");
        css.append("    font-size: 14px;\n");
        css.append("    transition: border-color 0.3s;\n");
        css.append("}\n\n");
        css.append(".form-group input:focus, .form-group select:focus {\n");
        css.append("    outline: none;\n");
        css.append("    border-color: #1e90ff;\n");
        css.append("    box-shadow: 0 0 0 3px rgba(30, 144, 255, 0.1);\n");
        css.append("}\n\n");

        // Botones
        css.append(".btn, .btn-group a {\n");
        css.append("    display: inline-block;\n");
        css.append("    padding: 10px 20px;\n");
        css.append("    border: none;\n");
        css.append("    border-radius: 8px;\n");
        css.append("    font-size: 14px;\n");
        css.append("    font-weight: 600;\n");
        css.append("    cursor: pointer;\n");
        css.append("    transition: all 0.3s ease;\n");
        css.append("    color: white;\n");
        css.append("    text-decoration: none;\n");
        css.append("    text-align: center;\n");
        css.append("}\n\n");
        css.append(".btn:hover, .btn-group a:hover {\n");
        css.append("    transform: translateY(-2px);\n");
        css.append("    box-shadow: 0 4px 12px rgba(0,0,0,0.15);\n");
        css.append("}\n\n");
        css.append(".btn-success { background: #38a169; }\n");
        css.append(".btn-success:hover { background: #2f855a; }\n");
        css.append(".btn-danger { background: #e53e3e; }\n");
        css.append(".btn-danger:hover { background: #c53030; }\n");
        css.append(".btn-warning { background: #d69e2e; }\n");
        css.append(".btn-warning:hover { background: #b7791f; }\n");
        css.append(".btn-info { background: #3182ce; }\n");
        css.append(".btn-info:hover { background: #2b6cb0; }\n");
        css.append(".btn-primary { background: #1e90ff; }\n");
        css.append(".btn-primary:hover { background: #0066cc; }\n\n");

        css.append(".btn-group {\n");
        css.append("    display: flex;\n");
        css.append("    gap: 10px;\n");
        css.append("    flex-wrap: wrap;\n");
        css.append("    margin: 10px 0;\n");
        css.append("}\n\n");

        // Búsqueda rápida
        css.append(".busqueda-rapida {\n");
        css.append("    display: flex;\n");
        css.append("    align-items: center;\n");
        css.append("    gap: 10px;\n");
        css.append("    padding: 15px;\n");
        css.append("    background: #f7fafc;\n");
        css.append("    border-radius: 8px;\n");
        css.append("    margin-bottom: 20px;\n");
        css.append("    flex-wrap: wrap;\n");
        css.append("}\n\n");
        css.append(".busqueda-rapida label { font-weight: 600; }\n\n");
        css.append(".eliminar-form { margin-top: 20px; padding-top: 20px; border-top: 2px solid #e2e8f0; }\n\n");

        // Tablas
        css.append(".table-container {\n");
        css.append("    overflow-x: auto;\n");
        css.append("    margin-top: 15px;\n");
        css.append("}\n\n");
        css.append("table {\n");
        css.append("    width: 100%;\n");
        css.append("    border-collapse: collapse;\n");
        css.append("    font-size: 14px;\n");
        css.append("}\n\n");
        css.append("table th {\n");
        css.append("    background: #1e90ff;\n");
        css.append("    color: white;\n");
        css.append("    padding: 12px 15px;\n");
        css.append("    text-align: left;\n");
        css.append("}\n\n");
        css.append("table td {\n");
        css.append("    padding: 10px 15px;\n");
        css.append("    border-bottom: 1px solid #e2e8f0;\n");
        css.append("}\n\n");
        css.append("table tr:hover td { background: #f7fafc; }\n\n");

        // Resultados
        css.append(".resultado-container {\n");
        css.append("    background: white;\n");
        css.append("    border-radius: 12px;\n");
        css.append("    padding: 20px;\n");
        css.append("    margin-bottom: 25px;\n");
        css.append("    box-shadow: 0 2px 10px rgba(0,0,0,0.08);\n");
        css.append("    border-left: 4px solid #1e90ff;\n");
        css.append("}\n\n");
        css.append(".resultado-busqueda.success { border-left-color: #38a169; }\n");
        css.append(".resultado-busqueda.error { border-left-color: #e53e3e; }\n\n");
        css.append(".resultado-busqueda ul { margin: 10px 0 0 20px; }\n");
        css.append(".resultado-busqueda li { margin: 5px 0; }\n\n");

        // Estadísticas
        css.append(".stats-grid {\n");
        css.append("    display: grid;\n");
        css.append("    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n");
        css.append("    gap: 20px;\n");
        css.append("    margin: 20px 0;\n");
        css.append("}\n\n");
        css.append(".stat-box {\n");
        css.append("    background: #f7fafc;\n");
        css.append("    padding: 20px;\n");
        css.append("    border-radius: 8px;\n");
        css.append("    text-align: center;\n");
        css.append("}\n\n");
        css.append(".stat-num {\n");
        css.append("    display: block;\n");
        css.append("    font-size: 32px;\n");
        css.append("    font-weight: 700;\n");
        css.append("    color: #2d3748;\n");
        css.append("}\n\n");
        css.append(".stat-label {\n");
        css.append("    display: block;\n");
        css.append("    font-size: 14px;\n");
        css.append("    color: #4a5568;\n");
        css.append("    margin-top: 5px;\n");
        css.append("}\n\n");

        // Ruta
        css.append(".ruta-camino { margin: 15px 0; }\n");
        css.append(".paso-ruta {\n");
        css.append("    padding: 8px 12px;\n");
        css.append("    margin: 5px 0;\n");
        css.append("    background: #f7fafc;\n");
        css.append("    border-radius: 6px;\n");
        css.append("    border-left: 3px solid #1e90ff;\n");
        css.append("}\n\n");
        css.append(".flecha { color: #718096; font-size: 13px; margin-left: 10px; }\n");
        css.append(".costo-total {\n");
        css.append("    margin-top: 15px;\n");
        css.append("    padding: 12px;\n");
        css.append("    background: #ebf8ff;\n");
        css.append("    border-radius: 6px;\n");
        css.append("    font-weight: 600;\n");
        css.append("}\n\n");

        // Relaciones
        css.append(".relacion-item {\n");
        css.append("    padding: 10px;\n");
        css.append("    margin: 8px 0;\n");
        css.append("    background: #f7fafc;\n");
        css.append("    border-radius: 6px;\n");
        css.append("}\n\n");
        css.append(".relacion-item ul { margin: 5px 0 0 20px; }\n");
        css.append(".relacion-item li { margin: 3px 0; }\n\n");

        // Exploración
        css.append(".resultado-exploracion ul { margin: 10px 0 0 20px; }\n");
        css.append(".resultado-exploracion li { margin: 5px 0; padding: 3px 0; }\n");
        css.append(".info-bfs { margin-top: 15px; color: #3182ce; font-style: italic; }\n\n");

        // Footer
        css.append(".footer {\n");
        css.append("    text-align: center;\n");
        css.append("    padding: 20px;\n");
        css.append("    color: #718096;\n");
        css.append("    font-size: 13px;\n");
        css.append("    border-top: 1px solid #e2e8f0;\n");
        css.append("    margin-top: 20px;\n");
        css.append("}\n\n");

        // Responsive
        css.append("@media (max-width: 768px) {\n");
        css.append("    .form-grid { grid-template-columns: 1fr; }\n");
        css.append("    .stats-grid { grid-template-columns: 1fr 1fr; }\n");
        css.append("    .header-content h1 { font-size: 20px; }\n");
        css.append("    .busqueda-rapida { flex-direction: column; align-items: stretch; }\n");
        css.append("    .btn-group { flex-direction: column; }\n");
        css.append("    .btn, .btn-group a { width: 100%; }\n");
        css.append("}\n");

        return css.toString();
    }
}
