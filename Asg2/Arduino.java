/**
 * Classe que representa os arduinos.
 * 
 * @author Davi E. N. Frossard, Rodolfo V. Valentim
 */
public class Arduino implements Runnable {

	/**
	 * Contador da quantidade de objetos instanciados.
	 */
	private static int count = 0;
	
	/**
	 * Id do Arduino.
	 */
	private int id;
	
	/**
	 * Fila de produtor/consumidor a ser produzida.
	 */
	private ProducerConsumer<Double> queueIn;
	
	/**
	 * Fila de produtor/consumidor a ser consumida.
	 */
	private ProducerConsumer<MotorData<Integer, Integer>> queueOut[];
	
	/**
	 * Quantidade de motores a serem controlados.
	 */
	private int num_motors;
	
	/**
	 * Instancia um objeto da classe Arduino.
	 * 
	 * @param ArdSen
	 * 		Fila de P/C a ser consumida.
	 * @param ArdMot
	 * 		Fila de P/C a ser produzida.
	 * @param num_motors
	 * 		Número de motores a serem controlados.
	 */
	public Arduino(ProducerConsumer<Double> ArdSen, ProducerConsumer<MotorData<Integer, Integer>>[] ArdMot, int num_motors) {
		queueIn = ArdSen;
		queueOut = ArdMot;
		this.num_motors = num_motors;
		id = ++count;
	}

	/**
	 * Método de execução da thread.
	 * Consome os valores dos sensores e a partir deles produz 
	 * comandos para um motor arbitrário (sendo a direção 
	 * arbitrada como sendo 1 [CW] se a leitura for maior que 
	 * 5 e 0 [CCW] se menor, a própria leitura é a quantidade
	 * de passos). O comando é então inserido na fila de P/C do
	 * motor. 
	 * Delay de 1 segundo entre cada execução.
	 */
	@Override
	public void run() {
		while (true) {
			int dir = -1;
			int pwm = -1;
			int seed = 0;
			try {
				seed = (int)queueIn.get("Arduino #"+id).doubleValue();
			
				if (seed > 5)
					dir = 1;
				else
					dir = 0;
					
				pwm = seed;
	
				Thread.sleep(1000);
				int motorDest = (int)(Math.random()*10)%num_motors;
				//dir += (motorDest+1)*10; //Garantir que os motores estão recebendo os comandos certos
				queueOut[motorDest].put(new MotorData<Integer, Integer>(new Integer(dir), new Integer(pwm)), "Arduino #"+id);
			}
			
			catch (InterruptedException e) {
				System.out.println("Falha na interrupção!");
			}
		}
	}
}
