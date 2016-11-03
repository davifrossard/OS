/**
 * Classe que representa os sensores.
 * 
 * @author Davi E. N. Frossard, Rodolfo V. Valentim
 */
public class Sensor implements Runnable {
	
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
	private ProducerConsumer<Double> queue;

	/**
	 * Instancia um objeto da classe Sensor.
	 * 
	 * @param ArdSen
	 * 		Fila de P/C a ser produzida.
	 */
	public Sensor(ProducerConsumer<Double> ArdSen) {
		queue = ArdSen;
		id = ++count;
	}

	/** 
	 * Método da execução da thread. Gera um valor aleatório
	 * entre 0 e 10 e insere na fila de P/C. 
	 * Delay de 1 segundo entre cada execução.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				queue.put(new Double(Math.random()*10), "Sensor #"+id);
			} catch (InterruptedException e) {
				System.out.println("Falha na interrupção!");
			}
		}
	}

}
