# Scripts de Prueba - Sistema de Cocina

Esta carpeta contiene scripts de prueba para el sistema de cocina con diferentes configuraciones de recetas.

## 📋 Uso del programa principal:

```bash
./kitchen <hamburguesas> <tacos> <sandwiches> <quesadillas>
```

## 🧪 Scripts de prueba disponibles:

### `test_quick.sh`
- **Hamburguesas:** 2, **Tacos:** 3, **Sandwiches:** 1, **Quesadillas:** 4
- **Total:** 10 platos
- **Propósito:** Prueba rápida para verificar funcionamiento básico

### `test_10_each.sh`
- **De cada receta:** 10
- **Total:** 40 platos
- **Propósito:** Prueba equilibrada de todas las recetas

### `test_mixed.sh`
- **Hamburguesas:** 50, **Tacos:** 25, **Sandwiches:** 75, **Quesadillas:** 100
- **Total:** 250 platos
- **Propósito:** Prueba con cantidades desbalanceadas

### `test_1000_hamburguesas.sh`
- **Solo hamburguesas:** 1000
- **Total:** 1000 platos
- **Propósito:** Prueba de stress con una sola receta (la más compleja)

### `test_only_quesadillas.sh`
- **Solo quesadillas:** 500
- **Total:** 500 platos
- **Propósito:** Prueba de velocidad (quesadillas son las más rápidas)

### `test_1000_each.sh`
- **De cada receta:** 1000
- **Total:** 4000 platos
- **Propósito:** Prueba de stress máximo ⚠️ **TOMA MUCHO TIEMPO**

## 🚀 Cómo ejecutar los tests:

```bash
# Dar permisos de ejecución
chmod +x test/*.sh

# Ejecutar un test específico
cd test
./test_quick.sh

# O desde la carpeta principal
./test/test_quick.sh
```

## 📊 Recetas disponibles:

1. **Hamburguesa** (3s): Pan(2), Carne(1), Lechuga(1), Tomate(1), Queso(1)
2. **Taco** (2s): Carne(1), Lechuga(1), Tomate(1), Queso(1), Tortilla(1), Salsa(1)
3. **Sandwich Vegetal** (2s): Pan(2), Lechuga(2), Tomate(2), Queso(1), Zanahoria(1)
4. **Quesadilla** (1s): Queso(2), Tortilla(2), Salsa(1)

## 💡 Tips:

- Los tests más largos pueden terminarse manualmente presionando Enter
- Usa `test_quick.sh` para verificar que todo funciona antes de ejecutar tests largos
- El monitor visual (`./kitchen_monitor`) puede ejecutarse en paralelo para ver el progreso