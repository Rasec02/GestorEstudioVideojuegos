Sistema desarrollado en Java que integra un Árbol Binario de Búsqueda para el registro maestro de videojuegos y un Grafo Ponderado para modelar relaciones de afinidad, permitiendo gestionar un catálogo y generar cadenas de recomendación mediante el algoritmo de Dijkstra.

La aplicación permite registrar, buscar, modificar y eliminar videojuegos, así como establecer relaciones de afinidad entre ellos. La coordinación entre ambas estructuras la realiza una clase fachada llamada Catalogo, que mantiene la coherencia entre el árbol y el grafo.

El sistema incluye un servidor HTTP en el puerto 8080 con interfaz web intuitiva donde los usuarios pueden realizar todas las operaciones y visualizar los resultados de manera clara. El proyecto demuestra cómo combinar estructuras de datos clásicas para resolver un problema real con requisitos complejos.

Autor: César Paredes Real
