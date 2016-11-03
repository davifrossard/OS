/**
 * Classe principal do sistema (main).
 * 
 * @author Davi E. N. Frossard, Rodolfo V. Valentim
 */
public class Central {
	
	/**
	 * Número de motores no sistema.
	 */
	private final static int NUM_MOTORS = 4;
	
	/**
	 * Número de arduinos no sistema.
	 */
	private final static int NUM_ARDUINOS = 5;
	
	/**
	 * Número de sensores no sistema.
	 */
	private final static int NUM_SENSORS = 10;
	
	/**
	 * Quantidade de entradas nas filas de produtores/consumidores.
	 */
	private final static int QUEUE_SIZE = 100;
	
	/**
	 * Modo verboso (imprime todas as operações de produção/consumo
	 */
	private final static boolean VERBOSITY = false;
	
	/**
	 * O método main somente instancia a quantidade predefinida de 
	 * Arduinos, Sensores e Motores e suas respectivas threads, após
	 * isso fica em loop constante exibindo o estado dos motores.
	 * 
	 * @param args 
	 * 			Não utilizado.
	 */
	public static void main(String[] args) {
		
		Sensor sens[] = new Sensor[NUM_SENSORS];
		Thread tSens[] = new Thread[NUM_SENSORS];

		Arduino ard[] = new Arduino[NUM_ARDUINOS];
		Thread tArd[] = new Thread[NUM_ARDUINOS];
		
		Motor mot[] = new Motor[NUM_MOTORS];
		Thread tMot[] = new Thread[NUM_MOTORS];
		
		ProducerConsumer<Double> SenArd = new ProducerConsumer<Double>(QUEUE_SIZE, VERBOSITY);
		ProducerConsumer<?>[] ArdMot = new ProducerConsumer<?>[NUM_MOTORS];

		for (int i = 0; i < NUM_SENSORS; i++) {
			sens[i] = new Sensor(SenArd);
			tSens[i] = new Thread(sens[i]);
			tSens[i].start();
			System.out.println("Iniciado Sensor #" + i);
		}
		
		for (int i = 0; i < NUM_MOTORS; i++) {
			ArdMot[i] = new ProducerConsumer<MotorData<Integer, Integer>>(QUEUE_SIZE, VERBOSITY);
			mot[i] = new Motor((ProducerConsumer<MotorData<Integer, Integer>>) ArdMot[i]);
			tMot[i] = new Thread(mot[i]);
			tMot[i].start();
			System.out.println("Iniciado Motor #" + i);
		}
		
		for (int i = 0; i < NUM_ARDUINOS; i++) {
			ard[i] = new Arduino(SenArd, (ProducerConsumer<MotorData<Integer, Integer>>[]) ArdMot, NUM_MOTORS);
			tArd[i] = new Thread(ard[i]);
			tArd[i].start();
			System.out.println("Iniciado Arduino #" + i);
		}
		
		while(true)
		{
			try {
				Thread.sleep(1000);
				System.out.format(System.lineSeparator()+"%s%9s%9s"+System.lineSeparator(), "Motor", "Sentido", "Passos");
				for (int i = 0; i < NUM_MOTORS; i++) {
					String sentido = (mot[i].getDir() == 0) ? "CCW" : "CW";
					System.out.format("%3s%9s%9s"+System.lineSeparator(), (i+1), sentido, mot[i].getPos());
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
