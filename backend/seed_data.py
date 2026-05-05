#!/usr/bin/env python3
"""
Seed de ejercicios de ejemplo para GuiaGym.
Ejecutar desde la carpeta backend/:
    python seed_data.py

Si la base de datos aún no existe, arranca el servidor al menos una vez
para que FastAPI cree las tablas, o ejecuta primero:
    python -c "from app.main import *"
"""

import os
import sys
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# ── Conexión ──────────────────────────────────────────────────────────────────

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./guiagym.db")

connect_args = {"check_same_thread": False} if "sqlite" in DATABASE_URL else {}
engine = create_engine(DATABASE_URL, connect_args=connect_args)
Session = sessionmaker(bind=engine)

# ── Datos ─────────────────────────────────────────────────────────────────────

EJERCICIOS = [

    # ── PECHO ─────────────────────────────────────────────────────────────────
    {
        "nombre":         "Press de banca con barra",
        "descripcion":    "Tumbado en banco plano, bajar la barra hasta el pecho y empujar hacia arriba. "
                          "Codos a 45-75°. Equipo: barra, banco.",
        "grupo_muscular": "pecho",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Press inclinado con mancuernas",
        "descripcion":    "Banco inclinado a 30-45°. Subir las mancuernas con un arco natural hasta la extensión. "
                          "Activa la parte alta del pectoral. Equipo: mancuernas, banco inclinado.",
        "grupo_muscular": "pecho",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Fondos en paralelas (pecho)",
        "descripcion":    "Inclinar el torso hacia delante unos 30° para enfatizar el pecho. Bajar hasta "
                          "que los hombros estén por debajo de los codos. Equipo: barras paralelas.",
        "grupo_muscular": "pecho",
        "tipo":           "fuerza",
        "dificultad":     "avanzado",
    },
    {
        "nombre":         "Aperturas con mancuernas",
        "descripcion":    "Banco plano, brazos ligeramente flexionados. Bajar las mancuernas en arco hasta "
                          "sentir el estiramiento del pecho y volver. Equipo: mancuernas, banco.",
        "grupo_muscular": "pecho",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },

    # ── ESPALDA ───────────────────────────────────────────────────────────────
    {
        "nombre":         "Dominadas",
        "descripcion":    "Colgado de la barra con agarre prono (palmas hacia fuera), subir hasta que el mentón "
                          "supere la barra. Equipo: barra de dominadas.",
        "grupo_muscular": "espalda",
        "tipo":           "fuerza",
        "dificultad":     "avanzado",
    },
    {
        "nombre":         "Remo con barra",
        "descripcion":    "Torso inclinado 45°, barra colgando. Llevar la barra hacia el abdomen manteniendo "
                          "la espalda recta. Equipo: barra, discos.",
        "grupo_muscular": "espalda",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Jalón al pecho en polea",
        "descripcion":    "Sentado en la máquina, tirar de la barra hasta el pecho con agarre ancho. "
                          "Retractar las escápulas al bajar. Equipo: polea alta.",
        "grupo_muscular": "espalda",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Remo con mancuerna a una mano",
        "descripcion":    "Apoyar una rodilla y mano en el banco. Con la mano libre subir la mancuerna hasta "
                          "la cadera manteniendo el codo pegado al cuerpo. Equipo: mancuerna, banco.",
        "grupo_muscular": "espalda",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },

    # ── PIERNAS ───────────────────────────────────────────────────────────────
    {
        "nombre":         "Sentadilla con barra",
        "descripcion":    "Barra sobre trapecios, pies a la anchura de los hombros. Bajar hasta que los muslos "
                          "queden paralelos al suelo. Espalda neutra en todo momento. Equipo: barra, soporte.",
        "grupo_muscular": "piernas",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Prensa de piernas",
        "descripcion":    "Sentado en la máquina, pies a la anchura de los hombros. Empujar la plataforma "
                          "hasta casi extender las rodillas sin bloquearlas. Equipo: prensa.",
        "grupo_muscular": "piernas",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Peso muerto convencional",
        "descripcion":    "Pies a la anchura de caderas, barra sobre el empeine. Empujar el suelo con los pies "
                          "y extender caderas y rodillas simultáneamente. Equipo: barra, discos.",
        "grupo_muscular": "piernas",
        "tipo":           "fuerza",
        "dificultad":     "avanzado",
    },
    {
        "nombre":         "Zancadas con mancuernas",
        "descripcion":    "De pie con mancuernas en cada mano, dar un paso largo hacia delante y bajar la "
                          "rodilla trasera hasta casi tocar el suelo. Alternar piernas. Equipo: mancuernas.",
        "grupo_muscular": "piernas",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },

    # ── HOMBROS ───────────────────────────────────────────────────────────────
    {
        "nombre":         "Press militar con barra",
        "descripcion":    "De pie o sentado, barra a la altura del pecho con agarre prono. Empujar hacia "
                          "arriba hasta extender los brazos. Equipo: barra.",
        "grupo_muscular": "hombros",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Elevaciones laterales con mancuernas",
        "descripcion":    "De pie, mancuernas a los lados. Subir los brazos ligeramente flexionados hasta "
                          "la horizontal. Bajar controlado. Equipo: mancuernas.",
        "grupo_muscular": "hombros",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Press Arnold",
        "descripcion":    "Sentado, mancuernas frente a la cara con palmas hacia ti. Al empujar hacia arriba "
                          "rotar las palmas hacia fuera. Equipo: mancuernas, banco.",
        "grupo_muscular": "hombros",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Pájaros con mancuernas",
        "descripcion":    "Inclinado hacia delante, brazos colgando. Subir las mancuernas en arco hasta "
                          "la horizontal trabajando el deltoides posterior. Equipo: mancuernas.",
        "grupo_muscular": "hombros",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },

    # ── BRAZOS ────────────────────────────────────────────────────────────────
    {
        "nombre":         "Curl de bíceps con barra",
        "descripcion":    "De pie, agarre supino. Flexionar los codos llevando la barra hacia los hombros "
                          "sin mover el torso. Equipo: barra o EZ.",
        "grupo_muscular": "brazos",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Extensiones de tríceps en polea",
        "descripcion":    "De pie frente a la polea alta con cuerda. Codos fijos a los lados, extender los "
                          "brazos hacia abajo hasta la extensión completa. Equipo: polea alta.",
        "grupo_muscular": "brazos",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Curl martillo",
        "descripcion":    "De pie, agarre neutro (palmas enfrentadas). Flexionar los codos de forma alterna "
                          "o simultánea. Trabaja bíceps y braquiorradial. Equipo: mancuernas.",
        "grupo_muscular": "brazos",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Fondos en banco (tríceps)",
        "descripcion":    "Manos en el borde del banco, pies en el suelo. Bajar flexionando los codos hasta "
                          "90° y empujar hacia arriba. Equipo: banco.",
        "grupo_muscular": "brazos",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },

    # ── ABDOMEN ───────────────────────────────────────────────────────────────
    {
        "nombre":         "Crunch abdominal",
        "descripcion":    "Tumbado, rodillas flexionadas. Elevar el torso contrayendo el abdomen, sin tirar "
                          "del cuello. Bajar controlado. Equipo: ninguno.",
        "grupo_muscular": "abdomen",
        "tipo":           "fuerza",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Plancha frontal",
        "descripcion":    "Apoyado en antebrazos y punteras de pie, mantener el cuerpo recto como una tabla. "
                          "Contraer abdomen y glúteos durante el tiempo indicado. Equipo: ninguno.",
        "grupo_muscular": "abdomen",
        "tipo":           "funcional",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Elevaciones de piernas colgado",
        "descripcion":    "Colgado de la barra de dominadas, elevar las piernas estiradas hasta la horizontal "
                          "(o rodillas al pecho en versión más fácil). Equipo: barra de dominadas.",
        "grupo_muscular": "abdomen",
        "tipo":           "fuerza",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Russian twist",
        "descripcion":    "Sentado en el suelo con torso inclinado 45° y piernas elevadas. Girar el torso de "
                          "lado a lado tocando el suelo. Con o sin peso. Equipo: disco o balón medicinal (opcional).",
        "grupo_muscular": "abdomen",
        "tipo":           "funcional",
        "dificultad":     "intermedio",
    },

    # ── CARDIO ────────────────────────────────────────────────────────────────
    {
        "nombre":         "Carrera en cinta",
        "descripcion":    "Correr a ritmo constante o en intervalos. Ajustar velocidad e inclinación según "
                          "el objetivo. Equipo: cinta de correr.",
        "grupo_muscular": "cardio",
        "tipo":           "cardio",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Salto a la comba",
        "descripcion":    "Saltar a la cuerda de forma continua. Excelente para mejorar la coordinación y la "
                          "capacidad cardiovascular. Equipo: comba.",
        "grupo_muscular": "cardio",
        "tipo":           "cardio",
        "dificultad":     "principiante",
    },
    {
        "nombre":         "Burpees",
        "descripcion":    "De pie, bajar a posición de plancha, hacer una flexión, saltar hacia los pies y "
                          "saltar con los brazos arriba. Repetir sin pausa. Equipo: ninguno.",
        "grupo_muscular": "cardio",
        "tipo":           "cardio",
        "dificultad":     "intermedio",
    },
    {
        "nombre":         "Bicicleta estática",
        "descripcion":    "Pedalear a ritmo constante o con intervalos de alta intensidad (HIIT). Ajustar "
                          "la resistencia según el objetivo. Equipo: bicicleta estática.",
        "grupo_muscular": "cardio",
        "tipo":           "cardio",
        "dificultad":     "principiante",
    },
]

# ── Inserción ─────────────────────────────────────────────────────────────────

INSERT_SQL = text("""
    INSERT INTO ejercicio (nombre, descripcion, grupo_muscular, tipo, dificultad, creado_por)
    VALUES (:nombre, :descripcion, :grupo_muscular, :tipo, :dificultad, NULL)
""")

SELECT_NOMBRES_SQL = text("SELECT nombre FROM ejercicio")


def seed():
    with Session() as db:
        try:
            existing = {row[0] for row in db.execute(SELECT_NOMBRES_SQL)}
        except Exception:
            print(
                "ERROR: no se puede acceder a la tabla 'ejercicio'.\n"
                "Inicia el servidor FastAPI al menos una vez para crear las tablas y vuelve a ejecutar este script.",
                file=sys.stderr,
            )
            sys.exit(1)

        grupos = {}
        inserted = 0
        skipped = 0

        for ej in EJERCICIOS:
            grupo = ej["grupo_muscular"]
            grupos.setdefault(grupo, {"ok": 0, "skip": 0})

            if ej["nombre"] in existing:
                grupos[grupo]["skip"] += 1
                skipped += 1
            else:
                db.execute(INSERT_SQL, ej)
                grupos[grupo]["ok"] += 1
                inserted += 1

        db.commit()

    # ── Resumen ───────────────────────────────────────────────────────────────
    print("\nGuiaGym — seed de ejercicios")
    print("=" * 40)
    for grupo, counts in grupos.items():
        ok   = counts["ok"]
        skip = counts["skip"]
        tag  = f"+{ok} insertados" if ok else ""
        if skip:
            tag += f"  ({skip} ya existían)" if tag else f"{skip} ya existían"
        print(f"  {grupo:<12} {tag}")
    print("=" * 40)
    print(f"  Total: {inserted} insertados, {skipped} omitidos.")


if __name__ == "__main__":
    seed()
