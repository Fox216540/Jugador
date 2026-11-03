import java.util.Random;

class Jugador {
    final int LONGITUD_BARRA = 10;

    String nombre;

    Random random = new Random();

    int ataque, defensa, vitalidad, vidaMax;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.ataque = random.nextInt(51, 101);
        this.defensa = random.nextInt(1,51);
        this.vidaMax = 500;
        this.vitalidad = vidaMax;
    }

    void atacar(Jugador jugador) {
        int dano = this.ataque - jugador.defensa;
        recibirDano(jugador, dano);
    }

    void recibirDano(Jugador jugador, int dano) {
        jugador.vitalidad -= dano;
        if (jugador.vitalidad < 0) {
            jugador.vitalidad = 0;
        }
    }

    @Override
    public String toString() {
        return String.format("Nombre: %s\nAtaque: %d\nDefensa: %d",
                this.nombre, this.ataque, this.defensa);
    }

    String mostrarVida() {
        String cadena = "";
        cadena += String.format("Vida: [%d/%d]\n", this.vitalidad, this.vidaMax);
        int longitudBarraVida =(int) Math.ceil((double) this.vitalidad / this.vidaMax * LONGITUD_BARRA);
        cadena += "*".repeat(longitudBarraVida) + "-".repeat(LONGITUD_BARRA - longitudBarraVida);
        return cadena;
    }

}

class User extends Jugador {
    final static String ER_NOMBRE = "[A-Z][a-z]{0,6}";

    int cantidadPocion = 3;
    double experiencia;
    int nivel = 0;

    static boolean verificarNombre(String nombre) {
        return nombre.matches(ER_NOMBRE);
    }

    public User(String nombre) {
        super(nombre);
    }

    @Override
    public String toString() {
        return String.format(
                """
                        Nombre: %s
                        Ataque: %d
                        Defensa: %d
                        Pociones: %d
                        Nivel: %d
                        Experiencia: %.0f""",
                this.nombre, this.ataque, this.defensa,
                this.cantidadPocion, this.nivel, this.experiencia);
    }

    void defenderse(Enemigo enemigo) {
        cantidadPocion++;
        int dano = enemigo.calcularDano(this) / 2;
        recibirDano(this, dano);
    }

    void usarPocion() {
        if (cantidadPocion > 0) {
            cantidadPocion--;
            this.vitalidad += 100;
            if (this.vitalidad > this.vidaMax) {
                this.vitalidad = this.vidaMax;
            }
        }
    }

    void anadirNivel() {
        this.nivel++;
        IO.println("¡Has subido al nivel " + this.nivel + "!");
        int randomBonus = random.nextInt(1, 5);
        switch (randomBonus) {
            case 1 -> {
                this.ataque *= 2;
                IO.println("Tu ataque se ha duplicado.");
            }
            case 2 -> {
                this.defensa *= 2;
                IO.println("Tu defensa se ha duplicado.");
            }
            case 3 -> {
                this.vitalidad = this.vidaMax;
                IO.println("Tu vitalidad se ha recuperado completamente.");
            }
            case 4 -> {
                this.cantidadPocion++;
                IO.println("Has recibido una poción.");
            }
        }
    }

    void anadirExperiencia() {
        this.experiencia += 50;
        if (this.experiencia >= 100) {
            experiencia-=100;
            anadirNivel();
        }
    }

}

class Enemigo extends Jugador {
    public Enemigo() {
        super("Enemigo");
    }

    int calcularDano(Jugador jugador) {
        int dano = this.ataque - jugador.defensa;
        int probabilidad = random.nextInt(1,5);
        if (probabilidad == 2) {
            dano *= 2;
            IO.println("Golpe crítico");
        }
        return dano;
    }

    @Override
    void atacar(Jugador jugador) {
        recibirDano(jugador, calcularDano(jugador));
    }
}

class EnemigoFinal extends Enemigo {
    public EnemigoFinal() {
        super();
        this.vidaMax = 2000;
        this.vitalidad = vidaMax;
        this.nombre= "Enemigo Final";
    }
    @Override
    int calcularDano(Jugador jugador) {
        int dano = this.ataque - jugador.defensa;
        int probabilidad = random.nextInt(1, 101);
        if (probabilidad <= 40) {
            dano *= 2;
            IO.println("Golpe crítico");
        }
        return dano;
    }

}

class Menu {
    User user;
    Enemigo bot = new Enemigo();
    int numeroEnemigo = 1;
    boolean juegoTerminado;

    void verEstatisticasDeJugadores() {
        IO.println(user);
        IO.println(user.mostrarVida());

        IO.println(bot);
        IO.println(bot.mostrarVida());
    }

    int leerAccion() {
        String menu = """
            Elige una acción:
            0 - Atacar
            1 - Defenderse
            """;

        if (user.cantidadPocion > 0) {
            menu += "2 - Usar poción\n";
        }

        menu += """
            3 - Salir
            Opción:\s""";

        return Integer.parseInt(IO.readln(menu));
    }

    boolean jugadorHaPerdido() {
        if (user.vitalidad <= 0) {
            IO.println("Has perdido. Tu vitalidad ha llegado a cero.");
            return true;
        }
        return false;
    }

    void atacar() {
        user.atacar(bot);
        if (bot.vitalidad > 0) {
            bot.atacar(user);
            return;
        }

        user.anadirExperiencia();
        if (numeroEnemigo == 10) {
            IO.println("¡Felicidades! Has derrotado al enemigo final.");
            juegoTerminado = true;
            return;
        }

        IO.println("¡Has derrotado al enemigo!");
        numeroEnemigo++;
        if (numeroEnemigo == 10) {
            bot = new EnemigoFinal();
        } else {
            bot = new Enemigo();
        }
    }

    void usarPocion() {
        if (user.cantidadPocion > 0) {
            user.usarPocion();
            bot.atacar(user);
        } else {
            IO.println("No te quedan pociones.");
        }
    }

    void jugar() {
        int action;
        do {
            action = leerAccion();
            switch (action) {
                case 0 -> atacar();
                case 1 -> user.defenderse(bot);
                case 2 -> usarPocion();
                case 3 -> IO.println("Juego terminado.");
                default -> IO.println("Opción no válida.");
            }

            if (action >= 0 && action <= 2) {
                verEstatisticasDeJugadores();
                if (jugadorHaPerdido()) {
                    action = 3;
                }
            }
        } while (action != 3 && !juegoTerminado);
    }

    void start() {
        String nombreDeUsario = "";
        do {
            nombreDeUsario = IO.readln("Dime tu nombre: ");
        } while(!User.verificarNombre(nombreDeUsario));
        user = new User(nombreDeUsario);
        verEstatisticasDeJugadores();
        jugar();
    }

}

public class Juego {
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.start();
    }
}

