# Sistema de Cocina

Sistema de gestión de cocina con procesos cocineros y reponedor de stock, implementado con memoria compartida.

## Compilación

Para compilar el sistema, ejecuta:

```bash
./compile.sh
```

Este script:
1. Crea el directorio `out` si no existe
2. Limpia compilaciones previas
3. Elimina archivos `.class` de `src`
4. Compila todos los archivos `.java` a `out`

## Ejecución

Hay dos formas de ejecutar el sistema:

### 1. Modo Interactivo (Recomendado)

```bash
java -cp out OrderInput
```

Este modo te presenta un menú interactivo donde puedes:
- Seleccionar platos del menú
- Especificar cantidades
- Enviar órdenes a la cocina
- Ver el progreso en tiempo real

### 2. Modo Directo

```bash
java -cp out KitchenSystem "plato1:cantidad1,plato2:cantidad2,..."
```

Ejemplo:
```bash
java -cp out KitchenSystem "hamburguesa:2,ensalada:1,taco:3"
```

### 3. Monitor de Stock en Tiempo Real

Para ver el estado del stock en tiempo real mientras el sistema está funcionando:

```bash
java -cp out StockViewer
```

El StockViewer muestra:
- Niveles actuales de todos los ingredientes
- Actualizaciones en tiempo real
- Alertas cuando ingredientes están bajos
- Estado de reposición

Puedes ejecutar el StockViewer en una terminal separada mientras el sistema principal está corriendo para monitorear el estado de la cocina.

## Estructura del Sistema

- **KitchenSystem**: Sistema principal que gestiona la cocina
- **CookProcess**: Procesos cocineros que preparan los platos
- **StockReplenisher**: Proceso que repone ingredientes
- **OrderInput**: Interfaz interactiva para hacer pedidos
- **StockViewer**: Monitor en tiempo real del inventario

## Platos Disponibles

- Hamburguesa
- Ensalada
- Taco
- Sanguche

## Implementación

El sistema utiliza:
- Memoria compartida a través de `MappedByteBuffer`
- Sincronización mediante locks de archivo
- Procesos independientes para cocineros y reponedor
- Gestión de stock en tiempo real
- Monitoreo en vivo del inventario
